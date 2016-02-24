package org.jahia.services.render.scripting.thymeleaf.template.include;

import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
import org.jahia.services.render.scripting.thymeleaf.core.template.include.AddResourceService;
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
 * Created by smomin on 2/9/16.
 */
public class AddResourcesElementProcessor extends AbstractUnescapedTextChildModifierElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddResourcesElementProcessor.class);
    private static final String ADD_RESOURCES = "add-resources";

    public AddResourcesElementProcessor() {
        super(ADD_RESOURCES);
    }

    @Override
    protected String getText(final Arguments arguments, final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final String type = element.getAttributeValue(ScriptingConstants.DX_ATTR_TYPE);
        final String resources = element.getAttributeValue(ScriptingConstants.DX_ATTR_RESOURCES);
        final String title = element.getAttributeValue(ScriptingConstants.DX_ATTR_TITLE);
        final String key = element.getAttributeValue(ScriptingConstants.DX_ATTR_KEY);
        final String targetTag = element.getAttributeValue(ScriptingConstants.DX_ATTR_TARGET_TAG);
        final String rel = element.getAttributeValue(ScriptingConstants.DX_ATTR_REL);
        final String media = element.getAttributeValue(ScriptingConstants.DX_ATTR_MEDIA);
        final String condition = element.getAttributeValue(ScriptingConstants.DX_ATTR_CONDITION);
        final StringBuilder bodyContent = new StringBuilder();
        if (element.hasChildren()) {
            for (final Node node : element.getChildren()) {
                bodyContent.append(DOMUtils.getHtml5For(node));
            }
        }

        final Map<String, Attribute> attributeMap = element.getAttributeMap();
        final boolean insert = ProcessorUtil.getBooleanValue(element, attributeMap, ScriptingConstants.DX_ATTR_INSERT, false);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("insert is {}", insert);
            LOGGER.debug("type is {}", type);
            LOGGER.debug("resources is {}", resources);
            LOGGER.debug("title is {}", title);
            LOGGER.debug("key is {}", key);
            LOGGER.debug("targetTag is {}", targetTag);
            LOGGER.debug("rel is {}", rel);
            LOGGER.debug("media is {}", media);
            LOGGER.debug("condition is {}", condition);
            LOGGER.debug("bodyContent is {}", bodyContent);
        }

        return new AddResourceService(context.getRenderContext(),
                insert,
                type,
                resources,
                title,
                key,
                targetTag,
                rel,
                media,
                condition,
                bodyContent.toString(),
                false).doProcess();
    }

    @Override
    public int getPrecedence() {
        return 5000;
    }
}
