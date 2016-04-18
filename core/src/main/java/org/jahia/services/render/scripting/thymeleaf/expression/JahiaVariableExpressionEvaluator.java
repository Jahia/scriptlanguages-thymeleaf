package org.jahia.services.render.scripting.thymeleaf.expression;

import org.jahia.services.render.scripting.thymeleaf.expression.variables.FacetUtils;
import org.jahia.services.render.scripting.thymeleaf.expression.variables.FunctionUtils;
import org.jahia.services.render.scripting.thymeleaf.expression.variables.JCRUtils;
import org.jahia.services.render.scripting.thymeleaf.expression.variables.UserUtils;
import org.jahia.services.render.scripting.thymeleaf.expression.variables.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.standard.expression.OgnlVariableExpressionEvaluator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by smomin on 2/12/16.
 */
public class JahiaVariableExpressionEvaluator extends OgnlVariableExpressionEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JahiaVariableExpressionEvaluator.class);

    private static final JahiaVariableExpressionEvaluator INSTANCE = new JahiaVariableExpressionEvaluator();
    private static final String FACET = "facet";
    private static final String JCR = "jcr";
    private static final String USER = "user";
    private static final String WORFLOW = "worflow";
    private static final String FUNCTION = "function";

    public static JahiaVariableExpressionEvaluator getInstance() {
        return INSTANCE;
    }

    @Override
    protected Map<String, Object> computeAdditionalContextVariables(final IProcessingContext processingContext) {
        LOGGER.debug("Loading Jahia Context Variables");

        final Map<String, Object> variables = new HashMap<String, Object>(5);
        variables.put(FACET, new FacetUtils());
        variables.put(JCR, new JCRUtils());
        variables.put(USER, new UserUtils());
        variables.put(WORFLOW, new WorkflowUtils());
        variables.put(FUNCTION, new FunctionUtils());
        return variables;
    }
}
