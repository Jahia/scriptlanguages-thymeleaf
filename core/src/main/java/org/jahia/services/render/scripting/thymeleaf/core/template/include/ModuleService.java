package org.jahia.services.render.scripting.thymeleaf.core.template.include;

import com.google.common.collect.Ordering;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.core.ScriptingService;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Created by smomin on 2/9/16.
 */
public class ModuleService implements ScriptingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleService.class);
    private static final int DEFAULT_LIST_LIMIT = -1;
    private static final boolean DEFAULT_CHECK_CONSTRAINTS = true;

    private static volatile AbstractFilter exclusionFilter;
    private static volatile boolean exclusionFilterChecked;

    protected RenderContext renderContext;
    protected JCRNodeWrapper node;
    protected String path;
    protected String view;
    protected String templateType;
    protected String nodeTypes;
    protected boolean editable;
    protected Map<String, String> parameters;
    protected String constraints;
    protected Resource currentResource;
    protected HttpServletRequest request;
    protected HttpServletResponse response;

    private JCRSiteNode contextSite;
    private boolean checkConstraints;
    private int listLimit;
    private String nodeName;
    private StringBuilder builder;

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param node
     * @param contextSite
     * @param nodeName
     * @param path
     * @param view
     * @param templateType
     * @param nodeTypes
     * @param editable
     * @param parameters
     */
    public ModuleService(final RenderContext renderContext,
                         final Resource currentResource,
                         final JCRNodeWrapper node,
                         final JCRSiteNode contextSite,
                         final String nodeName,
                         final String path,
                         final String view,
                         final String templateType,
                         final String nodeTypes,
                         final boolean editable,
                         final Map<String, String> parameters) {
        this(renderContext, currentResource, node, contextSite, nodeName, path, view, templateType,
                nodeTypes, DEFAULT_LIST_LIMIT, editable, DEFAULT_CHECK_CONSTRAINTS, parameters);
    }

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param node
     * @param view
     * @param templateType
     * @param editable
     * @param checkConstraints
     * @param parameters
     */
    public ModuleService(final RenderContext renderContext,
                         final Resource currentResource,
                         final JCRNodeWrapper node,
                         final String view,
                         final String templateType,
                         final boolean editable,
                         final boolean checkConstraints,
                         final Map<String, String> parameters) {
        this(renderContext, currentResource, node, null, null, null, view, templateType,
                null, DEFAULT_LIST_LIMIT, editable, checkConstraints, parameters);
    }

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param path
     * @param view
     * @param templateType
     * @param nodeTypes
     * @param editable
     * @param parameters
     */
    public ModuleService(final RenderContext renderContext,
                         final Resource currentResource,
                         final String path,
                         final String view,
                         final String templateType,
                         final String nodeTypes,
                         final boolean editable,
                         final Map<String, String> parameters) {
        this(renderContext, currentResource, null, null, null, path, view, templateType,
                nodeTypes, DEFAULT_LIST_LIMIT, editable, DEFAULT_CHECK_CONSTRAINTS, parameters);
    }

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param path
     * @param view
     * @param templateType
     * @param nodeTypes
     * @param listLimit
     * @param editable
     * @param parameters
     */
    public ModuleService(final RenderContext renderContext,
                         final Resource currentResource,
                         final String path,
                         final String view,
                         final String templateType,
                         final String nodeTypes,
                         final Integer listLimit,
                         final boolean editable,
                         final Map<String, String> parameters) {
        this(renderContext, currentResource, null, null, null, path, view, templateType,
                nodeTypes, listLimit, editable, DEFAULT_CHECK_CONSTRAINTS, parameters);
    }

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param node
     * @param contextSite
     * @param nodeName
     * @param path
     * @param view
     * @param templateType
     * @param nodeTypes
     * @param listLimit
     * @param editable
     * @param checkConstraints
     * @param parameters
     */
    public ModuleService(final RenderContext renderContext,
                         final Resource currentResource,
                         final JCRNodeWrapper node,
                         final JCRSiteNode contextSite,
                         final String nodeName,
                         final String path,
                         final String view,
                         final String templateType,
                         final String nodeTypes,
                         final Integer listLimit,
                         final boolean editable,
                         final boolean checkConstraints,
                         final Map<String, String> parameters) {
        this.path = path;
        this.nodeTypes = nodeTypes;
        this.node = node;
        this.contextSite = contextSite;
        this.nodeName = nodeName;
        this.renderContext = renderContext;
        this.currentResource = currentResource;
        this.view = view;
        this.templateType = templateType;
        this.listLimit = listLimit;
        this.editable = editable;
        this.checkConstraints = checkConstraints;
        this.parameters = parameters;

        this.builder = new StringBuilder();
        request = renderContext.getRequest();
        response = renderContext.getResponse();
    }

    /**
     *
     */
    @Override
    public void init() {
        // Begin: The two lines below were in the doStartTag, not sure how to emulate this in Thymeleaf processor
        final Integer level = (Integer) request.getAttribute(ScriptingConstants.ATTR_ORG_JAHIA_MODULES_LEVEL);
        request.setAttribute(ScriptingConstants.ATTR_ORG_JAHIA_MODULES_LEVEL, level != null ? level + 1 : 2);
        // End
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    public String doProcess() {
        init();
        try {
            if (node == null) {
                setNode();
            }

            String resourceNodeType = null;
            if (parameters.containsKey(ScriptingConstants.ATTR_RESOURCE_NODE_TYPE)) {
                resourceNodeType = URLDecoder.decode(parameters.get(ScriptingConstants.ATTR_RESOURCE_NODE_TYPE),
                        ScriptingConstants.CONTENT_TYPE_UTF_8);
            }

            if (node != null) {
                final Integer currentLevel = (Integer) request.getAttribute(ScriptingConstants.ATTR_ORG_JAHIA_MODULES_LEVEL);
                boolean hasParentConstraint = false;
                try {
                    constraints = ConstraintsHelper.getConstraints(node);
                    hasParentConstraint = path == null && !StringUtils.isEmpty(ConstraintsHelper
                            .getConstraints(node.getParent(), node.getName()));

                } catch (RepositoryException e) {
                    LOGGER.error("Error when getting list constraints", e);
                }
                if (checkConstraints && !hasParentConstraint && (path == null || path.equals("*"))) {
                    String constrainedNodeTypes = null;
                    if (currentLevel != null) {
                        constrainedNodeTypes = (String) request.getAttribute(ScriptingConstants.ATTR_AREA_NODE_TYPES_RESTRICTION
                                + (currentLevel - 1));
                    }
                    try {
                        if (constrainedNodeTypes != null
                                && !"".equals(constrainedNodeTypes.trim())
                                && !node.isNodeType(ScriptingConstants.MIX_JMIX_SKIP_CONSTRAINT_CHECK)
                                && !node.getParent().isNodeType(ScriptingConstants.MIX_JMIX_SKIP_CONSTRAINT_CHECK)) {
                            final StringTokenizer st = new StringTokenizer(constrainedNodeTypes, " ");
                            boolean found = false;
                            Node displayedNode = node;
                            if (node.isNodeType(ScriptingConstants.NT_JNT_CONTENT_REFERENCE) && node.hasProperty(Constants.NODE)) {
                                try {
                                    displayedNode = node.getProperty(Constants.NODE).getNode();
                                } catch (ItemNotFoundException e) {
                                    return "";
                                }
                            }
                            while (st.hasMoreTokens()) {
                                final String tok = st.nextToken();
                                if (displayedNode.isNodeType(tok) || tok.equals(resourceNodeType)) {
                                    found = true;
                                    break;
                                }
                            }
                            // Remove test until we find a better solution to avoid displaying unnecessary nodes
                            if (!found) {
                                return "";
                            }
                        }
                    } catch (RepositoryException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                if (templateType == null) {
                    templateType = currentResource.getTemplateType();
                }

                final Resource resource = new Resource(node, templateType, view, getConfiguration());
                final String charset = response.getCharacterEncoding();
                for (final Map.Entry<String, String> param : parameters.entrySet()) {
                    resource.getModuleParams().put(URLDecoder.decode(param.getKey(), charset),
                            URLDecoder.decode(param.getValue(), charset));
                }

                if (resourceNodeType != null) {
                    try {
                        resource.setResourceNodeType(NodeTypeRegistry.getInstance().getNodeType(resourceNodeType));
                    } catch (NoSuchNodeTypeException e) {
                        throw new Exception(e);
                    }
                }

                final boolean isVisible = isVisible();
                try {
                    boolean canEdit = canEdit()
                            && contributeAccess(resource.getNode())
                            && !isExcluded(resource);

                    final boolean nodeEditable = checkNodeEditable(node);
                    request.setAttribute(ScriptingConstants.ATTR_EDITABLE_MODULE, canEdit && nodeEditable);
                    if (canEdit) {
                        final String type = getModuleType();
                        final List<String> contributeTypes = contributeTypes(resource.getNode());
                        final String oldNodeTypes = nodeTypes;
                        final String additionalParams = getAdditionalParams(nodeEditable, contributeTypes);

                        try {
                            final Script script = RenderService.getInstance().resolveScript(resource, renderContext);
                            printModuleStart(type, node.getPath(), script, additionalParams);
                        } catch (TemplateNotFoundException e) {
                            printModuleStart(type, node.getPath(), null, additionalParams);
                        }
                        nodeTypes = oldNodeTypes;
                        currentResource.getDependencies().add(node.getCanonicalPath());
                        if (isVisible) {
                            render(resource);
                        }
                        //Copy dependencies to parent Resource (only for include of the same node)
                        if (currentResource.getNode().getPath().equals(resource.getNode().getPath())) {
                            currentResource.getRegexpDependencies().addAll(resource.getRegexpDependencies());
                            currentResource.getDependencies().addAll(resource.getDependencies());
                        }
                        printModuleEnd();
                    } else {
                        currentResource.getDependencies().add(node.getCanonicalPath());
                        if (isVisible) {
                            render(resource);
                        } else {
                            builder.append("&nbsp;");
                        }
                        //Copy dependencies to parent Resource (only for include of the same node)
                        if (currentResource.getNode().getPath().equals(resource.getNode().getPath())) {
                            currentResource.getRegexpDependencies().addAll(resource.getRegexpDependencies());
                            currentResource.getDependencies().addAll(resource.getDependencies());
                        }
                    }
                } catch (RepositoryException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return builder.toString();
    }

    /**
     *
     * @return
     */
    private boolean isVisible() {
        try {
            return renderContext.getEditModeConfig() == null || renderContext.isVisible(node);
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return true;
    }

    /**
     *
     * @param nodeEditable
     * @param contributeTypes
     * @return
     * @throws RepositoryException
     */
    private String getAdditionalParams(final boolean nodeEditable,
                                       final List<String> contributeTypes) throws RepositoryException {
        String add = "";
        if (!nodeEditable) {
            add = "editable=\"false\"";
        }
        if (contributeTypes != null) {
            nodeTypes = StringUtils.join(contributeTypes, " ");
            add = "editable=\"false\"";
        }
        if (node.isNodeType(Constants.JAHIAMIX_BOUND_COMPONENT)) {
            add += " bindable=\"true\"";
        }
        return add;
    }

    /**
     * @param resource
     * @return
     * @throws RepositoryException
     */
    private boolean isExcluded(final Resource resource) throws RepositoryException {
        final AbstractFilter filter = getExclusionFilter();
        if (filter == null) {
            return false;
        }
        try {
            return filter.prepare(renderContext, resource, null) != null;
        } catch (Exception e) {
            LOGGER.error("Cannot evaluate exclude filter", e);
        }
        return false;
    }

    /**
     * @return
     */
    private static AbstractFilter getExclusionFilter() {
        if (!exclusionFilterChecked) {
            synchronized (ModuleService.class) {
                if (!exclusionFilterChecked) {
                    try {
                        exclusionFilter = (AbstractFilter) SpringContextSingleton
                                .getBeanInModulesContext(ScriptingConstants.FILTER_CHANNEL_EXCLUSION_FILTER);
                    } catch (Exception e) {
                    }
                    exclusionFilterChecked = true;
                }
            }
        }
        return exclusionFilter;
    }

    /**
     * @param node
     * @return
     */
    private List<String> contributeTypes(final JCRNodeWrapper node) {
        if (!ScriptingConstants.MODE_CONTRIBUTE_MODE.equals(renderContext.getEditModeConfigName())) {
            return null;
        }
        JCRNodeWrapper contributeNode = null;
        if (renderContext.getRequest().getAttribute(ScriptingConstants.ATTR_AREA_LIST_RESOURCE) != null) {
            contributeNode = (JCRNodeWrapper) renderContext.getRequest()
                    .getAttribute(ScriptingConstants.ATTR_AREA_LIST_RESOURCE);
        }

        try {
            if (node.hasProperty(Constants.JAHIA_CONTRIBUTE_TYPES)) {
                contributeNode = node;
            }
            if (contributeNode != null && contributeNode.hasProperty(Constants.JAHIA_CONTRIBUTE_TYPES)) {
                final LinkedHashSet<String> l = new LinkedHashSet<String>();
                final Value[] v = contributeNode.getProperty(Constants.JAHIA_CONTRIBUTE_TYPES).getValues();
                if (v.length == 0) {
                    l.add(ScriptingConstants.MIX_JMIX_EDITORIAL_CONTENT);
                } else {
                    for (final Value value : v) {
                        l.add(value.getString());
                    }
                }
                final LinkedHashSet<String> subtypes = new LinkedHashSet<String>();
                final Set<String> installedModulesWithAllDependencies = renderContext.getSite()
                        .getInstalledModulesWithAllDependencies();
                for (final String s : l) {
                    final ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s);
                    if (nt != null) {
                        if (!nt.isAbstract() && !nt.isMixin() &&
                                (nt.getTemplatePackage() == null || installedModulesWithAllDependencies
                                        .contains(nt.getTemplatePackage().getId()))) {
                            subtypes.add(nt.getName());
                        }
                        for (final ExtendedNodeType subtype : nt.getSubtypesAsList()) {
                            if (!subtype.isAbstract() && !subtype.isMixin() &&
                                    (subtype.getTemplatePackage() == null || installedModulesWithAllDependencies
                                            .contains(subtype.getTemplatePackage().getId()))) {
                                subtypes.add(subtype.getName());
                            }
                        }
                    }
                }
                if (subtypes.size() < 10) {
                    return new ArrayList<String>(subtypes);
                }
                return new ArrayList<String>(l);
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @param node
     * @return
     */
    private boolean contributeAccess(JCRNodeWrapper node) {
        if (!ScriptingConstants.MODE_CONTRIBUTE_MODE.equals(renderContext.getEditModeConfigName())) {
            return true;
        }
        final JCRNodeWrapper contributeNode;
        final Object areaListResource = renderContext.getRequest().getAttribute(ScriptingConstants.ATTR_AREA_LIST_RESOURCE);
        if (areaListResource != null) {
            contributeNode = (JCRNodeWrapper) areaListResource;
        } else {
            contributeNode = (JCRNodeWrapper) renderContext.getRequest().getAttribute(ScriptingConstants.ATTR_AREA_RESOURCE);
        }

        try {
            final Boolean nodeStatus = isNodeEditableInContributeMode(node);
            final Boolean contributeNodeStatus = contributeNode != null ? isNodeEditableInContributeMode(contributeNode) : null;
            final String sitePath = renderContext.getSite().getPath();
            if (nodeStatus != null) {
                // first look at the current node's status with respect to editable in contribution mode, if it's determined, then use that
                return nodeStatus;
            } else if (contributeNodeStatus != null) {
                // otherwise, look at the contribute node's status if it exists and use that
                return contributeNodeStatus;
            } else if (node.getPath().startsWith(sitePath)) {
                // otherwise, if the property wasn't defined on the nodes we are interested in, look at the parent iteratively until we know the status of the property
                while (!node.getPath().equals(sitePath)) {
                    node = node.getParent();
                    final Boolean parentStatus = isNodeEditableInContributeMode(node);
                    if (parentStatus != null) {
                        return parentStatus;
                    }
                }
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Returns <code>null</code> if the node we're looking at doesn't have the editable in contribution mode property, otherwise returns the value of the property.
     *
     * @param node the node we're interested in
     * @return <code>null</code> if the node we're looking at doesn't have the editable in contribution mode property, otherwise returns the value of the property.
     * @throws RepositoryException
     */
    private Boolean isNodeEditableInContributeMode(final JCRNodeWrapper node) throws RepositoryException {
        final boolean hasProperty = node.hasProperty(Constants.JAHIA_EDITABLE_IN_CONTRIBUTION);
        if (hasProperty) {
            return node.getProperty(Constants.JAHIA_EDITABLE_IN_CONTRIBUTION).getBoolean();
        } else {
            return null;
        }
    }

    /**
     * @throws IOException
     */
    protected void setNode() throws IOException {
        if (nodeName != null) {
            node = (JCRNodeWrapper) request.getAttribute(nodeName);
        } else if (path != null && currentResource != null) {
            try {
                if (!path.startsWith("/")) {
                    final JCRNodeWrapper nodeWrapper = currentResource.getNode();
                    if (!path.equals("*") && nodeWrapper.hasNode(path)) {
                        node = nodeWrapper.getNode(path);
                    } else {
                        missingResource();
                    }
                } else if (path.startsWith("/")) {
                    final JCRSessionWrapper session = currentResource.getNode().getSession();
                    try {
                        node = (JCRNodeWrapper) session.getItem(path);
                    } catch (PathNotFoundException e) {
                        missingResource();
                    }
                }
            } catch (RepositoryException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @return
     */
    protected String getConfiguration() {
        return Resource.CONFIGURATION_MODULE;
    }

    /**
     * @param node
     * @return
     */
    protected boolean checkNodeEditable(final JCRNodeWrapper node) {
        try {
            if (node != null && !renderContext.isEditable(node)) {
                return false;
            }
        } catch (RepositoryException e) {
            LOGGER.error("Failed to check if the node " + node.getPath() + " is editable.", e);
        }
        return true;
    }

    /**
     * @return
     */
    protected boolean canEdit() {
        return renderContext.isEditMode() && editable &&
                !Boolean.TRUE.equals(renderContext.getRequest().getAttribute("inWrapper")) &&
                renderContext.getRequest().getAttribute("inArea") == null;
    }

    /**
     * @param type
     * @param path
     * @param script
     * @param additionalParameters
     * @throws RepositoryException
     * @throws IOException
     */
    protected void printModuleStart(final String type,
                                    final String path,
                                    final Script script,
                                    final String additionalParameters)
            throws RepositoryException, IOException {
        builder.append("<div class=\"jahia-template-gxt\" jahiatype=\"module\" ").append("id=\"module")
                .append(UUID.randomUUID().toString()).append("\" type=\"").append(type).append("\"");
        builder.append((script != null && script.getView().getInfo() != null)
                ? " scriptInfo=\"" + script.getView().getInfo() + "\"" : "");

        if (script != null && script.getView().getModule().getSourcesFolder() != null) {
            String version = script.getView().getModule().getIdWithVersion();
            builder.append(" sourceInfo=\"/modules/")
                    .append(version)
                    .append("/sources/src/main/resources")
                    .append(StringUtils.substringAfter(script.getView().getPath(),
                            "/modules/" + script.getView().getModule().getId()))
                    .append("\"");
        }

        builder.append(" path=\"").append(path != null && path.indexOf('"') != -1 ? Patterns.DOUBLE_QUOTE.matcher(path)
                .replaceAll("&quot;") : path).append("\"");

        if (!StringUtils.isEmpty(nodeTypes)) {
            nodeTypes = StringUtils.join(Ordering.natural()
                    .sortedCopy(Arrays.asList(Patterns.SPACE.split(nodeTypes))), ' ');
            builder.append(" nodetypes=\"").append(nodeTypes).append("\"");
        } else if (!StringUtils.isEmpty(constraints)) {
            constraints = StringUtils.join(Ordering.natural()
                    .sortedCopy(Arrays.asList(Patterns.SPACE.split(constraints))), ' ');
            builder.append(" nodetypes=\"").append(constraints).append("\"");
        }

        if (listLimit > DEFAULT_LIST_LIMIT) {
            builder.append(" listlimit=\"").append(listLimit).append("\"");
        }

        if (!StringUtils.isEmpty(constraints)) {
            String referenceTypes = ConstraintsHelper.getReferenceTypes(constraints, nodeTypes);
            builder.append((!StringUtils.isEmpty(referenceTypes)) ? " referenceTypes=\"" + referenceTypes + "\""
                    : " referenceTypes=\"none\"");
        }

        if (additionalParameters != null) {
            builder.append(" ").append(additionalParameters);
        }

        builder.append(">");
    }

    /**
     * @throws IOException
     */
    protected void printModuleEnd() throws IOException {
        builder.append("</div>");
    }

    /**
     * @param resource
     * @throws IOException
     * @throws RenderException
     */
    protected void render(final Resource resource) throws IOException, RenderException {
        try {
            final Integer level = (Integer) request.getAttribute(ScriptingConstants.ATTR_ORG_JAHIA_MODULES_LEVEL);

            String restriction = null;
            if (!StringUtils.isEmpty(nodeTypes)) {
                restriction = nodeTypes;
            } else if (!StringUtils.isEmpty(constraints)) {
                restriction = constraints;
            }

            final boolean setRestrictions = request.getAttribute(ScriptingConstants
                    .ATTR_AREA_NODE_TYPES_RESTRICTION + level) == null
                    && !StringUtils.isEmpty(restriction);
            if (setRestrictions) {
                request.setAttribute(ScriptingConstants.ATTR_AREA_NODE_TYPES_RESTRICTION + level, restriction);
            }

            final JCRSiteNode previousSite = renderContext.getSite();
            if (contextSite != null) {
                renderContext.setSite(contextSite);
            }

            builder.append(RenderService.getInstance().render(resource, renderContext));
            renderContext.setSite(previousSite);

            if (setRestrictions) {
                request.removeAttribute(ScriptingConstants.ATTR_AREA_NODE_TYPES_RESTRICTION + level);
            }
        } catch (TemplateNotFoundException io) {
            builder.append(io);
        } catch (RenderException e) {
            if (renderContext.isEditMode() && ((e.getCause() instanceof TemplateNotFoundException)
                    || (e.getCause() instanceof AccessDeniedException))) {
                if (!(e.getCause() instanceof AccessDeniedException)) {
                    LOGGER.error(e.getMessage(), e);
                }
                builder.append(e.getCause().getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * @return
     * @throws RepositoryException
     */
    protected String getModuleType() throws RepositoryException {
        String type = ScriptingConstants.TYPE_EXISTING_NODE;

        if (node.isNodeType(ScriptingConstants.MIX_JMIX_LIST_CONTENT)) {
            type = ScriptingConstants.MODULE_TYPE_LIST;
        } else if (renderContext.getEditModeConfig().isForceHeaders()) {
            type = ScriptingConstants.TYPE_EXISTING_NODE_WITH_HEADER;
        }
        return type;
    }

    /**
     * @throws RepositoryException
     * @throws IOException
     */
    protected void missingResource()
            throws RepositoryException, IOException {
        String currentPath = currentResource.getNode().getPath();
        if (path.startsWith(currentPath + "/") && path.substring(currentPath.length() + 1).indexOf('/') == -1) {
            currentResource.getMissingResources().add(path.substring(currentPath.length() + 1));
        } else if (!path.startsWith("/")) {
            currentResource.getMissingResources().add(path);
        }

        if (!"*".equals(path) && (!path.contains("/"))) {
            // we have a named path that is missing, let's see if we can figure out it's node type.
            constraints = ConstraintsHelper.getConstraints(currentResource.getNode(), path);
        }

        if (canEdit() && checkNodeEditable(currentResource.getNode()) && contributeAccess(currentResource.getNode())) {
            if (currentResource.getNode().hasPermission("jcr:addChildNodes")) {
                List<String> contributeTypes = contributeTypes(currentResource.getNode());
                if (contributeTypes != null) {
                    nodeTypes = StringUtils.join(contributeTypes, " ");
                }
                printModuleStart("placeholder", path, null, null);
                printModuleEnd();
            }
        }
    }
}
