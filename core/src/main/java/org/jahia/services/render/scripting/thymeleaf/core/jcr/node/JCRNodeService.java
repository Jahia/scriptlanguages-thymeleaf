package org.jahia.services.render.scripting.thymeleaf.core.jcr.node;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.jcr.AbstractJCRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

/**
 * Created by smomin on 2/22/16.
 */
public class JCRNodeService extends AbstractJCRService {
    private final static Logger LOGGER = LoggerFactory.getLogger(JCRNodeService.class);

    private String path;
    private String uuid;

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param languageCode
     * @param path
     * @param uuid
     */
    public JCRNodeService(final RenderContext renderContext,
                          final Resource currentResource,
                          final String languageCode,
                          final String path,
                          final String uuid) {
        super(renderContext, currentResource, languageCode);
        this.path = path;
        this.uuid = uuid;
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
    public JCRNodeWrapper doProcess() {
        JCRNodeWrapper node = null;
        try {
            if (uuid != null) {
                node = getJCRSession().getNodeByUUID(uuid);
            } else {
                if (path.startsWith("/")) {
                    node = getJCRSession().getNode(path);
                } else {
                    node = currentResource.getNode();
                    if (!StringUtils.isEmpty(path)) {
                        node = node.getNode(path);
                    }
                }
            }
        } catch (PathNotFoundException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Item not found '" + path + "'", e);
            }
        } catch (ItemNotFoundException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Item not found '" + path + "'", e);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Could not retrieve JCR node using path '" + path + "'", e);
        }
        return node;
    }
}
