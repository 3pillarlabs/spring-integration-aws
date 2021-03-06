package org.springframework.integration.aws.sqs.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.config.AwsParserUtils;
import org.springframework.integration.aws.sqs.outbound.SqsOutboundGateway;
import org.springframework.integration.config.xml.AbstractConsumerEndpointParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

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

		AwsParserUtils.registerPermissions(gatewayElement, sqsExecutorBuilder,
				parserContext);

		return sqsOutboundGatewayBuilder;

	}

	@Override
	protected String getInputChannelAttributeName() {
		return "channel";
	}

}
