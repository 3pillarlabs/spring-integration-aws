package org.springframework.integration.aws.sqs.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.sqs.channel.SubscribableSqsChannel;
import org.springframework.integration.config.xml.AbstractChannelParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;


public class SqsChannelParser extends AbstractChannelParser {

	@Override
	protected BeanDefinitionBuilder buildBeanDefinition(Element element,
			ParserContext parserContext) {

		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SubscribableSqsChannel.class);

		// create and set sqsExecutor
		final BeanDefinitionBuilder sqsExecutorBuilder = SqsParserUtils
				.getSqsExecutorBuilder(element, parserContext);

		final BeanDefinition sqsExecutorBuilderBeanDefinition = sqsExecutorBuilder
				.getBeanDefinition();
		final String channelAdapterId = this.resolveId(element,
				beanDefinitionBuilder.getRawBeanDefinition(), parserContext);
		final String sqsExecutorBeanName = SqsParserUtils
				.getExecutorBeanName(channelAdapterId);

		parserContext.registerBeanComponent(new BeanComponentDefinition(
				sqsExecutorBuilderBeanDefinition, sqsExecutorBeanName));

		beanDefinitionBuilder.addPropertyReference("sqsExecutor",
				sqsExecutorBeanName);

		SqsParserUtils.registerExecutorProxy(element, sqsExecutorBeanName,
				parserContext);
		// ---

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				beanDefinitionBuilder, element, "concurrent-consumers");

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				beanDefinitionBuilder, element, "message-driven");

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				beanDefinitionBuilder, element, "phase");

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				beanDefinitionBuilder, element, "worker-shutdown-timeout");

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				beanDefinitionBuilder, element, "receive-message-wait-timeout");

		return beanDefinitionBuilder;
	}

}
