package org.springframework.integration.aws.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.Permission;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Sayantam Dey
 * @since 2.0.0
 * 
 */
public final class AwsParserUtils {

	private AwsParserUtils() {
	}

	public static void registerPermissions(Element element,
			BeanDefinitionBuilder executorBuilder,
			ParserContext parserContext) {

		Element permissionsElement = DomUtils.getChildElementByTagName(element,
				"permissions");
		if (permissionsElement != null) {
			Set<Permission> permissions = new HashSet<Permission>();
			NodeList permNodes = permissionsElement.getChildNodes();
			for (int i = 0; i < permNodes.getLength(); i++) {
				Node permNode = permNodes.item(i);
				if (Node.ELEMENT_NODE == permNode.getNodeType()) {
					Element permissionElement = (Element) permNode;
					Permission permission = new Permission(
							permissionElement.getAttribute("label"),
							new HashSet<String>(), new HashSet<String>());

					Element actionsElement = DomUtils.getChildElementByTagName(
							permissionElement, "actions");
					NodeList actionNodes = actionsElement.getChildNodes();
					for (int j = 0; j < actionNodes.getLength(); j++) {
						Node actionNode = actionNodes.item(j);
						if (Node.ELEMENT_NODE == actionNode.getNodeType()) {
							Element actionElement = (Element) actionNode;
							permission.getActions().add(
									actionElement.getTextContent());
						}
					}

					Element accountsElement = DomUtils
							.getChildElementByTagName(permissionElement,
									"aws-accounts");
					NodeList accountNodes = accountsElement.getChildNodes();
					for (int j = 0; j < accountNodes.getLength(); j++) {
						Node accountNode = accountNodes.item(j);
						if (Node.ELEMENT_NODE == accountNode.getNodeType()) {
							Element accountElement = (Element) accountNode;
							permission.getAwsAccountIds().add(
									accountElement.getTextContent());
						}
					}

					permissions.add(permission);
				}
			}

			executorBuilder.addPropertyValue("permissions", permissions);
		}
	}

}
