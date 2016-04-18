package org.jahia.services.render.scripting.thymeleaf.dialect;

import org.jahia.services.render.scripting.thymeleaf.template.include.AddCacheDependencyElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.template.include.AddResourcesElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.template.include.AddWrapperElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.template.include.AreaElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.template.include.IncludeElementProcessor;
import org.jahia.services.render.scripting.thymeleaf.template.include.ModuleElementProcessor;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;

import java.util.HashSet;
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
        final Set<IProcessor> processors = new HashSet<IProcessor>(6);
        processors.add(new AreaElementProcessor());
        processors.add(new ModuleElementProcessor());
        processors.add(new AddCacheDependencyElementProcessor());
        processors.add(new AddResourcesElementProcessor());
        processors.add(new AddWrapperElementProcessor());
        processors.add(new IncludeElementProcessor());
        
        return processors;
    }
}
