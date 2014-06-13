package org.springframework.integration.aws.sqs.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.config.AwsParserUtils;
import org.springframework.integration.aws.sqs.outbound.SqsOutboundGateway;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.w3c.dom.Element;

/**
 * The parser for the Sqs Outbound Channel Adapter.
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SqsOutboundChannelAdapterParser extends
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

		final BeanDefinitionBuilder sqsOutboundChannelAdapterBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SqsOutboundGateway.class);
		final BeanDefinitionBuilder sqsExecutorBuilder = SqsParserUtils
				.getSqsExecutorBuilder(element, parserContext);

		final BeanDefinition sqsExecutorBuilderBeanDefinition = sqsExecutorBuilder
				.getBeanDefinition();
		final String channelAdapterId = this.resolveId(element,
				sqsOutboundChannelAdapterBuilder.getRawBeanDefinition(),
				parserContext);
		final String sqsExecutorBeanName = SqsParserUtils
				.getExecutorBeanName(channelAdapterId);

		parserContext.registerBeanComponent(new BeanComponentDefinition(
				sqsExecutorBuilderBeanDefinition, sqsExecutorBeanName));

		sqsOutboundChannelAdapterBuilder.addPropertyReference("sqsExecutor",
				sqsExecutorBeanName);

		SqsParserUtils.registerExecutorProxy(element, sqsExecutorBeanName,
				parserContext);

		sqsOutboundChannelAdapterBuilder.addPropertyValue("producesReply",
				Boolean.FALSE);

		AwsParserUtils.registerPermissions(element, sqsExecutorBuilder,
				parserContext);

		return sqsOutboundChannelAdapterBuilder.getBeanDefinition();

	}

}
