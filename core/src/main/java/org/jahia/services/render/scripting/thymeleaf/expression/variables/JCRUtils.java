package org.jahia.services.render.scripting.thymeleaf.expression.variables;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.api.Constants;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.templates.ComponentRegistry;
import org.jahia.taglibs.uicomponents.Functions;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by smomin on 2/16/16.
 */
public class JCRUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JCRUtils.class);
    private static final Comparator<Map<String, Object>> DISPLAY_NAME_COMPARATOR = new Comparator<Map<String, Object>>() {
        public int compare(final Map<String, Object> o1, final Map<String, Object> o2) {
            return StringUtils
                    .defaultString((String) o1.get("displayName"))
                    .toLowerCase()
                    .compareTo(
                            StringUtils.defaultString((String) o2.get("displayName")).toLowerCase());
        }
    };

    public JCRUtils() {
        super();
    }

    /**
     *
     * @param node
     * @return
     */
    public static VanityUrl getDefaultVanityUrl(final JCRNodeWrapper node) {
        try {
            final VanityUrlService vanityUrlService = (VanityUrlService) SpringContextSingleton.getBean(VanityUrlService.class.getName());
            List<VanityUrl> l = vanityUrlService.getVanityUrls(node, node.getSession().getLocale().toString(), node.getSession());
            VanityUrl vanityUrl = null;
            for (VanityUrl v : l) {
                if (v.isDefaultMapping()) {
                    vanityUrl = v;
                }
            }
            return vanityUrl;
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    /**
     *
     * @param node
     * @param includeInherited
     * @param expandGroups
     * @param roles
     * @param limit
     * @param sortType
     * @return
     */
    public static List<Map<String, Object>> getRolesForNode(final JCRNodeWrapper node,
                                                            final boolean includeInherited,
                                                            final boolean expandGroups,
                                                            final String roles,
                                                            final int limit,
                                                            final String sortType) {
        List<Map<String, Object>> results;

        boolean sortByDisplayName = sortType != null && sortType.equalsIgnoreCase("displayName");
        results = JCRContentUtils.getRolesForNode(node, includeInherited, expandGroups, roles, limit, sortType != null && sortType.equalsIgnoreCase("latestFirst"));
        if (sortByDisplayName) {
            for (Map<String, Object> result : results) {
                result.put("displayName", PrincipalViewHelper.getFullName(result.get("principal")));
            }
            Collections.sort(results, DISPLAY_NAME_COMPARATOR);
        }
        return results;
    }

    /**
     *
     * @param node
     * @param viewName
     * @param renderContext
     * @return
     */
    public static Boolean hasScriptView(final JCRNodeWrapper node,
                                        final String viewName,
                                        final RenderContext renderContext) {
        try {
            return RenderService.getInstance().resolveScript(new org.jahia.services.render.Resource(node,
                    renderContext.getMainResource().getTemplateType(),
                    viewName,
                    renderContext.getMainResource().getContextConfiguration()), renderContext) != null;
        } catch (TemplateNotFoundException e) {
            //Do nothing
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }

    /**
     * Returns the first parent of the specified node, which has the ACL inheritance broken. If not found, null<code>null</code> is
     * returned.
     *
     * @param node the node to search parent for
     * @return the first parent of the specified node, which has the ACL inheritance broken. If not found, null<code>null</code> is returned
     */
    public static JCRNodeWrapper getParentWithAclInheritanceBroken(JCRNodeWrapper node) {
        try {
            return JCRContentUtils.getParentWithAclInheritanceBroken(node);
        } catch (RepositoryException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            } else {
                LOGGER.warn("Unable to get parent of a node " + node.getPath()
                        + " with ACL inheritance break. Cause: " + e.getMessage());
            }
        }

        return null;
    }

    /**
     *
     * @param currentNode
     * @param renderContext
     * @param property
     * @return
     */
    public static JCRNodeWrapper getBoundComponent(final JCRNodeWrapper currentNode,
                                                   final RenderContext renderContext,
                                                   final String property) {
        return Functions.getBoundComponent(currentNode, renderContext, property);
    }

    /**
     *
     * @param currentNode
     * @param renderContext
     * @param property
     * @return
     */
    public static String getBoundComponentPath(final JCRNodeWrapper currentNode,
                                               final RenderContext renderContext,
                                               final String property) {
        return Functions.getBoundComponentPath(currentNode, renderContext, property);
    }

    /**
     * Get the node or property display name depending on the locale
     *
     * @param nodeObject the item to get the label for
     * @param locale current locale
     * @return the node or property display name depending on the locale
     */
    public static String label(final Object nodeObject, final Locale locale) {
        return JCRContentUtils.getDisplayLabel(nodeObject, locale, null);
    }

    /**
     * Get the label value depending on the locale
     *
     * @param nodeObject
     * @param locale as a string
     * @return
     */
    public static String label(final Object nodeObject, final String locale) {
        return label(nodeObject, LanguageCodeConverters.languageCodeToLocale(locale));
    }

    /**
     *
     * @param propertyDefinition
     * @param locale
     * @param nodeType
     * @return
     */
    public static String label(final ExtendedPropertyDefinition propertyDefinition,
                               final String locale,
                               final ExtendedNodeType nodeType) {
        return JCRContentUtils.getDisplayLabel(propertyDefinition,
                LanguageCodeConverters.languageCodeToLocale(locale),
                nodeType);
    }

    /**
     * Returns <code>true</code> if the current node has the specified type or at least one of the specified node types.
     *
     * @param node current node to check the type
     * @param type the node type name to match or a comma-separated list of node
     *            types (at least one should be matched)
     * @return <code>true</code> if the current node has the specified type or at least one of the specified node types
     */
    public static boolean isNodeType(final JCRNodeWrapper node, final String type) {
        if (node == null) {
            throw new IllegalArgumentException("The specified node is null");
        }

        boolean hasType = false;
        try {
            if (type.contains(",")) {
                for (final String typeToCheck : StringUtils.split(type, ',')) {
                    if (node.isNodeType(typeToCheck.trim())) {
                        hasType = true;
                        break;
                    }
                }
            } else {
                hasType = node.isNodeType(type);
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return hasType;
    }

    /**
     *
     * @param node
     * @param type
     * @return
     */
    public static List<JCRNodeWrapper> getNodes(final JCRNodeWrapper node,
                                                final String type) {
        return JCRContentUtils.getNodes(node, type);
    }

    /**
     * Returns <code>true</code> if the current node has at least one child node
     * of the specified type.
     *
     * @param node current node whose children will be queried
     * @param type the node type name to match or a comma-separated list of node
     *            types (at least one should be matched)
     * @return <code>true</code> if the current node has at least one child node
     *         of the specified type
     */
    public static boolean hasChildrenOfType(final JCRNodeWrapper node, final String type) {
        boolean hasChildrenOfType = false;
        final String[] typesToCheck = StringUtils.split(type, ',');
        try {
            for (final NodeIterator iterator = node.getNodes(); iterator.hasNext() && !hasChildrenOfType;) {
                final Node child = iterator.nextNode();
                for (final String matchType : typesToCheck) {
                    if (child.isNodeType(matchType)) {
                        hasChildrenOfType = true;
                        break;
                    }
                }
            }
        } catch (RepositoryException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return hasChildrenOfType;
    }

    /**
     * Returns an iterator with the child nodes of the current node, which match
     * the specified node type name. This is an advanced version of the
     * {@link #getNodes(JCRNodeWrapper, String)} method to handle multiple node
     * types.
     *
     * @param node current node whose children will be queried
     * @param type the node type name to match or a comma-separated list of node
     *            types (at least one should be matched)
     * @return an iterator with the child nodes of the current node, which match
     *         the specified node type name
     */
    public static List<JCRNodeWrapper> getChildrenOfType(final JCRNodeWrapper node, final String type) {
        return JCRContentUtils.getChildrenOfType(node, type);
    }

    /**
     * Returns an iterator with the descendant nodes of the current node, which match
     * the specified node type name.
     *
     * @param node current node whose descendants will be queried
     * @param type the node type name to match
     * @return an iterator with the descendant nodes of the current node, which match
     *         the specified node type name
     */
    public static NodeIterator getDescendantNodes(JCRNodeWrapper node, String type) {
        return JCRContentUtils.getDescendantNodes(node, type);
    }

    /**
     *
     * @param nodeContainingProperties
     * @param nodeContainingNodeNames
     * @param type
     * @return
     */
    public static Map<String, String> getPropertiesAsStringFromNodeNameOfThatType(final JCRNodeWrapper nodeContainingProperties,
                                                                                  final JCRNodeWrapper nodeContainingNodeNames,
                                                                                  final String type) {
        final List<JCRNodeWrapper> nodeNames = getNodes(nodeContainingNodeNames,type);
        final Map<String, String> props = new LinkedHashMap<String, String>();
        for (final JCRNodeWrapper nodeWrapper : nodeNames) {
            final String name = nodeWrapper.getName();
            try {
                final JCRPropertyWrapper property = nodeContainingProperties.getProperty(name);
                final String value;
                if(property.isMultiple()) {
                    value = property.getValues()[0].getString();
                } else {
                    value = property.getValue().getString();
                }
                props.put(name,value);
            } catch (PathNotFoundException e) {
                LOGGER.debug(e.getMessage(), e);
            } catch (ValueFormatException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (RepositoryException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return props;
    }

    /**
     * Returns all the parents of the current node that have the specified node type. If no matching node is found, an
     * empty list.
     *
     * @param node
     *            the current node to start the lookup from
     * @param type
     *            the required type of the parent node(s)
     * @return the parents of the current node that have the specified node type. If no matching node is found, an
     *         empty list is returned
     */
    public static List<JCRNodeWrapper> getParentsOfType(final JCRNodeWrapper node,
                                                        final String type) {
        final List<JCRNodeWrapper> parents = new ArrayList<JCRNodeWrapper>();
        JCRNodeWrapper parentOfTypeNode = node;
        do {
            parentOfTypeNode = getParentOfType(parentOfTypeNode, type);
            if (parentOfTypeNode != null) {
                parents.add(parentOfTypeNode);
            }
        } while (parentOfTypeNode != null);

        return parents;
    }

    /**
     * Returns the first parent of the current node that has the specified node type. If no matching node is found, <code>null</code> is
     * returned.
     *
     * @param node
     *            the current node to start the lookup from
     * @param type
     *            the required type of the parent node
     * @return the first parent of the current node that has the specified node type. If no matching node is found, <code>null</code> is
     *         returned
     */
    public static JCRNodeWrapper getParentOfType(final JCRNodeWrapper node,
                                                 final String type) {
        return JCRContentUtils.getParentOfType(node, type);
    }

    /**
     *
     * @param node
     * @param permission
     * @return
     */
    public static boolean hasPermission(final JCRNodeWrapper node,
                                        final String permission) {
        return node != null && node.hasPermission(permission);
    }

    /**
     *
     * @param node
     * @return
     */
    public static String humanReadableFileLength(final JCRNodeWrapper node) {
        return FileUtils.byteCountToDisplaySize(node.getFileContent().getContentLength());
    }

    /**
     * Returns all the parents of the current node that have the specified node type. If no matching node is found, an
     * empty list.
     *
     * @param node
     *            the current node to start the lookup from
     * @param type
     *            the required type of the parent node(s)
     * @return the parents of the current node that have the specified node type. If no matching node is found, an
     *         empty list is returned
     */
    public static List<JCRNodeWrapper> getMeAndParentsOfType(final JCRNodeWrapper node,
                                                             final String type) {

        final List<JCRNodeWrapper> parents = new ArrayList<JCRNodeWrapper>();
        try {
            if(node.isNodeType(type)) {
                parents.add(node);
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        JCRNodeWrapper parentOfTypeNode = node;
        do {
            parentOfTypeNode = getParentOfType(parentOfTypeNode, type);
            if (parentOfTypeNode != null) {
                parents.add(parentOfTypeNode);
            }
        } while (parentOfTypeNode != null);
        return parents;
    }

    /**
     *
     * @param node
     * @return
     */
    public static boolean hasOrderableChildNodes(final JCRNodeWrapper node) {
        try {
            return node.getPrimaryNodeType().hasOrderableChildNodes();
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     *
     * @param node
     * @return
     */
    public static String getConstraints(final JCRNodeWrapper node) {
        try {
            return Patterns.SPACE.matcher(ConstraintsHelper.getConstraints(node)).replaceAll(",");
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
            return "";
        }
    }

    /**
     * @see org.apache.jackrabbit.util.Text#escapeIllegalJcrChars(String)
     */
    public static String escapeIllegalJcrChars(final String inputString) {
        return Text.escapeIllegalJcrChars(inputString);
    }

    /**
     *
     * @param node
     * @param areaNode
     * @param typelistValues
     * @return
     * @throws Exception
     */
    public static List<ExtendedNodeType> getContributeTypes(final JCRNodeWrapper node,
                                                            final JCRNodeWrapper areaNode,
                                                            final Value[] typelistValues) throws Exception {
        final List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>();
        final List<String> typeList = getContributeTypesAsString(node, areaNode, typelistValues);

        if (!typeList.isEmpty()) {
            final List<JCRNodeWrapper> components = new ArrayList<JCRNodeWrapper>();
            components.add(node.getResolveSite().getNode("components"));
            for (int i = 0; i < components.size(); i++) {
                final JCRNodeWrapper n = components.get(i);
                if (n.isNodeType("jnt:componentFolder")) {
                    final NodeIterator nodeIterator = n.getNodes();
                    while (nodeIterator.hasNext()) {
                        final JCRNodeWrapper next = (JCRNodeWrapper) nodeIterator.next();
                        components.add(next);
                    }
                } else if (n.isNodeType("jnt:simpleComponent") && n.hasPermission("useComponentForCreate")) {
                    final ExtendedNodeType t = NodeTypeRegistry.getInstance().getNodeType(n.getName());
                    for (String s : typeList) {
                        if (t.isNodeType(s)) {
                            types.add(t);
                            break;
                        }
                    }
                }
            }
        }

        final String[] constraints = Patterns.SPACE.split(ConstraintsHelper.getConstraints(node));
        final List<ExtendedNodeType> finaltypes = new ArrayList<ExtendedNodeType>();
        for (final ExtendedNodeType type : types) {
            for (final String s : constraints) {
                if (!finaltypes.contains(type) && type.isNodeType(s)) {
                    finaltypes.add(type);
                }
            }
        }
        return finaltypes;
    }

    /**
     *
     * @param node
     * @param areaNode
     * @param typelistValues
     * @param displayLocale
     * @return
     * @throws Exception
     */
    public static Map<String, String> getContributeTypesDisplay(final JCRNodeWrapper node,
                                                                final JCRNodeWrapper areaNode,
                                                                final Value[] typelistValues,
                                                                final Locale displayLocale) throws Exception {
        if (node == null) {
            return Collections.emptyMap();
        }

        final List<String> typeList = getContributeTypesAsString(node, areaNode, typelistValues);
        if (typeList == null) { // there is type restriction defined and none is allowed in contribute mode
            return Collections.emptyMap();
        }

        return ComponentRegistry.getComponentTypes(node, typeList, null, displayLocale);
    }

    /**
     *
     * @param node
     * @param context
     * @return
     */
    public static JCRNodeWrapper findDisplayableNode(final JCRNodeWrapper node,
                                                     final RenderContext context) {
        return JCRContentUtils.findDisplayableNode(node, context);
    }

    /**
     *
     * @param node
     * @param context
     * @param siteNode
     * @return
     */
    public static JCRNodeWrapper findDisplayableNodeInSite(final JCRNodeWrapper node,
                                                           final RenderContext context,
                                                           final JCRSiteNode siteNode) {
        return JCRContentUtils.findDisplayableNode(node, context, siteNode);
    }

    /**
     *
     * @param node
     * @param nodeType
     * @return
     * @throws RepositoryException
     */
    public static boolean isAllowedChildNodeType(final JCRNodeWrapper node,
                                                 final String nodeType) throws RepositoryException {
        try {
            node.getApplicableChildNodeDefinition("*", nodeType);
            return true;
        } catch (ConstraintViolationException e) {
            return false;
        }
    }

    /**
     *
     * @param permission
     * @param parentNode
     * @param nodeType
     * @return
     */
    public static List<JCRNodeWrapper> findAllowedNodesForPermission(final String permission,
                                                                     final JCRNodeWrapper parentNode,
                                                                     final String nodeType) {
        final List<JCRNodeWrapper> results = new LinkedList<JCRNodeWrapper>();
        try {
            final JCRSessionWrapper session = parentNode.getSession();
            final Query groupQuery = session.getWorkspace().getQueryManager().createQuery(
                    "select * from ["+ nodeType + "] as u where isdescendantnode(u,'" + JCRContentUtils.sqlEncode(parentNode.getPath()) + "')",
                    Query.JCR_SQL2);
            final QueryResult groupQueryResult = groupQuery.execute();
            final NodeIterator nodeIterator = groupQueryResult.getNodes();
            while (nodeIterator.hasNext()) {
                final JCRNodeWrapper node = (JCRNodeWrapper) nodeIterator.next();
                if (hasPermission(node, permission)) {
                    results.add(node);
                }
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return results;
    }

    /**
     *
     * @param permission
     * @param parentNode
     * @param nodeType
     * @return
     */
    public static JCRNodeWrapper getFirstAllowedNodeForPermission(final String permission,
                                                                  final JCRNodeWrapper parentNode,
                                                                  final String nodeType) {
        try {
            final JCRSessionWrapper session = parentNode.getSession();
            final Query groupQuery = session.getWorkspace().getQueryManager().createQuery(
                    "select * from ["+ nodeType + "] as u where isdescendantnode(u,'" + JCRContentUtils.sqlEncode(parentNode.getPath()) + "')",
                    Query.JCR_SQL2);
            final QueryResult groupQueryResult = groupQuery.execute();
            final NodeIterator nodeIterator = groupQueryResult.getNodes();
            while (nodeIterator.hasNext()) {
                final JCRNodeWrapper node = (JCRNodeWrapper) nodeIterator.next();
                if (hasPermission(node, permission)) {
                    return node;
                }
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns a string with comma-separated keywords, found on the current node (or the parent one, if inheritance is considered), or an
     * empty string if no keywords are present.
     *
     * @param node
     *            the node to retrieve keywords from
     * @param considerInherted
     *            if set to <code>true</code> the keywords are also looked up to the parent nodes, if not found on the current one
     * @return a string with comma-separated keywords, found on the current node (or the parent one, if inheritance is considered), or an
     *         empty string if no keywords are present
     */
    public static String getKeywords(final JCRNodeWrapper node,
                                     final boolean considerInherted) {
        if (node == null) {
            return StringUtils.EMPTY;
        }
        String keywords = null;
        try {
            JCRNodeWrapper current = node;
            while (current != null) {
                final JCRPropertyWrapper property = current.hasProperty("j:keywords") ? current
                        .getProperty("j:keywords") : null;

                if (property != null) {
                    if (property.getDefinition().isMultiple()) {
                        final StringBuilder buff = new StringBuilder(64);
                        for (final Value val : property.getValues()) {
                            String keyword = val.getString();
                            if (StringUtils.isNotEmpty(keyword)) {
                                if (buff.length() > 0) {
                                    buff.append(", ");
                                }
                                buff.append(keyword);
                            }
                        }
                        keywords = buff.toString();
                    } else {
                        keywords = property.getString();
                    }
                    break;
                } else if (considerInherted && !"/".equals(current.getPath())) {
                    current = current.getParent();
                } else {
                    break;
                }
            }
        } catch (RepositoryException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            } else {
                LOGGER.warn("Unable to get keyworkds for node " + node.getPath() + ". Cause: "
                        + e.getMessage());
            }
        }

        return StringUtils.defaultString(keywords);
    }

    /**
     *
     * @param node
     * @param language
     * @param includesReferences
     * @param includesSubnodes
     * @param allsubtree
     * @return
     */
    public static boolean needPublication(final JCRNodeWrapper node,
                                          final String language,
                                          final boolean includesReferences,
                                          final boolean includesSubnodes,
                                          final boolean allsubtree) {
        if (node != null) {
            final JCRPublicationService publicationService = JCRPublicationService.getInstance();
            if (publicationService != null) {
                try {
                    final List<PublicationInfo> publicationInfos = publicationService.getPublicationInfo(node.getIdentifier(),
                            (StringUtils.isEmpty(language) ? null : Collections.singleton(language)), includesReferences, includesSubnodes,
                            allsubtree, node.getSession().getWorkspace().getName(), Constants.LIVE_WORKSPACE);
                    for (final PublicationInfo publicationInfo : publicationInfos) {
                        if (publicationInfo.needPublication(StringUtils.isEmpty(language) ? null : language)) {
                            return true;
                        }
                    }
                } catch (RepositoryException e) {
                    LOGGER.error("Failed to get PublicationInfo for node " + node.getPath(), e);
                }
            }
        }
        return false;
    }

    /**
     *
     * @param nodeType
     * @param allowedTypes
     * @return
     */
    private static boolean isAllowedSubnodeType(final String nodeType,
                                                final Set<String> allowedTypes) {
        boolean isAllowed = false;
        try {
            final ExtendedNodeType t = NodeTypeRegistry.getInstance().getNodeType(nodeType);
            for (String allowedType : allowedTypes) {
                if (t.isNodeType(allowedType)) {
                    isAllowed = true;
                    break;
                }
            }
        } catch (RepositoryException e) {
            LOGGER.warn("Nodetype " + nodeType + " not found while checking for allowed node types!", nodeType);
        }

        return isAllowed;
    }

    /**
     *
     * @param node
     * @param areaNode
     * @param typelistValues
     * @return
     * @throws RepositoryException
     */
    private static List<String> getContributeTypesAsString(final JCRNodeWrapper node,
                                                           final JCRNodeWrapper areaNode,
                                                           Value[] typelistValues) throws RepositoryException {
        if ((typelistValues == null || typelistValues.length == 0) && !node.isNodeType("jnt:contentList") && !node.isNodeType("jnt:contentFolder")) {
            return Arrays.asList(Patterns.SPACE.split(ConstraintsHelper.getConstraints(node)));
        }
        if (typelistValues == null && node.hasProperty(Constants.JAHIA_CONTRIBUTE_TYPES)) {
            typelistValues = node.getProperty(Constants.JAHIA_CONTRIBUTE_TYPES).getValues();
        }
        if (typelistValues == null && areaNode != null && areaNode.hasProperty(Constants.JAHIA_CONTRIBUTE_TYPES)) {
            typelistValues = areaNode.getProperty(Constants.JAHIA_CONTRIBUTE_TYPES).getValues();
        }

        if (typelistValues == null) {
            return Collections.emptyList();
        }

        Value[] allowedTypeValues = null;
        if (node.hasProperty("j:allowedTypes")) {
            allowedTypeValues = node.getProperty("j:allowedTypes").getValues();
        }
        if (allowedTypeValues == null && areaNode != null && areaNode.hasProperty("j:allowedTypes")) {
            allowedTypeValues = areaNode.getProperty("j:allowedTypes").getValues();
        }

        final Set<String> allowedTypes = allowedTypeValues == null ? Collections.<String>emptySet() : new HashSet<String>(allowedTypeValues.length);
        if (allowedTypeValues != null) {
            for (final Value value : allowedTypeValues) {
                allowedTypes.add(value.getString());
            }
        }

        final List<String> typeList = new LinkedList<String>();
        for (final Value value : typelistValues) {
            final String type = value.getString();
            if (allowedTypes.isEmpty() || allowedTypes.contains(type)
                    || isAllowedSubnodeType(type, allowedTypes)) {
                typeList.add(type);
            }
        }
        return !allowedTypes.isEmpty() && typeList.isEmpty() ? null : typeList;
    }
}
