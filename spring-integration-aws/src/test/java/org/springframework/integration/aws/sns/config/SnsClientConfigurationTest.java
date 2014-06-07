package org.springframework.integration.aws.sns.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.sns.channel.PublishSubscribeSnsChannel;
import org.springframework.integration.test.util.TestUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;

/**
 * SnsClientConfigurationTest
 * 
 * @author scubi
 */
public class SnsClientConfigurationTest {

	private ConfigurableApplicationContext context;
	private PublishSubscribeSnsChannel snsChannelWithClientConfiguration;

	@Test
	public void testClientConfigurationInjectedExplicitly() {

		setUp("SnsClientConfigurationTests.xml", getClass(),
				"snsChannelWithClientConfiguration");

		final ClientConfiguration clientConfiguration = TestUtils
				.getPropertyValue(snsChannelWithClientConfiguration,
						"snsExecutor.awsClientConfiguration",
						ClientConfiguration.class);
		assertThat(clientConfiguration.getProtocol(), is(Protocol.HTTPS));
		assertThat(clientConfiguration.getProxyHost(), is("PROXY_HOST"));
	}

	public void setUp(String name, Class<?> cls, String consumerId) {
		context = new ClassPathXmlApplicationContext(name, cls);
		snsChannelWithClientConfiguration = this.context.getBean(consumerId,
				PublishSubscribeSnsChannel.class);
	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}
}
