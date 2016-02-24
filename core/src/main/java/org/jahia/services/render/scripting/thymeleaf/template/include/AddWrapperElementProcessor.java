package org.jahia.services.render.scripting.thymeleaf.template.include;

import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
import org.jahia.services.render.scripting.thymeleaf.core.template.include.AddWrapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.element.AbstractNoOpElementProcessor;

/**
 * Created by smomin on 2/9/16.
 */
public class AddWrapperElementProcessor extends AbstractNoOpElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddWrapperElementProcessor.class);
    private static final String ADD_WRAPPER = "add-wrapper";

    public AddWrapperElementProcessor() {
        super(ADD_WRAPPER);
    }

    @Override
    protected boolean removeHostElement(final Arguments arguments, final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final String name = element.getAttributeValue(ScriptingConstants.DX_ATTR_NAME);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("name is {}", name);
        }

        new AddWrapperService(context.getCurrentResource(), name).doProcess();
        return true;
    }

    @Override
    public int getPrecedence() {
        return 5000;
    }
}
