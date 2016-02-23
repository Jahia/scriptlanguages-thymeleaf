package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.Source;

/**
 * Created by smomin on 2/22/16.
 */
public abstract class QOMBuildingService extends QueryDefinitionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QOMBuildingService.class);
    protected String selectorName;

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
    public QOMBuildingService(final RenderContext renderContext,
                              final Resource currentResource,
                              final String languageCode,
                              final String qomBeanName,
                              final String statement,
                              final long limit,
                              final long offset,
                              final String selectorName) {
        super(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset);
        this.selectorName = selectorName;
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
    public QOMBuildingService(final RenderContext renderContext,
                              final Resource currentResource,
                              final String languageCode,
                              final Object qom,
                              final String qomBeanName,
                              final String statement,
                              final long limit,
                              final long offset,
                              final String selectorName) {
        super(renderContext, currentResource, languageCode, qom, qomBeanName, statement, limit, offset);
        this.selectorName = selectorName;
    }

    /**
     * Returns a <code>QueryObjectModelFactory</code> with which a JCR-JQOM
     * query can be built programmatically.
     *
     * @return a <code>QueryObjectModelFactory</code> object
     */
    protected final QueryObjectModelFactory getQOMFactory() {
        return getQOMBuilder().getQOMFactory();
    }

    protected String getSelectorName() {
        if (selectorName == null) {
            final Source source = getQOMBuilder().getSource();
            if (source != null) {
                if (source instanceof Selector) {
                    selectorName = ((Selector) source).getSelectorName();
                } else {
                    throw new IllegalAccessError(
                            "Need to specify the selector name because the query contains more than one selector.");
                }
            }
        }
        return selectorName;
    }
}
