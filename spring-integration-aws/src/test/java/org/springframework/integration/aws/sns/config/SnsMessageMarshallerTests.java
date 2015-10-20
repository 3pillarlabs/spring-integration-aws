package org.springframework.integration.aws.sns.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.MessageMarshaller;
import org.springframework.integration.aws.sns.core.SnsExecutor;
import org.springframework.integration.aws.support.TestMessageMarshaller;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.SubscribableChannel;

public class SnsMessageMarshallerTests {

	private ConfigurableApplicationContext context;

	@Before
	public void setup() {
		context = new ClassPathXmlApplicationContext(
				"SnsMessageMarshallerTests.xml", getClass());
	}

	@Test
	public void inboundAdapterConfig() {

		final MessageProducer producer = context.getBean("snsInbound",
				MessageProducer.class);
		checkMessageMarshallerRef(getSnsExecutor(producer, "snsExecutor"));
	}

	@Test
	public void outboundAdapterConfig() {

		final EventDrivenConsumer consumer = context.getBean("snsOutbound",
				EventDrivenConsumer.class);
		checkMessageMarshallerRef(getSnsExecutor(consumer,
				"handler.snsExecutor"));
	}

	@Test
	public void outboundGatewayConfig() {

		final EventDrivenConsumer consumer = context.getBean("snsGateway",
				EventDrivenConsumer.class);
		checkMessageMarshallerRef(getSnsExecutor(consumer,
				"handler.snsExecutor"));
	}

	@Test
	public void channelConfig() {

		final SubscribableChannel channel = context.getBean("snsChannel",
				SubscribableChannel.class);
		checkMessageMarshallerRef(getSnsExecutor(channel, "snsExecutor"));
	}

	private SnsExecutor getSnsExecutor(Object root, String property) {
		final SnsExecutor snsExecutor = TestUtils.getPropertyValue(root,
				property, SnsExecutor.class);
		assertNotNull(snsExecutor);
		return snsExecutor;
	}

	private void checkMessageMarshallerRef(SnsExecutor snsExecutor) {
		final MessageMarshaller messageMarshaller = TestUtils.getPropertyValue(
				snsExecutor, "messageMarshaller", MessageMarshaller.class);
		assertNotNull(messageMarshaller);
		assertThat(messageMarshaller, instanceOf(TestMessageMarshaller.class));
	}

}
