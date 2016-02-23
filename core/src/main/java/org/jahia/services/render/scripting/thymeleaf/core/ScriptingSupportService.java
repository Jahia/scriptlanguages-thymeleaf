package org.jahia.services.render.scripting.thymeleaf.core;

import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.ResourceBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by smomin on 2/17/16.
 */
public abstract class ScriptingSupportService implements ScriptingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptingSupportService.class);

    /**
     *
     * @param ctx
     * @param resourceBundle
     * @return
     */
    public static String getResourceBundle(final RenderContext ctx,
                                           final String resourceBundle) {
        String bundle = resourceBundle;
        if (bundle == null || "".equals(bundle)) {
            try {
                bundle = ServicesRegistry.getInstance()
                        .getJahiaTemplateManagerService().getTemplatePackage(
                                ctx.getSite().getTemplatePackageName())
                        .getResourceBundleName();
            } catch (Exception e) {
                LOGGER.warn(
                        "Unable to retrieve resource bundle name for current template set. Cause: "
                                + e.getMessage(), e);
            }
        }
        return bundle;
    }

    /**
     *
     * @param ctx
     * @param localizationCtx
     * @param resourceBundle
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getMessage(final RenderContext ctx,
                                    final LocalizationContext localizationCtx,
                                    final String resourceBundle,
                                    final String key,
                                    final String defaultValue) {
        String message = defaultValue;
        if (key != null) {
            try {
                message = retrieveResourceBundle(ctx, localizationCtx, resourceBundle).getString(key);
            } catch (MissingResourceException e) {
                // use default value
            }
        }
        return message;
    }

    /**
     *
     * @param ctx
     * @param localizationCtx
     * @param resourceBundle
     * @param key
     * @return
     */
    public static String getMessage(final RenderContext ctx,
                                    final LocalizationContext localizationCtx,
                                    final String resourceBundle,
                                    final String key) {
        return getMessage(ctx, localizationCtx, resourceBundle, key, "???" + key + "???");
    }

    /**
     * Retrieve the parent resource bundle if any and if the current one is null.
     * This has to be called in subtags of TemplateTag (any tag within a template should do actually).
     *
     * @param ctx
     * @param localizationCtx
     * @param resourceBundle
     *
     * @return
     */
    public static ResourceBundle retrieveResourceBundle(final RenderContext ctx,
                                                        final LocalizationContext localizationCtx,
                                                        final String resourceBundle) {
        ResourceBundle bundle = null;
        if (localizationCtx != null) {
            bundle = localizationCtx.getResourceBundle();
        }
        if (bundle == null) {
            bundle = ResourceBundles.get(resourceBundle,
                    ctx.getSite().getTemplatePackage(),
                    ctx.getMainResourceLocale());
        }
        return bundle;
    }

    /**
     * Generate jahia_gwt_dictionary JavaScript include
     *
     * @param locale
     * @param request
     * @return jahia_gwt_dictionary JavaScript include
     */
    public static String getGwtDictionaryInclude(final HttpServletRequest request,
                                                 final Locale locale) {
        final StringBuilder s = new StringBuilder();
        s.append("<script type=\"text/javascript\" src=\"").append(
                request.getContextPath())
                .append("/gwt/resources/i18n/messages");
        if (LanguageCodeConverters.getAvailableBundleLocales().contains(locale)) {
            s.append("_").append(locale.toString());
        }
        s.append(".js\"></script>");
        return s.toString();
    }

    /**
     *
     * @param ctx
     * @return
     */
    public static boolean isLogged(final RenderContext ctx) {
        return ctx.isLoggedIn();
    }

    /**
     * Generates the language switching link for the specified language.
     *
     * @param ctx
     * @param langCode the language to generate a link for
     * @return the language switching link for the specified language
     */
    private static String generateCurrentNodeLangSwitchLink(final RenderContext ctx,
                                                            final String langCode) {
        if (ctx != null) {
            return ctx.getURLGenerator().getLanguages().get(langCode);
        } else {
            LOGGER.error("Unable to get lang[" + langCode + "] link for current resource");
            return "";
        }
    }

    /**
     * Generates the language switching link for the specified node and language.
     *
     * @param ctx
     * @param node     the node to generate the link for
     * @param langCode the language to generate a link for
     * @return the language switching link for the specified language
     */
    public static String generateNodeLangSwitchLink(final RenderContext ctx,
                                                          final JCRNodeWrapper node,
                                                          final String langCode) {
        if (node == null) {
            LOGGER.warn("Node not specified. Language link will be generated for current node.");
            return generateCurrentNodeLangSwitchLink(ctx, langCode);
        }
        if (ctx != null) {
            final Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
            final URLGenerator url = new URLGenerator(ctx, resource);
            return url.getLanguages().get(langCode);
        } else {
            LOGGER.error("Unable to get lang[" + langCode + "] link for home page");
            return "";
        }
    }

    /**
     * Returns the current user.
     *
     * @param ctx
     * @return the current user
     */
    public static JahiaUser getUser(final RenderContext ctx) {
        return ctx != null ? ctx.getUser() : null;
    }

    /**
     *
     * @param renderContext
     * @param session
     * @param request
     * @return
     */
    public static Locale getUILocale(final RenderContext renderContext,
                                     final HttpSession session,
                                     final HttpServletRequest request) {
        Locale currentLocale = renderContext != null ? renderContext.getUILocale() : null;
        if (session != null) {
            if (session.getAttribute(Constants.SESSION_UI_LOCALE) != null) {
                currentLocale = (Locale) session.getAttribute(Constants.SESSION_UI_LOCALE);
            }
        }
        if (currentLocale == null) {
            currentLocale = renderContext != null ? renderContext.getFallbackLocale() : null;
        }
        if (currentLocale == null) {
            currentLocale = request.getLocale();
        }
        return currentLocale;
    }
}
