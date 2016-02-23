package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.Constraint;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by smomin on 2/22/16.
 */
public abstract class CompoundConstraintService extends ConstraintService {

    private List<Constraint> constraints = new LinkedList<Constraint>();

    public CompoundConstraintService(final RenderContext renderContext, final Resource currentResource,
                                     final String languageCode, final String qomBeanName, final String statement,
                                     final long limit, final long offset, final String selectorName) {
        super(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset, selectorName);
    }

    public CompoundConstraintService(final RenderContext renderContext, final Resource currentResource,
                                     final String languageCode, final Object qom, final String qomBeanName,
                                     final String statement, final long limit, final long offset,
                                     final String selectorName) {
        super(renderContext, currentResource, languageCode, qom, qomBeanName, statement, limit, offset, selectorName);
    }


    /**
     * Performs the conjunction/disjunction of the provided constraints.
     *
     * @param constraint1 the first constraint to use in the logical operation
     * @param constraint2 the first constraint to use in the logical operation
     * @return the resulting constraint
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    protected abstract Constraint doLogic(final Constraint constraint1,
                                          final Constraint constraint2) throws RepositoryException;

    @Override
    protected Constraint getConstraint() throws Exception {
        Constraint compoundConstraint = null;
        if (!constraints.isEmpty()) {
            if (constraints.size() == 1) {
                compoundConstraint = constraints.get(0);
            } else {
                for (Constraint constraint : constraints) {
                    compoundConstraint = compoundConstraint != null ? doLogic(compoundConstraint,
                            constraint) : constraint;
                }
            }
        }
        return compoundConstraint;
    }

    public final void addConstraint(final Constraint constraint) {
        constraints.add(constraint);
    }
}
