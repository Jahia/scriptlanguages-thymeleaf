package org.jahia.services.render.scripting.thymeleaf.core.template.include;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by smomin on 2/17/16.
 */
public class IncludeService extends ModuleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IncludeService.class);

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param node
     * @param view
     * @param templateType
     */
    public IncludeService(final RenderContext renderContext,
                          final Resource currentResource,
                          final JCRNodeWrapper node,
                          final String view,
                          final String templateType,
                          final Map<String, String> parameters) {
        super(renderContext, currentResource, node, view, templateType, false, false, parameters);
    }
}
