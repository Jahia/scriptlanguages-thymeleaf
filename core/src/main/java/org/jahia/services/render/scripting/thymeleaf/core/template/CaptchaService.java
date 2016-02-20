package org.jahia.services.render.scripting.thymeleaf.core.template;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.core.ScriptingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by smomin on 2/19/16.
 */
public class CaptchaService implements ScriptingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaptchaService.class);
    private HttpServletRequest request;
    private boolean display = true;
    private boolean displayReloadLink = true;

    /**
     *
     * @param renderContext
     * @param display
     * @param displayReloadLink
     */
    public CaptchaService(final RenderContext renderContext,
                          final boolean display,
                          final boolean displayReloadLink) {
        this.request = renderContext.getRequest();
        this.display = display;
        this.displayReloadLink = displayReloadLink;
    }

    @Override
    public void init() {

    }

    @Override
    public String doProcess() {
        final StringBuilder out = new StringBuilder();
        final URLGenerator urlGen = (URLGenerator) request.getAttribute("url");
        final StringBuilder url = new StringBuilder();
        final String formId = (String) request.getAttribute(ScriptingConstants.ATTR_CURRENT_FORM_ID);
        url.append(urlGen.getContext()).append(urlGen.getCaptcha())
                .append("?token=##formtoken(").append(formId).append(")##");

        if (display) {
            out.append("<img id=\"jahia-captcha-").append(formId)
                    .append("\" alt=\"captcha\" src=\"").append(url).append("\" />");
        }
        if (displayReloadLink) {
            out.append("&nbsp;")
                    .append("<a href=\"#reload-captcha\" onclick=\"var captcha=document.getElementById('jahia-captcha-")
                    .append(formId)
                    .append("'); var captchaUrl=captcha.src; if (captchaUrl.indexOf('&amp;tst=') != -1){"
                            + "captchaUrl=captchaUrl.substring(0,captchaUrl.indexOf('&amp;tst='));}"
                            + "captchaUrl=captchaUrl+'&amp;tst='+new Date().getTime();"
                            + " captcha.src=captchaUrl; return false;\"><img src=\"")
                    .append(urlGen.getContext())
                    .append("/icons/refresh.png\" alt=\"refresh\"/></a>");
        }

        request.setAttribute(ScriptingConstants.ATTR_HAS_CAPTCHA, true);
        return out.toString();
    }
}
