package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * Created by smomin on 2/22/16.
 */
public class SelectorService extends QOMBuildingService {

    private String nodeTypeName;
    private String selectorName;

    public SelectorService(final RenderContext renderContext, final Resource currentResource, final String languageCode, final String qomBeanName, final String statement, final long limit, final long offset, final String selectorName, final String nodeTypeName, final String selectorName1) {
        super(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset, selectorName);
        this.nodeTypeName = nodeTypeName;
        this.selectorName = selectorName1;
    }

    public SelectorService(final RenderContext renderContext, final Resource currentResource, final String languageCode, final Object qom, final String qomBeanName, final String statement, final long limit, final long offset, final String selectorName, final String nodeTypeName, final String selectorName1) {
        super(renderContext, currentResource, languageCode, qom, qomBeanName, statement, limit, offset, selectorName);
        this.nodeTypeName = nodeTypeName;
        this.selectorName = selectorName1;
    }
}
