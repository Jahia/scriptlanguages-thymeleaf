package org.jahia.services.render.scripting.thymeleaf.support.expression;

import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.standard.expression.OgnlVariableExpressionEvaluator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by smomin on 2/12/16.
 */
public class JahiaVariableExpressionEvaluator extends OgnlVariableExpressionEvaluator {

    public static final String WORD_UTILS = "wordUtils";
    public static final String UICOMPONENTS_FUNCTIONS = "uicomponentsFunctions";
    public static final String STRING_ESCAPE_UTILS = "stringEscapeUtils";
    public static final String USER = "user";
    public static final String RANDOM_UTILS = "randomUtils";
    public static final String LANGUAGE_CODE_CONVERTERS = "languageCodeConverters";
    public static final String JCR_TAG_UTILS = "jcrTagUtils";
    public static final String WORKFLOW_FUNCTIONS = "workflowFunctions";

    public static final JahiaVariableExpressionEvaluator INSTANCE = new JahiaVariableExpressionEvaluator();

    @Override
    protected Map<String, Object> computeAdditionalContextVariables(final IProcessingContext processingContext) {
        final Map<String, Object> variables = new HashMap<String, Object>();

        variables.put(WORD_UTILS, new org.apache.commons.lang.WordUtils());
//        variables.put("functions", new org.jahia.scripting.functions.Functions());
//        variables.put("facetfunctions", new org.jahia.scripting.facet.Functions());
        variables.put(UICOMPONENTS_FUNCTIONS, new org.jahia.taglibs.uicomponents.Functions());
//        variables.put("webUtils", new org.jahia.utils.WebUtils());
        variables.put(STRING_ESCAPE_UTILS, new org.apache.commons.lang.StringEscapeUtils());
        variables.put(USER, new org.jahia.taglibs.user.User());
        variables.put(RANDOM_UTILS, new org.apache.commons.lang.math.RandomUtils());
//        variables.put("math", new java.lang.Math());
//        variables.put("identifierUtils", new org.apache.commons.id.IdentifierUtils());
//        variables.put("fileUtils", new org.jahia.utils.FileUtils());
        variables.put(LANGUAGE_CODE_CONVERTERS, new org.jahia.utils.LanguageCodeConverters());
//        variables.put("principalViewHelper", new org.jahia.data.viewhelper.principal.PrincipalViewHelper());
        variables.put(JCR_TAG_UTILS, new org.jahia.taglibs.jcr.node.JCRTagUtils());
//        variables.put("extendedPropertyType", new org.jahia.services.content.nodetypes.ExtendedPropertyType());
//        variables.put("jcrContentUtils", new org.jahia.services.content.JCRContentUtils.getInstance());
        variables.put(WORKFLOW_FUNCTIONS, new org.jahia.taglibs.workflow.WorkflowFunctions());

        return variables;
    }
}
