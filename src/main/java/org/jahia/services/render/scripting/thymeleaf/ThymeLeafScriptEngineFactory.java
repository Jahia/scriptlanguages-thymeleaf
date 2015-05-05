package org.jahia.services.render.scripting.thymeleaf;

import org.springframework.beans.factory.InitializingBean;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.TemplateResolver;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by loom on 04.05.15.
 */
public class ThymeLeafScriptEngineFactory implements ScriptEngineFactory, InitializingBean {

    TemplateEngine templateEngine;
    ThymeLeafResourceResolver thymeLeafResourceResolver;

    private List<String> extensions = new ArrayList<String>();
    private List<String> names = new ArrayList<String>();
    private List<String> mimeTypes = new ArrayList<String>();
    private String engineName = "Thymeleaf";
    private String engineVersion = "1.0";
    private String languageName = "thymeleaf";
    private String languageVersion = "2.1.4.RELEASE";

    public ThymeLeafScriptEngineFactory() {
        extensions.add("html");
        names.add("thymeleaf");
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public void setMimeTypes(List<String> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public void setLanguageVersion(String languageVersion) {
        this.languageVersion = languageVersion;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeTemplateEngine();
    }

    @Override
    public String getEngineName() {
        return engineName;
    }

    @Override
    public String getEngineVersion() {
        return engineVersion;
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public String getLanguageName() {
        return languageName;
    }

    @Override
    public String getLanguageVersion() {
        return languageVersion;
    }

    @Override
    public Object getParameter(String key) {
        return null;
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        return null;
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return null;
    }

    @Override
    public String getProgram(String... statements) {
        return null;
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new ThymeLeafScriptEngine(this, templateEngine, thymeLeafResourceResolver);
    }

    private void initializeTemplateEngine() {

        TemplateResolver templateResolver =
                new TemplateResolver();
        thymeLeafResourceResolver = new ThymeLeafResourceResolver();
        templateResolver.setResourceResolver(thymeLeafResourceResolver);
        // XHTML is the default mode, but we set it anyway for better understanding of code
        templateResolver.setTemplateMode("XHTML");
        // Template cache TTL=1h. If not set, entries would be cached until expelled by LRU
        templateResolver.setCacheTTLMs(3600000L);

        templateEngine = new TemplateEngine();
        templateEngine.setMessageResolver(new ThymeLeafMessageResolver());
        templateEngine.setTemplateResolver(templateResolver);

    }

}
