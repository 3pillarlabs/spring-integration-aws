package org.springframework.integration.aws.sns.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.config.AwsParserUtils;
import org.springframework.integration.aws.sns.inbound.SnsInboundChannelAdapter;
import org.springframework.integration.config.xml.AbstractChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;

/**
 * The Sns Inbound Channel adapter parser
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SnsInboundChannelAdapterParser extends
		AbstractChannelAdapterParser {

	@Override
	protected AbstractBeanDefinition doParse(Element element,
			ParserContext parserContext, String channelName) {

		final BeanDefinitionBuilder snsInboundChannelAdapterBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SnsInboundChannelAdapter.class);

		snsInboundChannelAdapterBuilder.addPropertyReference("outputChannel",
				channelName);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				snsInboundChannelAdapterBuilder, element, "send-timeout");

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				snsInboundChannelAdapterBuilder, element, "auto-startup");

		final BeanDefinitionBuilder snsExecutorBuilder = SnsParserUtils
				.getSnsExecutorBuilder(element, parserContext);

		final BeanDefinition snsExecutorBuilderBeanDefinition = snsExecutorBuilder
				.getBeanDefinition();
		final String channelAdapterId = this.resolveId(element,
				snsInboundChannelAdapterBuilder.getRawBeanDefinition(),
				parserContext);
		final String snsExecutorBeanName = channelAdapterId + ".snsExecutor";

		SnsParserUtils.registerSubscriptions(element, parserContext,
				snsExecutorBuilder, channelAdapterId);

		parserContext.registerBeanComponent(new BeanComponentDefinition(
				snsExecutorBuilderBeanDefinition, snsExecutorBeanName));

		snsInboundChannelAdapterBuilder.addPropertyReference("snsExecutor",
				snsExecutorBeanName);

		SnsParserUtils.registerExecutorProxy(element, snsExecutorBeanName,
				parserContext);

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				snsInboundChannelAdapterBuilder, element, "phase");

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				snsInboundChannelAdapterBuilder, element, "auto-startup");

		AwsParserUtils.registerPermissions(element, snsExecutorBuilder,
				parserContext);

		return snsInboundChannelAdapterBuilder.getBeanDefinition();
	}
}
