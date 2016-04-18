package org.jahia.services.render.scripting.thymeleaf.util;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.scripting.thymeleaf.DXDialectConstants;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Attribute;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.expression.*;
import org.thymeleaf.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by smomin on 2/12/16.
 */
public final class ProcessorUtil {

    private ProcessorUtil() {
        super();
    }

    /**
     * @param element
     * @param attributeMap
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getStringValue(final Element element,
                                        final Map<String, Attribute> attributeMap,
                                        final String key,
                                        final String defaultValue) {
        if (attributeMap.containsKey(key)) {
            return element.getAttributeValue(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * @param element
     * @param attributeMap
     * @param key
     * @return
     */
    public static Integer getIntegerValue(final Element element,
                                          final Map<String, Attribute> attributeMap,
                                          final String key,
                                          final Integer defaultValue) {
        if (attributeMap.containsKey(key)) {
            return Integer.parseInt(element.getAttributeValue(key));
        } else {
            return defaultValue;
        }
    }

    /**
     * @param element
     * @param attributeMap
     * @param key
     * @param defaultValue
     * @return
     */
    public static Long getLongValue(final Element element,
                                    final Map<String, Attribute> attributeMap,
                                    final String key,
                                    final Long defaultValue) {
        if (attributeMap.containsKey(key)) {
            return Long.parseLong(element.getAttributeValue(key));
        } else {
            return defaultValue;
        }
    }

    /**
     * @param element
     * @param attributeMap
     * @return
     */
    public static boolean getBooleanValue(final Element element,
                                          final Map<String, Attribute> attributeMap,
                                          final String key,
                                          final boolean defaultValue) {
        if (attributeMap.containsKey(key)) {
            return Boolean.parseBoolean(element.getAttributeValue(key));
        } else {
            return defaultValue;
        }
    }

    /**
     * @param arguments
     * @param element
     * @param attributeMap
     * @param configuration
     * @param parser
     * @return
     */
    public static JCRNodeWrapper getJcrNodeWrapper(final Arguments arguments,
                                                   final Element element,
                                                   final Map<String, Attribute> attributeMap,
                                                   final Configuration configuration,
                                                   final IStandardExpressionParser parser) {
        if (attributeMap.containsKey(DXDialectConstants.DX_ATTR_NODE)) {
            final String attributeValue = element.getAttributeValue(DXDialectConstants.DX_ATTR_NODE);
            final IStandardExpression expression = parser.parseExpression(configuration, arguments, attributeValue);
            return (JCRNodeWrapper) expression.execute(configuration, arguments);
        } else {
            return null;
        }
    }

    /**
     * @param arguments
     * @param element
     * @param attributeMap
     * @param configuration
     * @param parser
     * @return
     */
    public static JCRSiteNode getJcrSiteNode(final Arguments arguments,
                                             final Element element,
                                             final Map<String, Attribute> attributeMap,
                                             final Configuration configuration,
                                             final IStandardExpressionParser parser) {
        if (attributeMap.containsKey(DXDialectConstants.DX_ATTR_CONTEXT_SITE)) {
            final String attributeValue = element.getAttributeValue(DXDialectConstants.DX_ATTR_CONTEXT_SITE);
            final IStandardExpression expression = parser.parseExpression(configuration, arguments, attributeValue);
            return (JCRSiteNode) expression.execute(configuration, arguments);
        } else {
            return null;
        }
    }

    /**
     * @param arguments
     * @param element
     * @param key
     * @return
     */
    public static Map<String, String> getParameter(final Arguments arguments,
                                                   final Element element,
                                                   final String key) {
        final String attributeValue = element.getAttributeValue(key);
        if (attributeValue != null) {
            final Configuration configuration = arguments.getConfiguration();
            final AssignationSequence assignations = AssignationUtils.parseAssignationSequence(
                    configuration, arguments, attributeValue, false /* no parameters without value */);
            if (assignations == null) {
                throw new TemplateProcessingException(
                        "Could not parse value as attribute assignations: \"" + attributeValue + "\"");
            }

            final Map<String, String> parameters = new HashMap<String, String>(assignations.size() + 1, 1.0f);
            for (final Assignation assignation : assignations) {

                final IStandardExpression leftExpr = assignation.getLeft();
                final Object leftValue = leftExpr.execute(configuration, arguments);

                final IStandardExpression rightExpr = assignation.getRight();
                final Object rightValue = rightExpr.execute(configuration, arguments);

                final String newVariableName = (leftValue == null ? null : leftValue.toString());
                if (StringUtils.isEmptyOrWhitespace(newVariableName)) {
                    throw new TemplateProcessingException(
                            "Variable name expression evaluated as null or empty: \"" + leftExpr + "\"");
                }
                parameters.put(newVariableName, rightValue.toString());
            }
            return parameters;
        }
        return Collections.emptyMap();
    }
}
