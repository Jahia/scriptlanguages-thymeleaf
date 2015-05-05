package org.jahia.services.render.scripting.thymeleaf;

import org.thymeleaf.context.IContext;
import org.thymeleaf.context.VariablesMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
import java.util.Locale;
import java.util.Map;

/**
 * Created by loom on 04.05.15.
 */
public class ThymeLeafContext implements IContext {

    VariablesMap<String,Object> variables = new VariablesMap<String, Object>();
    Locale locale;

    public ThymeLeafContext(ScriptContext scriptContext) {
        Bindings globalBindings = scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE);
        Bindings engineBindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        variables.putAll(globalBindings);
        variables.putAll(engineBindings);
        locale = (Locale) variables.get("currentLocale");
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
}
