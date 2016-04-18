package org.jahia.services.render.scripting.thymeleaf.template;

import org.jahia.modules.render.scripting.services.template.CaptchaService;
import org.jahia.services.render.scripting.thymeleaf.DXDialectConstants;
import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
import org.jahia.services.render.scripting.thymeleaf.util.ProcessorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Attribute;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.element.AbstractUnescapedTextChildModifierElementProcessor;

import java.util.Map;

/**
 * Created by smomin on 2/24/16.
 */
public class CaptchaElementProcessor extends AbstractUnescapedTextChildModifierElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaptchaElementProcessor.class);
    private static final String CAPTCHA = "captcha";

    public CaptchaElementProcessor() {
        super(CAPTCHA);
    }

    @Override
    protected String getText(final Arguments arguments, final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final Map<String, Attribute> attributeMap = element.getAttributeMap();

        final boolean display = ProcessorUtil.getBooleanValue(element, attributeMap,
                DXDialectConstants.DX_ATTR_DISPLAY, true);
        final boolean displayReloadLink = ProcessorUtil.getBooleanValue(element, attributeMap,
                DXDialectConstants.DX_ATTR_DISPLAY_RELOAD_LINK, true);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("display is {}", display);
            LOGGER.debug("displayReloadLink is {}", displayReloadLink);
        }

        return new CaptchaService(context.getRenderContext(), display, displayReloadLink).doProcess();
    }

    @Override
    public int getPrecedence() {
        return 5000;
    }
}
