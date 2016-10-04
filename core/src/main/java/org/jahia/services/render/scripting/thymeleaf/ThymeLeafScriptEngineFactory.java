package org.jahia.services.render.scripting.thymeleaf;

import org.jahia.services.render.scripting.bundle.Configurable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by loom on 04.05.15.
 */
public class ThymeLeafScriptEngineFactory implements ScriptEngineFactory, Configurable {
    private ServiceTracker<IDialect, IDialect> dialectServiceTracker;
    private TemplateEngine templateEngine;
    private final ThymeLeafResourceResolver thymeLeafResourceResolver = new ThymeLeafResourceResolver();

    private final static List<String> extensions = Collections.singletonList("html");
    private final static List<String> names = Collections.singletonList("thymeleaf");
    private final static List<String> mimeTypes = Collections.emptyList();
    private Long templateCacheTTL = 3600000L;
    private String templateMode = "XHTML";
    private final TemplateResolver templateResolver = new TemplateResolver();
    private final IMessageResolver messageResolver = new ThymeLeafMessageResolver();
    private final Map<String, IDialect> additionalDialects = new HashMap<>();

    public ThymeLeafScriptEngineFactory() {
        templateResolver.setResourceResolver(thymeLeafResourceResolver);
        // XHTML is the default mode, but we set it anyway for better understanding of code
        templateResolver.setTemplateMode(templateMode);
        // Template cache TTL=1h. If not set, entries would be cached until expelled by LRU
        templateResolver.setCacheTTLMs(templateCacheTTL);

        initializeTemplateEngine();
    }

    @Override
    public String getEngineName() {
        return "Thymeleaf";
    }

    @Override
    public String getEngineVersion() {
        return "1.0";
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
        return "thymeleaf";
    }

    @Override
    public String getLanguageVersion() {
        return "2.1.4.RELEASE";
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

    void initializeTemplateEngine() {
        templateEngine = new TemplateEngine();
        if (additionalDialects.size() > 0) {
            for (IDialect additionalDialect : additionalDialects.values()) {
                templateEngine.addDialect(additionalDialect);
            }
        }
        templateEngine.setMessageResolver(messageResolver);
        templateEngine.setTemplateResolver(templateResolver);

    }


    public void setTemplateCacheTTL(Long templateCacheTTL) {
        this.templateCacheTTL = templateCacheTTL;
    }

    public void setTemplateMode(String templateMode) {
        this.templateMode = templateMode;
    }

    public void addDialect(IDialect dialect) {
        additionalDialects.put(dialect.getPrefix(), dialect);
    }

    public void removeDialect(String dialectPrefix) {
        additionalDialects.remove(dialectPrefix);
    }

    @Override
    public void configurePreRegistration(Bundle bundle) {
        final BundleContext bundleContext = bundle.getBundleContext();

        ServiceTrackerCustomizer<IDialect, IDialect> serviceTrackerCustomizer = new ServiceTrackerCustomizer<IDialect, IDialect>() {
            @Override
            public IDialect addingService(ServiceReference reference) {
                IDialect addedDialect = (IDialect) bundleContext.getService(reference);
                addDialect(addedDialect);
                initializeTemplateEngine();
                return addedDialect;
            }

            @Override
            public void modifiedService(ServiceReference reference, IDialect service) {

            }

            @Override
            public void removedService(ServiceReference reference, IDialect service) {
                if (service != null) {
                    removeDialect(service.getPrefix());
                }
                initializeTemplateEngine();
            }
        };

        dialectServiceTracker = new ServiceTracker<>(bundleContext, IDialect.class, serviceTrackerCustomizer);
        dialectServiceTracker.open();
    }

    @Override
    public void destroy(Bundle bundle) {
        dialectServiceTracker.close();
    }

    @Override
    public void configurePreScriptEngineCreation() {

    }
}
