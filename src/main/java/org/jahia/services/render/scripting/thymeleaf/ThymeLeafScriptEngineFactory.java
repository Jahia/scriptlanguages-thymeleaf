package org.jahia.services.render.scripting.thymeleaf;

import org.springframework.beans.factory.InitializingBean;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.*;

/**
 * Created by loom on 04.05.15.
 */
public class ThymeLeafScriptEngineFactory implements ScriptEngineFactory, InitializingBean {

    TemplateEngine templateEngine;
    ThymeLeafResourceResolver thymeLeafResourceResolver =  new ThymeLeafResourceResolver();

    private List<String> extensions = new ArrayList<String>();
    private List<String> names = new ArrayList<String>();
    private List<String> mimeTypes = new ArrayList<String>();
    private String engineName = "Thymeleaf";
    private String engineVersion = "1.0";
    private String languageName = "thymeleaf";
    private String languageVersion = "2.1.4.RELEASE";
    private Long templateCacheTTL = 3600000L;
    private String templateMode = "XHTML";
    private TemplateResolver templateResolver = new TemplateResolver();
    private IMessageResolver messageResolver = new ThymeLeafMessageResolver();
    private Map<String,IDialect> additionalDialects = new HashMap<String,IDialect>();

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

    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void setThymeLeafResourceResolver(ThymeLeafResourceResolver thymeLeafResourceResolver) {
        this.thymeLeafResourceResolver = thymeLeafResourceResolver;
    }

    public void setTemplateCacheTTL(Long templateCacheTTL) {
        this.templateCacheTTL = templateCacheTTL;
    }

    public void setTemplateMode(String templateMode) {
        this.templateMode = templateMode;
    }

    public void setTemplateResolver(TemplateResolver templateResolver) {
        this.templateResolver = templateResolver;
    }

    public void setMessageResolver(IMessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    public void setAdditionalDialects(Map<String, IDialect> additionalDialects) {
        this.additionalDialects = additionalDialects;
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public ThymeLeafResourceResolver getThymeLeafResourceResolver() {
        return thymeLeafResourceResolver;
    }

    public Long getTemplateCacheTTL() {
        return templateCacheTTL;
    }

    public String getTemplateMode() {
        return templateMode;
    }

    public TemplateResolver getTemplateResolver() {
        return templateResolver;
    }

    public IMessageResolver getMessageResolver() {
        return messageResolver;
    }

    public Map<String, IDialect> getAdditionalDialects() {
        return additionalDialects;
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

    public void initializeTemplateEngine() {

        templateResolver.setResourceResolver(thymeLeafResourceResolver);
        // XHTML is the default mode, but we set it anyway for better understanding of code
        templateResolver.setTemplateMode(templateMode);
        // Template cache TTL=1h. If not set, entries would be cached until expelled by LRU
        templateResolver.setCacheTTLMs(templateCacheTTL);

        templateEngine = new TemplateEngine();
        if (additionalDialects.size() > 0) {
            for (IDialect additionalDialect : additionalDialects.values()) {
                templateEngine.addDialect(additionalDialect);
            }
        }
        templateEngine.setMessageResolver(messageResolver);
        templateEngine.setTemplateResolver(templateResolver);

    }

}
