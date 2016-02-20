package org.jahia.services.render.scripting.thymeleaf.include;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.element.AbstractUnescapedTextChildModifierElementProcessor;

/**
 * Created by smomin on 2/9/16.
 */
public class OptionElementProcessor extends AbstractUnescapedTextChildModifierElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OptionElementProcessor.class);

    public OptionElementProcessor() {
        super("option");
    }

    @Override
    protected String getText(final Arguments arguments, final Element element) {
        return null;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }
}
