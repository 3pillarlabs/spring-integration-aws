package com.threepillar.labs.si.sqs.unit;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.threepillar.labs.si.sqs.core.SqsExecutor;
import com.threepillar.labs.si.sqs.outbound.SqsOutboundGateway;
import com.threepillar.labs.si.sqs.unit.SqsOutboundGatewayTest.SqsOutboundGatewayContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SqsOutboundGatewayContext.class })
public class SqsOutboundGatewayTest {

	@Autowired
	ApplicationContext applicationContext;

	@Test
	public void testOutboundOperation() throws Exception {

		MessageHandler messageHandler = applicationContext
				.getBean(MessageHandler.class);
		@SuppressWarnings("unchecked")
		BlockingQueue<String> queue = applicationContext
				.getBean(BlockingQueue.class);

		String payload = "Hello, World";
		messageHandler.handleMessage(MessageBuilder.withPayload(payload)
				.build());

		String queued = queue.poll(5, TimeUnit.SECONDS);
		Assert.assertNotNull("'queued' message must not be null", queued);
		Assert.assertTrue("queued message must contain payload",
				queued.contains(payload));
	}

	@Configuration
	public static class SqsOutboundGatewayContext {

		@Bean
		public BlockingQueue<String> getQueue() {
			return new LinkedBlockingQueue<String>();
		}

		@Bean
		public SqsExecutor getSqsExecutor() {
			SqsExecutor sqsExecutor = new SqsExecutor();
			sqsExecutor.setQueueName("sqsOutboundGatewayTest");
			sqsExecutor.setQueue(getQueue());
			return sqsExecutor;
		}

		@Bean
		public MessageHandler getSqsOutboundAdapter() {
			SqsOutboundGateway gateway = new SqsOutboundGateway();
			gateway.setProducesReply(false);
			gateway.setSqsExecutor(getSqsExecutor());
			return gateway;
		}
	}
}
