package org.jahia.services.render.scripting.thymeleaf.support.template;

import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
import org.jahia.services.render.scripting.thymeleaf.support.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.support.core.AreaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Attribute;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.element.AbstractElementProcessor;
import org.thymeleaf.processor.element.AbstractUnescapedTextChildModifierElementProcessor;

import java.util.Map;

/**
 * Created by smomin on 2/9/16.
 */
public class AreaElementProcessor extends AbstractUnescapedTextChildModifierElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractElementProcessor.class);

    public AreaElementProcessor() {
        super(ScriptingConstants.MODULE_TYPE_AREA);
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    protected String getText(final Arguments arguments, final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final String path = element.getAttributeValue(ScriptingConstants.ATTR_PATH);
        final String view = element.getAttributeValue(ScriptingConstants.ATTR_VIEW);
        final String templateType = element.getAttributeValue(ScriptingConstants.ATTR_TEMPLATE_TYPE);
        final String nodeTypes = element.getAttributeValue(ScriptingConstants.ATTR_NODE_TYPES);
        final String mockupStyle = element.getAttributeValue(ScriptingConstants.ATTR_MOCKUP_STYLE);

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

        return new AreaService(context.getRenderContext(),
                context.getCurrentResource(),
                path,
                areaType,
                view,
                templateType,
                nodeTypes,
                moduleType,
                mockupStyle,
                listLimit,
                level,
                areaAsSubNode,
                limitedAbsoluteAreaEdit,
                editable).doProcess();
    }
}
