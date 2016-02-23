package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.Constraint;

/**
 * Created by smomin on 2/22/16.
 */
public abstract class ConstraintService extends QOMBuildingService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConstraintService.class);

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
    public ConstraintService(final RenderContext renderContext,
                             final Resource currentResource,
                             final String languageCode,
                             final String qomBeanName,
                             final String statement,
                             final long limit,
                             final long offset,
                             final String selectorName) {
        super(renderContext, currentResource, languageCode, qomBeanName,
                statement, limit, offset, selectorName);
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
    public ConstraintService(final RenderContext renderContext,
                             final Resource currentResource,
                             final String languageCode,
                             final Object qom,
                             final String qomBeanName,
                             final String statement,
                             final long limit,
                             final long offset,
                             final String selectorName) {
        super(renderContext, currentResource, languageCode, qom, qomBeanName,
                statement, limit, offset, selectorName);
    }

    /**
     *
     * @return
     */
    @Override
    public Object doProcess() {
        try {
            getQOMBuilder().andConstraint(getConstraint());
        } catch (InvalidQueryException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    protected abstract Constraint getConstraint() throws Exception;
}
