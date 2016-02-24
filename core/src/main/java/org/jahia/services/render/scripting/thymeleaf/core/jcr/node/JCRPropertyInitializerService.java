package org.jahia.services.render.scripting.thymeleaf.core.jcr.node;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedItemDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.jcr.AbstractJCRService;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.i18n.ResourceBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by smomin on 2/22/16.
 */
public class JCRPropertyInitializerService extends AbstractJCRService {
    private final static Logger LOGGER = LoggerFactory.getLogger(JCRPropertyInitializerService.class);
    private JCRNodeWrapper node;
    private String nodeType;
    private String name;
    private String initializers;

    /**
     * @param renderContext
     * @param currentResource
     * @param languageCode
     * @param node
     * @param nodeType
     * @param name
     * @param initializers
     */
    public JCRPropertyInitializerService(final RenderContext renderContext,
                                         final Resource currentResource,
                                         final String languageCode,
                                         final JCRNodeWrapper node,
                                         final String nodeType,
                                         final String name,
                                         final String initializers) {
        super(renderContext, currentResource, languageCode);
        this.node = node;
        this.nodeType = nodeType;
        this.name = name;
        this.initializers = initializers;
    }

    @Override
    public Object doProcess() {
        try {
            ExtendedNodeType type = null;
            if (nodeType != null) {
                type = NodeTypeRegistry.getInstance().getNodeType(nodeType);
            } else if (node != null) {
                type = node.getPrimaryNodeType();
            }
            if (type != null) {
                final List<ExtendedItemDefinition> extendedItemDefinitionList = type.getItems();
                for (final ExtendedItemDefinition definition : extendedItemDefinitionList) {
                    if (definition.getName().equals(name)) {
                        final Map<String, String> map;
                        if (initializers == null) {
                            map = definition.getSelectorOptions();
                        } else {
                            map = new LinkedHashMap<String, String>();
                            final String[] strings = Patterns.COMMA.split(initializers);
                            for (final String string : strings) {
                                map.put(string, "");
                            }
                        }
                        if (map.size() > 0) {
                            List<ChoiceListValue> listValues = null;
                            final Map<String, ChoiceListInitializer> initializers = ChoiceListInitializerService
                                    .getInstance().getInitializers();
                            final HashMap<String, Object> context = new HashMap<String, Object>();
                            context.put("contextNode", node);
                            for (final Map.Entry<String, String> entry : map.entrySet()) {
                                if (initializers.containsKey(entry.getKey())) {
                                    listValues = initializers.get(entry.getKey()).getChoiceListValues(
                                            (ExtendedPropertyDefinition) definition, entry.getValue(), listValues,
                                            renderContext.getMainResourceLocale(), context
                                    );
                                }
                            }
                            if (listValues != null) {
                                return listValues;
                            }
                        } else if (definition instanceof ExtendedPropertyDefinition) {
                            final JahiaTemplatesPackage pkg = definition
                                    .getDeclaringNodeType().getTemplatePackage();
                            final ResourceBundle rb = ResourceBundles.get(pkg != null ? pkg : ServicesRegistry
                                    .getInstance().getJahiaTemplateManagerService().getTemplatePackageById("default"),
                                    renderContext.getMainResourceLocale());
                            final ExtendedPropertyDefinition propertyDefinition = (ExtendedPropertyDefinition) definition;
                            final List<ChoiceListValue> listValues = new ArrayList<ChoiceListValue>();
                            final String resourceBundleKey = definition.getResourceBundleKey();
                            for (final String value : propertyDefinition.getValueConstraints()) {
                                final String display = Messages.get(rb, resourceBundleKey + "."
                                        + JCRContentUtils.replaceColon(value), value);
                                listValues.add(new ChoiceListValue(display, null, new ValueImpl(value,
                                        propertyDefinition.getRequiredType())));
                            }
                            return listValues;
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
