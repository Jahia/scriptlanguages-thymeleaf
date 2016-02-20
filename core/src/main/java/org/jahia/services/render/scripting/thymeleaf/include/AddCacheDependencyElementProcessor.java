package org.jahia.services.render.scripting.thymeleaf.include;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
import org.jahia.services.render.scripting.thymeleaf.core.template.include.AddCacheDependencyService;
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
public class AddCacheDependencyElementProcessor extends AbstractUnescapedTextChildModifierElementProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddCacheDependencyElementProcessor.class);

    public AddCacheDependencyElementProcessor() {
        super("add-cache-dependency");
    }

    @Override
    protected String getText(final Arguments arguments, final Element element) {
        final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
        final String uuid = element.getAttributeValue(ScriptingConstants
                .DX_ATTR_UUID);
        final String stringDependency = element.getAttributeValue(ScriptingConstants
                .DX_ATTR_STRING_DEPENDENCY);
        final String flushOnPathMatchingRegexp = element.getAttributeValue(ScriptingConstants
                .DX_ATTR_FLUSH_ON_PATH_MATCHING_REGEXP);

        final Configuration configuration = arguments.getConfiguration();
        final Map<String, Attribute> attributeMap = element.getAttributeMap();
        final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
        final JCRNodeWrapper node = ProcessorUtil.getJcrNodeWrapper(arguments, element, attributeMap, configuration, parser);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("uuid is {}", uuid);
            LOGGER.debug("stringDependency is {}", stringDependency);
            LOGGER.debug("flushOnPathMatchingRegexp is {}", flushOnPathMatchingRegexp);
        }

        if (node != null) {
            return new AddCacheDependencyService(context.getCurrentResource(),
                    context.getOptionalResource(),
                    node).doProcess();
        } else if (StringUtils.isNotEmpty(uuid)
                || StringUtils.isNotEmpty(stringDependency)
                || StringUtils.isNotEmpty(flushOnPathMatchingRegexp)) {
            return new AddCacheDependencyService(context.getCurrentResource(),
                    context.getOptionalResource(),
                    flushOnPathMatchingRegexp,
                    stringDependency,
                    uuid).doProcess();
        } else {
            return new AddCacheDependencyService(context.getCurrentResource(),
                    context.getOptionalResource()).doProcess();
        }
    }

    @Override
    public int getPrecedence() {
        return 5000;
    }
}
