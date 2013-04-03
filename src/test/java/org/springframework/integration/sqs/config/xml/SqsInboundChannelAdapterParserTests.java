package org.springframework.integration.sqs.config.xml;

import static org.junit.Assert.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.test.util.TestUtils;

import com.threepillar.labs.si.sqs.SqsHeaders;
import com.threepillar.labs.si.sqs.core.SqsExecutor;
import com.threepillar.labs.si.sqs.inbound.SqsSubscribableChannelAdapter;

/**
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SqsInboundChannelAdapterParserTests {

	private ConfigurableApplicationContext context;

	private SqsSubscribableChannelAdapter consumer;

	private String payload;

	private Callable<String> ackCallback;

	@Test
	public void testSqsInboundChannelAdapterParser() throws Exception {

		setUp("SqsInboundChannelAdapterParserTests.xml", getClass(),
				"sqsInboundChannelAdapter");

		final AbstractMessageChannel outputChannel = TestUtils
				.getPropertyValue(this.consumer, "outputChannel",
						AbstractMessageChannel.class);

		assertEquals("out", outputChannel.getComponentName());

		final SqsExecutor sqsExecutor = TestUtils.getPropertyValue(
				this.consumer, "sqsExecutor", SqsExecutor.class);

		assertNotNull(sqsExecutor);

		final String queueNameProperty = TestUtils.getPropertyValue(
				sqsExecutor, "queueName", String.class);

		assertEquals("testQueue", queueNameProperty);

	}

	@Test
	public void testSqsExecutorBeanIdNaming() throws Exception {

		this.context = new ClassPathXmlApplicationContext(
				"SqsInboundChannelAdapterParserTests.xml", getClass());
		assertNotNull(context.getBean("sqsInboundChannelAdapter.sqsExecutor",
				SqsExecutor.class));

	}

	@Test
	public void testMessageFlow() throws Exception {

		setUp("SqsInboundChannelAdapterParserTests.xml", getClass(),
				"sqsInboundChannelAdapter");

		@SuppressWarnings("unchecked")
		BlockingQueue<String> testQueue = (BlockingQueue<String>) context
				.getBean("testQueue");
		DirectChannel out = (DirectChannel) context.getBean("out");
		out.subscribe(new MessageHandler() {

			@Override
			public void handleMessage(Message<?> message)
					throws MessagingException {
				assertEquals("Hello, World", message.getPayload());
			}
		});

		testQueue
				.add("{\"payload\": \"Hello, World\", \"payloadClazz\": \"java.lang.String\"}");

	}

	@Test
	public void testPolling() throws Exception {

		payload = null;
		ackCallback = null;
		context = new ClassPathXmlApplicationContext(
				"SqsPollingInboundChannelAdapterParserTests.xml", getClass());

		@SuppressWarnings("unchecked")
		BlockingQueue<String> testQueue = (BlockingQueue<String>) context
				.getBean("testQueue");
		DirectChannel out = (DirectChannel) context.getBean("out");
		out.subscribe(new MessageHandler() {

			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message<?> message)
					throws MessagingException {
				payload = (String) message.getPayload();
				ackCallback = (Callable<String>) message.getHeaders().get(
						SqsHeaders.ACK_CALLBACK);
			}
		});

		testQueue
				.add("{\"payload\": \"Hello, World\", \"payloadClazz\": \"java.lang.String\"}");

		Thread.sleep(2500);
		assertEquals("Hello, World", payload);

		assertNotNull(ackCallback);
		assertTrue(ackCallback.call().isEmpty());

	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

	public void setUp(String name, Class<?> cls, String consumerId) {
		context = new ClassPathXmlApplicationContext(name, cls);
		consumer = this.context.getBean(consumerId,
				SqsSubscribableChannelAdapter.class);
	}

}
