package com.threepillar.labs.si.sns.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
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

		return snsExecutorBuilder;

	}

	public static void registerSubscriptions(final Element element,
			final ParserContext parserContext,
			final BeanDefinitionBuilder snsExecutorBuilder,
			final String channelAdapterId) {

		List<Subscription> subscriptionList = new ArrayList<Subscription>();
		Map<String, BeanReference> sqsExecutorMap = new ManagedMap<String, BeanReference>();

		// HTTP endpoint
		Element endpointElement = DomUtils.getChildElementByTagName(element,
				"endpoint");
		if (endpointElement != null) {
			String baseURI = endpointElement.getAttribute("base-uri");
			String requestPath = null;
			if (endpointElement.hasAttribute("request-path")) {
				requestPath = endpointElement.getAttribute("request-path");
			} else {
				requestPath = String.format("/%s.do", channelAdapterId);
			}
			subscriptionList.add(new Subscription()
					.withEndpoint(baseURI.concat(requestPath))
					.withProtocol(
							baseURI.startsWith("https") ? "https" : "http"));

			// register a HttpEndpoint at this path
			BeanDefinitionBuilder httpEndpointBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(HttpEndpoint.class);
			String beanName = String
					.format("%s-httpEndpoint", channelAdapterId);
			parserContext.registerBeanComponent(new BeanComponentDefinition(
					httpEndpointBuilder.getBeanDefinition(), beanName,
					new String[] { requestPath }));
			snsExecutorBuilder.addPropertyReference("httpEndpoint", beanName);
		}

		// subscriptions element
		Element subscriptionsElement = DomUtils.getChildElementByTagName(
				element, "subscriptions");
		if (subscriptionsElement != null) {
			NodeList childNodes = subscriptionsElement.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					Element childElement = (Element) child;
					String localName = child.getLocalName();
					if ("http".equals(localName)) {
						String uri = childElement.getAttribute("base-uri");
						if (childElement.hasAttribute("request-path")) {
							uri = uri.concat(childElement
									.getAttribute("request-path"));
						}
						subscriptionList.add(new Subscription()
								.withEndpoint(uri)
								.withProtocol(
										uri.startsWith("https") ? "https" :
												"http"));

					} else if ("sqs".equals(localName)) {
						String queueArn = null;
						if (childElement.hasAttribute("queue-arn")) {
							queueArn = childElement.getAttribute("queue-arn");
						}
						String queueId = null;
						if (childElement.hasAttribute("queue-id")) {
							queueId = childElement.getAttribute("queue-id");
						}
						Assert.state(queueArn != null || queueId != null,
								"One of 'queue-arn' or 'queue-id' needs to be defined");
						if (queueId != null) {
							String sqsBeanName = SqsParserUtils
									.getExecutorBeanName(queueId);
							snsExecutorBuilder.addDependsOn(sqsBeanName);
							sqsExecutorMap.put(queueId,
									new RuntimeBeanReference(sqsBeanName));
							subscriptionList.add(new Subscription()
									.withEndpoint(queueId)
									.withProtocol("sqs"));

						} else {
							subscriptionList.add(new Subscription()
									.withEndpoint(queueArn)
									.withProtocol("sqs"));
						}
					}
				}
			}
			if (!sqsExecutorMap.isEmpty()) {
				snsExecutorBuilder.addPropertyValue("sqsExecutorMap",
						sqsExecutorMap);
			}
		}

		snsExecutorBuilder.addPropertyValue("subscriptionList",
				subscriptionList);

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
