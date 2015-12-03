package org.jahia.services.render.scripting.thymeleaf;

import org.apache.commons.io.IOUtils;
import org.thymeleaf.TemplateEngine;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Created by loom on 04.05.15.
 */
class ThymeLeafScriptEngine extends AbstractScriptEngine {

    private ThymeLeafScriptEngineFactory factory;
    private TemplateEngine templateEngine;
    private ThymeLeafResourceResolver thymeLeafResourceResolver;
    private static long invocationCounts = 0;

    public ThymeLeafScriptEngine(ThymeLeafScriptEngineFactory factory, TemplateEngine templateEngine, ThymeLeafResourceResolver thymeLeafResourceResolver) {
        this.factory = factory;
        this.templateEngine = templateEngine;
        this.thymeLeafResourceResolver = thymeLeafResourceResolver;
    }

    public ThymeLeafScriptEngine(Bindings n, ThymeLeafScriptEngineFactory factory, TemplateEngine templateEngine, ThymeLeafResourceResolver thymeLeafResourceResolver) {
        super(n);
        this.factory = factory;
        this.templateEngine = templateEngine;
        this.thymeLeafResourceResolver = thymeLeafResourceResolver;
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        ThymeLeafContext templateContext = new ThymeLeafContext(context);
        invocationCounts++;
        String templateName = "template-" + invocationCounts + System.currentTimeMillis();
        thymeLeafResourceResolver.putScript(templateName, script);
        String result = templateEngine.process(templateName, templateContext);
        try {
            context.getWriter().append(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        thymeLeafResourceResolver.removeScript(templateName);
        return result;
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        ThymeLeafContext templateContext = new ThymeLeafContext(context);
        invocationCounts++;
        String templateName = "template-" + invocationCounts + System.currentTimeMillis();
        StringBuilder script = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(reader);
        try {
            while (bufferedReader.ready()) {
                script.append(bufferedReader.readLine());
                script.append("\n");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
        thymeLeafResourceResolver.putScript(templateName, script.toString());
        String result = templateEngine.process(templateName, templateContext);
        try {
            context.getWriter().append(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        thymeLeafResourceResolver.removeScript(templateName);
        return result;
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }
}
