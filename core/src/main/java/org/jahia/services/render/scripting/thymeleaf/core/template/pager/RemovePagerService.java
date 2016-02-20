package org.jahia.services.render.scripting.thymeleaf.core.template.pager;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.core.ScriptingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by smomin on 2/19/16.
 */
public class RemovePagerService implements ScriptingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemovePagerService.class);

    private HttpServletRequest request;
    private String id;

    public RemovePagerService(final RenderContext renderContext,
                            final String id) {
        this.request = renderContext.getRequest();
        this.id = id;
    }

    @Override
    public void init() {

    }

    @Override
    public Object doProcess() {
        Map<String, Object> moduleMap = (HashMap<String, Object>) request.getAttribute(ScriptingConstants.ATTR_MODULE_MAP);
        if (moduleMap == null) {
            moduleMap = new HashMap<String, Object>();
        }
        Object value = moduleMap.get(ScriptingConstants.ATTR_OLD_BEGIN_PREFIX);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_BEGIN, value);
        }
        value = moduleMap.get(ScriptingConstants.ATTR_OLD_END_PREFIX + id);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_END, value);
        }
        value = moduleMap.get(ScriptingConstants.ATTR_OLD_PAGE_SIZE);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_PAGE_SIZE, value);
        }
        value = moduleMap.get(ScriptingConstants.ATTR_OLD_NB_PAGES_PREFIX + id);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_NB_PAGES, value);
        }
        value = moduleMap.get(ScriptingConstants.ATTR_OLD_CURRENT_PAGE_PREFIX + id);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_CURRENT_PAGE, value);
        }
        value = moduleMap.get(ScriptingConstants.ATTR_OLD_PAGINATION_ACTIVE_PREFIX + id);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_PAGINATION_ACTIVE, value);
        }
        value = moduleMap.get(ScriptingConstants.ATTR_OLD_TOTAL_SIZE_PREFIX + id);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_TOTAL_SIZE, value);
        }
        return null;
    }
}
