package org.springframework.integration.aws.sqs.config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.Permission;
import org.springframework.integration.aws.sqs.core.SqsExecutor;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.test.util.TestUtils;

/**
 * @author Sayantam Dey
 * @since 2.0.0
 * 
 */
public class SqsPermissionsParserTests {

	private ConfigurableApplicationContext context;

	@Before
	public void setup() {
		context = new ClassPathXmlApplicationContext(
				"SqsPermissionsParserTests.xml", this.getClass());
	}

	@Test
	public void inboundAdapterPermissions() {
		MessageProducer producer = context.getBean("sqs-inbound",
				MessageProducer.class);
		assertThat(producer, is(notNullValue()));

		final SqsExecutor executor = TestUtils.getPropertyValue(producer,
				"sqsExecutor", SqsExecutor.class);
		assertThat(executor, is(notNullValue()));

		@SuppressWarnings("unchecked")
		Set<Permission> permissions = (Set<Permission>) TestUtils
				.getPropertyValue(executor, "permissions");
		assertThat("permissions is not null", permissions, is(notNullValue()));
		assertThat("all permissions loaded", permissions.size(), is(equalTo(2)));

		Set<String> labels = new HashSet<String>();
		Map<String, Set<String>> labelActionMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> labelAccountMap = new HashMap<String, Set<String>>();

		for (Permission p : permissions) {
			labels.add(p.getLabel());
			assertThat("actions are not null", p.getActions(),
					is(notNullValue()));
			assertThat("awsAccounts are not null", p.getAwsAccountIds(),
					is(notNullValue()));
			labelActionMap.put(p.getLabel(), p.getActions());
			labelAccountMap.put(p.getLabel(), p.getAwsAccountIds());
		}

		assertThat("All labels found", labels,
				containsInAnyOrder("label1", "label2"));
		assertThat("All label1 actions loaded", labelActionMap.get("label1"),
				containsInAnyOrder("SendMessage", "GetQueueUrl"));
		assertThat("All label2 actions loaded", labelActionMap.get("label2"),
				containsInAnyOrder("ReceiveMessage"));
		assertThat("All label1 accounts loaded", labelAccountMap.get("label1"),
				containsInAnyOrder("12345", "23456", "34567"));
		assertThat("All label2 accounts loaded", labelAccountMap.get("label2"),
				containsInAnyOrder("45678"));
	}

	@Test
	public void outboundPermissions() {
		EventDrivenConsumer consumer = context.getBean("sqs-outbound",
				EventDrivenConsumer.class);
		assertThat(consumer, is(notNullValue()));

		final SqsExecutor executor = TestUtils.getPropertyValue(consumer,
				"handler.sqsExecutor", SqsExecutor.class);
		assertThat(executor, is(notNullValue()));

		@SuppressWarnings("unchecked")
		Set<Permission> permissions = (Set<Permission>) TestUtils
				.getPropertyValue(executor, "permissions");
		assertThat("permissions is not null", permissions, is(notNullValue()));
		assertThat("all permissions loaded", permissions.size(), is(equalTo(1)));
	}

	@Test
	public void outboundGatewayPermissions() {
		EventDrivenConsumer consumer = context.getBean("sqs-gateway",
				EventDrivenConsumer.class);
		assertThat(consumer, is(notNullValue()));

		final SqsExecutor executor = TestUtils.getPropertyValue(consumer,
				"handler.sqsExecutor", SqsExecutor.class);
		assertThat(executor, is(notNullValue()));

		@SuppressWarnings("unchecked")
		Set<Permission> permissions = (Set<Permission>) TestUtils
				.getPropertyValue(executor, "permissions");
		assertThat("permissions is not null", permissions, is(notNullValue()));
		assertThat("all permissions loaded", permissions.size(), is(equalTo(1)));
	}

	@Test
	public void channelPermissions() {
		SubscribableChannel channel = context.getBean("sqs-channel",
				SubscribableChannel.class);
		assertThat(channel, is(notNullValue()));

		final SqsExecutor executor = TestUtils.getPropertyValue(channel,
				"sqsExecutor", SqsExecutor.class);
		assertThat(executor, is(notNullValue()));

		@SuppressWarnings("unchecked")
		Set<Permission> permissions = (Set<Permission>) TestUtils
				.getPropertyValue(executor, "permissions");
		assertThat("permissions is not null", permissions, is(notNullValue()));
		assertThat("all permissions loaded", permissions.size(), is(equalTo(1)));
	}

	@After
	public void teardown() {
		if (context != null) {
			context.close();
		}
	}
}
