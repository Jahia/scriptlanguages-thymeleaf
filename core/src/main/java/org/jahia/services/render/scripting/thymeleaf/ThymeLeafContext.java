package org.jahia.services.render.scripting.thymeleaf;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.VariablesMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
import java.util.Locale;

/**
 * Created by loom on 04.05.15.
 */
public class ThymeLeafContext implements IContext {

    private final VariablesMap<String,Object> variables = new VariablesMap<String, Object>();
    private final Locale locale;
    private final RenderContext renderContext;
    private final Resource currentResource;

    public ThymeLeafContext(ScriptContext scriptContext) {
        final Bindings globalBindings = scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE);
        final Bindings engineBindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        variables.putAll(globalBindings);
        variables.putAll(engineBindings);

        locale = (Locale) variables.get("currentLocale");
        currentResource = (Resource) variables.get("currentResource");
        renderContext = (RenderContext) variables.get("renderContext");
    }

    @Override
    public VariablesMap<String, Object> getVariables() {
        return variables;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void addContextExecutionInfo(String s) {

    }

    public RenderContext getRenderContext() {
        return renderContext;
    }

    public Resource getCurrentResource() {
        return currentResource;
    }
}
