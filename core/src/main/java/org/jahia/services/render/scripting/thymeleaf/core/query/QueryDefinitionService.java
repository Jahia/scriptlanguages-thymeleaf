package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.query.QueryObjectModelBuilder;
import org.apache.jackrabbit.commons.query.QueryObjectModelBuilderRegistry;
import org.jahia.services.query.QOMBuilder;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.jcr.AbstractJCRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;

/**
 * Created by smomin on 2/22/16.
 */
public class QueryDefinitionService extends AbstractJCRService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDefinitionService.class);
    private QOMBuilder qomBuilder;
    private QueryObjectModel queryObjectModel;

    private Object qom;
    private String qomBeanName;
    private String statement;
    private long limit;
    private long offset;

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param languageCode
     * @param qomBeanName
     * @param statement
     * @param limit
     * @param offset
     */
    public QueryDefinitionService(final RenderContext renderContext,
                                  final Resource currentResource,
                                  final String languageCode,
                                  final String qomBeanName,
                                  final String statement,
                                  final long limit,
                                  final long offset) {
        super(renderContext, currentResource, languageCode);
        this.qomBeanName = qomBeanName;
        this.statement = statement;
        this.limit = limit;
        this.offset = offset;
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
     */
    public QueryDefinitionService(final RenderContext renderContext,
                                  final Resource currentResource,
                                  final String languageCode,
                                  final Object qom,
                                  final String qomBeanName,
                                  final String statement,
                                  final long limit,
                                  final long offset) {
        this(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset);
        this.qom = qom;
    }

    @Override
    public void init() {

    }

    @Override
    public Object doProcess() {
        try {
            return getQueryObjectModel();
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns current QOM builder instance.
     *
     * @return an instance of current {@link QOMBuilder}
     */
    protected QOMBuilder getQOMBuilder() {
        if (qomBuilder == null) {
            try {
                qomBuilder = new QOMBuilder(getJCRSession()
                        .getWorkspace().getQueryManager().getQOMFactory(), getJCRSession().getValueFactory());

                final QueryObjectModel qom = getInitialQueryObjectModel();
                if (qom != null) {
                    qomBuilder.setSource(qom.getSource());
                    for (final Column column : qom.getColumns()) {
                        qomBuilder.getColumns().add(column);
                    }
                    qomBuilder.andConstraint(qom.getConstraint());
                    for (final Ordering ordering : qom.getOrderings()) {
                        qomBuilder.getOrderings().add(ordering);
                    }
                }
            } catch (RepositoryException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return qomBuilder;
    }

    /**
     *
     * @return
     * @throws RepositoryException
     */
    protected QueryObjectModel getInitialQueryObjectModel() throws RepositoryException {
        if (qomBeanName != null) {
            return (QueryObjectModel) renderContext.getRequest().getAttribute(qomBeanName);
        } else if (qom != null) {
            return (QueryObjectModel) qom;
        } else if (!StringUtils.isEmpty(statement)) {
            final QueryObjectModelFactory qf = getJCRSession()
                    .getWorkspace().getQueryManager().getQOMFactory();
            final ValueFactory vf = getJCRSession().getValueFactory();
            final QueryObjectModelBuilder builder = QueryObjectModelBuilderRegistry
                    .getQueryObjectModelBuilder(Query.JCR_SQL2);
            return builder.createQueryObjectModel(statement, qf, vf);
        }
        return null;
    }

    /**
     *
     * @return
     * @throws RepositoryException
     */
    protected QueryObjectModel getQueryObjectModel() throws RepositoryException {
        if (queryObjectModel == null) {
            if (qomBuilder != null) {
                queryObjectModel = qomBuilder.createQOM();
            } else {
                final QueryObjectModel initialQOM = getInitialQueryObjectModel();
                if (initialQOM != null) {
                    queryObjectModel = initialQOM;
                }
            }
            if (queryObjectModel != null) {
                if (limit > 0) {
                    queryObjectModel.setLimit(limit);
                }
                if (offset > 0) {
                    queryObjectModel.setOffset(offset);
                }
            }
        }
        return queryObjectModel;
    }
}
