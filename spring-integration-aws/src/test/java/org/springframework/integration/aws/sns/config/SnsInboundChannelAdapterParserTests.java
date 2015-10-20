package org.springframework.integration.aws.sns.config;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.JsonMessageMarshaller;
import org.springframework.integration.aws.MessageMarshaller;
import org.springframework.integration.aws.sns.core.SnsExecutor;
import org.springframework.integration.aws.sns.support.SnsTestProxy;
import org.springframework.integration.aws.support.SnsTestProxyImpl;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

/**
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SnsInboundChannelAdapterParserTests {

	private ConfigurableApplicationContext context;
	private MessageProducer producer;
	private Object recvPayload;
	private MessageMarshaller messageMarshaller;

	@Test
	public void testSnsInboundChannelAdapterParser() throws Exception {

		setUp("SnsInboundChannelAdapterParserTests.xml", getClass(),
				"snsInboundChannelAdapter");

		final AbstractMessageChannel outputChannel = TestUtils
				.getPropertyValue(this.producer, "outputChannel",
						AbstractMessageChannel.class);

		assertEquals("out", outputChannel.getComponentName());

		final SnsExecutor snsExecutor = TestUtils.getPropertyValue(
				this.producer, "snsExecutor", SnsExecutor.class);

		assertNotNull(snsExecutor);

		final String topicNameProperty = TestUtils.getPropertyValue(
				snsExecutor, "topicName", String.class);

		assertEquals("testTopic", topicNameProperty);

	}

	@Test
	public void testSnsExecutorBeanIdNaming() throws Exception {

		this.context = new ClassPathXmlApplicationContext(
				"SnsInboundChannelAdapterParserTests.xml", getClass());
		assertNotNull(context.getBean("snsInboundChannelAdapter.snsExecutor",
				SnsExecutor.class));

	}

	@Test
	public void testMessageFlow() throws Exception {
		setUp("SnsInboundChannelAdapterParserTests.xml", getClass(),
				"snsInboundChannelAdapter");

		recvPayload = null;
		SnsTestProxy snsTestProxy = context.getBean(SnsTestProxyImpl.class);
		DirectChannel channel = (DirectChannel) context.getBean("out");
		channel.subscribe(new MessageHandler() {

			@Override
			public void handleMessage(Message<?> message)
					throws MessagingException {
				recvPayload = message.getPayload();
			}
		});

		String payload = "Hello, World";
		Message<?> message = MessageBuilder.withPayload(payload).build();
		snsTestProxy.dispatchMessage(messageMarshaller.serialize(message));
		Thread.sleep(1000);

		assertNotNull(recvPayload);
		assertEquals(payload, recvPayload);
	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

	public void setUp(String name, Class<?> cls, String consumerId) {
		context = new ClassPathXmlApplicationContext(name, cls);
		producer = this.context.getBean(consumerId, MessageProducer.class);
		messageMarshaller = new JsonMessageMarshaller();
	}

}
