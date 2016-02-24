package org.jahia.services.render.scripting.thymeleaf.core.template.include;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.ScriptingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * Created by smomin on 2/17/16.
 */
public class AddCacheDependencyService implements ScriptingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddCacheDependencyService.class);

    private Resource currentResource;
    private Resource optionResource;
    private JCRNodeWrapper node;
    private String stringDependency;
    private String flushOnPathMatchingRegexp;

    public AddCacheDependencyService(final Resource currentResource,
                                     final Resource optionResource,
                                     final JCRNodeWrapper node) {
        this(currentResource, optionResource);
        this.node = node;
    }

    public AddCacheDependencyService(final Resource currentResource,
                                     final Resource optionResource,
                                     final String flushOnPathMatchingRegexp,
                                     final String stringDependency,
                                     final String uuid) {
        this(currentResource, optionResource);
        this.flushOnPathMatchingRegexp = flushOnPathMatchingRegexp;
        setPath(stringDependency);
        setUuid(uuid);
    }


    public AddCacheDependencyService(final Resource currentResource,
                                     final Resource optionResource) {
        this.currentResource = currentResource;
        this.optionResource = optionResource;
    }

    /**
     *
     * @return
     */
    @Override
    public String doProcess() {
        addDependency(currentResource);
        if (optionResource != null) {
            addDependency(optionResource);
        }
        return "";
    }

    /**
     *
     * @param uuid
     */
    private void setUuid(final String uuid) {
        if (!StringUtils.isEmpty(uuid)) {
            try {
                setPath(currentResource.getNode().getSession().getNodeByIdentifier(uuid).getPath());
            } catch (RepositoryException e) {
                LOGGER.warn(e.getMessage());
                this.stringDependency = uuid;
            }
        }
    }

    /**
     *
     * @param path
     */
    private void setPath(final String path) {
        if (!StringUtils.isEmpty(path)) {
            if (path.endsWith("/")) {
                this.stringDependency = StringUtils.substringBeforeLast(path, "/");
            } else {
                this.stringDependency = path;
            }
        }
    }

    /**
     *
     * @param resource
     */
    private void addDependency(final Resource resource) {
        if (node != null) {
            resource.getDependencies().add(node.getCanonicalPath());
        } else if (stringDependency != null) {
            resource.getDependencies().add(stringDependency);
        } else if(flushOnPathMatchingRegexp != null) {
            resource.getRegexpDependencies().add(flushOnPathMatchingRegexp);
        }
    }
}
