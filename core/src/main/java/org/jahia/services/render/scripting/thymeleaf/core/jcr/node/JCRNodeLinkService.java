package org.jahia.services.render.scripting.thymeleaf.core.jcr.node;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.jcr.AbstractJCRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * Created by smomin on 2/22/16.
 */
public class JCRNodeLinkService extends AbstractJCRService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JCRNodeLinkService.class);

    private JCRNodeWrapper node;
    private String path;
    private boolean absolute;

    private StringBuilder builder;

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param languageCode
     * @param path
     * @param absolute
     */
    public JCRNodeLinkService(final RenderContext renderContext,
                              final Resource currentResource,
                              final String languageCode,
                              final String path,
                              final boolean absolute) {
        super(renderContext, currentResource, languageCode);
        this.path = path;
        this.absolute = absolute;
        this.builder = new StringBuilder();
    }

    /**
     *
     */
    @Override
    public void init() {
        try {
            node = getJCRSession().getNode(path);
            if (node.isFile()) {
                builder.append("<a href=\"");
                if (absolute) {
                    builder.append(node.getAbsoluteUrl(renderContext.getRequest()));
                } else {
                    builder.append(node.getUrl());
                }
                builder.append("\">");
            } else {
                LOGGER.warn("The path '" + path + "' is not a file");
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String doProcess() {
        init();
        if (node != null && node.isFile()) {
            builder.append("</a>");
        }
        return builder.toString();
    }
}
