package org.springframework.integration.aws.sns.config;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.sns.core.SnsExecutor;
import org.springframework.integration.aws.sns.outbound.SnsOutboundGateway;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.test.util.TestUtils;


/**
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SnsOutboundGatewayParserTests {

	private ConfigurableApplicationContext context;

	private EventDrivenConsumer consumer;

	@Test
	public void testSnsOutboundGatewayParser() throws Exception {
		setUp("SnsOutboundGatewayParserTests.xml", getClass(),
				"snsOutboundGateway");

		final AbstractMessageChannel inputChannel = TestUtils.getPropertyValue(
				this.consumer, "inputChannel", AbstractMessageChannel.class);

		assertEquals("in", inputChannel.getComponentName());

		final SnsOutboundGateway snsOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						SnsOutboundGateway.class);

		long sendTimeout = TestUtils.getPropertyValue(snsOutboundGateway,
				"messagingTemplate.sendTimeout", Long.class);

		assertEquals(100, sendTimeout);

		final SnsExecutor snsExecutor = TestUtils.getPropertyValue(
				this.consumer, "handler.snsExecutor", SnsExecutor.class);

		assertNotNull(snsExecutor);

		final String topicNameProperty = TestUtils.getPropertyValue(
				snsExecutor, "topicName", String.class);

		assertEquals("testTopic", topicNameProperty);

	}

	@Test
	public void testSnsExecutorBeanIdNaming() throws Exception {

		this.context = new ClassPathXmlApplicationContext(
				"SnsOutboundGatewayParserTests.xml", getClass());
		assertNotNull(context.getBean("snsOutboundGateway.snsExecutor",
				SnsExecutor.class));

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
