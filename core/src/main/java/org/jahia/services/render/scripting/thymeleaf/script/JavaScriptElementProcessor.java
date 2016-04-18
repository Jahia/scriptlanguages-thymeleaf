package org.jahia.services.render.scripting.thymeleaf.script;

import org.jahia.services.render.scripting.thymeleaf.ThymeLeafContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.VariablesMap;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.dom.Text;
import org.thymeleaf.processor.element.AbstractUnescapedTextChildModifierElementProcessor;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import java.util.List;

/**
 * Created by smomin on 4/15/16.
 */
public class JavaScriptElementProcessor extends AbstractUnescapedTextChildModifierElementProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptElementProcessor.class);
    private static final String SCRIPT = "script";
    private static final String JS_SCRIPT_ENGINE = "jsScriptEngine";

    public JavaScriptElementProcessor() {
        super(SCRIPT);
    }

    @Override
    public int getPrecedence() {
        return 5000;
    }

    @Override
    protected String getText(final Arguments arguments, final Element element) {
        if (element.hasChildren()) {
            final ThymeLeafContext context = (ThymeLeafContext) arguments.getContext();
            final ScriptEngine engine = getScriptEngine(context);
            final List<Node> children = element.getChildren();
            for (final Node child : children) {
                if (child instanceof Text) {
                    processChildContent((Text) child, engine);
                    storeNashornVariables(engine);
                }
            }
        }
        return "";
    }

    /**
     * Add code to set the nashorn global in a new key that doesn't have a special char.
     *
     * @param engine
     */
    private void storeNashornVariables(final ScriptEngine engine) {
        final ScriptContext scriptContext = engine.getContext();
        if (scriptContext.getAttribute("nashorn.global") != null) {
            scriptContext.setAttribute("nashornGlobal",
                    scriptContext.getAttribute("nashorn.global"),
                    ScriptContext.ENGINE_SCOPE);
        }
    }

    /**
     * Process the content of the dx:script element
     *
     * @param child
     * @param engine
     */
    private void processChildContent(final Text child, final ScriptEngine engine) {
        try {
            engine.eval(child.getContent());
        } catch (ScriptException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Try to use the same script engine to persist variables to create a global space.
     * @param context
     * @return
     */
    private ScriptEngine getScriptEngine(final ThymeLeafContext context) {
        final ScriptEngine engine;
        final VariablesMap<String, Object> variables = context.getVariables();
        if (variables.get(JS_SCRIPT_ENGINE) == null) {
            final Bindings engineBindings = new SimpleBindings(variables);
            final ScriptContext scriptContext = new SimpleScriptContext();
            scriptContext.setBindings(engineBindings, ScriptContext.ENGINE_SCOPE);
            engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.setContext(scriptContext);
            variables.put(JS_SCRIPT_ENGINE, engine);
        } else {
            engine = (ScriptEngine) variables.get(JS_SCRIPT_ENGINE);
        }
        return engine;
    }
}
