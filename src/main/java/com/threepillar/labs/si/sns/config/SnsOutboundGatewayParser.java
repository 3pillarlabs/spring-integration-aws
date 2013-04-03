package com.threepillar.labs.si.sns.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractConsumerEndpointParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.threepillar.labs.si.sns.outbound.SnsOutboundGateway;

/**
 * The Parser for Sns Outbound Gateway.
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SnsOutboundGatewayParser extends AbstractConsumerEndpointParser {

	@Override
	protected BeanDefinitionBuilder parseHandler(Element gatewayElement,
			ParserContext parserContext) {

		final BeanDefinitionBuilder snsOutboundGatewayBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SnsOutboundGateway.class);

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				snsOutboundGatewayBuilder, gatewayElement, "reply-timeout",
				"sendTimeout");

		final String replyChannel = gatewayElement
				.getAttribute("reply-channel");

		if (StringUtils.hasText(replyChannel)) {
			snsOutboundGatewayBuilder.addPropertyReference("outputChannel",
					replyChannel);
		}

		final BeanDefinitionBuilder snsExecutorBuilder = SnsParserUtils
				.getSnsExecutorBuilder(gatewayElement, parserContext);

		final BeanDefinition snsExecutorBuilderBeanDefinition = snsExecutorBuilder
				.getBeanDefinition();
		final String gatewayId = this
				.resolveId(gatewayElement,
						snsOutboundGatewayBuilder.getRawBeanDefinition(),
						parserContext);
		final String snsExecutorBeanName = gatewayId + ".snsExecutor";

		parserContext.registerBeanComponent(new BeanComponentDefinition(
				snsExecutorBuilderBeanDefinition, snsExecutorBeanName));

		snsOutboundGatewayBuilder.addPropertyReference("snsExecutor",
				snsExecutorBeanName);

		SnsParserUtils.registerExecutorProxy(gatewayElement,
				snsExecutorBeanName, parserContext);

		return snsOutboundGatewayBuilder;

	}

	@Override
	protected String getInputChannelAttributeName() {
		return "request-channel";
	}

}
