package org.jahia.services.render.scripting.thymeleaf.template.pager;

import org.jahia.modules.render.scripting.services.template.pager.InitPagerService;
import org.jahia.services.render.scripting.thymeleaf.DXDialectConstants;
import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
import org.jahia.services.render.scripting.thymeleaf.util.ProcessorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Attribute;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.element.AbstractNoOpElementProcessor;

import java.util.Map;

/**
 * Created by smomin on 2/24/16.
 */
public class InitPagerElementProcessor extends AbstractNoOpElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitPagerElementProcessor.class);

    private static final String INIT_PAGER = "init-pager";

    /**
     *
     */
    protected InitPagerElementProcessor() {
        super(INIT_PAGER);
    }

    /**
     *
     * @param arguments
     * @param element
     * @return
     */
    @Override
    protected boolean removeHostElement(final Arguments arguments,
                                        final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final String id = element.getAttributeValue(DXDialectConstants.DX_ATTR_ID);

        final Map<String, Attribute> attributeMap = element.getAttributeMap();
        final Integer pageSize = ProcessorUtil.getIntegerValue(element, attributeMap,
                DXDialectConstants.DX_ATTR_PAGE_SIZE, null);
        final Long totalSize = ProcessorUtil.getLongValue(element, attributeMap,
                DXDialectConstants.DX_ATTR_TOTAL_SIZE, null);
        final Boolean sizeNotExact = ProcessorUtil.getBooleanValue(element, attributeMap,
                DXDialectConstants.DX_ATTR_SIZE_NOT_EXACT, false);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("id is {}", id);
            LOGGER.debug("pageSize is {}", pageSize);
            LOGGER.debug("totalSize is {}", totalSize);
            LOGGER.debug("sizeNotExact is {}", sizeNotExact);
        }

        new InitPagerService(context.getRenderContext(), id, pageSize, totalSize, sizeNotExact);
        return true;
    }

    @Override
    public int getPrecedence() {
        return 5000;
    }
}
