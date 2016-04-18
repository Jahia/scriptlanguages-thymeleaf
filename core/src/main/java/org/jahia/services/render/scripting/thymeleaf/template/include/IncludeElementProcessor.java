package org.jahia.services.render.scripting.thymeleaf.template.include;

import org.jahia.modules.render.scripting.services.template.include.IncludeService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.scripting.thymeleaf.DXDialectConstants;
import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
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
 * Created by smomin on 2/9/16.
 */
public class IncludeElementProcessor extends AbstractUnescapedTextChildModifierElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(IncludeElementProcessor.class);
    private static final String INCLUDE = "include";

    public IncludeElementProcessor() {
        super(INCLUDE);
    }

    @Override
    protected String getText(final Arguments arguments, final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final String view = element.getAttributeValue(DXDialectConstants.DX_ATTR_VIEW);
        final String templateType = element.getAttributeValue(DXDialectConstants.DX_ATTR_TEMPLATE_TYPE);

        final Map<String, Attribute> attributeMap = element.getAttributeMap();
        final Configuration configuration = arguments.getConfiguration();
        final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
        final JCRNodeWrapper node = ProcessorUtil.getJcrNodeWrapper(arguments, element, attributeMap, configuration, parser);
        final Map<String, String> parameters = ProcessorUtil.getParameter(arguments, element, DXDialectConstants.DX_ATTR_PARAMS);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("view is {}", view);
            LOGGER.debug("templateType is {}", templateType);
        }

        return new IncludeService(context.getRenderContext(),
                context.getCurrentResource(),
                node,
                view,
                templateType,
                parameters).doProcess();
    }

    @Override
    public int getPrecedence() {
        return 5000;
    }
}
