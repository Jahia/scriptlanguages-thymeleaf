package org.jahia.services.render.scripting.thymeleaf.core.template.pager;

import org.apache.commons.lang.StringEscapeUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.core.ScriptingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by smomin on 2/19/16.
 */
public class InitPagerService implements ScriptingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitPagerService.class);

    private HttpServletRequest request;
    private String id;
    private int pageSize;
    private long totalSize;
    private boolean sizeNotExact;

    public InitPagerService(final RenderContext renderContext,
                            final String id,
                            final int pageSize,
                            final long totalSize,
                            final boolean sizeNotExact) {
        this.request = renderContext.getRequest();
        this.id = id;
        this.pageSize = pageSize;
        this.totalSize = totalSize;
        this.sizeNotExact = sizeNotExact;
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
        Object value = moduleMap.get(ScriptingConstants.ATTR_BEGIN);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_OLD_BEGIN_PREFIX + id, value);
        }
        value = moduleMap.get(ScriptingConstants.ATTR_END);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_OLD_END_PREFIX + id, value);
        }
        value = moduleMap.get(ScriptingConstants.ATTR_PAGE_SIZE);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_OLD_PAGE_SIZE + id, value);
        }
        value = moduleMap.get(ScriptingConstants.ATTR_NB_PAGES);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_OLD_NB_PAGES_PREFIX + id, value);
        }
        value = moduleMap.get(ScriptingConstants.ATTR_CURRENT_PAGE);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_OLD_CURRENT_PAGE_PREFIX + id, value);
        }
        value = moduleMap.get(ScriptingConstants.ATTR_PAGINATION_ACTIVE);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_OLD_PAGINATION_ACTIVE_PREFIX + id, value);
        }
        value = moduleMap.get(ScriptingConstants.ATTR_TOTAL_SIZE);
        if (value != null) {
            moduleMap.put(ScriptingConstants.ATTR_OLD_TOTAL_SIZE_PREFIX + id, value);
        }
        String beginStr = StringEscapeUtils.escapeXml(request.getParameter(ScriptingConstants.ATTR_BEGIN + id));
        String endStr = StringEscapeUtils.escapeXml(request.getParameter(ScriptingConstants.ATTR_END + id));

        if (request.getParameter(ScriptingConstants.ATTR_PAGESIZE + id) != null) {
            pageSize = Integer.parseInt(StringEscapeUtils.escapeXml(request.getParameter(ScriptingConstants.ATTR_PAGESIZE + id)));
        }

        int begin = beginStr == null ? 0 : Integer.parseInt(beginStr);
        int end = endStr == null ? pageSize - 1 : Integer.parseInt(endStr);

        int currentPage = begin / pageSize + 1;

        long nbPages = totalSize / pageSize;
        if (nbPages * pageSize < totalSize) {
            nbPages++;
        }
        if (totalSize == Integer.MAX_VALUE) {
            nbPages = currentPage;// + 1;
        }

        if (totalSize < pageSize) {
            begin = 0;
        } else if (begin > totalSize) {
            begin = (int) ((nbPages - 1) * pageSize);
            end = begin + pageSize - 1;
        }

        if (currentPage > nbPages) {
            currentPage = (int) nbPages;
        }

        moduleMap.put(ScriptingConstants.ATTR_BEGIN, begin);
        moduleMap.put(ScriptingConstants.ATTR_END, end);
        moduleMap.put(ScriptingConstants.ATTR_PAGE_SIZE, pageSize);
        moduleMap.put(ScriptingConstants.ATTR_NB_PAGES, nbPages);
        moduleMap.put(ScriptingConstants.ATTR_CURRENT_PAGE, currentPage);
        moduleMap.put(ScriptingConstants.ATTR_PAGINATION_ACTIVE, true);
        moduleMap.put(ScriptingConstants.ATTR_TOTAL_SIZE, totalSize);
        moduleMap.put(ScriptingConstants.ATTR_SIZE_NOT_EXACT, sizeNotExact);
        moduleMap.put(ScriptingConstants.ATTR_TOTAL_SIZE_UNKNOWN, totalSize == Integer.MAX_VALUE);
        request.setAttribute(ScriptingConstants.ATTR_MODULE_MAP, moduleMap);
        request.setAttribute(ScriptingConstants.ATTR_BEGIN_PREFIX + id, begin);
        request.setAttribute(ScriptingConstants.ATTR_END_PREFIX + id, end);

        moduleMap.put(ScriptingConstants.ATTR_REQUEST_ATTRIBUTES_TO_CACHE,
                Arrays.asList(ScriptingConstants.ATTR_BEGIN_PREFIX + id, ScriptingConstants.ATTR_END_PREFIX + id));
        return null;
    }
}
