package org.jahia.services.render.scripting.thymeleaf.core.jcr.node;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.renderer.ChoiceListRenderer;
import org.jahia.services.content.nodetypes.renderer.ChoiceListRendererService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.jcr.AbstractJCRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by smomin on 2/22/16.
 */
public class JCRPropertyCustomRendererService extends AbstractJCRService {
    private final static Logger LOGGER = LoggerFactory.getLogger(JCRPropertyCustomRendererService.class);
    private JCRNodeWrapper node;
    private String name;
    private String renderer;

    /**
     * @param renderContext
     * @param currentResource
     * @param languageCode
     * @param node
     * @param name
     * @param renderer
     */
    public JCRPropertyCustomRendererService(final RenderContext renderContext,
                                            final Resource currentResource,
                                            final String languageCode,
                                            final JCRNodeWrapper node,
                                            final String name,
                                            final String renderer) {
        super(renderContext, currentResource, languageCode);
        this.node = node;
        this.name = name;
        this.renderer = renderer;
    }

    /**
     *
     */
    @Override
    public void init() {

    }

    /**
     * @return
     */
    @Override
    public Object doProcess() {
        try {
            final Property property = node.getProperty(name);
            if (property != null) {
                boolean isMultiple = property.getDefinition().isMultiple();
                if (!"".equals(renderer)) {
                    final ChoiceListRenderer renderer1 = ChoiceListRendererService.getInstance()
                            .getRenderers().get(renderer);
                    final List<Map<String, Object>> rendererList = new ArrayList<Map<String, Object>>();
                    final Map<String, Object> rendererMap = new HashMap<String, Object>();
                    if (isMultiple) {
                        for (final Value v : property.getValues()) {
                            rendererList.add(renderer1.getObjectRendering(renderContext,
                                    (ExtendedPropertyDefinition) property.getDefinition(), v.getString()));
                        }
                    } else {
                        rendererMap.putAll(renderer1.getObjectRendering(renderContext, (JCRPropertyWrapper) property));
                    }
                    return isMultiple ? rendererList : rendererMap;
                } else {
                    if (isMultiple) {
                        return property.getValues();
                    } else {
                        return property.getValues();
                    }
                }
            }
        } catch (PathNotFoundException e) {
            LOGGER.debug("Property : " + name + " not found in node " + node.getPath());
        } catch (ConstraintViolationException e) {
            LOGGER.warn("Property : " + name + " not defined in node " + node.getPath());
        } catch (ValueFormatException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
