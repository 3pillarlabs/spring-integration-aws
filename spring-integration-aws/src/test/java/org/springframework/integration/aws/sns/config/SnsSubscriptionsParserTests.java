package org.springframework.integration.aws.sns.config;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.integration.support.MessageBuilder;

/**
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SnsSubscriptionsParserTests {

	private ConfigurableApplicationContext context;

	@Test
	public void testSubscription() throws Exception {
		context = new ClassPathXmlApplicationContext(
				"SnsSubscriptionsParserTests.xml", getClass());

		final String expectedPayload = "Hello, World";

		MessageChannel target = (MessageChannel) context.getBean("target");
		target.send(MessageBuilder.withPayload(expectedPayload).build());

		PollableChannel sink = (PollableChannel) context.getBean("sink");
		Message<?> message = sink.receive(1000);
		String payload = (String) message.getPayload();
		assertEquals(expectedPayload, payload);
	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

}
