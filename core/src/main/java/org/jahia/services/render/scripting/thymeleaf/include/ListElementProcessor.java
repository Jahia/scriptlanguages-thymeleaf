package org.jahia.services.render.scripting.thymeleaf.include;

import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
import org.jahia.services.render.scripting.thymeleaf.core.template.include.ListService;
import org.jahia.services.render.scripting.thymeleaf.util.ProcessorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Attribute;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.element.AbstractUnescapedTextChildModifierElementProcessor;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

import java.util.Map;

/**
 * Created by smomin on 2/19/16.
 */
public class ListElementProcessor extends AbstractUnescapedTextChildModifierElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListElementProcessor.class);

    public ListElementProcessor() {
        super("list");
    }

    @Override
    protected String getText(final Arguments arguments, final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final String path = element.getAttributeValue(ScriptingConstants.DX_ATTR_PATH);
        final String view = element.getAttributeValue(ScriptingConstants.DX_ATTR_VIEW);
        final String templateType = element.getAttributeValue(ScriptingConstants.DX_ATTR_TEMPLATE_TYPE);
        final String nodeTypes = element.getAttributeValue(ScriptingConstants.DX_ATTR_NODE_TYPES);

        final Map<String, Attribute> attributeMap = element.getAttributeMap();
        final String listType = ProcessorUtil.getStringValue(element, attributeMap, ScriptingConstants.DX_ATTR_LIST_TYPE, ScriptingConstants.NT_JNT_CONTENT_LIST);
        final boolean editable = ProcessorUtil.getBooleanValue(element, attributeMap, ScriptingConstants.DX_ATTR_EDITABLE, true);

        final Configuration configuration = arguments.getConfiguration();
        final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
        final Map<String, String> parameters = ProcessorUtil.getParameter(arguments, element, ScriptingConstants.DX_ATTR_PARAMS);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("path is {}", path);
            LOGGER.debug("view is {}", view);
            LOGGER.debug("templateType is {}", templateType);
            LOGGER.debug("nodeTypes is {}", nodeTypes);
        }

        return new ListService(context.getRenderContext(),
                context.getCurrentResource(),
                path,
                view,
                templateType,
                nodeTypes,
                listType,
                editable,
                parameters).doProcess();
    }

    @Override
    public int getPrecedence() {
        return 5000;
    }
}
