package org.jahia.services.render.scripting.thymeleaf.core.jcr.node;

import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.jcr.AbstractJCRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.nodetype.NoSuchNodeTypeException;

/**
 * Created by smomin on 2/22/16.
 */
public class JCRNodeTypeService extends AbstractJCRService {
    private final static Logger LOGGER = LoggerFactory.getLogger(JCRNodeTypeService.class);
    private String name;

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param languageCode
     * @param name
     */
    public JCRNodeTypeService(final RenderContext renderContext,
                              final Resource currentResource,
                              final String languageCode,
                              final String name) {
        super(renderContext, currentResource, languageCode);
        this.name = name;
    }

    /**
     *
     */
    @Override
    public void init() {

    }

    /**
     *
     * @return
     */
    @Override
    public ExtendedNodeType doProcess() {
        try {
            return NodeTypeRegistry.getInstance().getNodeType(name);
        } catch (NoSuchNodeTypeException e) {
            LOGGER.warn(name + " is not a valid node type");
        }
        return null;
    }
}
