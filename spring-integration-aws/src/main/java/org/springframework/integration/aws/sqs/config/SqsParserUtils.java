package org.springframework.integration.aws.sqs.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.sqs.SqsExecutorProxy;
import org.springframework.integration.aws.sqs.core.SqsExecutor;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

/**
 * Contains various utility methods for parsing Sqs Adapter specific namesspace
 * elements as well as for the generation of the the respective
 * {@link BeanDefinition}s.
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public final class SqsParserUtils {

	/** Prevent instantiation. */
	private SqsParserUtils() {
		throw new AssertionError();
	}

	/**
	 * Create a new {@link BeanDefinitionBuilder} for the class
	 * {@link SqsExecutor}. Initialize the wrapped {@link SqsExecutor} with
	 * common properties.
	 * 
	 * @param element
	 *            Must not be null
	 * @param parserContext
	 *            Must not be null
	 * @return The BeanDefinitionBuilder for the SqsExecutor
	 */
	public static BeanDefinitionBuilder getSqsExecutorBuilder(
			final Element element, final ParserContext parserContext) {

		Assert.notNull(element, "The provided element must not be null.");
		Assert.notNull(parserContext,
				"The provided parserContext must not be null.");

		final BeanDefinitionBuilder sqsExecutorBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SqsExecutor.class);

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				sqsExecutorBuilder, element, "queue-name");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(
				sqsExecutorBuilder, element, "queue");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(
				sqsExecutorBuilder, element, "aws-credentials-provider");

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				sqsExecutorBuilder, element, "receive-message-wait-timeout");

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				sqsExecutorBuilder, element, "region-id");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(
				sqsExecutorBuilder, element, "prefetch-count");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(
				sqsExecutorBuilder, element, "message-delay");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(
				sqsExecutorBuilder, element, "maximum-message-size");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(
				sqsExecutorBuilder, element, "message-retention-period");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(
				sqsExecutorBuilder, element, "visibility-timeout");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(
				sqsExecutorBuilder, element, "aws-client-configuration");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(
				sqsExecutorBuilder, element, "message-marshaller");

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				sqsExecutorBuilder, element, "queue-url");

		return sqsExecutorBuilder;

	}

	public static String getExecutorBeanName(String channelId) {
		return String.format("%s.sqsExecutor", channelId);
	}

	public static void registerExecutorProxy(Element element,
			String sqsExecutorBeanName, ParserContext parserContext) {

		if (element.hasAttribute("sqs-executor-proxy")) {
			String sqsProxyBeanName = element
					.getAttribute("sqs-executor-proxy");
			BeanDefinitionBuilder sqsExecutorProxyBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(SqsExecutorProxy.class);
			sqsExecutorProxyBuilder
					.addConstructorArgReference(sqsExecutorBeanName);
			parserContext.registerBeanComponent(new BeanComponentDefinition(
					sqsExecutorProxyBuilder.getBeanDefinition(),
					sqsProxyBeanName));
		}
	}

}
