package org.springframework.integration.aws.sns.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.sns.channel.PublishSubscribeSnsChannel;
import org.springframework.integration.config.xml.AbstractChannelParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;


public class SnsPublishSubscribeChannelParser extends AbstractChannelParser {

	@Override
	protected BeanDefinitionBuilder buildBeanDefinition(Element element,
			ParserContext parserContext) {

		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(PublishSubscribeSnsChannel.class);

		// create and set snsExecutor
		final BeanDefinitionBuilder snsExecutorBuilder = SnsParserUtils
				.getSnsExecutorBuilder(element, parserContext);

		final BeanDefinition snsExecutorBuilderBeanDefinition = snsExecutorBuilder
				.getBeanDefinition();
		final String channelAdapterId = this.resolveId(element,
				beanDefinitionBuilder.getRawBeanDefinition(), parserContext);
		final String snsExecutorBeanName = channelAdapterId + ".snsExecutor";

		SnsParserUtils.registerSubscriptions(element, parserContext,
				snsExecutorBuilder, channelAdapterId);

		parserContext.registerBeanComponent(new BeanComponentDefinition(
				snsExecutorBuilderBeanDefinition, snsExecutorBeanName));

		beanDefinitionBuilder.addPropertyReference("snsExecutor",
				snsExecutorBeanName);

		SnsParserUtils.registerExecutorProxy(element, snsExecutorBeanName,
				parserContext);
		// ---

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				beanDefinitionBuilder, element, "phase");

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				beanDefinitionBuilder, element, "auto-startup");

		return beanDefinitionBuilder;
	}

}
