package org.springframework.integration.aws.sqs.config;

import static org.junit.Assert.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.aws.JsonMessageMarshaller;
import org.springframework.integration.aws.MessageMarshaller;
import org.springframework.integration.aws.sqs.core.SqsExecutor;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;

/**
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SqsMessageHandlerParserTests {

	private ConfigurableApplicationContext context;
	private MessageMarshaller messageMarshaller;
	private EventDrivenConsumer consumer;

	@Test
	public void testSqsMessageHandlerParser() throws Exception {
		setUp("SqsMessageHandlerParserTests.xml", getClass());

		final AbstractMessageChannel inputChannel = TestUtils.getPropertyValue(
				this.consumer, "inputChannel", AbstractMessageChannel.class);

		assertEquals("target", inputChannel.getComponentName());

		final SqsExecutor sqsExecutor = TestUtils.getPropertyValue(
				this.consumer, "handler.sqsExecutor", SqsExecutor.class);

		assertNotNull(sqsExecutor);

		final String queueNameProperty = TestUtils.getPropertyValue(
				sqsExecutor, "queueName", String.class);

		assertEquals("testQueue", queueNameProperty);

	}

	@Test
	public void testSqsExecutorBeanIdNaming() throws Exception {

		this.context = new ClassPathXmlApplicationContext(
				"SqsMessageHandlerParserTests.xml", getClass());
		assertNotNull(context.getBean("sqsOutboundChannelAdapter.sqsExecutor",
				SqsExecutor.class));

	}

	@Test
	public void testMessageSendingCapability() throws Exception {

		setUp("SqsMessageHandlerParserTests.xml", getClass());

		MessageChannel inputChannel = (MessageChannel) context
				.getBean("target");

		@SuppressWarnings("unchecked")
		BlockingQueue<String> outputQueue = (BlockingQueue<String>) context
				.getBean("testQueue");

		String inputMessage = "Hello, World";
		inputChannel.send(MessageBuilder.withPayload(inputMessage).build());

		String outputMessage = outputQueue.poll(5, TimeUnit.SECONDS);
		assertNotNull("outputMessage is not null", outputMessage);

		String payload = (String) messageMarshaller.deserialize(outputMessage)
				.getPayload();

		assertTrue("payload equals message", payload.equals(inputMessage));
	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

	public void setUp(String name, Class<?> cls) {
		context = new ClassPathXmlApplicationContext(name, cls);
		consumer = this.context.getBean("sqsOutboundChannelAdapter",
				EventDrivenConsumer.class);
		messageMarshaller = new JsonMessageMarshaller();
	}

}
