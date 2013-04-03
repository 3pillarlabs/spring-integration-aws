package org.springframework.integration.sqs.config.xml;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;

import com.threepillar.labs.si.sqs.core.SqsExecutor;
import com.threepillar.labs.si.sqs.outbound.SqsOutboundGateway;

/**
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SqsOutboundGatewayParserTests {

	private ConfigurableApplicationContext context;

	private EventDrivenConsumer consumer;

	@Test
	public void testRetrievingSqsOutboundGatewayParser() throws Exception {
		setUp("SqsOutboundGatewayParserTests.xml", getClass(),
				"sqsOutboundGateway");

		final AbstractMessageChannel inputChannel = TestUtils.getPropertyValue(
				this.consumer, "inputChannel", AbstractMessageChannel.class);

		assertEquals("in", inputChannel.getComponentName());

		final SqsOutboundGateway sqsOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						SqsOutboundGateway.class);

		long sendTimeout = TestUtils.getPropertyValue(sqsOutboundGateway,
				"messagingTemplate.sendTimeout", Long.class);

		assertEquals(100, sendTimeout);

		final SqsExecutor sqsExecutor = TestUtils.getPropertyValue(
				this.consumer, "handler.sqsExecutor", SqsExecutor.class);

		assertNotNull(sqsExecutor);

		final String queueNameProperty = TestUtils.getPropertyValue(
				sqsExecutor, "queueName", String.class);

		assertEquals("testQueue", queueNameProperty);

	}

	@Test
	public void testMessageFlow() {
		setUp("SqsOutboundGatewayParserTests.xml", getClass(),
				"sqsOutboundGateway");

		String payload = "Hello World";
		MessageChannel in = (MessageChannel) context.getBean("in");
		in.send(MessageBuilder.withPayload(payload).build());
		PollableChannel out = (PollableChannel) context.getBean("out");
		Message<?> recv = out.receive(1000);
		assertEquals(payload, recv.getPayload());
	}

	@Test
	public void testSqsExecutorBeanIdNaming() throws Exception {

		this.context = new ClassPathXmlApplicationContext(
				"SqsOutboundGatewayParserTests.xml", getClass());
		assertNotNull(context.getBean("sqsOutboundGateway.sqsExecutor",
				SqsExecutor.class));

	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

	public void setUp(String name, Class<?> cls, String gatewayId) {
		context = new ClassPathXmlApplicationContext(name, cls);
		consumer = this.context.getBean(gatewayId, EventDrivenConsumer.class);
	}

}
