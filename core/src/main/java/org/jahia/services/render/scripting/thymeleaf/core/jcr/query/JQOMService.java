package org.jahia.services.render.scripting.thymeleaf.core.jcr.query;

import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.query.QueryDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.QueryObjectModel;

/**
 * Created by smomin on 2/22/16.
 */
public class JQOMService extends QueryDefinitionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JQOMService.class);

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
    public JQOMService(final RenderContext renderContext,
                       final Resource currentResource,
                       final String languageCode,
                       final String qomBeanName,
                       final String statement,
                       final long limit,
                       final long offset) {
        super(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset);
    }

    @Override
    public void init() {
    }

    @Override
    public Object doProcess() {
        try {
            final QueryObjectModel queryModel = getQueryObjectModel();
            return findQueryResultByQOM(queryModel);
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Executes the query of the provided QueryObjectModel.
     *
     * @param queryModel
     *            a QueryObjectModel to perform the JCR query
     * @return the {@link QueryResult} instance with the results of
     *         the query
     * @throws RepositoryException
     */
    private QueryResult findQueryResultByQOM(final QueryObjectModel queryModel)
            throws RepositoryException {
        QueryResult queryResult = null;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Find node by qom [ " + queryModel.getStatement() + " ]");
        }
        // execute query
        long x = System.currentTimeMillis();
        queryResult = queryModel.execute();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Query {} --> found {} values in {} ms.",
                    new Object[] { queryModel.getStatement(),
                            JCRContentUtils.size(queryResult.getNodes()),
                            System.currentTimeMillis() - x });
        }

        return queryResult;
    }
}
