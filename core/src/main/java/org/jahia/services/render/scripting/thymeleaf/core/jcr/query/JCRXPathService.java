package org.jahia.services.render.scripting.thymeleaf.core.jcr.query;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.query.Query;

/**
 * Created by smomin on 2/22/16.
 */
public class JCRXPathService extends JCRSQLService {

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param languageCode
     * @param statement
     * @param limit
     * @param offset
     * @param useRootUser
     */
    public JCRXPathService(final RenderContext renderContext,
                           final Resource currentResource,
                           final String languageCode,
                           final String statement,
                           final long limit,
                           final long offset,
                           final boolean useRootUser) {
        super(renderContext, currentResource, languageCode, statement, limit, offset, useRootUser);
    }

    @Override
    protected String getQueryLanguage() {
        return Query.XPATH;
    }
}
