package org.springframework.integration.aws.sns.config;

import static org.junit.Assert.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.aws.JsonMessageMarshaller;
import org.springframework.integration.aws.MessageMarshaller;
import org.springframework.integration.aws.sns.core.SnsExecutor;
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
public class SnsMessageHandlerParserTests {

	private ConfigurableApplicationContext context;
	private MessageMarshaller messageMarshaller;

	@Test
	public void testSnsMessageHandlerParser() throws Exception {
		context = new ClassPathXmlApplicationContext(
				"SnsMessageHandlerParserTests.xml", getClass());

		EventDrivenConsumer consumer = context.getBean(
				"snsOutboundChannelAdapter", EventDrivenConsumer.class);

		final AbstractMessageChannel inputChannel = TestUtils.getPropertyValue(
				consumer, "inputChannel", AbstractMessageChannel.class);

		assertEquals("target", inputChannel.getComponentName());

		final SnsExecutor snsExecutor = TestUtils.getPropertyValue(consumer,
				"handler.snsExecutor", SnsExecutor.class);

		assertNotNull(snsExecutor);

		final String topicNameProperty = TestUtils.getPropertyValue(
				snsExecutor, "topicName", String.class);

		assertEquals("testTopic", topicNameProperty);

	}

	@Test
	public void testEventedFlow() throws Exception {
		context = new ClassPathXmlApplicationContext(
				"SnsMessageHandlerParserTests.xml", getClass());

		MessageChannel inputChannel = (MessageChannel) context
				.getBean("target");
		@SuppressWarnings("unchecked")
		BlockingQueue<String> dummyQueue = (BlockingQueue<String>) context
				.getBean("dummyQueue");

		String payload = "Hello, World";
		inputChannel.send(MessageBuilder.withPayload(payload).build());
		String messageJSON = dummyQueue.poll(100, TimeUnit.MILLISECONDS);
		assertNotNull(messageJSON);

		assertEquals(payload, messageMarshaller.deserialize(messageJSON)
				.getPayload());
	}

	@Test
	public void testPolledFlow() throws Exception {
		context = new ClassPathXmlApplicationContext(
				"SnsMessageHandlerParserTests.xml", getClass());

		MessageChannel inputChannel = (MessageChannel) context
				.getBean("pollableTarget");
		@SuppressWarnings("unchecked")
		BlockingQueue<String> dummyQueue = (BlockingQueue<String>) context
				.getBean("dummyQueue");

		String payload = "Hello, World";
		inputChannel.send(MessageBuilder.withPayload(payload).build());
		String messageJSON = dummyQueue.poll(1000, TimeUnit.MILLISECONDS);
		assertNotNull(messageJSON);

		assertEquals(payload, messageMarshaller.deserialize(messageJSON)
				.getPayload());
	}

	@Test
	public void testSnsExecutorBeanIdNaming() throws Exception {

		this.context = new ClassPathXmlApplicationContext(
				"SnsMessageHandlerParserTests.xml", getClass());
		assertNotNull(context.getBean("snsOutboundChannelAdapter.snsExecutor",
				SnsExecutor.class));
	}

	@Before
	public void setup() {
		messageMarshaller = new JsonMessageMarshaller();
	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

}
