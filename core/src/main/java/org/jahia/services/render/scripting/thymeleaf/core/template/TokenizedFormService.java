package org.jahia.services.render.scripting.thymeleaf.core.template;

import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.apache.commons.lang.StringUtils;
import org.apache.noggit.JSONUtil;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.core.ScriptingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by smomin on 2/19/16.
 */
public class TokenizedFormService implements ScriptingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenizedFormService.class);
    private RenderContext renderContext;
    private HttpServletRequest request;
    private String bodyContent;
    private boolean disableXSSFiltering = false;
    private boolean allowsMultipleSubmits = false;

    /**
     *
     * @param renderContext
     * @param bodyContent
     * @param disableXSSFiltering
     * @param allowsMultipleSubmits
     */
    public TokenizedFormService(final RenderContext renderContext,
                                final String bodyContent,
                                final boolean disableXSSFiltering,
                                final boolean allowsMultipleSubmits) {
        this.renderContext = renderContext;
        this.request = renderContext.getRequest();
        this.bodyContent = bodyContent;
        this.disableXSSFiltering = disableXSSFiltering;
        this.allowsMultipleSubmits = allowsMultipleSubmits;
    }

    @Override
    public void init() {
        final String id = java.util.UUID.randomUUID().toString();
        request.setAttribute(ScriptingConstants.ATTR_CURRENT_FORM_ID, id);
    }

    @Override
    public String doProcess() {
        init();
        boolean hasCaptcha = false;
        if (request.getAttribute(ScriptingConstants.ATTR_HAS_CAPTCHA) != null) {
            hasCaptcha = (Boolean) request.getAttribute(ScriptingConstants.ATTR_HAS_CAPTCHA);
        }

        final String id = (String) request.getAttribute(ScriptingConstants.ATTR_CURRENT_FORM_ID);
        final Source source = new Source(bodyContent);
        final OutputDocument outputDocument = new OutputDocument(source);
        final TreeMap<String,List<String>> hiddenInputs = new TreeMap<String,List<String>>();
        final List<StartTag> formTags = source.getAllStartTags("form");
        final StartTag formTag = formTags.get(0);
        String action = formTag.getAttributeValue("action");

        if (!action.startsWith("/") && !action.contains("://")) {
            String requestURI = request.getRequestURI();
            if (requestURI.startsWith("/gwt/")) {
                requestURI = renderContext.getURLGenerator().buildURL(renderContext.getMainResource().getNode(),
                        renderContext.getMainResourceLocale().toString(),
                        renderContext.getMainResource().getTemplate(),
                        renderContext.getMainResource().getTemplateType());
            }
            action = StringUtils.substringBeforeLast(requestURI, "/")+ "/" +action;
        }
        hiddenInputs.put("form-action", Arrays.asList(StringUtils.substringBeforeLast(action,";")));
        hiddenInputs.put("form-method", Arrays.asList(StringUtils.capitalize(formTag.getAttributeValue("method"))));

        final List<StartTag> inputTags = source.getAllStartTags("input");
        for (final StartTag inputTag : inputTags) {
            if ("hidden".equals(inputTag.getAttributeValue("type"))) {
                final String name = inputTag.getAttributeValue("name");
                List<String> strings = hiddenInputs.get(name);
                final String value = inputTag.getAttributeValue("value");
                if(strings == null) {
                    strings = new LinkedList<String>();
                }
                strings.add(value);
                hiddenInputs.put(name, strings);
            }
        }

        if (hasCaptcha) {
            // Put random number here, will be replaced by the captcha servlet with the expected value
            hiddenInputs.put("jcrCaptcha", Arrays.asList(java.util.UUID.randomUUID().toString()));
        }

        hiddenInputs.put("disableXSSFiltering", Arrays.asList(String.valueOf(disableXSSFiltering)));
        hiddenInputs.put("allowsMultipleSubmits", Arrays.asList(String.valueOf(allowsMultipleSubmits)));
        outputDocument.insert(formTag.getEnd(), "<input type=\"hidden\" name=\"disableXSSFiltering\" value=\"" + disableXSSFiltering + "\"/>");

        outputDocument.insert(formTag.getEnd(),"<jahia:token-form id='"+id+"' forms-data='"+ JSONUtil.toJSON(hiddenInputs)+"'/>");

        request.removeAttribute(ScriptingConstants.ATTR_HAS_CAPTCHA);
        request.removeAttribute(ScriptingConstants.ATTR_CURRENT_FORM_ID);

        return outputDocument.toString();

    }
}
