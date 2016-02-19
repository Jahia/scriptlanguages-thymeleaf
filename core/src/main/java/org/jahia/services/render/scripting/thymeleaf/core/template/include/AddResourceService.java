package org.jahia.services.render.scripting.thymeleaf.core.template.include;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.scripting.thymeleaf.core.ScriptingService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by smomin on 2/17/16.
 */
public class AddResourceService implements ScriptingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddResourceService.class);

    private RenderContext renderContext;
    private boolean insert;
    private String type;
    private String resources;
    private String title;
    private String key;
    private String targetTag;
    private String rel;
    private String media;
    private String condition;
    private String bodyContent;
    private boolean rawValue;

    /**
     *
     * @param renderContext
     * @param insert
     * @param type
     * @param resources
     * @param title
     * @param key
     * @param targetTag
     * @param rel
     * @param media
     * @param condition
     * @param bodyContent
     * @param rawValue
     */
    public AddResourceService(final RenderContext renderContext,
                              final boolean insert,
                              final String type,
                              final String resources,
                              final String title,
                              final String key,
                              final String targetTag,
                              final String rel,
                              final String media,
                              final String condition,
                              final String bodyContent,
                              final boolean rawValue) {
        this.renderContext = renderContext;
        this.insert = insert;
        this.type = type;
        this.resources = resources;
        this.title = title;
        this.key = key;
        this.targetTag = targetTag;
        this.rel = rel;
        this.media = media;
        this.condition = condition;
        this.bodyContent = bodyContent;
        this.rawValue = rawValue;
    }

    /**
     * 
     */
    @Override
    public void init() {
        
    }

    /**
     * 
     * @return
     */
    @Override
    public String doProcess() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Site : " + renderContext.getSite() + " type : " + type + " resources : " + resources);
        }
        if (!StringUtils.isEmpty(bodyContent)) {
            return generateResourceTag(type != null ? type : "inline", bodyContent, null);
        }
        if (renderContext == null) {
            LOGGER.warn("No render context found. Unable to add a resoure");
            return "";
        }

        final Map<String, String> mapping = getStaticAssetMapping();
        final Set<String> strings = new LinkedHashSet<String>();
        for (final String sourceResource : Patterns.COMMA.split(resources)) {
            final String replacement = mapping.get(sourceResource);
            if (replacement != null) {
                Collections.addAll(strings, StringUtils.split(replacement, " "));
            } else {
                strings.add(sourceResource);
            }
        }

        final Set<JahiaTemplatesPackage> packages = new TreeSet<JahiaTemplatesPackage>(TemplatePackageRegistry.TEMPLATE_PACKAGE_COMPARATOR);
        final JCRSiteNode site = renderContext.getSite();
        final JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        if (site.getPath().startsWith("/sites/")) {
            for (final String s : site.getInstalledModulesWithAllDependencies()) {
                final JahiaTemplatesPackage templatePackageById = templateManagerService.getTemplatePackageById(s);
                if (templatePackageById != null) {
                    packages.add(templatePackageById);
                }
            }
        } else if (site.getPath().startsWith("/modules/")) {
            final JahiaTemplatesPackage aPackage = templateManagerService.getTemplatePackageById(site.getName());
            if (aPackage != null) {
                packages.add(aPackage);
                for (final JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                    if (!packages.contains(depend)) {
                        packages.add(depend);
                    }
                }
            }

        }

        final StringBuilder builder = new StringBuilder();
        final StringBuilder generatedResourceTags = new StringBuilder();
        for (String resource : strings) {
            resource = resource.trim();
            if (resource.startsWith("/") || resource.startsWith("http://") || resource.startsWith("https://")) {
                generatedResourceTags.append(generateResourceTag(type, resource, resource));
            } else {
                final String relativeResourcePath = "/" + type + "/" + resource;
                for (final JahiaTemplatesPackage pack : packages) {
                    if (pack.resourceExists(relativeResourcePath)) {
                        // we found it
                        String path = pack.getRootFolderPath() + relativeResourcePath;
                        final String contextPath = renderContext.getRequest().getContextPath();
                        String pathWithContext = contextPath.isEmpty() ? path : contextPath + path;

                        // apply mapping
                        final String mappedPath = mapping.get(path);
                        if (mappedPath != null) {
                            for (final String mappedResource : StringUtils.split(mappedPath, " ")) {
                                path = mappedResource;
                                pathWithContext = !path.startsWith("http://") && !path.startsWith("https://") ? (contextPath
                                        .isEmpty() ? path : contextPath + path) : path;
                                generatedResourceTags.append(generateResourceTag(type, pathWithContext, resource));
                            }
                        } else {
                            generatedResourceTags.append(generateResourceTag(type, pathWithContext, resource));
                        }

                        if (builder.length() > 0) {
                            builder.append(",");
                        }
                        builder.append(pathWithContext);
                        break;
                    }
                }
            }
        }
        if (rawValue) {
            return builder.toString();
        } else {
            return generatedResourceTags.toString();
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getStaticAssetMapping() {
        return (Map<String, String>) SpringContextSingleton.getBean(
                "org.jahia.services.render.StaticAssetMappingRegistry");
    }

    /**
     *
     * @param type
     * @param path
     * @param resource
     * @return
     */
    private String generateResourceTag(final String type,
                                       final String path,
                                       final String resource) {
        final StringBuilder builder = new StringBuilder();
        builder.append("<jahia:resource type=\"");
        builder.append(type != null ? type : "").append("\"");
        try {
            builder.append(" path=\"").append(URLEncoder.encode(path != null ? path : "", "UTF-8")).append("\"");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        builder.append(" insert=\"").append(insert).append("\"");
        if (targetTag != null) {
            builder.append(" targetTag=\"").append(targetTag).append("\"");
        }
        if (rel != null) {
            builder.append(" rel=\"").append(rel).append("\"");
        }
        if (media != null) {
            builder.append(" media=\"").append(media).append("\"");
        }
        if (condition != null) {
            builder.append(" condition=\"").append(condition).append("\"");
        }
        builder.append(" resource=\"").append(resource != null ? resource : "").append("\"");
        builder.append(" title=\"").append(title != null ? title : "").append("\"");
        builder.append(" key=\"").append(key != null ? key : "").append("\"");
        builder.append(" />\n");
        return builder.toString();
    }
}
