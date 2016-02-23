package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * Created by smomin on 2/22/16.
 */
public class ColumnService extends QOMBuildingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColumnService.class);

    private String selectorName;
    private String propertyName;
    private String columnName;

    public ColumnService(final RenderContext renderContext, final Resource currentResource, final String languageCode,
                         final String qomBeanName, final String statement, final long limit, final long offset,
                         final String selectorName, final String selectorName1, final String propertyName,
                         final String columnName) {
        super(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset, selectorName);
        this.selectorName = selectorName1;
        this.propertyName = propertyName;
        this.columnName = columnName;
    }

    public ColumnService(final RenderContext renderContext, final Resource currentResource, final String languageCode,
                         final Object qom, final String qomBeanName, final String statement, final long limit,
                         final long offset, final String selectorName, final String selectorName1,
                         final String propertyName, final String columnName) {
        super(renderContext, currentResource, languageCode, qom, qomBeanName, statement, limit, offset, selectorName);
        this.selectorName = selectorName1;
        this.propertyName = propertyName;
        this.columnName = columnName;
    }

    @Override
    public Object doProcess() {
        try {
            getQOMBuilder().getColumns().add(getQOMFactory().column(getSelectorName(), propertyName, columnName));
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
