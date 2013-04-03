package com.threepillar.labs.si.sqs.config;

import org.springframework.integration.config.xml.AbstractIntegrationNamespaceHandler;

/**
 * The namespace handler for the Sqs namespace
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SqsNamespaceHandler extends AbstractIntegrationNamespaceHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
	 */
	@Override
	public void init() {
		this.registerBeanDefinitionParser("inbound-channel-adapter",
				new SqsInboundChannelAdapterParser());
		this.registerBeanDefinitionParser("outbound-channel-adapter",
				new SqsOutboundChannelAdapterParser());
		this.registerBeanDefinitionParser("outbound-gateway",
				new SqsOutboundGatewayParser());
		this.registerBeanDefinitionParser("channel", new SqsChannelParser());
	}
}
