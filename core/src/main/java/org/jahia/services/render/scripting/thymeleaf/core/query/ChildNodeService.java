package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.Constraint;

/**
 * Created by smomin on 2/22/16.
 */
public class ChildNodeService extends ConstraintService {

    private String selectorName;
    private String path;

    public ChildNodeService(final RenderContext renderContext, final Resource currentResource, final String languageCode, final String qomBeanName, final String statement, final long limit, final long offset, final String selectorName, final String selectorName1, final String path) {
        super(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset, selectorName);
        this.selectorName = selectorName1;
        this.path = path;
    }

    public ChildNodeService(final RenderContext renderContext, final Resource currentResource, final String languageCode, final Object qom, final String qomBeanName, final String statement, final long limit, final long offset, final String selectorName, final String selectorName1, final String path) {
        super(renderContext, currentResource, languageCode, qom, qomBeanName, statement, limit, offset, selectorName);
        this.selectorName = selectorName1;
        this.path = path;
    }

    @Override
    public Constraint getConstraint() throws RepositoryException {
        return path != null ? getQOMFactory().childNode(getSelectorName(), path) : null;
    }
}
