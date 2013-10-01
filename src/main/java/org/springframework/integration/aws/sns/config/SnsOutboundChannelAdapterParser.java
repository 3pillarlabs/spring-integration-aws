package org.springframework.integration.aws.sns.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.sns.outbound.SnsOutboundGateway;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.w3c.dom.Element;


/**
 * The parser for the Sns Outbound Channel Adapter.
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SnsOutboundChannelAdapterParser extends
		AbstractOutboundChannelAdapterParser {

	@Override
	protected boolean shouldGenerateId() {
		return false;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	@Override
	protected AbstractBeanDefinition parseConsumer(Element element,
			ParserContext parserContext) {

		final BeanDefinitionBuilder snsOutboundChannelAdapterBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SnsOutboundGateway.class);
		final BeanDefinitionBuilder snsExecutorBuilder = SnsParserUtils
				.getSnsExecutorBuilder(element, parserContext);

		final BeanDefinition snsExecutorBuilderBeanDefinition = snsExecutorBuilder
				.getBeanDefinition();
		final String channelAdapterId = this.resolveId(element,
				snsOutboundChannelAdapterBuilder.getRawBeanDefinition(),
				parserContext);
		final String snsExecutorBeanName = channelAdapterId + ".snsExecutor";

		SnsParserUtils.registerSubscriptions(element, parserContext,
				snsExecutorBuilder, channelAdapterId);

		parserContext.registerBeanComponent(new BeanComponentDefinition(
				snsExecutorBuilderBeanDefinition, snsExecutorBeanName));

		snsOutboundChannelAdapterBuilder.addPropertyReference("snsExecutor",
				snsExecutorBeanName);

		SnsParserUtils.registerExecutorProxy(element, snsExecutorBeanName,
				parserContext);

		snsOutboundChannelAdapterBuilder.addPropertyValue("producesReply",
				Boolean.FALSE);

		return snsOutboundChannelAdapterBuilder.getBeanDefinition();

	}

}
