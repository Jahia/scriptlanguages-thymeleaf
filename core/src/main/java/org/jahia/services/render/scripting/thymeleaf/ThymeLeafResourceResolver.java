package org.jahia.services.render.scripting.thymeleaf;

import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.context.IContext;
import org.thymeleaf.resourceresolver.IResourceResolver;
import org.thymeleaf.util.ClassLoaderUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by loom on 04.05.15.
 */
public class ThymeLeafResourceResolver implements IResourceResolver {

    public static final String NAME = "JAHIA";

    private Map<String,String> scripts = new ConcurrentHashMap<String, String>();

    @Override
    public String getName() {
        return NAME;
    }

    public String putScript(String scriptName, String scriptSourceCode) {
        return scripts.put(scriptName, scriptSourceCode);
    }

    public String removeScript(String scriptName) {
        return scripts.remove(scriptName);
    }

    @Override
    public InputStream getResourceAsStream(final TemplateProcessingParameters templateProcessingParameters, final String resourceName) {
        if (scripts.containsKey(resourceName)) {
            return new ByteArrayInputStream(scripts.get(resourceName).getBytes(StandardCharsets.UTF_8));
        }

        return ClassLoaderUtils.getClassLoader(ThymeLeafResourceResolver.class).getResourceAsStream(resourceName);
    }
}
