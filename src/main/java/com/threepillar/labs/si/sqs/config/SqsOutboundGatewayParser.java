package com.threepillar.labs.si.sqs.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractConsumerEndpointParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.threepillar.labs.si.sqs.outbound.SqsOutboundGateway;

/**
 * The Parser for Sqs Outbound Gateway.
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SqsOutboundGatewayParser extends AbstractConsumerEndpointParser {

	@Override
	protected BeanDefinitionBuilder parseHandler(Element gatewayElement,
			ParserContext parserContext) {

		final BeanDefinitionBuilder sqsOutboundGatewayBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SqsOutboundGateway.class);

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				sqsOutboundGatewayBuilder, gatewayElement, "reply-timeout",
				"sendTimeout");

		final String replyChannel = gatewayElement
				.getAttribute("reply-channel");

		if (StringUtils.hasText(replyChannel)) {
			sqsOutboundGatewayBuilder.addPropertyReference("outputChannel",
					replyChannel);
		}

		final BeanDefinitionBuilder sqsExecutorBuilder = SqsParserUtils
				.getSqsExecutorBuilder(gatewayElement, parserContext);

		final BeanDefinition sqsExecutorBuilderBeanDefinition = sqsExecutorBuilder
				.getBeanDefinition();
		final String gatewayId = this
				.resolveId(gatewayElement,
						sqsOutboundGatewayBuilder.getRawBeanDefinition(),
						parserContext);
		final String sqsExecutorBeanName = SqsParserUtils
				.getExecutorBeanName(gatewayId);

		parserContext.registerBeanComponent(new BeanComponentDefinition(
				sqsExecutorBuilderBeanDefinition, sqsExecutorBeanName));

		sqsOutboundGatewayBuilder.addPropertyReference("sqsExecutor",
				sqsExecutorBeanName);

		SqsParserUtils.registerExecutorProxy(gatewayElement,
				sqsExecutorBeanName, parserContext);

		return sqsOutboundGatewayBuilder;

	}

	@Override
	protected String getInputChannelAttributeName() {
		return "request-channel";
	}

}
