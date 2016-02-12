package org.jahia.services.render.scripting.thymeleaf.support.dialect;

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

import java.util.HashSet;
import java.util.Set;

/**
 * Created by smomin on 2/9/16.
 */
public class TemplateDialect extends AbstractDialect {

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
}
