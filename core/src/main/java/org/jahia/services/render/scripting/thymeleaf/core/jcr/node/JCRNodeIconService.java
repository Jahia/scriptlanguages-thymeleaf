package org.jahia.services.render.scripting.thymeleaf.core.jcr.node;

import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.jcr.AbstractJCRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * Created by smomin on 2/22/16.
 */
public class JCRNodeIconService extends AbstractJCRService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JCRNodeIconService.class);

    private JCRNodeWrapper node;
    private Object type;

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param languageCode
     * @param node
     * @param type
     */
    public JCRNodeIconService(final RenderContext renderContext,
                              final Resource currentResource,
                              final String languageCode,
                              final JCRNodeWrapper node,
                              final Object type) {
        super(renderContext, currentResource, languageCode);
        this.node = node;
        this.type = type;
    }

    @Override
    public String doProcess() {
        try {
            if (node != null) {
                return JCRContentUtils.getIcon(node);
            } else if (type != null) {
                return JCRContentUtils.getIcon(type instanceof ExtendedNodeType ? (ExtendedNodeType) type
                        : NodeTypeRegistry.getInstance().getNodeType(String.valueOf(type)));
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "";
    }
}
