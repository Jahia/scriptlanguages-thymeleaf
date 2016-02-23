package org.jahia.services.render.scripting.thymeleaf.core.jcr;

import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.ScriptingSupportService;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import java.util.Locale;

/**
 * Created by smomin on 2/22/16.
 */
public abstract class AbstractJCRService extends ScriptingSupportService {

    private JCRSessionWrapper session;
    private String languageCode;

    protected RenderContext renderContext;
    protected Resource currentResource;

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param languageCode
     */
    public AbstractJCRService(final RenderContext renderContext,
                              final Resource currentResource,
                              final String languageCode) {
        this.renderContext = renderContext;
        this.currentResource = currentResource;
        this.languageCode = languageCode;
    }

    protected String getWorkspace() throws RepositoryException {
        String workspace = null;
        currentResource = currentResource != null ? currentResource : (renderContext != null ? renderContext.getMainResource() : null);
        if (currentResource != null) {
            workspace = currentResource.getWorkspace();
        }
        return workspace;
    }

    protected Locale getLocale() {
        final Locale locale;
        currentResource = currentResource != null ? currentResource : (renderContext != null ? renderContext.getMainResource() : null);
        if (currentResource != null) {
            locale = currentResource.getLocale();
        } else {
            locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
        }
        return locale;
    }

    protected JCRSessionWrapper getJCRSession() throws RepositoryException {
        if (session == null) {
            session = renderContext != null ? JCRSessionFactory.getInstance().getCurrentUserSession(getWorkspace(), getLocale(),
                    renderContext.getFallbackLocale()) : JCRSessionFactory.getInstance().getCurrentUserSession(getWorkspace(), getLocale());
        }
        return session;
    }
}
