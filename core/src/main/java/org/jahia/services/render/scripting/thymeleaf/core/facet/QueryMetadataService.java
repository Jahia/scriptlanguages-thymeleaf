package org.jahia.services.render.scripting.thymeleaf.core.facet;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.query.QOMBuilder;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.ScriptingSupportService;
import org.jahia.services.render.scripting.thymeleaf.expression.variables.FacetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by smomin on 2/17/16.
 */
public class QueryMetadataService extends ScriptingSupportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryMetadataService.class);

    private RenderContext renderContext;
    private LocalizationContext localizationContext;
    private String resourceBundle;
    private Resource currentResource;
    private QueryObjectModel existing;
    private JCRNodeWrapper boundComponent;
    private Map<String, List<KeyValue>> activeFacets;
    private Map<String, ExtendedNodeType> facetValueNodeTypes;
    private Map<String, String> facetLabels;
    private Map<String, String> facetValueLabels;
    private Map<String, String> facetValueFormats;
    private Map<String, String> facetValueRenderers;

    /**
     *
     * @param renderContext
     * @param localizationContext
     * @param resourceBundle
     * @param currentResource
     * @param existing
     * @param boundComponent
     * @param activeFacets
     * @param facetValueNodeTypes
     * @param facetLabels
     * @param facetValueLabels
     * @param facetValueFormats
     * @param facetValueRenderers
     */
    public QueryMetadataService(final RenderContext renderContext,
                                final LocalizationContext localizationContext,
                                final String resourceBundle,
                                final Resource currentResource,
                                final QueryObjectModel existing,
                                final JCRNodeWrapper boundComponent,
                                final Map<String, List<KeyValue>> activeFacets,
                                final Map<String, ExtendedNodeType> facetValueNodeTypes,
                                final Map<String, String> facetLabels,
                                final Map<String, String> facetValueLabels,
                                final Map<String, String> facetValueFormats,
                                final Map<String, String> facetValueRenderers) {
        this.renderContext = renderContext;
        this.localizationContext = localizationContext;
        this.resourceBundle = resourceBundle;
        this.currentResource = currentResource;
        this.existing = existing;
        this.boundComponent = boundComponent;
        this.activeFacets = activeFacets;
        this.facetValueNodeTypes = facetValueNodeTypes;
        this.facetLabels = facetLabels;
        this.facetValueLabels = facetValueLabels;
        this.facetValueFormats = facetValueFormats;
        this.facetValueRenderers = facetValueRenderers;
    }

    /**
     *
     * @return
     */
    @Override
    public QueryObjectModel doProcess() {
        try {
            final JCRNodeWrapper currentNode = currentResource.getNode();
            final JCRSessionWrapper session = currentNode.getSession();
            final QueryObjectModelFactory factory = session.getWorkspace().getQueryManager().getQOMFactory();
            final QOMBuilder qomBuilder = new QOMBuilder(factory, session.getValueFactory());

            String selectorName;
            if (existing == null) {
                // here we assume that if existing is null, then bound component is not of type jnt:query
                String wantedNodeType = "jnt:content";
                if (currentNode.hasProperty("j:type")) {
                    wantedNodeType = currentNode.getPropertyAsString("j:type");
                }

                selectorName = wantedNodeType;
                qomBuilder.setSource(factory.selector(wantedNodeType, selectorName));

                // replace the site name in bound component by the one from the render context
                String path = boundComponent.getPath();
                final String siteName = renderContext.getSite().getName();
                final int afterSites = "/sites/".length();
                final int afterSite = path.indexOf('/', afterSites + 1);
                if (afterSite > 0 && afterSite < path.length()) {
                    String restOfPath = path.substring(afterSite);
                    path = "/sites/" + siteName + restOfPath;
                }
                qomBuilder.andConstraint(factory.descendantNode(selectorName, path));
            } else {
                final Selector selector = (Selector) existing.getSource();
                selectorName = selector.getSelectorName();
                qomBuilder.setSource(selector);
                qomBuilder.andConstraint(existing.getConstraint());
            }

            // specify query for unapplied facets
            final List<JCRNodeWrapper> facets = JCRContentUtils.getNodes(currentNode, "jnt:facet");
            for (final JCRNodeWrapper facet : facets) {

                // extra query parameters
                String extra = null;

                // min count
                final String minCount = facet.getPropertyAsString("mincount");

                // field components
                final String field = facet.getPropertyAsString("field");
                final String[] fieldComponents = StringUtils.split(field, ";");
                int i = 0;
                final String facetNodeTypeName = fieldComponents != null && fieldComponents.length > 1 ? fieldComponents[i++] : null;
                final String facetPropertyName = fieldComponents != null ? fieldComponents[i] : null;

                // are we dealing with a query facet?
                boolean isQuery = facet.hasProperty("query");

                // query value if it exists
                final String queryProperty = isQuery ? facet.getPropertyAsString("query") : null;

                // key used in metadata maps
                final String metadataKey = isQuery ? queryProperty : facetPropertyName;

                // get node type if we can
                ExtendedNodeType nodeType = null;
                if (StringUtils.isNotEmpty(facetNodeTypeName)) {
                    // first check if we don't already have resolved the nodeType to avoid resolving it again
                    if (facetValueNodeTypes != null) {
                        nodeType = (ExtendedNodeType) facetValueNodeTypes.get(metadataKey);
                    }

                    // since we haven't already resolved it, try that now
                    if (nodeType == null) {
                        nodeType = NodeTypeRegistry.getInstance().getNodeType(facetNodeTypeName);
                        if (facetValueNodeTypes != null && StringUtils.isNotEmpty(metadataKey)) {
                            facetValueNodeTypes.put(metadataKey, nodeType);
                        }
                    }
                }

                // label
                String currentFacetLabel = null;
                // use label property if it exists
                if (facet.hasProperty("label")) {
                    currentFacetLabel = facet.getPropertyAsString("label");
                }
                // otherwise try to derive a label from node type and field name
                if (StringUtils.isEmpty(currentFacetLabel) && StringUtils.isNotEmpty(facetNodeTypeName) && StringUtils.isNotEmpty(facetPropertyName)) {
                    final String labelKey = facetNodeTypeName.replace(':', '_') + "." + facetPropertyName.replace(':', '_');

                    currentFacetLabel = getMessage(renderContext, localizationContext, resourceBundle, labelKey);
                }
                if (facetLabels != null && StringUtils.isNotEmpty(currentFacetLabel)) {
                    facetLabels.put(metadataKey, currentFacetLabel);
                }

                // value format
                if (facetValueFormats != null && facet.hasProperty("labelFormat")) {
                    facetValueFormats.put(metadataKey, facet.getPropertyAsString("labelFormat"));
                }

                // label renderer
                String labelRenderer = null;
                if (facetValueRenderers != null && facet.hasProperty("labelRenderer")) {
                    labelRenderer = facet.getPropertyAsString("labelRenderer");
                    facetValueRenderers.put(metadataKey, labelRenderer);
                }

                // value label
                if (facetValueLabels != null && facet.hasProperty("valueLabel")) {
                    facetValueLabels.put(metadataKey, facet.getPropertyAsString("valueLabel"));
                }

                // is the current facet applied?
                final ExtendedPropertyDefinition propDef = nodeType != null ? nodeType.getPropertyDefinition(facetPropertyName) : null;
                final boolean isFacetApplied = FacetUtils.isFacetApplied(metadataKey, activeFacets, propDef);

                if (nodeType != null && StringUtils.isNotEmpty(facetPropertyName) && !isFacetApplied) {
                    StringBuilder extraBuilder = new StringBuilder();

                    // deal with facets with labelRenderers, currently only jnt:dateFacet or jnt:rangeFacet
                    String prefix = facet.isNodeType("jnt:dateFacet") ? "date." : (facet.isNodeType("jnt:rangeFacet") ? "range." : "");
                    if (StringUtils.isNotEmpty(labelRenderer)) {
                        extraBuilder.append(prefixedNameValuePair(prefix, "labelRenderer", labelRenderer));
                    }

                    for (ExtendedPropertyDefinition propertyDefinition : FacetUtils.getPropertyDefinitions(facet)) {
                        final String name = propertyDefinition.getName();
                        if (facet.hasProperty(name)) {
                            final JCRPropertyWrapper property = facet.getProperty(name);

                            if (property.isMultiple()) {
                                // if property is multiple append prefixed name value pair to query
                                for (JCRValueWrapper value : property.getValues()) {
                                    extraBuilder.append(prefixedNameValuePair(prefix, name, value.getString()));
                                }
                            } else {
                                String value = property.getString();

                                // adjust value for hierarchical facets
                                if (facet.isNodeType("jnt:fieldHierarchicalFacet") && name.equals("prefix")) {
                                    final List<KeyValue> active = activeFacets != null ? activeFacets.get(facetPropertyName) : Collections.<KeyValue>emptyList();
                                    if (active == null || active.isEmpty()) {
                                        value = FacetUtils.getIndexPrefixedPath(value);
                                    } else {
                                        value = FacetUtils.getDrillDownPrefix((String) active.get(active.size() - 1).getKey());
                                    }
                                }

                                extraBuilder.append(prefixedNameValuePair(prefix, name, value));
                            }
                        }
                    }

                    extra = extraBuilder.toString();

                }

                if (isQuery && !isFacetApplied) {
                    extra = "&facet.query=" + queryProperty;
                }

                // only add a column if the facet isn't already applied
                if (!isFacetApplied) {
                    // key used in the solr query string
                    final String key = isQuery ? facet.getName() : facetPropertyName;
                    final String query = buildQueryString(facetNodeTypeName, key, minCount, extra);
                    final String columnPropertyName = StringUtils.isNotEmpty(facetPropertyName) ? facetPropertyName : "rep:facet()";
                    qomBuilder.getColumns().add(factory.column(selectorName, columnPropertyName, query));
                }
            }

            // repeat applied facets
            if (activeFacets != null) {
                for (Map.Entry<String, List<KeyValue>> appliedFacet : activeFacets.entrySet()) {
                    for (KeyValue keyValue : appliedFacet.getValue()) {
                        final String propertyName = "rep:filter(" + Text.escapeIllegalJcrChars(appliedFacet.getKey()) + ")";
                        qomBuilder.andConstraint(factory.fullTextSearch(selectorName, propertyName, factory.literal(qomBuilder.getValueFactory().createValue(keyValue.getValue().toString()))));
                    }
                }
            }

            return qomBuilder.createQOM();
        } catch (RepositoryException e) {
            LOGGER.warn(e.getMessage());
        }
        return null;
    }

    private static String prefixedNameValuePair(String prefix, String name, String value) {
        return "&" + prefix + name + "=" + value;
    }

    private static String buildQueryString(String facetNodeTypeName, String key, String minCount, String extra) {
        final String nodeType = facetNodeTypeName != null ? "nodetype=" + facetNodeTypeName + "&" : "";
        return "rep:facet(" + nodeType + "key=" + key + "&mincount=" + minCount + (extra != null ? extra : "") + ")";
    }
}
