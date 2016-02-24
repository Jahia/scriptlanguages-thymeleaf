package org.jahia.services.render.scripting.thymeleaf.template.include;

import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
import org.jahia.services.render.scripting.thymeleaf.core.template.include.AreaService;
import org.jahia.services.render.scripting.thymeleaf.util.ProcessorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Attribute;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.element.AbstractUnescapedTextChildModifierElementProcessor;

import java.util.Map;

/**
 * Created by smomin on 2/9/16.
 */
public class AreaElementProcessor extends AbstractUnescapedTextChildModifierElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AreaElementProcessor.class);
    private static final String AREA = "area";

    public AreaElementProcessor() {
        super(AREA);
    }

    @Override
    public int getPrecedence() {
        return 5000;
    }

    @Override
    protected String getText(final Arguments arguments, final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final String path = element.getAttributeValue(ScriptingConstants.DX_ATTR_PATH);
        final String view = element.getAttributeValue(ScriptingConstants.DX_ATTR_VIEW);
        final String templateType = element.getAttributeValue(ScriptingConstants.DX_ATTR_TEMPLATE_TYPE);
        final String nodeTypes = element.getAttributeValue(ScriptingConstants.DX_ATTR_NODE_TYPES);
        final String mockupStyle = element.getAttributeValue(ScriptingConstants.DX_ATTR_MOCKUP_STYLE);

        final Map<String, Attribute> attributeMap = element.getAttributeMap();
        final String areaType = ProcessorUtil.getStringValue(element, attributeMap, ScriptingConstants.DX_ATTR_AREA_TYPE, ScriptingConstants.NT_JNT_CONTENT_LIST);
        final String moduleType = ProcessorUtil.getStringValue(element, attributeMap, ScriptingConstants.DX_ATTR_MODULE_TYPE, ScriptingConstants.MODULE_TYPE_AREA);
        final Integer listLimit = ProcessorUtil.getIntegerValue(element, attributeMap, ScriptingConstants.DX_ATTR_LEVEL, -1);
        final Integer level = ProcessorUtil.getIntegerValue(element, attributeMap, ScriptingConstants.DX_ATTR_LEVEL, null);
        final boolean areaAsSubNode = ProcessorUtil.getBooleanValue(element, attributeMap, ScriptingConstants.DX_ATTR_AREA_AS_SUB_NODE, false);
        final boolean editable = ProcessorUtil.getBooleanValue(element, attributeMap, ScriptingConstants.DX_ATTR_EDITABLE, true);
        final boolean limitedAbsoluteAreaEdit = ProcessorUtil.getBooleanValue(element, attributeMap, ScriptingConstants.DX_ATTR_LIMITED_ABSOLUTE_AREA_EDIT, true);

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
