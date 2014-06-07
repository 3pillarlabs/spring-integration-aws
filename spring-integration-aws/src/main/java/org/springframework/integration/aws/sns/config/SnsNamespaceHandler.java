package org.springframework.integration.aws.sns.config;

import org.springframework.integration.config.xml.AbstractIntegrationNamespaceHandler;

/**
 * The namespace handler for the Sns namespace
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SnsNamespaceHandler extends AbstractIntegrationNamespaceHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
	 */
	@Override
	public void init() {
		this.registerBeanDefinitionParser("inbound-channel-adapter",
				new SnsInboundChannelAdapterParser());
		this.registerBeanDefinitionParser("outbound-channel-adapter",
				new SnsOutboundChannelAdapterParser());
		this.registerBeanDefinitionParser("outbound-gateway",
				new SnsOutboundGatewayParser());
		this.registerBeanDefinitionParser("publish-subscribe-channel",
				new SnsPublishSubscribeChannelParser());
	}
}
