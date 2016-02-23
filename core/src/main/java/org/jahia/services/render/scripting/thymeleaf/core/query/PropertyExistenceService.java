package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.query.qom.Constraint;

/**
 * Created by smomin on 2/22/16.
 */
public class PropertyExistenceService extends ConstraintService {

    private String selectorName;
    private String propertyName;

    public PropertyExistenceService(final RenderContext renderContext, final Resource currentResource, final String languageCode, final String qomBeanName, final String statement, final long limit, final long offset, final String selectorName) {
        super(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset, selectorName);
    }

    public PropertyExistenceService(final RenderContext renderContext, final Resource currentResource, final String languageCode, final Object qom, final String qomBeanName, final String statement, final long limit, final long offset, final String selectorName) {
        super(renderContext, currentResource, languageCode, qom, qomBeanName, statement, limit, offset, selectorName);
    }


    @Override
    protected Constraint getConstraint() throws Exception {
        return null;
    }
}
