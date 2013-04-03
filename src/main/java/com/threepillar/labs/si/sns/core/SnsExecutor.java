package com.threepillar.labs.si.sns.core;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.util.Assert;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.Topic;
import com.threepillar.labs.si.aws.MessagePacket;
import com.threepillar.labs.si.sns.support.SnsTestProxy;
import com.threepillar.labs.si.sqs.core.SqsExecutor;

/**
 * Bundles common core logic for the Sns components.
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SnsExecutor implements InitializingBean, DisposableBean {

	private final Log log = LogFactory.getLog(SnsExecutor.class);

	private String topicName;
	private SnsTestProxy snsTestProxy;
	private AWSCredentialsProvider awsCredentialsProvider;
	private AmazonSNSClient client;
	private String regionId;
	private String topicArn;
	private HttpEndpoint httpEndpoint;
	private List<Subscription> subscriptionList;
	private String httpEndpointPath;
	private SqsExecutor sqsExecutor;

	/**
	 * Constructor.
	 */
	public SnsExecutor() {
	}

	/**
	 * Verifies and sets the parameters. E.g. initializes the to be used
	 */
	@Override
	public void afterPropertiesSet() {

		Assert.hasText(this.topicName, "topicName must not be empty.");

		Assert.isTrue(snsTestProxy != null || awsCredentialsProvider != null,
				"Either snsTestProxy or awsCredentialsProvider needs to be provided");

		if (snsTestProxy == null) {
			client = new AmazonSNSClient(awsCredentialsProvider);
			if (regionId != null) {
				client.setEndpoint(String.format("sns.%s.amazonaws.com",
						regionId));
			}
			createTopicIfNotExists();
			processSubscriptions();
		}
	}

	private void createTopicIfNotExists() {
		for (Topic topic : client.listTopics().getTopics()) {
			if (topic.getTopicArn().contains(topicName)) {
				topicArn = topic.getTopicArn();
				break;
			}
		}
		if (topicArn == null) {
			CreateTopicRequest request = new CreateTopicRequest(topicName);
			CreateTopicResult result = client.createTopic(request);
			topicArn = result.getTopicArn();
			log.debug("Topic created, arn: " + topicArn);
		} else {
			log.debug("Topic already created: " + topicArn);
		}
	}

	private void processSubscriptions() {
		if (subscriptionList != null) {
			for (Subscription subscription : subscriptionList) {
				if (subscription.getProtocol().startsWith("http")) {
					processUrlSubscription(subscription);
				} else {
					// sqs subscription
					processSqsSubscription(subscription);
				}
			}
		}
	}

	private void processUrlSubscription(Subscription urlSubscription) {

		if (!urlSubscription.getEndpoint().endsWith(httpEndpointPath)) {
			String stub = urlSubscription.getEndpoint();
			urlSubscription.setEndpoint(stub.concat(httpEndpointPath));
		}

		String snsUrlSubscriptionArn = null;
		for (Subscription subscription : client.listSubscriptions()
				.getSubscriptions()) {
			if (subscription.getTopicArn().equals(topicArn)
					&& subscription.getProtocol().equals(
							urlSubscription.getProtocol())
					&& subscription.getEndpoint().contains(
							urlSubscription.getEndpoint())) {
				if (!subscription.getSubscriptionArn().equals(
						"PendingConfirmation")) {
					snsUrlSubscriptionArn = subscription.getSubscriptionArn();
					break;
				}
			}
		}
		if (snsUrlSubscriptionArn == null) {

			SubscribeRequest request = new SubscribeRequest(topicArn,
					urlSubscription.getProtocol().toString(),
					urlSubscription.getEndpoint());
			SubscribeResult result = client.subscribe(request);
			snsUrlSubscriptionArn = result.getSubscriptionArn();
			log.info("Subscribed URL to SNS with subscription ARN: "
					+ snsUrlSubscriptionArn);
		} else {
			log.info("Already subscribed with ARN: " + snsUrlSubscriptionArn);
		}
	}

	private void processSqsSubscription(Subscription sqsSubscription) {
		Assert.state(sqsExecutor != null, "'sqsExecutor' must not be null");

		sqsSubscription.setEndpoint(sqsExecutor.getQueueArn());
		String snsSqsSubscriptionArn = null;
		for (Subscription subscription : client.listSubscriptions()
				.getSubscriptions()) {
			if (subscription.getTopicArn().equals(topicArn)
					&& subscription.getProtocol().equals(
							sqsSubscription.getProtocol())
					&& subscription.getEndpoint().equals(
							sqsSubscription.getEndpoint())) {
				snsSqsSubscriptionArn = subscription.getSubscriptionArn();
				break;
			}
		}
		if (snsSqsSubscriptionArn == null) {
			SubscribeRequest request = new SubscribeRequest(topicArn,
					sqsSubscription.getProtocol(),
					sqsSubscription.getEndpoint());
			SubscribeResult result = client.subscribe(request);
			snsSqsSubscriptionArn = result.getSubscriptionArn();
			log.info("Subscribed SQS to SNS with subscription ARN: "
					+ snsSqsSubscriptionArn);
		} else {
			log.info("Already subscribed with ARN: " + snsSqsSubscriptionArn);
		}
		sqsExecutor.addSnsPublishPolicy(topicName, topicArn);
	}

	/**
	 * Executes the outbound Sns Operation.
	 * 
	 */
	public Object executeOutboundOperation(final Message<?> message) {

		MessagePacket packet = new MessagePacket(message);
		if (snsTestProxy == null) {
			PublishRequest request = new PublishRequest();
			PublishResult result = client.publish(request
					.withTopicArn(topicArn).withMessage(packet.toJSON()));
			log.debug("Published message to topic: " + result.getMessageId());
		} else {
			snsTestProxy.dispatchMessage(packet.toJSON());
		}

		return message.getPayload();

	}

	public void registerHandler(NotificationHandler notificationHandler) {

		Assert.notNull(httpEndpoint, "'httpEndpoint' must not be null");
		Assert.notNull(notificationHandler,
				"'notificationHandler' must not be null");

		httpEndpoint.setNotificationHandler(notificationHandler);
		if (snsTestProxy != null) {
			snsTestProxy.setHttpEndpoint(httpEndpoint);
			httpEndpoint.setPassThru(true);
		}
	}

	public String getTopicArn() {
		return topicArn;
	}

	/**
	 * Example property to illustrate usage of properties in Spring Integration
	 * components. Replace with your own logic.
	 * 
	 * @param exampleProperty
	 *            Must not be null
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	/**
	 * Set the SnsTestProxy instance for testing without actual AWS.
	 * 
	 * @param snsTestProxy
	 */
	public void setSnsTestProxy(SnsTestProxy snsTestProxy) {
		this.snsTestProxy = snsTestProxy;
	}

	@Autowired(required = false)
	public void setAwsCredentialsProvider(
			AWSCredentialsProvider awsCredentialsProvider) {
		this.awsCredentialsProvider = awsCredentialsProvider;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public void setHttpEndpointPath(String httpEndpointPath) {
		this.httpEndpointPath = httpEndpointPath;
	}

	public void setHttpEndpoint(HttpEndpoint httpEndpoint) {
		this.httpEndpoint = httpEndpoint;
	}

	public void setSubscriptionList(List<Subscription> subscriptionList) {
		this.subscriptionList = subscriptionList;
	}

	public void setSqsExecutor(SqsExecutor sqsExecutor) {
		this.sqsExecutor = sqsExecutor;
	}

	@Override
	public void destroy() throws Exception {
		if (client != null) {
			client.shutdown();
		}
	}

}
