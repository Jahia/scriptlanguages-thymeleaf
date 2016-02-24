package org.jahia.services.render.scripting.thymeleaf.template.pager;

import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
import org.jahia.services.render.scripting.thymeleaf.core.template.pager.RemovePagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.element.AbstractNoOpElementProcessor;

/**
 * Created by smomin on 2/24/16.
 */
public class RemovePagerElementProcessor extends AbstractNoOpElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitPagerElementProcessor.class);
    private static final String REMOVE_PAGER = "remove-pager";

    public RemovePagerElementProcessor() {
        super(REMOVE_PAGER);
    }

    @Override
    protected boolean removeHostElement(final Arguments arguments, final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final String id = element.getAttributeValue(ScriptingConstants.DX_ATTR_ID);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("id is {}", id);
        }

        new RemovePagerService(context.getRenderContext(), id).doProcess();
        return true;
    }

    @Override
    public int getPrecedence() {
        return 5000;
    }
}
