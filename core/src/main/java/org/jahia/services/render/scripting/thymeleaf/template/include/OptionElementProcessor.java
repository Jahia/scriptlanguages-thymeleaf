package org.jahia.services.render.scripting.thymeleaf.template.include;

import org.jahia.modules.render.scripting.services.template.include.OptionService;
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
public class OptionElementProcessor extends AbstractUnescapedTextChildModifierElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OptionElementProcessor.class);
    private static final String OPTION = "option";

    public OptionElementProcessor() {
        super(OPTION);
    }

    @Override
    protected String getText(final Arguments arguments, final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final String nodeType = element.getAttributeValue(DXDialectConstants.DX_ATTR_NODE_TYPE);
        final String view = element.getAttributeValue(DXDialectConstants.DX_ATTR_VIEW);

        final Map<String, Attribute> attributeMap = element.getAttributeMap();
        final Configuration configuration = arguments.getConfiguration();
        final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
        final JCRNodeWrapper node = ProcessorUtil.getJcrNodeWrapper(arguments, element, attributeMap, configuration, parser);
        final Map<String, String> parameters = ProcessorUtil.getParameter(arguments, element, DXDialectConstants.DX_ATTR_PARAMS);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("nodeType is {}", nodeType);
            LOGGER.debug("view is {}", view);
        }

        return new OptionService(context.getRenderContext(),
                context.getCurrentResource(),
                node,
                nodeType,
                view,
                parameters).doProcess();
    }

    @Override
    public int getPrecedence() {
        return 0;
    }
}
