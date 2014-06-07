package org.springframework.integration.aws.sqs.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.sqs.channel.SubscribableSqsChannel;
import org.springframework.integration.test.util.TestUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;

/**
 * SnsClientConfigurationTest
 * 
 * @author scubi
 */
public class SqsClientConfigurationTest {

	private ConfigurableApplicationContext context;
	private SubscribableSqsChannel sqsChannelWithClientConfiguration;

	@Test
	public void testClientConfigurationInjectedExplicitly() {

		setUp("SqsClientConfigurationTests.xml", getClass(),
				"sqsChannelWithClientConfiguration");

		final ClientConfiguration clientConfiguration = TestUtils
				.getPropertyValue(sqsChannelWithClientConfiguration,
						"sqsExecutor.awsClientConfiguration",
						ClientConfiguration.class);
		assertThat(clientConfiguration.getProtocol(), is(Protocol.HTTPS));
		assertThat(clientConfiguration.getProxyHost(), is("PROXY_HOST"));
	}

	public void setUp(String name, Class<?> cls, String consumerId) {
		context = new ClassPathXmlApplicationContext(name, cls);
		sqsChannelWithClientConfiguration = this.context.getBean(consumerId,
				SubscribableSqsChannel.class);
	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}
}
