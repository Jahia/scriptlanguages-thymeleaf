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
    public Object doProcess() {
        Map<String, Object> moduleMap = (Map<String, Object>) request
                .getAttribute(ScriptingConstants.ATTR_MODULE_MAP);
        if (moduleMap == null) {
            moduleMap = new HashMap<String, Object>();
        }

        final Object oldBegin = moduleMap.get(ScriptingConstants.ATTR_OLD_BEGIN_PREFIX + id);
        if (oldBegin != null) {
            moduleMap.put(ScriptingConstants.ATTR_BEGIN, oldBegin);
        }

        final Object oldEnd = moduleMap.get(ScriptingConstants.ATTR_OLD_END_PREFIX + id);
        if (oldEnd != null) {
            moduleMap.put(ScriptingConstants.ATTR_END, oldEnd);
        }

        final Object oldPageSize = moduleMap.get(ScriptingConstants.ATTR_OLD_PAGE_SIZE);
        if (oldPageSize != null) {
            moduleMap.put(ScriptingConstants.ATTR_PAGE_SIZE, oldPageSize);
        }

        final Object oldNBPages = moduleMap.get(ScriptingConstants.ATTR_OLD_NB_PAGES_PREFIX + id);
        if (oldNBPages != null) {
            moduleMap.put(ScriptingConstants.ATTR_NB_PAGES, oldNBPages);
        }

        final Object oldCurrentPage = moduleMap.get(ScriptingConstants.ATTR_OLD_CURRENT_PAGE_PREFIX + id);
        if (oldCurrentPage != null) {
            moduleMap.put(ScriptingConstants.ATTR_CURRENT_PAGE, oldCurrentPage);
        }

        final Object oldPaginationActive = moduleMap.get(ScriptingConstants.ATTR_OLD_PAGINATION_ACTIVE_PREFIX + id);
        if (oldPaginationActive != null) {
            moduleMap.put(ScriptingConstants.ATTR_PAGINATION_ACTIVE, oldPaginationActive);
        }

        final Object oldTotalSize = moduleMap.get(ScriptingConstants.ATTR_OLD_TOTAL_SIZE_PREFIX + id);
        if (oldTotalSize != null) {
            moduleMap.put(ScriptingConstants.ATTR_TOTAL_SIZE, oldTotalSize);
        }
        return null;
    }
}
