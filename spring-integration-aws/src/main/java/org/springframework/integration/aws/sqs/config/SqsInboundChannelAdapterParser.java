package org.springframework.integration.aws.sqs.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.sqs.inbound.SqsSubscribableChannelAdapter;
import org.springframework.integration.config.SourcePollingChannelAdapterFactoryBean;
import org.springframework.integration.config.xml.AbstractChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;


/**
 * The Sqs Inbound Channel adapter parser
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SqsInboundChannelAdapterParser extends
		AbstractChannelAdapterParser {

	@Override
	protected AbstractBeanDefinition doParse(Element element,
			ParserContext parserContext, String channelName) {

		AbstractBeanDefinition beanDefinitition = null;

		final BeanDefinitionBuilder sqsAdapterBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SqsSubscribableChannelAdapter.class);

		// create and set sqsExecutor
		final BeanDefinitionBuilder sqsExecutorBuilder = SqsParserUtils
				.getSqsExecutorBuilder(element, parserContext);

		final BeanDefinition sqsExecutorBuilderBeanDefinition = sqsExecutorBuilder
				.getBeanDefinition();
		final String channelAdapterId = this.resolveId(element,
				sqsAdapterBuilder.getRawBeanDefinition(), parserContext);
		final String sqsExecutorBeanName = SqsParserUtils
				.getExecutorBeanName(channelAdapterId);

		parserContext.registerBeanComponent(new BeanComponentDefinition(
				sqsExecutorBuilderBeanDefinition, sqsExecutorBeanName));

		sqsAdapterBuilder.addPropertyReference("sqsExecutor",
				sqsExecutorBeanName);

		SqsParserUtils.registerExecutorProxy(element, sqsExecutorBeanName,
				parserContext);
		// ---

		// Core messaging properties
		sqsAdapterBuilder.addPropertyReference("outputChannel", channelName);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(sqsAdapterBuilder,
				element, "send-timeout");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(sqsAdapterBuilder,
				element, "auto-startup");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(sqsAdapterBuilder,
				element, "phase");
		// ---

		IntegrationNamespaceUtils.setValueIfAttributeDefined(sqsAdapterBuilder,
				element, "worker-shutdown-timeout");

		Element pollerElement = DomUtils.getChildElementByTagName(element,
				"poller");

		if (pollerElement == null) {
			IntegrationNamespaceUtils.setValueIfAttributeDefined(
					sqsAdapterBuilder, element, "concurrent-consumers");

			beanDefinitition = sqsAdapterBuilder.getBeanDefinition();

		} else {
			sqsAdapterBuilder.addPropertyValue("pollableMode", Boolean.TRUE);

			BeanMetadataElement source = sqsAdapterBuilder.getBeanDefinition();
			if (source == null) {
				parserContext.getReaderContext().error(
						"failed to parse source", element);
			}
			BeanDefinitionBuilder adapterBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(SourcePollingChannelAdapterFactoryBean.class);
			adapterBuilder.addPropertyValue("source", source);
			adapterBuilder.addPropertyReference("outputChannel", channelName);
			IntegrationNamespaceUtils.setValueIfAttributeDefined(
					adapterBuilder, element, "send-timeout");
			IntegrationNamespaceUtils.setValueIfAttributeDefined(
					adapterBuilder, element, "auto-startup");
			IntegrationNamespaceUtils.configurePollerMetadata(pollerElement,
					adapterBuilder, parserContext);

			beanDefinitition = adapterBuilder.getBeanDefinition();
		}

		return beanDefinitition;
	}

}
