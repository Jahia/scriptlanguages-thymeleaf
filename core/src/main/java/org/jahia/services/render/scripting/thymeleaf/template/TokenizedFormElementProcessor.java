package org.jahia.services.render.scripting.thymeleaf.template;

import org.jahia.modules.render.scripting.services.template.TokenizedFormService;
import org.jahia.services.render.scripting.thymeleaf.DXDialectConstants;
import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
import org.jahia.services.render.scripting.thymeleaf.util.ProcessorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Attribute;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.element.AbstractUnescapedTextChildModifierElementProcessor;
import org.thymeleaf.util.DOMUtils;

import java.util.Map;

/**
 * Created by smomin on 2/24/16.
 */
public class TokenizedFormElementProcessor extends AbstractUnescapedTextChildModifierElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaptchaElementProcessor.class);
    private static final String TOKENIZED_FORM = "tokenized-form";

    protected TokenizedFormElementProcessor() {
        super(TOKENIZED_FORM);
    }

    @Override
    protected String getText(final Arguments arguments, final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final Map<String, Attribute> attributeMap = element.getAttributeMap();

        final boolean disableXSSFiltering = ProcessorUtil.getBooleanValue(element, attributeMap,
                DXDialectConstants.DX_ATTR_DISABLE_XSS_FILTERING, false);
        final boolean allowsMultipleSubmits = ProcessorUtil.getBooleanValue(element, attributeMap,
                DXDialectConstants.DX_ATTR_ALLOW_MULTIPLE_SUBMITS, false);
        final StringBuilder bodyContent = new StringBuilder();
        if (element.hasChildren()) {
            for (final Node node : element.getChildren()) {
                bodyContent.append(DOMUtils.getHtml5For(node));
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("disableXSSFiltering is {}", disableXSSFiltering);
            LOGGER.debug("allowsMultipleSubmits is {}", allowsMultipleSubmits);
            LOGGER.debug("bodyContent is {}", bodyContent);
        }

        return new TokenizedFormService(context.getRenderContext(), bodyContent.toString(),
                disableXSSFiltering, allowsMultipleSubmits).doProcess();
    }

    @Override
    public int getPrecedence() {
        return 5000;
    }
}
