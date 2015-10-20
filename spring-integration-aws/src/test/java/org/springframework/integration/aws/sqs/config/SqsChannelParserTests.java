package org.springframework.integration.aws.sqs.config;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.sqs.SqsExecutorProxy;
import org.springframework.integration.aws.sqs.SqsHeaders;
import org.springframework.integration.aws.sqs.channel.SubscribableSqsChannel;
import org.springframework.integration.aws.sqs.core.SqsExecutor;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.PollableChannel;


public class SqsChannelParserTests {

	private ConfigurableApplicationContext context;

	private SubscribableSqsChannel channel;

	private String recvMessage;

	@Test
	public void testSqsInboundChannelAdapterParser() throws Exception {

		setUp("SqsChannelParserTests.xml", getClass(), "sqsChannel");

		final SqsExecutor sqsExecutor = TestUtils.getPropertyValue(
				this.channel, "sqsExecutor", SqsExecutor.class);

		assertNotNull(sqsExecutor);

		final String queueNameProperty = TestUtils.getPropertyValue(
				sqsExecutor, "queueName", String.class);

		assertEquals("testQueue", queueNameProperty);

		Object sqsExecutorProxy = context.getBean("sqsExecutorProxy");
		assertNotNull(sqsExecutorProxy);
		assertEquals(SqsExecutorProxy.class, sqsExecutorProxy.getClass());
		SqsExecutor proxiedExecutor = TestUtils.getPropertyValue(
				sqsExecutorProxy, "sqsExecutor", SqsExecutor.class);
		assertNotNull(proxiedExecutor);
		SqsExecutor innerBean = context.getBean(SqsExecutor.class);
		assertSame(innerBean, proxiedExecutor);
	}

	@Test
	public void testMessageDrivenFlow() throws Exception {

		setUp("SqsChannelParserTests.xml", getClass(), "sqsChannel");

		recvMessage = null;
		DirectChannel log = (DirectChannel) context.getBean("log");
		log.subscribe(new MessageHandler() {

			@Override
			public void handleMessage(Message<?> message)
					throws MessagingException {
				recvMessage = (String) message.getPayload();
			}
		});

		String payload = "Hello, World";
		MessageChannel channel = (MessageChannel) context.getBean("sqsChannel");
		channel.send(MessageBuilder.withPayload(payload).build());
		Thread.sleep(1000);
		assertEquals(payload, recvMessage);
	}

	@Test
	public void testPollingFlow() throws Exception {
		setUp("SqsPollingChannelParserTests.xml", getClass(), "sqsChannel");

		PollableChannel channel = (PollableChannel) context
				.getBean("sqsChannel");

		String payload = "Hello, World";
		channel.send(MessageBuilder.withPayload(payload).build());

		Message<?> recv = channel.receive(1000);
		assertEquals(payload, recv.getPayload());

		@SuppressWarnings("unchecked")
		Callable<String> ackCallback = (Callable<String>) recv.getHeaders()
				.get(SqsHeaders.ACK_CALLBACK);
		assertNotNull(ackCallback);
		assertTrue(ackCallback.call().isEmpty());
	}

	@Test
	public void testSqsExecutorBeanIdNaming() throws Exception {

		this.context = new ClassPathXmlApplicationContext(
				"SqsChannelParserTests.xml", getClass());
		assertNotNull(context.getBean("sqsChannel.sqsExecutor",
				SqsExecutor.class));

	}

	public void setUp(String name, Class<?> cls, String consumerId) {
		context = new ClassPathXmlApplicationContext(name, cls);
		channel = this.context
				.getBean(consumerId, SubscribableSqsChannel.class);
	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

}
