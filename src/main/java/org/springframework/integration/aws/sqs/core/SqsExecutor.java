package org.springframework.integration.aws.sqs.core;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.amazonaws.ClientConfiguration;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.aws.MessagePacket;
import org.springframework.integration.aws.sqs.SqsHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.auth.policy.conditions.ArnCondition;
import com.amazonaws.auth.policy.conditions.ArnCondition.ArnComparisonType;
import com.amazonaws.auth.policy.conditions.ConditionFactory;
import com.amazonaws.auth.policy.internal.JsonPolicyWriter;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.amazonaws.util.Md5Utils;

/**
 * Bundles common core logic for the Sqs components.
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SqsExecutor implements InitializingBean, DisposableBean {

	private static final String SNS_MESSAGE_KEY = "Message";
	private static final int DEFAULT_RECV_MESG_WAIT = 20; // seconds
	private static final String QUEUE_ARN_KEY = "QueueArn";
	private static final int DEFAULT_MESSAGE_PREFETCH_COUNT = 10;

	private final Log log = LogFactory.getLog(SqsExecutor.class);

	private String queueName;
	private BlockingQueue<String> queue;
	private AWSCredentialsProvider awsCredentialsProvider;
	private AmazonSQS sqsClient;
	private String queueUrl;
	private String queueArn;
	private String regionId;
	private int receiveMessageWaitTimeout;
	private int prefetchCount;
	private final BlockingQueue<com.amazonaws.services.sqs.model.Message> prefetchQueue;
	private Integer messageDelay;
	private Integer maximumMessageSize;
	private Integer messageRetentionPeriod;
	private Integer visibilityTimeout;

	private volatile int destroyWaitTime;

    private ClientConfiguration awsClientConfiguration;

	/**
	 * Constructor.
	 */
	public SqsExecutor() {
		this.receiveMessageWaitTimeout = DEFAULT_RECV_MESG_WAIT;
		this.destroyWaitTime = 0;
		this.prefetchCount = DEFAULT_MESSAGE_PREFETCH_COUNT;
		this.prefetchQueue = new LinkedBlockingQueue<com.amazonaws.services.sqs.model.Message>(
				prefetchCount);
	}

	/**
	 * Verifies and sets the parameters. E.g. initializes the to be used
	 */
	@Override
	public void afterPropertiesSet() {
		Assert.hasText(this.queueName, "queueName must not be empty.");
		Assert.isTrue(queue != null || awsCredentialsProvider != null,
				"Either queue or awsCredentialsProvider needs to be provided");

		if (queue == null) {
			if (sqsClient == null) {
                if (awsClientConfiguration == null) {
                    sqsClient = new AmazonSQSClient(awsCredentialsProvider);
                } else {
                    sqsClient = new AmazonSQSClient(awsCredentialsProvider, awsClientConfiguration);
                }
            }
			if (regionId != null) {
				sqsClient.setEndpoint(String.format("sqs.%s.amazonaws.com",
						regionId));
			}
			createQueueIfNotExists();
		}
	}

	private void createQueueIfNotExists() {
		for (String qUrl : sqsClient.listQueues().getQueueUrls()) {
			if (qUrl.contains(queueName)) {
				queueUrl = qUrl;
				break;
			}
		}
		if (queueUrl == null) {
			CreateQueueRequest request = new CreateQueueRequest(queueName);
			Map<String, String> queueAttributes = new HashMap<String, String>();
			queueAttributes.put("ReceiveMessageWaitTimeSeconds", Integer
					.valueOf(receiveMessageWaitTimeout).toString());
			if (messageDelay != null) {
				queueAttributes.put("DelaySeconds", messageDelay.toString());
			}
			if (maximumMessageSize != null) {
				queueAttributes.put("MaximumMessageSize",
						maximumMessageSize.toString());
			}
			if (messageRetentionPeriod != null) {
				queueAttributes.put("MessageRetentionPeriod",
						messageRetentionPeriod.toString());
			}
			if (visibilityTimeout != null) {
				queueAttributes.put("VisibilityTimeout",
						visibilityTimeout.toString());
			}
			request.setAttributes(queueAttributes);
			CreateQueueResult result = sqsClient.createQueue(request);
			queueUrl = result.getQueueUrl();
			log.debug("New queue available at: " + queueUrl);
		} else {
			log.debug("Queue already exists: " + queueUrl);
		}

		resolveQueueArn();
	}

	private void resolveQueueArn() {
		GetQueueAttributesRequest request = new GetQueueAttributesRequest(
				queueUrl);
		GetQueueAttributesResult result = sqsClient.getQueueAttributes(request
				.withAttributeNames(Collections.singletonList(QUEUE_ARN_KEY)));
		queueArn = result.getAttributes().get(QUEUE_ARN_KEY);
	}

	/**
	 * Executes the outbound Sqs Operation.
	 * 
	 */
	public Object executeOutboundOperation(final Message<?> message) {

		MessagePacket packet = new MessagePacket(message);
		if (queue == null) {
			SendMessageRequest request = new SendMessageRequest(queueUrl,
					packet.toJSON());
			SendMessageResult result = sqsClient.sendMessage(request);
			log.debug("Message sent, Id:" + result.getMessageId());
		} else {
			queue.add(packet.toJSON());
		}

		return message.getPayload();

	}

	/**
	 * Execute the Sqs operation. Delegates to {@link SqsExecutor#poll(Message)}
	 * .
	 */
	public Message<?> poll() {
		return poll(0);
	}

	/**
	 * Execute a retrieving (polling) Sqs operation.
	 * 
	 * @param timeout
	 *            time to wait for a message to return.
	 * 
	 * @return The payload object, which may be null.
	 */
	public Message<?> poll(long timeout) {

		Message<?> message = null;
		String payloadJSON = null;
		com.amazonaws.services.sqs.model.Message qMessage = null;
		int timeoutSeconds = (timeout > 0 ? ((int) (timeout / 1000))
				: receiveMessageWaitTimeout);
		destroyWaitTime = timeoutSeconds;
		try {
			if (queue == null) {
				if (prefetchQueue.isEmpty()) {
					ReceiveMessageRequest request = new ReceiveMessageRequest(
							queueUrl).withWaitTimeSeconds(timeoutSeconds)
							.withMaxNumberOfMessages(prefetchCount)
							.withAttributeNames("All");

					ReceiveMessageResult result = sqsClient
							.receiveMessage(request);
					for (com.amazonaws.services.sqs.model.Message sqsMessage : result
							.getMessages()) {
						prefetchQueue.offer(sqsMessage);
					}
					qMessage = prefetchQueue.poll();
				} else {
					qMessage = prefetchQueue.remove();
				}
				if (qMessage != null) {
					payloadJSON = qMessage.getBody();
					// MD5 verification
					try {
						byte[] computedHash = Md5Utils
								.computeMD5Hash(payloadJSON.getBytes("UTF-8"));
						String hexDigest = new String(
								Hex.encodeHex(computedHash));
						if (!hexDigest.equals(qMessage.getMD5OfBody())) {
							payloadJSON = null; // ignore this message
							log.warn("Dropped message due to MD5 checksum failure");
						}
					} catch (Exception e) {
						log.warn(
								"Failed to verify MD5 checksum: "
										+ e.getMessage(), e);
					}
				}
			} else {
				try {
					payloadJSON = queue.poll(timeoutSeconds, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					log.warn(e.getMessage(), e);
				}
			}
			if (payloadJSON != null) {
				JSONObject qMessageJSON = new JSONObject(payloadJSON);
				if (qMessageJSON.has(SNS_MESSAGE_KEY)) { // posted from SNS
					payloadJSON = qMessageJSON.getString(SNS_MESSAGE_KEY);
					// XXX: other SNS attributes?
				}
				MessagePacket packet = MessagePacket.fromJSON(payloadJSON);
				MessageBuilder<?> builder = MessageBuilder.fromMessage(packet
						.assemble());
				if (qMessage != null) {
					builder.setHeader(SqsHeaders.MSG_RECEIPT_HANDLE,
							qMessage.getReceiptHandle());
					builder.setHeader(SqsHeaders.AWS_MESSAGE_ID,
							qMessage.getMessageId());
					for (Map.Entry<String, String> e : qMessage.getAttributes()
							.entrySet()) {
						if (e.getKey().equals("ApproximateReceiveCount")) {
							builder.setHeader(SqsHeaders.RECEIVE_COUNT,
									Integer.valueOf(e.getValue()));
						} else if (e.getKey().equals("SentTimestamp")) {
							builder.setHeader(SqsHeaders.SENT_AT,
									new Date(Long.valueOf(e.getValue())));
						} else if (e.getKey().equals(
								"ApproximateFirstReceiveTimestamp")) {
							builder.setHeader(SqsHeaders.FIRST_RECEIVED_AT,
									new Date(Long.valueOf(e.getValue())));
						} else if (e.getKey().equals("SenderId")) {
							builder.setHeader(SqsHeaders.SENDER_AWS_ID,
									e.getValue());
						} else {
							builder.setHeader(e.getKey(), e.getValue());
						}
					}
				} else {
					builder.setHeader(SqsHeaders.MSG_RECEIPT_HANDLE, "");
					// to satisfy test conditions
				}

				message = builder.build();
			}

		} catch (JSONException e) {
			log.warn(e.getMessage(), e);

		} finally {
			destroyWaitTime = 0;
		}

		return message;
	}

	public String acknowlegdeReceipt(Message<?> message) {
		String receiptHandle = (String) message.getHeaders().get(
				SqsHeaders.MSG_RECEIPT_HANDLE);
		if (sqsClient != null && receiptHandle != null
				&& !receiptHandle.isEmpty()) {
			sqsClient.deleteMessage(new DeleteMessageRequest(queueUrl,
					receiptHandle));
		}

		return receiptHandle;
	}

	public String getQueueArn() {
		return queueArn;
	}

	public String getQueueUrl() {
		return queueUrl;
	}

	/**
	 * Example property to illustrate usage of properties in Spring Integration
	 * components. Replace with your own logic.
	 * 
	 * @param queueName
	 *            Must not be null
	 */
	public void setQueueName(String queueName) {
		Assert.hasText(queueName, "queueName must be neither null nor empty");
		this.queueName = queueName;
	}

	/**
	 * Set the queue implementation. Useful for testing the queue without
	 * actually invoking AWS.
	 * 
	 * @param queue
	 */
	public void setQueue(BlockingQueue<String> queue) {
		this.queue = queue;
	}

    @Autowired(required = false)
    public void setAwsClientConfiguration(ClientConfiguration awsClientConfiguration) {
        log.info("Set AWS client configuration to '" + awsClientConfiguration + "'.");
        this.awsClientConfiguration = awsClientConfiguration;
    }

    @Autowired(required = false)
	public void setAwsCredentialsProvider(
			AWSCredentialsProvider awsCredentialsProvider) {
		this.awsCredentialsProvider = awsCredentialsProvider;
	}

	public int getReceiveMessageWaitTimeout() {
		return receiveMessageWaitTimeout;
	}

	public void setReceiveMessageWaitTimeout(int receiveMessageWaitTimeout) {
		Assert.isTrue(receiveMessageWaitTimeout >= 0
				&& receiveMessageWaitTimeout <= 20,
				"'receiveMessageWaitTimeout' must be an integer from 0 to 20 (seconds).");
		this.receiveMessageWaitTimeout = receiveMessageWaitTimeout;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public void setPrefetchCount(int prefetchCount) {
		Assert.isTrue(prefetchCount >= 0 && prefetchCount <= 10,
				"'prefetchCount' must be an integer from 0 to 10.");
		this.prefetchCount = prefetchCount;
	}

	public void setMessageDelay(Integer messageDelay) {
		Assert.isTrue(messageDelay >= 0 && messageDelay <= 900,
				"'messageDelay' must be an integer from 0 to 900 (15 minutes).");
		this.messageDelay = messageDelay;
	}

	public void setMaximumMessageSize(Integer maximumMessageSize) {
		Assert.isTrue(
				maximumMessageSize >= 1024 && maximumMessageSize <= 65536,
				"'maximumMessageSize' must be an integer from 1024 bytes (1 KiB) up to 65536 bytes (64 KiB).");
		this.maximumMessageSize = maximumMessageSize;
	}

	public void setMessageRetentionPeriod(Integer messageRetentionPeriod) {
		Assert.isTrue(
				messageRetentionPeriod >= 60
						&& messageRetentionPeriod <= 1209600,
				"'messageRetentionPeriod' must be an integer representing seconds, from 60 (1 minute) to 1209600 (14 days)");
		this.messageRetentionPeriod = messageRetentionPeriod;
	}

	public void setVisibilityTimeout(Integer visibilityTimeout) {
		Assert.isTrue(
				visibilityTimeout >= 0 && visibilityTimeout <= 43200,
				"'visibilityTimeout' must be an integer representing seconds, from 0 to 43200 (12 hours)");
		this.visibilityTimeout = visibilityTimeout;
	}

	@Override
	public void destroy() throws Exception {
		if (sqsClient != null) {
			if (destroyWaitTime > 0) {
				Thread.sleep(destroyWaitTime * 1000);
			}
			sqsClient.shutdown();
		}

	}

	public void addSnsPublishPolicy(String topicName, String topicArn) {

		String publishPolicyKey = String.format("SNS-%s-SQS-%s", topicName,
				queueName);
		String policyId = null;
		GetQueueAttributesRequest getAttrRequest = new GetQueueAttributesRequest(
				queueUrl);
		getAttrRequest.setAttributeNames(Collections.singletonList("Policy"));
		GetQueueAttributesResult result = sqsClient
				.getQueueAttributes(getAttrRequest);
		Map<String, String> attributes = result.getAttributes();
		String policyStr = attributes.get("Policy");
		log.debug("Policy:" + policyStr);
		if (policyStr != null) {
			try {
				JSONObject policyJSON = new JSONObject(policyStr);
				policyId = policyJSON.getString("Id");
			} catch (JSONException e) {
				log.error(e.getMessage(), e);
			}
		}

		if (policyId == null || !policyId.equals(publishPolicyKey)) {
			Statement statement = new Statement(Effect.Allow);
			statement
					.withActions(SQSActions.SendMessage)
					.withPrincipals(Principal.AllUsers)
					.withResources(new Resource(queueArn))
					.withConditions(
							new ArnCondition(ArnComparisonType.ArnEquals,
									ConditionFactory.SOURCE_ARN_CONDITION_KEY,
									topicArn));
			Policy policy = new Policy();
			policy.setId(publishPolicyKey);
			policy.setStatements(Collections.singletonList(statement));

			SetQueueAttributesRequest request = new SetQueueAttributesRequest();
			request.setQueueUrl(queueUrl);
			String policyJSON = (new JsonPolicyWriter())
					.writePolicyToString(policy);
			log.debug(policyJSON);
			request.setAttributes(Collections
					.singletonMap("Policy", policyJSON));

			sqsClient.setQueueAttributes(request);
		}
	}

	public void setSqsClient(AmazonSQS sqsClient) {
		this.sqsClient = sqsClient;
	}

}
