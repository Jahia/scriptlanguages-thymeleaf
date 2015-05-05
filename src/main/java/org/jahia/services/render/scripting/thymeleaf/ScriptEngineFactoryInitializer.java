package org.jahia.services.render.scripting.thymeleaf;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.services.render.scripting.ScriptFactory;
import org.jahia.services.render.scripting.bundle.BundleJSR223ScriptFactory;
import org.jahia.services.render.scripting.bundle.BundleScriptResolver;
import org.jahia.utils.ScriptEngineUtils;
import org.ops4j.pax.swissbox.extender.BundleURLScanner;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.InitializingBean;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * This class is mostly a hack to register the script engine into the global ScriptEngineManager. Also it doesn't support
 * unregistration since the ScriptEngineManager doesn't support it.
 *
 * Possibly a better solution would be to implement inside Jahia's core the following use of the OsgiScriptEngineManager
 * as illustrated here :
 * http://svn.apache.org/repos/asf/felix/trunk/mishell/src/main/java/org/apache/felix/mishell/OSGiScriptEngineManager.java
 * but this requires modifying the ScriptEngineUtils class inside Jahia.
 *
 * Also, this hack doesn't support unregistering the ScriptEngineFactory so it is not clear what will happen if we
 * re-register the same factory multiple times. We should also de-register it normally when the module is stopped.
 */
public class ScriptEngineFactoryInitializer implements InitializingBean, BundleContextAware {

    ScriptEngineFactory scriptEngineFactory;
    BundleScriptResolver bundleScriptResolver;
    BundleJSR223ScriptFactory bundleJSR223ScriptFactory;
    BundleContext bundleContext;

    public void setScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
        this.scriptEngineFactory = scriptEngineFactory;
    }

    public void setBundleScriptResolver(BundleScriptResolver bundleScriptResolver) {
        this.bundleScriptResolver = bundleScriptResolver;
    }

    public void setBundleJSR223ScriptFactory(BundleJSR223ScriptFactory bundleJSR223ScriptFactory) {
        this.bundleJSR223ScriptFactory = bundleJSR223ScriptFactory;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ScriptEngineUtils scriptEngineUtils = ScriptEngineUtils.getInstance();
        Field scriptEngineManagerField = scriptEngineUtils.getClass().getDeclaredField("scriptEngineManager");
        scriptEngineManagerField.setAccessible(true);
        ScriptEngineManager scriptEngineManager = (ScriptEngineManager) scriptEngineManagerField.get(scriptEngineUtils);
        for (String extension : scriptEngineFactory.getExtensions()) {
            scriptEngineManager.registerEngineExtension(extension, scriptEngineFactory);
        }
        for (String mimeType : scriptEngineFactory.getMimeTypes()) {
            scriptEngineManager.registerEngineMimeType(mimeType, scriptEngineFactory);
        }
        for (String name : scriptEngineFactory.getNames()) {
            scriptEngineManager.registerEngineName(name, scriptEngineFactory);
        }
        if (bundleScriptResolver != null) {
            List<String> scriptExtensionsOrdering = bundleScriptResolver.getScriptExtensionsOrdering();
            scriptExtensionsOrdering.addAll(scriptEngineFactory.getExtensions());
            bundleScriptResolver.setScriptExtensionsOrdering(scriptExtensionsOrdering);
            Field scriptFactoryMapField = bundleScriptResolver.getClass().getDeclaredField("scriptFactoryMap");
            scriptFactoryMapField.setAccessible(true);
            Map<String,ScriptFactory> scriptFactoryMap = (Map<String,ScriptFactory>) scriptFactoryMapField.get(bundleScriptResolver);
            for (String extension : scriptEngineFactory.getExtensions()) {
                scriptFactoryMap.put(extension, bundleJSR223ScriptFactory);
            }

            // now we need to activate the bundle script scanner inside of newly deployed or existing bundles
            // register view script observers
            BundleURLScanner bundleURLScanner = new BundleURLScanner("/", "*.html", true);
            List<URL> scripts = bundleURLScanner.scan(bundleContext.getBundle());
            bundleScriptResolver.addBundleScripts(bundleContext.getBundle(), scripts);
        }
    }

}
