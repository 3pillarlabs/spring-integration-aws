package org.springframework.integration.aws.sns.config;

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
import org.springframework.integration.aws.sns.core.SnsExecutor;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.test.util.TestUtils;

/**
 * @author Sayantam Dey
 * @since 2.0.0
 * 
 */
public class SnsPermissionsParserTests {

	private ConfigurableApplicationContext context;

	@Before
	public void setup() {
		context = new ClassPathXmlApplicationContext(
				"SnsPermissionsParserTests.xml", this.getClass());
	}

	@Test
	public void inboundAdapterPermissions() {
		MessageProducer producer = context.getBean("sns-inbound",
				MessageProducer.class);
		assertThat(producer, is(notNullValue()));

		final SnsExecutor executor = TestUtils.getPropertyValue(producer,
				"snsExecutor", SnsExecutor.class);
		assertThat("snsExecutor is not null", executor, is(notNullValue()));

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
				containsInAnyOrder("Publish", "Receive"));
		assertThat("All label2 actions loaded", labelActionMap.get("label2"),
				containsInAnyOrder("GetTopicAttributes"));
		assertThat("All label1 accounts loaded", labelAccountMap.get("label1"),
				containsInAnyOrder("123456", "234567", "345678"));
		assertThat("All label2 accounts loaded", labelAccountMap.get("label2"),
				containsInAnyOrder("456789"));
	}

	@Test
	public void outboundPermissions() {
		EventDrivenConsumer consumer = context.getBean("sns-outbound",
				EventDrivenConsumer.class);
		assertThat(consumer, is(notNullValue()));

		final SnsExecutor executor = TestUtils.getPropertyValue(consumer,
				"handler.snsExecutor", SnsExecutor.class);
		assertThat("snsExecutor is not null", executor, is(notNullValue()));

		@SuppressWarnings("unchecked")
		Set<Permission> permissions = (Set<Permission>) TestUtils
				.getPropertyValue(executor, "permissions");
		assertThat("permissions is not null", permissions, is(notNullValue()));
		assertThat("all permissions loaded", permissions.size(), is(equalTo(1)));
	}

	@Test
	public void outboundGatewayPermissions() {
		EventDrivenConsumer consumer = context.getBean("sns-gateway",
				EventDrivenConsumer.class);
		assertThat(consumer, is(notNullValue()));

		final SnsExecutor executor = TestUtils.getPropertyValue(consumer,
				"handler.snsExecutor", SnsExecutor.class);
		assertThat("snsExecutor is not null", executor, is(notNullValue()));

		@SuppressWarnings("unchecked")
		Set<Permission> permissions = (Set<Permission>) TestUtils
				.getPropertyValue(executor, "permissions");
		assertThat("permissions is not null", permissions, is(notNullValue()));
		assertThat("all permissions loaded", permissions.size(), is(equalTo(1)));
	}

	@Test
	public void channelPermissions() {
		SubscribableChannel channel = context.getBean("sns-channel",
				SubscribableChannel.class);
		assertThat(channel, is(notNullValue()));

		final SnsExecutor executor = TestUtils.getPropertyValue(channel,
				"snsExecutor", SnsExecutor.class);
		assertThat("snsExecutor is not null", executor, is(notNullValue()));

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
