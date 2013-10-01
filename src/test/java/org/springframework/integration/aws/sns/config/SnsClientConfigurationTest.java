package org.springframework.integration.aws.sns.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.aws.sns.channel.PublishSubscribeSnsChannel;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;

/**
 * SnsClientConfigurationTest
 * 
 * @author scubi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/org/springframework/integration/aws/sns/config/SnsClientConfigurationTests.xml")
public class SnsClientConfigurationTest {

	@Autowired(required = true)
	private PublishSubscribeSnsChannel snsChannelWithClientConfiguration;

	@Test
	public void testClientConfigurationInjectedExplicitly() {
		final ClientConfiguration clientConfiguration = TestUtils
				.getPropertyValue(snsChannelWithClientConfiguration,
						"snsExecutor.awsClientConfiguration",
						ClientConfiguration.class);
		assertThat(clientConfiguration.getProtocol(), is(Protocol.HTTPS));
		assertThat(clientConfiguration.getProxyHost(), is("PROXY_HOST"));
	}
}
