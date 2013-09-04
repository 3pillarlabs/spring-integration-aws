package org.springframework.integration.sqs.config.xml;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.threepillar.labs.si.sns.channel.PublishSubscribeSnsChannel;
import com.threepillar.labs.si.sqs.channel.SubscribableSqsChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * SnsClientConfigurationTest
 *
 * @author scubi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/org/springframework/integration/sqs/config/xml/SqsClientConfigurationTests.xml")
public class SqsClientConfigurationTest {

    @Autowired(required = true)
    private SubscribableSqsChannel sqsChannelWithClientConfiguration;

    @Test
    public void testClientConfigurationInjectedExplicitly() {
        final ClientConfiguration clientConfiguration = TestUtils.getPropertyValue(sqsChannelWithClientConfiguration, "sqsExecutor.awsClientConfiguration", ClientConfiguration.class);
        assertThat(clientConfiguration.getProtocol(), is(Protocol.HTTPS));
        assertThat(clientConfiguration.getProxyHost(), is("PROXY_HOST"));
    }
}
