package org.jahia.services.render.scripting.thymeleaf.support.template;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
import org.jahia.services.render.scripting.thymeleaf.support.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.support.core.ModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Attribute;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.element.AbstractUnescapedTextChildModifierElementProcessor;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

import java.util.Map;

/**
 * Created by smomin on 2/9/16.
 */
public class ModuleElementProcessor extends AbstractUnescapedTextChildModifierElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleElementProcessor.class);

    public ModuleElementProcessor() {
        super("module");
    }

    @Override
    public int getPrecedence() {
        return 100;
    }

    @Override
    protected String getText(final Arguments arguments,
                             final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final String path = element.getAttributeValue(ScriptingConstants.ATTR_PATH);
        final String view = element.getAttributeValue(ScriptingConstants.ATTR_VIEW);
        final String templateType = element.getAttributeValue(ScriptingConstants.ATTR_TEMPLATE_TYPE);
        final String nodeTypes = element.getAttributeValue(ScriptingConstants.ATTR_NODE_TYPES);
        final String mockupStyle = element.getAttributeValue(ScriptingConstants.ATTR_MOCKUP_STYLE);
        final String nodeName = element.getAttributeValue(ScriptingConstants.ATTR_NODE_NAME);

        final Map<String, Attribute> attributeMap = element.getAttributeMap();
        final String areaType;
        if (attributeMap.containsKey(ScriptingConstants.KEY_AREA_TYPE)) {
            areaType = element.getAttributeValue(ScriptingConstants.KEY_AREA_TYPE);
        } else {
            areaType = ScriptingConstants.NT_JNT_CONTENT_LIST;
        }

        final String moduleType;
        if (attributeMap.containsKey(ScriptingConstants.KEY_MODULE_TYPE)) {
            moduleType = element.getAttributeValue(ScriptingConstants.KEY_MODULE_TYPE);
        } else {
            moduleType = ScriptingConstants.MODULE_TYPE_AREA;
        }

        final Integer listLimit;
        if (attributeMap.containsKey(ScriptingConstants.KEY_LIST_LIMIT)) {
            listLimit = Integer.parseInt(element.getAttributeValue(ScriptingConstants.KEY_LIST_LIMIT));
        } else {
            listLimit = -1;
        }

        final Integer level;
        if (attributeMap.containsKey(ScriptingConstants.KEY_LEVEL)) {
            level = Integer.parseInt(element.getAttributeValue(ScriptingConstants.KEY_LEVEL));
        } else {
            level = null;
        }

        final boolean areaAsSubNode;
        if (attributeMap.containsKey(ScriptingConstants.KEY_AREA_AS_SUB_NODE)) {
            areaAsSubNode = Boolean.parseBoolean(element.getAttributeValue(ScriptingConstants.KEY_AREA_AS_SUB_NODE));
        } else {
            areaAsSubNode = false;
        }

        final boolean editable;
        if (attributeMap.containsKey(ScriptingConstants.KEY_EDITABLE)) {
            editable = Boolean.parseBoolean(element.getAttributeValue(ScriptingConstants.KEY_EDITABLE));
        } else {
            editable = true;
        }

        final boolean limitedAbsoluteAreaEdit;
        if (attributeMap.containsKey(ScriptingConstants.KEY_LIMITED_ABSOLUTE_AREA_EDIT)) {
            limitedAbsoluteAreaEdit = Boolean.parseBoolean(element.getAttributeValue(ScriptingConstants.KEY_LIMITED_ABSOLUTE_AREA_EDIT));
        } else {
            limitedAbsoluteAreaEdit = true;
        }


        final Configuration configuration = arguments.getConfiguration();
        final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);

        final JCRNodeWrapper node;
        if (attributeMap.containsKey(ScriptingConstants.ATTR_NODE)) {
            final String attributeValue = element.getAttributeValue(ScriptingConstants.ATTR_NODE);
            final IStandardExpression expression = parser.parseExpression(configuration, arguments, attributeValue);
            node = (JCRNodeWrapper) expression.execute(configuration, arguments);
        } else {
            node = null;
        }

        final JCRSiteNode contextSite;
        if (attributeMap.containsKey(ScriptingConstants.ATTR_CONTEXT_SITE)) {
            final String attributeValue = element.getAttributeValue(ScriptingConstants.ATTR_CONTEXT_SITE);
            final IStandardExpression expression = parser.parseExpression(configuration, arguments, attributeValue);
            contextSite = (JCRSiteNode) expression.execute(configuration, arguments);
        } else {
            contextSite = null;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("path is {}", path);
            LOGGER.debug("areaType is {}", areaType);
            LOGGER.debug("view is {}", view);
            LOGGER.debug("templateType is {}", templateType);
            LOGGER.debug("nodeTypes is {}", nodeTypes);
            LOGGER.debug("moduleType is {}", moduleType);
            LOGGER.debug("mockupStyle is {}", mockupStyle);
            LOGGER.debug("listLimit is {}", listLimit);
            LOGGER.debug("level is {}", level);
            LOGGER.debug("areaAsSubNode is {}", areaAsSubNode);
            LOGGER.debug("limitedAbsoluteAreaEdit is {}", limitedAbsoluteAreaEdit);
        }
//
        return new ModuleService(context.getRenderContext(),
                                context.getCurrentResource(),
                                node,
                                contextSite,
                                nodeName,
                                path,
                                view,
                                templateType,
                                nodeTypes,
                                editable).doProcess();
    }
}
