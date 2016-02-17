package org.jahia.services.render.scripting.thymeleaf.support.expression.variables;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.workflow.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by smomin on 2/16/16.
 */
public class WorkflowUtils {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(WorkflowUtils.class);

    public WorkflowUtils() {
        super();
    }

    public static boolean hasActivePublicationWorkflow(final JCRNodeWrapper node) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        boolean hasActiveWorkflowForType;
        try {
            Thread.currentThread().setContextClassLoader(WorkflowService.class.getClassLoader());
            hasActiveWorkflowForType = WorkflowService.getInstance().hasActiveWorkflowForType(node, "publish");
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
        return hasActiveWorkflowForType;
    }
}
