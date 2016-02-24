package org.jahia.services.render.scripting.thymeleaf.core.template.include;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.core.ScriptingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by smomin on 2/19/16.
 */
public class OptionService implements ScriptingService {
    private final static Logger LOGGER = LoggerFactory.getLogger(OptionService.class);

    private RenderContext renderContext;
    private Resource currentResource;
    private String nodetype;
    private JCRNodeWrapper node;
    private String view;
    private Map<String, String> parameters = new HashMap<String, String>();

    private HttpServletRequest request;
    private HttpServletResponse response;

    public OptionService(final RenderContext renderContext,
                         final Resource currentResource,
                         final JCRNodeWrapper node,
                         final String nodetype,
                         final String view,
                         final Map<String, String> parameters) {
        this.renderContext = renderContext;
        this.currentResource = currentResource;
        this.node = node;
        this.nodetype = nodetype;
        this.view = view;
        this.parameters = parameters;

        request = renderContext.getRequest();
        response = renderContext.getResponse();
    }

    @Override
    public String doProcess() {
        final StringBuilder out = new StringBuilder();
        final String charset = response.getCharacterEncoding();
        final String[] nodeTypes = StringUtils.split(nodetype, ",");

        try {
            if (nodeTypes.length > 0) {
                final String primaryNodeType = nodeTypes[0];
                if (node.isNodeType(primaryNodeType)) {
                    ExtendedNodeType mixinNodeType = NodeTypeRegistry.getInstance().getNodeType(primaryNodeType);

                    // what is this for? This doesn't seem to be used anywhere else in the code
                    if (request.getAttribute(ScriptingConstants.ATTR_OPTIONS_AUTO_RENDERING) == null) {
                        currentResource.removeOption(mixinNodeType);
                    }

                    // create a resource to render the current node with the specified view
                    final Resource wrappedResource = new Resource(node,
                            currentResource.getTemplateType(), view, Resource.CONFIGURATION_INCLUDE);
                    wrappedResource.setResourceNodeType(mixinNodeType);

                    // set parameters
                    for (final Map.Entry<String, String> param : parameters.entrySet()) {
                        wrappedResource.getModuleParams().put(URLDecoder.decode(param.getKey(), charset),
                                URLDecoder.decode(param.getValue(), charset));
                    }

                    // attempt to resolve script for the newly created resource
                    Script script = null;
                    try {
                        script = RenderService.getInstance().resolveScript(wrappedResource, renderContext);
                    } catch (RepositoryException e) {
                        LOGGER.error(e.getMessage(), e);
                    } catch (TemplateNotFoundException e) {
                        // if we didn't find a script, attempt to locate one based on secondary node type if one was specified
                        if (nodeTypes.length > 1) {
                            mixinNodeType = NodeTypeRegistry.getInstance().getNodeType(nodeTypes[1]);
                            wrappedResource.setResourceNodeType(mixinNodeType);
                            script = RenderService.getInstance().resolveScript(wrappedResource, renderContext);
                        }
                    }

                    // if we have found a script, render it
                    if (script != null) {
                        //save environment
                        final Object currentNode = request.getAttribute(ScriptingConstants.ATTR_CURRENT_NODE);
                        final Resource currentOption = (Resource) request.getAttribute(ScriptingConstants.ATTR_OPTION_RESOURCE);

                        // set attributes to render the newly created resource
                        request.setAttribute(ScriptingConstants.ATTR_OPTION_RESOURCE, currentResource);
                        request.setAttribute(ScriptingConstants.ATTR_CURRENT_NODE, node);
                        request.setAttribute(ScriptingConstants.ATTR_CURRENT_RESOURCE, wrappedResource);
                        try {
                            out.append(script.execute(wrappedResource, renderContext));
                        } finally {
                            // restore environment as it previously was
                            request.setAttribute(ScriptingConstants.ATTR_OPTION_RESOURCE, currentOption);
                            request.setAttribute(ScriptingConstants.ATTR_CURRENT_NODE, currentNode);
                            request.setAttribute(ScriptingConstants.ATTR_CURRENT_RESOURCE, currentResource);
                        }
                    }
                }
            }
        } catch (NoSuchNodeTypeException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (RenderException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return out.toString();
    }
}
