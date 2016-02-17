package org.jahia.services.render.scripting.thymeleaf.support.dialect;

import org.jahia.services.render.scripting.thymeleaf.support.expression.JahiaVariableExpressionEvaluator;
import org.jahia.services.render.scripting.thymeleaf.support.template.AddCacheDependencyElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.AddResourcesElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.AddWrapperElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.AreaElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.CaptchaElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.IncludeElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.InitPagerElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.ModuleElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.NodeAttrProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.OptionElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.ParamElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.RemovePagerElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.TokenizedFormElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.support.template.UrlElementProcessor;
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
        return "template";
    }

    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new AreaElementProcessor());
        processors.add(new ModuleElementProcessor());
        processors.add(new AddCacheDependencyElementProcessor());
        processors.add(new AddResourcesElementProcessor());
        processors.add(new AddWrapperElementProcessor());
        processors.add(new CaptchaElementProcessor());
        processors.add(new IncludeElementProcessor());
        processors.add(new InitPagerElementProcessor());
        processors.add(new OptionElementProcessor());
        processors.add(new ParamElementProcessor());
        processors.add(new RemovePagerElementProcessor());
        processors.add(new TokenizedFormElementProcessor());
        processors.add(new UrlElementProcessor());

        processors.add(new NodeAttrProcessor());
        
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
