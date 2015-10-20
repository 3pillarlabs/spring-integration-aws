package org.springframework.integration.aws.sqs.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.MessageMarshaller;
import org.springframework.integration.aws.sqs.core.SqsExecutor;
import org.springframework.integration.aws.support.TestMessageMarshaller;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.SubscribableChannel;

public class SqsMessageMarshallerTests {

	private ConfigurableApplicationContext context;

	@Before
	public void setup() {
		context = new ClassPathXmlApplicationContext(
				"SqsMessageMarshallerTests.xml", getClass());
	}

	@Test
	public void inboundAdapterConfig() {

		final MessageProducer producer = context.getBean("sqsInbound",
				MessageProducer.class);
		checkMessageMarshallerRef(getSqsExecutor(producer, "sqsExecutor"));
	}

	@Test
	public void outboundAdapterConfig() {

		final EventDrivenConsumer consumer = context.getBean("sqsOutbound",
				EventDrivenConsumer.class);
		checkMessageMarshallerRef(getSqsExecutor(consumer,
				"handler.sqsExecutor"));
	}

	@Test
	public void outboundGatewayConfig() {

		final EventDrivenConsumer consumer = context.getBean("sqsGateway",
				EventDrivenConsumer.class);
		checkMessageMarshallerRef(getSqsExecutor(consumer,
				"handler.sqsExecutor"));
	}

	@Test
	public void channelConfig() {

		final SubscribableChannel channel = context.getBean("sqsChannel",
				SubscribableChannel.class);
		checkMessageMarshallerRef(getSqsExecutor(channel, "sqsExecutor"));
	}

	private SqsExecutor getSqsExecutor(Object root, String property) {
		final SqsExecutor sqsExecutor = TestUtils.getPropertyValue(root,
				property, SqsExecutor.class);
		assertNotNull(sqsExecutor);
		return sqsExecutor;
	}

	private void checkMessageMarshallerRef(SqsExecutor sqsExecutor) {
		final MessageMarshaller messageMarshaller = TestUtils.getPropertyValue(
				sqsExecutor, "messageMarshaller", MessageMarshaller.class);
		assertNotNull(messageMarshaller);
		assertThat(messageMarshaller, instanceOf(TestMessageMarshaller.class));
	}

}
