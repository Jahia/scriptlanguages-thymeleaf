package org.jahia.services.render.scripting.thymeleaf.core.template.include;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import java.io.IOException;
import java.util.Map;

/**
 * Created by smomin on 2/19/16.
 */
public class ListService extends ModuleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListService.class);
    private String listType;

    public ListService(final RenderContext renderContext,
                       final Resource currentResource,
                       final String path,
                       final String view,
                       final String templateType,
                       final String nodeTypes,
                       final String listType,
                       final boolean editable,
                       final Map<String, String> parameters) {
        super(renderContext, currentResource, path, view, templateType, nodeTypes, editable, parameters);
        this.listType = listType;
    }

    /**
     *
     * @return
     * @throws RepositoryException
     */
    @Override
    protected String getModuleType() throws RepositoryException {
        return "area";
    }

    /**
     *
     * @throws RepositoryException
     * @throws IOException
     */
    @Override
    protected void missingResource() throws RepositoryException, IOException {
        try {
            if (renderContext.isEditMode()) {
                final JCRSessionWrapper session = currentResource.getNode().getSession();
                if (!path.startsWith("/")) {
                    final JCRNodeWrapper nodeWrapper = currentResource.getNode();
                    if(!nodeWrapper.isCheckedOut())
                        nodeWrapper.checkout();
                    node = nodeWrapper.addNode(path, listType);
                    session.save();
                } else {
                    final JCRNodeWrapper parent = session.getNode(StringUtils.substringBeforeLast(path, "/"));
                    if(!parent.isCheckedOut())
                        parent.checkout();
                    node = parent.addNode(StringUtils.substringAfterLast(path, "/"), listType);
                    session.save();
                }
            }
        } catch (ConstraintViolationException e) {
            super.missingResource();
        } catch (RepositoryException e) {
            LOGGER.error("Cannot create area",e);
        }
    }
}
