package org.jahia.services.render.scripting.thymeleaf.core.jcr.query;

import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.jcr.AbstractJCRService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

/**
 * Created by smomin on 2/22/16.
 */
public class JCRSQLService extends AbstractJCRService {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JCRSQLService.class);

    private String statement;
    private long limit;
    private long offset;
    private boolean useRootUser;

    public JCRSQLService(final RenderContext renderContext, 
                         final Resource currentResource, 
                         final String languageCode, 
                         final String statement,
                         final long limit, 
                         final long offset,
                         final boolean useRootUser) {
        super(renderContext, currentResource, languageCode);
        this.statement = statement;
        this.limit = limit;
        this.offset = offset;
        this.useRootUser = useRootUser;
    }

    @Override
    public Object doProcess() {
        QueryResult result = null;
        JahiaUser userToReset = null;
        try {
            if (useRootUser) {
                userToReset = JCRSessionFactory.getInstance().getCurrentUser();
                JCRSessionFactory.getInstance().setCurrentUser(JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser());
            }
            result = executeQuery(getJCRSession());
        } catch (InvalidQueryException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (userToReset != null)  {
                JCRSessionFactory.getInstance().setCurrentUser(userToReset);
            }
        }
        return result;
    }

    /**
     * Executes the query.
     *
     * @return the QueryResult instance with the results of the query
     * @throws RepositoryException in case of JCR errors
     * @throws InvalidQueryException in case of bad query statement
     */
    private QueryResult executeQuery(final JCRSessionWrapper session)
            throws RepositoryException {
        final long startTime = System.currentTimeMillis();
        final QueryResult queryResult;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Executing " + getQueryLanguage() + " query: " + statement);
        }

        final Query q = session.getWorkspace().getQueryManager().createQuery(statement, getQueryLanguage());
        if (limit > 0) {
            q.setLimit(limit);
        }
        if (offset > 0) {
            q.setOffset(offset);
        }
        // execute query
        queryResult = q.execute();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getQueryLanguage() + " [" + statement + "] executed in " + (System.currentTimeMillis() - startTime) +" ms --> found [" + queryResult.getRows().getSize() + "] values.");
        }

        return queryResult;
    }

    /**
     * Returns the type of the query language.
     *
     * @return the type of the query language
     */
    protected String getQueryLanguage() {
        return Query.JCR_SQL2;
    }
}
