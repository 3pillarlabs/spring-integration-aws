package org.springframework.integration.aws.sns.config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.integration.aws.sns.SnsExecutorProxy;
import org.springframework.integration.aws.sns.channel.PublishSubscribeSnsChannel;
import org.springframework.integration.aws.sns.core.SnsExecutor;
import org.springframework.messaging.MessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;

import com.amazonaws.services.sns.model.Subscription;

@RunWith(JUnit4.class)
public class SnsChannelParserTests {

	private ConfigurableApplicationContext context;
	private PublishSubscribeSnsChannel channel;
	private Object recvPayload;

	@Test
	public void testSnsChannelParser() {
		setUp("SnsChannelParserTests.xml", getClass(), "snsChannel");

		final SnsExecutor snsExecutor = TestUtils.getPropertyValue(
				this.channel, "snsExecutor", SnsExecutor.class);
		assertNotNull(snsExecutor);

		final String topicNameProperty = TestUtils.getPropertyValue(
				snsExecutor, "topicName", String.class);
		assertEquals("testTopic", topicNameProperty);

		assertEquals(true, TestUtils.getPropertyValue(channel, "autoStartup",
				Boolean.class));

		assertTrue(TestUtils.getPropertyValue(channel, "phase", Integer.class) == 0);

		@SuppressWarnings("unchecked")
		final List<Subscription> subscriptions = TestUtils.getPropertyValue(
				snsExecutor, "subscriptionList", List.class);
		assertThat(subscriptions, is(not(empty())));
		Subscription defS = subscriptions.get(0);
		assertThat(defS.getEndpoint(), containsString("www.example.com"));

		Object snsExecutorProxy = context.getBean("snsExecutorProxy");
		assertNotNull(snsExecutorProxy);
		assertEquals(SnsExecutorProxy.class, snsExecutorProxy.getClass());
		SnsExecutor proxiedExecutor = TestUtils.getPropertyValue(
				snsExecutorProxy, "snsExecutor", SnsExecutor.class);
		assertNotNull(proxiedExecutor);
		SnsExecutor innerBean = context.getBean(SnsExecutor.class);
		assertSame(innerBean, proxiedExecutor);
	}

	@Test
	public void testSnsExecutorBeanIdNaming() throws Exception {

		this.context = new ClassPathXmlApplicationContext(
				"SnsChannelParserTests.xml", getClass());
		assertNotNull(context.getBean("snsChannel.snsExecutor",
				SnsExecutor.class));

	}

	@Test
	public void testMessageFlow() throws Exception {

		setUp("SnsChannelParserTests.xml", getClass(), "snsChannel");

		recvPayload = null;
		PublishSubscribeSnsChannel snsChannel = (PublishSubscribeSnsChannel) context
				.getBean("snsChannel");
		snsChannel.subscribe(new MessageHandler() {

			@Override
			public void handleMessage(Message<?> message)
					throws MessagingException {
				recvPayload = message.getPayload();
			}
		});

		String payload = "Hello, World";
		snsChannel.send(MessageBuilder.withPayload(payload).build());
		Thread.sleep(1000);

		assertNotNull(recvPayload);
		assertEquals(payload, recvPayload);
	}

	public void setUp(String name, Class<?> cls, String consumerId) {
		context = new ClassPathXmlApplicationContext(name, cls);
		channel = this.context.getBean(consumerId,
				PublishSubscribeSnsChannel.class);
	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

}
