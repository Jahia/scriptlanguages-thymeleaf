package org.jahia.services.render.scripting.thymeleaf.support.template;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.element.AbstractTextChildModifierElementProcessor;

/**
 * Created by smomin on 2/9/16.
 */
public class IncludeElementProcessor extends AbstractTextChildModifierElementProcessor {
    public IncludeElementProcessor() {
        super("include");
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