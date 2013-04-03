package com.threepillar.labs.si.sns.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.Assert;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amazonaws.services.sns.model.Subscription;
import com.threepillar.labs.si.sns.SnsExecutorProxy;
import com.threepillar.labs.si.sns.core.HttpEndpoint;
import com.threepillar.labs.si.sns.core.SnsExecutor;
import com.threepillar.labs.si.sqs.config.SqsParserUtils;

/**
 * Contains various utility methods for parsing Sns Adapter specific namesspace
 * elements as well as for the generation of the the respective
 * {@link BeanDefinition}s.
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public final class SnsParserUtils {

	/** Prevent instantiation. */
	private SnsParserUtils() {
		throw new AssertionError();
	}

	/**
	 * Create a new {@link BeanDefinitionBuilder} for the class
	 * {@link SnsExecutor}. Initialize the wrapped {@link SnsExecutor} with
	 * common properties.
	 * 
	 * @param element
	 *            Must not be null
	 * @param parserContext
	 *            Must not be null
	 * @return The BeanDefinitionBuilder for the SnsExecutor
	 */
	public static BeanDefinitionBuilder getSnsExecutorBuilder(
			final Element element, final ParserContext parserContext) {

		Assert.notNull(element, "The provided element must not be null.");
		Assert.notNull(parserContext,
				"The provided parserContext must not be null.");

		final BeanDefinitionBuilder snsExecutorBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SnsExecutor.class);

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				snsExecutorBuilder, element, "topic-name");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(
				snsExecutorBuilder, element, "sns-test-proxy");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(
				snsExecutorBuilder, element, "aws-credentials-provider");

		IntegrationNamespaceUtils.setValueIfAttributeDefined(
				snsExecutorBuilder, element, "region-id");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(
				snsExecutorBuilder, element, "subscription-list");

		if (element.hasAttribute("http-endpoint-path")) {
			String urlPath = element.getAttribute("http-endpoint-path");
			snsExecutorBuilder.addPropertyValue("httpEndpointPath", urlPath);
			// register a HttpEndpoint at this path
			BeanDefinitionBuilder httpEndpointBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(HttpEndpoint.class);
			String beanName = String.format("%s-httpEndpoint",
					element.getAttribute("topic-name"));
			parserContext.registerBeanComponent(new BeanComponentDefinition(
					httpEndpointBuilder.getBeanDefinition(), beanName,
					new String[] { urlPath }));
			snsExecutorBuilder.addPropertyReference("httpEndpoint", beanName);
		}

		// subscriptions element
		Element subscriptionsElement = DomUtils.getChildElementByTagName(
				element, "subscriptions");
		if (subscriptionsElement != null) {
			NodeList childNodes = subscriptionsElement.getChildNodes();
			List<Subscription> subscriptionList = new ArrayList<Subscription>(
					childNodes.getLength());
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					Element childElement = (Element) child;
					String localName = child.getLocalName();
					if ("subscription".equals(localName)) {
						String protocol = childElement.getAttribute("protocol");
						String endpoint = childElement.getAttribute("endpoint");
						if (protocol.equals("sqs")) {
							// endpoint is actually a SQS channel (adapter) id
							String sqsBeanName = SqsParserUtils
									.getExecutorBeanName(endpoint);
							snsExecutorBuilder.addPropertyReference(
									"sqsExecutor", sqsBeanName);
						}
						subscriptionList.add(new Subscription().withEndpoint(
								endpoint).withProtocol(protocol));
					}
				}
			}
			snsExecutorBuilder.addPropertyValue("subscriptionList",
					subscriptionList);
		}

		return snsExecutorBuilder;

	}

	public static void registerExecutorProxy(Element element,
			String snsExecutorBeanName, ParserContext parserContext) {

		if (element.hasAttribute("sns-executor-proxy")) {
			String snsProxyBeanName = element
					.getAttribute("sns-executor-proxy");
			BeanDefinitionBuilder snsExecutorProxyBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(SnsExecutorProxy.class);
			snsExecutorProxyBuilder
					.addConstructorArgReference(snsExecutorBeanName);
			parserContext.registerBeanComponent(new BeanComponentDefinition(
					snsExecutorProxyBuilder.getBeanDefinition(),
					snsProxyBeanName));
		}
	}

}
