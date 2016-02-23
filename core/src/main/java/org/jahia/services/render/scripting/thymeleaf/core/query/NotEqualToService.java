package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.List;

/**
 * Created by smomin on 2/22/16.
 */
public class NotEqualToService extends ComparisonService {

    private String operandTypes;
    private String propertyName;
    private String value;

    public NotEqualToService(final RenderContext renderContext, final Resource currentResource, final String languageCode, final String qomBeanName, final String statement, final long limit, final long offset, final String selectorName, final List<OperandType> operandTypes, final String operator, final String propertyName, final String value, final String operandTypes1, final String propertyName1, final String value1) {
        super(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset, selectorName, operandTypes, operator, propertyName, value);
        this.operandTypes = operandTypes1;
        this.propertyName = propertyName1;
        this.value = value1;
    }

    public NotEqualToService(final RenderContext renderContext, final Resource currentResource, final String languageCode, final Object qom, final String qomBeanName, final String statement, final long limit, final long offset, final String selectorName, final List<OperandType> operandTypes, final String operator, final String propertyName, final String value, final String operandTypes1, final String propertyName1, final String value1) {
        super(renderContext, currentResource, languageCode, qom, qomBeanName, statement, limit, offset, selectorName, operandTypes, operator, propertyName, value);
        this.operandTypes = operandTypes1;
        this.propertyName = propertyName1;
        this.value = value1;
    }
}
