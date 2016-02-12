package org.jahia.services.render.scripting.thymeleaf.support.template;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.standard.processor.attr.AbstractStandardSingleAttributeModifierAttrProcessor;

/**
 * Created by smomin on 2/11/16.
 */
public class NodeAttrProcessor extends AbstractStandardSingleAttributeModifierAttrProcessor {

    public static final String ATTR_NAME = "node";

    public NodeAttrProcessor() {
        // Only execute this processor for 'sayto' attributes.
        super(ATTR_NAME);
    }

    @Override
    public int getPrecedence() {
        return 1000;
    }


    @Override
    protected String getTargetAttributeName(final Arguments arguments,
                                            final Element element,
                                            final String attributeName) {
        return ATTR_NAME;
    }

    @Override
    protected ModificationType getModificationType(final Arguments arguments,
                                                   final Element element,
                                                   final String attributeName,
                                                   final String newAttributeName) {
        return ModificationType.SUBSTITUTION;
    }

    @Override
    protected boolean removeAttributeIfEmpty(final Arguments arguments,
                                             final Element element,
                                             final String attributeName,
                                             final String newAttributeName) {
        return false;
    }
}
