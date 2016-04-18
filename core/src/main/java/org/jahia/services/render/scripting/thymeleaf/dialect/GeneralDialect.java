package org.jahia.services.render.scripting.thymeleaf.dialect;

import org.jahia.services.render.scripting.thymeleaf.expression.JahiaVariableExpressionEvaluator;
import org.jahia.services.render.scripting.thymeleaf.script.JavaScriptElementProcessor;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.StandardExpressions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by smomin on 2/9/16.
 */
public class GeneralDialect extends AbstractDialect {

    private IStandardVariableExpressionEvaluator variableExpressionEvaluator = null;

    @Override
    public String getPrefix() {
        return "dx";
    }

    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = new HashSet<IProcessor>(1);
        processors.add(new JavaScriptElementProcessor());
        return processors;
    }

    @Override
    public Map<String, Object> getExecutionAttributes() {
        final Map<String,Object> executionAttributes = new HashMap<String, Object>(5, 1.0f);
        executionAttributes.put(
                StandardExpressions.STANDARD_VARIABLE_EXPRESSION_EVALUATOR_ATTRIBUTE_NAME,
                getVariableExpressionEvaluator());
        return executionAttributes;
    }

    private IStandardVariableExpressionEvaluator getVariableExpressionEvaluator() {
        if (this.variableExpressionEvaluator == null) {
            this.variableExpressionEvaluator = JahiaVariableExpressionEvaluator.getInstance();
        }
        return this.variableExpressionEvaluator;
    }
}
