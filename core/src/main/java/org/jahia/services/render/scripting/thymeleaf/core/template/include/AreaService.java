package org.jahia.services.render.scripting.thymeleaf.core.template.include;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Template;
import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by smomin on 2/9/16.
 */
public class AreaService extends ModuleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AreaService.class);

    private Template templateNode;
    private String conflictsWith = null;

    private String areaType;
    private String moduleType = "area";
    private String mockupStyle;
    private Integer level;
    private boolean areaAsSubNode;
    private boolean limitedAbsoluteAreaEdit = true;

    /**
     * @param renderContext
     * @param path
     * @param areaType
     * @param view
     * @param templateType
     * @param nodeTypes
     * @param moduleType
     * @param mockupStyle
     * @param listLimit
     * @param level
     * @param areaAsSubNode
     * @param limitedAbsoluteAreaEdit
     */
    public AreaService(final RenderContext renderContext,
                       final Resource currentResource,
                       final String path,
                       final String areaType,
                       final String view,
                       final String templateType,
                       final String nodeTypes,
                       final String moduleType,
                       final String mockupStyle,
                       final Integer listLimit,
                       final Integer level,
                       final boolean areaAsSubNode,
                       final boolean limitedAbsoluteAreaEdit,
                       final boolean editable) {
        super(renderContext,
                currentResource,
                path,
                view,
                templateType,
                nodeTypes,
                listLimit,
                editable,
                new HashMap<String, String>());
        this.areaType = areaType;
        this.moduleType = moduleType;
        this.mockupStyle = mockupStyle;
        this.level = level;
        this.areaAsSubNode = areaAsSubNode;
        this.limitedAbsoluteAreaEdit = limitedAbsoluteAreaEdit;
    }

    /**
     * @return
     * @throws RepositoryException
     */
    @Override
    protected String getModuleType() throws RepositoryException {
        return moduleType;
    }

    /**
     * @throws RepositoryException
     * @throws IOException
     */
    @Override
    protected void missingResource()
            throws RepositoryException, IOException {
        if (renderContext.isEditMode() && checkNodeEditable(node)) {
            try {
                constraints = ConstraintsHelper.getConstraints(Arrays.asList(NodeTypeRegistry
                        .getInstance().getNodeType(areaType)), null);
            } catch (RepositoryException e) {
                LOGGER.error("Error when getting list constraints", e);
            }

            JCRNodeWrapper parent = null;
            String areaPath = path;
            if (!path.startsWith("/")) {
                if (areaAsSubNode && currentResource.getNode().getPath().startsWith(renderContext
                        .getMainResource().getNode().getPath())) {
                    areaPath = currentResource.getNode().getPath() + "/" + path;
                    if (path.indexOf('/') == -1) {
                        parent = currentResource.getNode();
                    } else {
                        try {
                            parent = currentResource.getNode().getSession()
                                    .getNode(StringUtils.substringBeforeLast(areaPath, "/"));
                        } catch (PathNotFoundException e) {
                            // ignore
                        }
                    }
                } else {
                    areaPath = renderContext.getMainResource().getNode().getPath() + "/" + path;
                    if (path.indexOf('/') == -1) {
                        parent = renderContext.getMainResource().getNode();
                    }
                }
            } else {
                try {
                    parent = renderContext.getMainResource().getNode().getSession()
                            .getNode(StringUtils.substringBeforeLast(areaPath, "/"));
                } catch (PathNotFoundException e) {
                    // ignore
                }
            }

            boolean isEditable = true;
            final StringBuilder additionalParameters = new StringBuilder();
            additionalParameters.append("missingList=\"true\"");
            if (conflictsWith != null) {
                additionalParameters.append(" conflictsWith=\"").append(conflictsWith).append("\"");
            }
            if (!areaType.equals(ScriptingConstants.NT_JNT_CONTENT_LIST)) {
                additionalParameters.append(" areaType=\"").append(areaType).append("\"");
            }

            if (renderContext.getEditModeConfigName().equals(ScriptingConstants.MODE_CONTRIBUTE_MODE)) {
                final JCRNodeWrapper contributeNode = (JCRNodeWrapper) request
                        .getAttribute(ScriptingConstants.ATTR_AREA_LIST_RESOURCE);
                if (contributeNode == null || !contributeNode.hasProperty(Constants.JAHIA_CONTRIBUTE_TYPES)) {
                    additionalParameters.append(" editable=\"false\"");
                    isEditable = false;
                }
            }
            if (!StringUtils.isEmpty(mockupStyle)) {
                additionalParameters.append(" mockupStyle=\"").append(mockupStyle).append("\"");
            }
            additionalParameters.append(" areaHolder=\"").append(currentResource.getNode().getIdentifier())
                    .append("\"");

            if (isEditable && JCRContentUtils.isLockedAndCannotBeEdited(parent)) {
                // if the parent is locked -> disable area editing
                additionalParameters.append(" editable=\"false\"");
            }

            printModuleStart(getModuleType(), areaPath, null, additionalParameters.toString());
            printModuleEnd();
        }
    }

    /**
     * @return
     */
    @Override
    protected String getConfiguration() {
        return Resource.CONFIGURATION_WRAPPEDCONTENT;
    }

    /**
     * @return
     */
    @Override
    protected boolean canEdit() {
        if (path != null) {
            return renderContext.isEditMode() && editable &&
                    request.getAttribute(ScriptingConstants.ATTR_IN_AREA) == null;
        } else if (node != null) {
            return renderContext.isEditMode() && editable &&
                    request.getAttribute(ScriptingConstants.ATTR_IN_AREA) == null && node.getPath().equals(renderContext.getMainResource().getNode().getPath());
        } else {
            return super.canEdit();
        }
    }

    /**
     * @throws IOException
     */
    @Override
    protected void setNode() throws IOException {
        Resource mainResource = renderContext.getMainResource();

        if (renderContext.isAjaxRequest() && renderContext.getAjaxResource() != null) {
            mainResource = renderContext.getAjaxResource();
        }

        request.removeAttribute(ScriptingConstants.ATTR_SKIP_WRAPPER);
        request.removeAttribute(ScriptingConstants.ATTR_IN_AREA);
        request.setAttribute(ScriptingConstants.ATTR_ORG_JAHIA_EMPTY_AREA, Boolean.TRUE);

        try {
            // path is null in main resource display
            Template t = (Template) request.getAttribute(ScriptingConstants.ATTR_PREVIOUS_TEMPLATE);
            templateNode = t;

            if (ScriptingConstants.MODULE_TYPE_ABSOLUTE_AREA.equals(moduleType)) {
                // No more areas in an absolute area
                request.setAttribute(ScriptingConstants.ATTR_PREVIOUS_TEMPLATE, null);
                JCRNodeWrapper main = null;
                try {
                    main = renderContext.getMainResource().getNode();
                    if (level != null && main.getDepth() >= level + 3) {
                        node = (JCRNodeWrapper) main.getAncestor(level + 3);
                    } else if (level == null) {
                        node = renderContext.getSite().getHome();
                    } else {
                        return;
                    }
                    if (node == null) {
                        return;
                    }
                    if ((limitedAbsoluteAreaEdit && !mainResource.getNode().getPath().equals(node.getPath()))
                            || (mainResource.getNode().getPath().startsWith("/modules")
                            && mainResource.getNode().isNodeType(ScriptingConstants.NT_JNT_TEMPLATE))) {
                        parameters.put("readOnly", "true");
                        editable = false;
                        request.setAttribute(ScriptingConstants.ATTR_IN_AREA, Boolean.TRUE);
                    }

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Looking for absolute area " + path + ", will be searched in node " + node.getPath() +
                                " saved template = " + (templateNode != null ? templateNode.serialize() : "none") + ", previousTemplate set to null");
                    }
                    node = node.getNode(path);
                    request.setAttribute(ScriptingConstants.ATTR_ORG_JAHIA_EMPTY_AREA, Boolean.FALSE);
                } catch (RepositoryException e) {
                    if (node != null) {
                        path = node.getPath() + "/" + path;
                    }
                    node = null;
                    if (editable) {
                        missingResource();
                    }
                } finally {
                    if (node == null && LOGGER.isDebugEnabled()) {
                        if (level == null) {
                            LOGGER.debug(
                                    "Cannot get a node {}, relative to the home page of site {}"
                                            + " for main resource {}",
                                    new String[]{
                                            path,
                                            main != null && main.getResolveSite() != null ? main.getResolveSite().getPath() : null,
                                            main != null ? main.getPath() : null});
                        } else {
                            LOGGER.debug(
                                    "Cannot get a node {}, with level {} for main resource {}",
                                    new String[]{path, String.valueOf(level), main != null ? main.getPath() : null});
                        }
                    }
                }
            } else if (path != null) {
                if (!path.startsWith("/")) {
                    final List<JCRNodeWrapper> nodes = new ArrayList<JCRNodeWrapper>();
                    if (t != null) {
                        for (final Template currentTemplate : t.getNextTemplates()) {
                            nodes.add(0, mainResource.getNode().getSession()
                                    .getNodeByIdentifier(currentTemplate.getNode()));
                        }
                    }
                    nodes.add(mainResource.getNode());
                    boolean isCurrentResource = false;
                    if (areaAsSubNode) {
                        nodes.add(0, currentResource.getNode());
                        isCurrentResource = true;
                    }
                    boolean found = false;
                    boolean notMainResource = false;

                    final Set<String> allPaths = renderContext.getRenderedPaths();
                    for (final JCRNodeWrapper node : nodes) {
                        if (!path.equals("*") && node.hasNode(path)
                                && !allPaths.contains(node.getPath() + "/" + path)) {
                            notMainResource = mainResource.getNode() != node
                                    && !node.getPath().startsWith(renderContext.getMainResource().getNode().getPath());
                            this.node = node.getNode(path);
                            if (currentResource.getNode().getParent().getPath().equals(this.node.getPath())) {
                                this.node = null;
                            } else {
                                // now let's check if the content node matches the areaType. If not it means we have a
                                // conflict with another content created outside of the content of the area (DEVMINEFI-223)
                                if (!this.node.isNodeType(areaType) && !this.node
                                        .isNodeType(ScriptingConstants.MIX_JMIX_SKIP_CONSTRAINT_CHECK)) {
//                                    conflictsWith = this.node.getPath();
                                    found = false;
                                    this.node = null;
                                    break;
                                } else {
                                    found = true;
                                    request.setAttribute(ScriptingConstants.ATTR_ORG_JAHIA_EMPTY_AREA, Boolean.FALSE);
                                    break;
                                }
                            }
                        }
                        if (t != null && !isCurrentResource) {
                            t = t.getNext();
                        }
                        isCurrentResource = false;
                    }

                    request.setAttribute(ScriptingConstants.ATTR_PREVIOUS_TEMPLATE, t);
                    if (LOGGER.isDebugEnabled()) {
                        String tempNS = (templateNode != null) ? templateNode.serialize() : null;
                        String prevNS = (t != null) ? t.serialize() : null;
                        LOGGER.debug("Looking for local area " + path + ", will be searched in node " + (node != null ? node.getPath() : null) +
                                " saved template = " + tempNS + ", previousTemplate set to " + prevNS);
                    }

                    boolean templateEdit = mainResource.getModuleParams().containsKey(ScriptingConstants.ATTR_TEMPLATE_EDIT)
                            && mainResource.getModuleParams().get(ScriptingConstants.ATTR_TEMPLATE_EDIT).equals(node.getParent().getIdentifier());
                    if (notMainResource && !templateEdit) {
                        request.setAttribute(ScriptingConstants.ATTR_IN_AREA, Boolean.TRUE);
                    }

                    if (!found) {
                        missingResource();
                    }
                } else if (path.startsWith("/")) {
                    JCRSessionWrapper session = mainResource.getNode().getSession();

                    // No more areas in an absolute area
                    request.setAttribute(ScriptingConstants.ATTR_PREVIOUS_TEMPLATE, null);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Looking for absolute area " + path + ", will be searched in node " + (node != null ? node.getPath() : null) +
                                " saved template = " + (templateNode != null ? templateNode.serialize() : "none") + ", previousTemplate set to null");
                    }

                    try {
                        node = (JCRNodeWrapper) session.getItem(path);
                        request.setAttribute(ScriptingConstants.ATTR_ORG_JAHIA_EMPTY_AREA, Boolean.FALSE);
                    } catch (PathNotFoundException e) {
                        missingResource();
                    }
                }
                request.setAttribute(ScriptingConstants.ATTR_SKIP_WRAPPER, Boolean.TRUE);
            } else {
                request.setAttribute(ScriptingConstants.ATTR_PREVIOUS_TEMPLATE, null);
                request.removeAttribute(ScriptingConstants.ATTR_SKIP_WRAPPER);
                node = mainResource.getNode();
                request.setAttribute(ScriptingConstants.ATTR_ORG_JAHIA_EMPTY_AREA, Boolean.FALSE);
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (node == null && LOGGER.isDebugEnabled()) {
            LOGGER.debug("Can not find the area node for path " + path + " with templates " + (templateNode != null ? templateNode.serialize() : "none") +
                    "rendercontext " + renderContext + " main resource " + mainResource +
                    " current resource " + currentResource);
        }
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    public String doProcess() {
        final Object o = request.getAttribute(ScriptingConstants.ATTR_IN_AREA);
        try {
            return super.doProcess();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            request.setAttribute(ScriptingConstants.ATTR_PREVIOUS_TEMPLATE, templateNode);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Restoring previous template "
                        + (templateNode != null ? templateNode.serialize() : "none"));
            }
            request.setAttribute(ScriptingConstants.ATTR_IN_AREA, o);
        }
        return "";
    }

    /**
     * @param resource
     * @throws IOException
     * @throws RenderException
     */
    @Override
    protected void render(final Resource resource) throws IOException, RenderException {
        if (canEdit() || !isEmptyArea() || path == null) {
            super.render(resource);
        }
    }

    /**
     * @return
     */
    protected boolean isEmptyArea() {
        for (final String s : constraints.split(" ")) {
            if (!JCRContentUtils.getChildrenOfType(node, s).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
