package org.springframework.integration.aws.sns.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.aws.AwsUtil;
import org.springframework.integration.aws.JsonMessageMarshaller;
import org.springframework.integration.aws.MessageMarshaller;
import org.springframework.integration.aws.MessageMarshallerException;
import org.springframework.integration.aws.Permission;
import org.springframework.integration.aws.sns.support.SnsTestProxy;
import org.springframework.integration.aws.sqs.core.SqsExecutor;
import org.springframework.util.Assert;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.AddPermissionRequest;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.GetTopicAttributesResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.Topic;

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
	private Map<String, SqsExecutor> sqsExecutorMap;
	private ClientConfiguration awsClientConfiguration;
	private MessageMarshaller messageMarshaller;
	private Set<Permission> permissions;

	/**
	 * Verifies and sets the parameters. E.g. initializes the to be used
	 */
	@Override
	public void afterPropertiesSet() {

		Assert.isTrue(this.topicName != null || this.topicArn != null,
				"Either topicName or topicArn must not be empty.");

		Assert.isTrue(snsTestProxy != null || awsCredentialsProvider != null,
				"Either snsTestProxy or awsCredentialsProvider needs to be provided");

		if (messageMarshaller == null) {
			messageMarshaller = new JsonMessageMarshaller();
		}

		if (snsTestProxy == null) {
			if (awsClientConfiguration == null) {
				client = new AmazonSNSClient(awsCredentialsProvider);
			} else {
				client = new AmazonSNSClient(awsCredentialsProvider,
						awsClientConfiguration);
			}
			if (regionId != null) {
				client.setEndpoint(String.format("sns.%s.amazonaws.com",
						regionId));
			}
			if (topicArn == null) {
				createTopicIfNotExists();
			}
			processSubscriptions();
			addPermissions();
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
		if (subscriptionList != null && !subscriptionList.isEmpty()) {
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
					urlSubscription.getProtocol(),
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
		Assert.state(sqsExecutorMap != null,
				"'sqsExecutorMap' must not be null");

		SqsExecutor sqsExecutor = null;
		String endpointValue = sqsSubscription.getEndpoint();
		if (sqsExecutorMap.containsKey(endpointValue)) {
			sqsExecutor = sqsExecutorMap.get(endpointValue);
			sqsSubscription.setEndpoint(sqsExecutor.getQueueArn());
		} else {
			// endpointValue is the queue-arn
			sqsSubscription.setEndpoint(endpointValue);
		}

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
		if (sqsExecutor != null) {
			sqsExecutor.addSnsPublishPolicy(topicName, topicArn);
		}
	}

	private void addPermissions() {
		if (permissions != null && permissions.isEmpty() == false) {
			GetTopicAttributesResult result = client
					.getTopicAttributes(topicArn);

			AwsUtil.addPermissions(result.getAttributes(), permissions,
					new AwsUtil.AddPermissionHandler() {

						@Override
						public void execute(Permission p) {
							client.addPermission(new AddPermissionRequest()
									.withTopicArn(topicArn)
									.withLabel(p.getLabel())
									.withAWSAccountIds(p.getAwsAccountIds())
									.withActionNames(p.getActions()));
						}
					});
		}
	}

	/**
	 * Executes the outbound Sns Operation.
	 * 
	 */
	public Object executeOutboundOperation(final Message<?> message) {

		try {
			String serializedMessage = messageMarshaller.serialize(message);

			if (snsTestProxy == null) {
				PublishRequest request = new PublishRequest();
				PublishResult result = client.publish(request.withTopicArn(
						topicArn).withMessage(serializedMessage));
				log.debug("Published message to topic: "
						+ result.getMessageId());
			} else {
				snsTestProxy.dispatchMessage(serializedMessage);
			}

		} catch (MessageMarshallerException e) {
			log.error(e.getMessage(), e);
			throw new MessagingException(e.getMessage(), e.getCause());
		}

		return message.getPayload();
	}

	public void registerHandler(NotificationHandler notificationHandler) {

		Assert.notNull(httpEndpoint, "'httpEndpoint' must not be null");
		Assert.notNull(notificationHandler,
				"'notificationHandler' must not be null");

		notificationHandler.setMessageMarshaller(messageMarshaller);
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
	 * Sets the topic ARN. Must not be empty.
	 * 
	 * @param topicArn
	 */
	public void setTopicArn(String topicArn) {
		this.topicArn = topicArn;
	}

	/**
	 * Sets topic name
	 * 
	 * @param topicName
	 *            Must not be null
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	/**
	 * Sets the SnsTestProxy instance for testing without actual AWS.
	 * 
	 * @param snsTestProxy
	 */
	public void setSnsTestProxy(SnsTestProxy snsTestProxy) {
		this.snsTestProxy = snsTestProxy;
	}

	/**
	 * Sets the AWS credentials provider.
	 * 
	 * @param awsCredentialsProvider
	 */
	public void setAwsCredentialsProvider(
			AWSCredentialsProvider awsCredentialsProvider) {
		this.awsCredentialsProvider = awsCredentialsProvider;
	}

	/**
	 * Sets the AWS client configuration.
	 * 
	 * @param awsClientConfiguration
	 */
	public void setAwsClientConfiguration(
			ClientConfiguration awsClientConfiguration) {
		this.awsClientConfiguration = awsClientConfiguration;
	}

	/**
	 * Sets the AWS region ID.
	 * 
	 * @param regionId
	 */
	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public void setHttpEndpoint(HttpEndpoint httpEndpoint) {
		this.httpEndpoint = httpEndpoint;
	}

	public void setSubscriptionList(List<Subscription> subscriptionList) {
		this.subscriptionList = subscriptionList;
	}

	public void setSqsExecutorMap(Map<String, SqsExecutor> sqsExecutorMap) {
		this.sqsExecutorMap = sqsExecutorMap;
	}

	public void setMessageMarshaller(MessageMarshaller messageMarshaller) {
		this.messageMarshaller = messageMarshaller;
	}

	/**
	 * Sets the permissions to be applied to the SNS topic.
	 * 
	 * @param permissions
	 */
	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	@Override
	public void destroy() throws Exception {
		if (client != null) {
			client.shutdown();
		}
	}

}
