package org.jahia.services.render.scripting.thymeleaf;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.services.render.scripting.ScriptFactory;
import org.jahia.services.render.scripting.bundle.BundleJSR223ScriptFactory;
import org.jahia.services.render.scripting.bundle.BundleScriptResolver;
import org.jahia.utils.ScriptEngineUtils;
import org.ops4j.pax.swissbox.extender.BundleURLScanner;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.thymeleaf.dialect.IDialect;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
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
public class Activator implements InitializingBean, BundleContextAware, DisposableBean, BundleListener {

    ScriptEngineFactory scriptEngineFactory;
    BundleScriptResolver bundleScriptResolver;
    BundleJSR223ScriptFactory bundleJSR223ScriptFactory;
    BundleContext bundleContext;
    BundleURLScanner bundleURLScanner;
    ServiceTracker<IDialect,IDialect> dialectServiceTracker;

    Map<Bundle,List<URL>> allBundleScripts = new HashMap<Bundle,List<URL>>();

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
            for (String extension : scriptEngineFactory.getExtensions()) {
                if (!scriptExtensionsOrdering.contains(extension)) {
                    scriptExtensionsOrdering.add(extension);
                }
            }
            bundleScriptResolver.setScriptExtensionsOrdering(scriptExtensionsOrdering);

            Field scriptFactoryMapField = bundleScriptResolver.getClass().getDeclaredField("scriptFactoryMap");
            scriptFactoryMapField.setAccessible(true);
            Map<String,ScriptFactory> scriptFactoryMap = (Map<String,ScriptFactory>) scriptFactoryMapField.get(bundleScriptResolver);
            for (String extension : scriptEngineFactory.getExtensions()) {
                scriptFactoryMap.put(extension, bundleJSR223ScriptFactory);
            }

            // now we need to activate the bundle script scanner inside of newly deployed or existing bundles
            // register view script observers
            bundleURLScanner = new BundleURLScanner("/", "*.html", true);
            addBundleScripts(bundleContext.getBundle());

            // as we are starting up we insert all the bundle scripts for all the deployed bundles.
            for (Bundle bundle : bundleContext.getBundles()) {
                if ((bundle.getBundleContext() != null && !bundle.getBundleContext().equals(bundleContext)) || (bundle.getBundleContext() == null) ) {
                    addBundleScripts(bundle);
                }
            }

        }

        ServiceTrackerCustomizer<IDialect,IDialect> serviceTrackerCustomizer = new ServiceTrackerCustomizer<IDialect, IDialect>() {
            @Override
            public IDialect addingService(ServiceReference reference) {
                IDialect addedDialect = (IDialect) bundleContext.getService(reference);
                ((ThymeLeafScriptEngineFactory)scriptEngineFactory).getAdditionalDialects().put(addedDialect.getPrefix(), addedDialect);
                ((ThymeLeafScriptEngineFactory)scriptEngineFactory).initializeTemplateEngine();
                return addedDialect;
            }

            @Override
            public void modifiedService(ServiceReference reference, IDialect service) {

            }

            @Override
            public void removedService(ServiceReference reference, IDialect service) {
                if (service != null) {
                    ((ThymeLeafScriptEngineFactory) scriptEngineFactory).getAdditionalDialects().put(service.getPrefix(), service);
                }
                ((ThymeLeafScriptEngineFactory)scriptEngineFactory).initializeTemplateEngine();
            }
        };

        dialectServiceTracker = new ServiceTracker<IDialect, IDialect>(bundleContext, IDialect.class, serviceTrackerCustomizer);
        dialectServiceTracker.open();

    }

    private void addBundleScripts(Bundle bundle) {
        List<URL> bundleScripts = bundleURLScanner.scan(bundle);
        allBundleScripts.put(bundle, bundleScripts);
        bundleScriptResolver.addBundleScripts(bundle, bundleScripts);
    }

    private void removeBundleScripts(Bundle bundle) {
        List<URL> bundleScripts = allBundleScripts.get(bundle);
        if (bundleScripts != null && bundleScripts.size() > 0) {
            bundleScriptResolver.removeBundleScripts(bundle, bundleScripts);
        }
    }

    @Override
    public void destroy() throws Exception {

        dialectServiceTracker.close();

        for (Bundle bundle : allBundleScripts.keySet()) {
            removeBundleScripts(bundle);
        }

        List<String> scriptExtensionsOrdering = bundleScriptResolver.getScriptExtensionsOrdering();
        for (String extension : scriptEngineFactory.getExtensions()) {
            if (scriptExtensionsOrdering.contains(extension)) {
                scriptExtensionsOrdering.remove(extension);
            }
        }
        bundleScriptResolver.setScriptExtensionsOrdering(scriptExtensionsOrdering);

        Field scriptFactoryMapField = bundleScriptResolver.getClass().getDeclaredField("scriptFactoryMap");
        scriptFactoryMapField.setAccessible(true);
        Map<String,ScriptFactory> scriptFactoryMap = (Map<String,ScriptFactory>) scriptFactoryMapField.get(bundleScriptResolver);
        for (String extension : scriptEngineFactory.getExtensions()) {
            scriptFactoryMap.remove(extension);
        }

    }

    @Override
    public void bundleChanged(BundleEvent event) {
        final Bundle bundle = event.getBundle();
        switch (event.getType()) {
            case BundleEvent.STARTED :
                addBundleScripts(bundle);
                break;
            case BundleEvent.STOPPED :
                removeBundleScripts(bundle);
                break;
        }
    }
}
