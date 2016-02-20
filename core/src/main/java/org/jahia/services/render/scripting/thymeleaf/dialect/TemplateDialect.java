package org.jahia.services.render.scripting.thymeleaf.dialect;

import org.jahia.services.render.scripting.thymeleaf.expression.JahiaVariableExpressionEvaluator;
import org.jahia.services.render.scripting.thymeleaf.include.AddCacheDependencyElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.include.AddResourcesElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.include.AddWrapperElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.include.AreaElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.include.IncludeElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.include.ModuleElementProcessor;
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
public class TemplateDialect extends AbstractDialect {

    private IStandardVariableExpressionEvaluator variableExpressionEvaluator = null;

    @Override
    public String getPrefix() {
        return "dx-template";
    }

    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new AreaElementProcessor());
        processors.add(new ModuleElementProcessor());
        processors.add(new AddCacheDependencyElementProcessor());
        processors.add(new AddResourcesElementProcessor());
        processors.add(new AddWrapperElementProcessor());
        processors.add(new IncludeElementProcessor());
        
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

    public IStandardVariableExpressionEvaluator getVariableExpressionEvaluator() {
        if (this.variableExpressionEvaluator == null) {
            this.variableExpressionEvaluator = JahiaVariableExpressionEvaluator.getInstance();
        }
        return this.variableExpressionEvaluator;
    }
}
