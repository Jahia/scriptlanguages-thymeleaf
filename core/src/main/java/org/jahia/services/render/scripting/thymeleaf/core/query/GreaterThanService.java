package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.List;

/**
 * Created by smomin on 2/22/16.
 */
public class GreaterThanService extends ComparisonService {

    private String operandTypes;
    private String propertyName;
    private String value;


    public GreaterThanService(final RenderContext renderContext, final Resource currentResource, final String languageCode, final String qomBeanName, final String statement, final long limit, final long offset, final String selectorName, final List<OperandType> operandTypes, final String operator, final String propertyName, final String value) {
        super(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset, selectorName, operandTypes, operator, propertyName, value);
    }

    public GreaterThanService(final RenderContext renderContext, final Resource currentResource, final String languageCode, final Object qom, final String qomBeanName, final String statement, final long limit, final long offset, final String selectorName, final List<OperandType> operandTypes, final String operator, final String propertyName, final String value) {
        super(renderContext, currentResource, languageCode, qom, qomBeanName, statement, limit, offset, selectorName, operandTypes, operator, propertyName, value);
    }
}
