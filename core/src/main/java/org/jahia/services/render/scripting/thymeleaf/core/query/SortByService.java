package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.PropertyValue;

/**
 * Created by smomin on 2/22/16.
 */
public class SortByService extends QOMBuildingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SortByService.class);
    private String propertyName;
    private String order;

    public SortByService(final RenderContext renderContext, final Resource currentResource, final String languageCode,
                         final String qomBeanName, final String statement, final long limit, final long offset,
                         final String selectorName) {
        super(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset, selectorName);
    }

    public SortByService(final RenderContext renderContext, final Resource currentResource, final String languageCode,
                         final Object qom, final String qomBeanName, final String statement, final long limit,
                         final long offset, final String selectorName) {
        super(renderContext, currentResource, languageCode, qom, qomBeanName, statement, limit, offset, selectorName);
    }

    @Override
    public Object doProcess() {
        try {
            final PropertyValue value = getQOMFactory().propertyValue(getSelectorName(), propertyName);
            getQOMBuilder().getOrderings().add(
                    StringUtils.equalsIgnoreCase("desc", order) ? getQOMFactory().descending(value) : getQOMFactory()
                            .ascending(value));
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
