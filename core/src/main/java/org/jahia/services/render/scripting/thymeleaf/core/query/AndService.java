package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.Constraint;

/**
 * Created by smomin on 2/22/16.
 */
public class AndService extends CompoundConstraintService {

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param languageCode
     * @param qomBeanName
     * @param statement
     * @param limit
     * @param offset
     * @param selectorName
     */
    public AndService(final RenderContext renderContext,
                      final Resource currentResource,
                      final String languageCode,
                      final String qomBeanName,
                      final String statement,
                      final long limit,
                      final long offset,
                      final String selectorName) {
        super(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset, selectorName);
    }

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param languageCode
     * @param qom
     * @param qomBeanName
     * @param statement
     * @param limit
     * @param offset
     * @param selectorName
     */
    public AndService(final RenderContext renderContext,
                      final Resource currentResource,
                      final String languageCode,
                      final Object qom,
                      final String qomBeanName,
                      final String statement,
                      final long limit,
                      final long offset,
                      final String selectorName) {
        super(renderContext, currentResource, languageCode, qom, qomBeanName, statement, limit, offset, selectorName);
    }

    /**
     *
     * @param constraint1 the first constraint to use in the logical operation
     * @param constraint2 the first constraint to use in the logical operation
     * @return
     * @throws RepositoryException
     */
    @Override
    protected Constraint doLogic(final Constraint constraint1,
                                 final Constraint constraint2) throws RepositoryException {
        return getQOMFactory().and(constraint1, constraint2);
    }
}
