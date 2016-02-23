package org.jahia.services.render.scripting.thymeleaf.core.query;

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.StaticOperand;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_LIKE;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO;

/**
 * Created by smomin on 2/22/16.
 */
public class ComparisonService extends ConstraintService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColumnService.class);

    public ComparisonService(final RenderContext renderContext, final Resource currentResource, final String languageCode, final String qomBeanName, final String statement, final long limit, final long offset, final String selectorName, final List<OperandType> operandTypes, final String operator, final String propertyName, final String value) {
        super(renderContext, currentResource, languageCode, qomBeanName, statement, limit, offset, selectorName);
        this.operandTypes = operandTypes;
        this.operator = operator;
        this.propertyName = propertyName;
        this.value = value;
    }

    public ComparisonService(final RenderContext renderContext, final Resource currentResource, final String languageCode, final Object qom, final String qomBeanName, final String statement, final long limit, final long offset, final String selectorName, final List<OperandType> operandTypes, final String operator, final String propertyName, final String value) {
        super(renderContext, currentResource, languageCode, qom, qomBeanName, statement, limit, offset, selectorName);
        this.operandTypes = operandTypes;
        this.operator = operator;
        this.propertyName = propertyName;
        this.value = value;
    }

    /**
     * Defines allowed dynamic operand types to be applied for the left operand
     * of the comparison.
     *
     * @author Sergiy Shyrkov
     */
    public enum OperandType {
        FULLTEXTSEARCHSCORE, LENGTH, LOWERCASE, NODELOCALNAME, NODENAME, PROPERTYVALUE, UPPERCASE;
    }

    private static final List<OperandType> DEF_OPERANDS = new LinkedList<OperandType>(Arrays
            .asList(new OperandType[] { OperandType.PROPERTYVALUE }));

    private static final Map<String, String> OPERATORS;

    private static final long serialVersionUID = -4684686849914698282L;

    static {
        final FastHashMap ops = new FastHashMap(8);
        ops.put("=", JCR_OPERATOR_EQUAL_TO);
        ops.put(">", JCR_OPERATOR_GREATER_THAN);
        ops.put(">=", JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO);
        ops.put("<", JCR_OPERATOR_LESS_THAN);
        ops.put("<=", JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO);
        ops.put("like", JCR_OPERATOR_LIKE);
        ops.put("!=", JCR_OPERATOR_NOT_EQUAL_TO);
        ops.put("<>", JCR_OPERATOR_NOT_EQUAL_TO);
        ops.setFast(true);
        OPERATORS = ops;
    }

    private List<OperandType> operandTypes = DEF_OPERANDS;
    private String operator = JCR_OPERATOR_EQUAL_TO;
    private String propertyName;
    private String value;

    @Override
    public Constraint getConstraint() throws Exception {
        return getQOMFactory().comparison(getOperand1(), getOperator(), getOperand2());
    }

    protected DynamicOperand getOperand1() throws RepositoryException {
        DynamicOperand result = null;
        for (OperandType opType : operandTypes) {
            switch (opType) {
                case FULLTEXTSEARCHSCORE:
                    result = getQOMFactory().fullTextSearchScore(getSelectorName());
                    break;
                case LENGTH:
                    if (result == null || !(result instanceof PropertyValue)) {
                        throw new IllegalArgumentException(
                                "Cannot find an operand to apply the 'Length' operand type to it."
                                        + " It must be preceded by PropertyValue operand.");
                    }
                    result = getQOMFactory().length((PropertyValue) result);
                    break;
                case LOWERCASE:
                    if (result == null) {
                        throw new IllegalArgumentException(
                                "Cannot find an operand to apply the 'LowerCase' operand type."
                                        + " It must be preceded by either PropertyValue, NodeName or NodeLocalName.");
                    }
                    result = getQOMFactory().lowerCase(result);
                    break;
                case NODELOCALNAME:
                    result = getQOMFactory().nodeLocalName(getSelectorName());
                    break;
                case NODENAME:
                    result = getQOMFactory().nodeName(getSelectorName());
                    break;
                case PROPERTYVALUE:
                    if (StringUtils.isEmpty(propertyName)) {
                        throw new IllegalArgumentException("propertyName attribute value is required for this constraint");
                    }
                    result = getQOMFactory().propertyValue(getSelectorName(), propertyName);
                    break;
                case UPPERCASE:
                    if (result == null) {
                        throw new IllegalArgumentException(
                                "Cannot find an operand to apply the 'UpperCase' operand type."
                                        + " It must be preceded by either PropertyValue, NodeName or NodeLocalName.");
                    }
                    result = getQOMFactory().upperCase(result);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown DynamicOperand type '" + opType + "'");
            }
        }
        return result;
    }

    protected StaticOperand getOperand2() throws RepositoryException {
        return getQOMFactory().literal(getQOMBuilder().getValueFactory().createValue(value));
    }

    protected String getOperator() {
        if (!operator.startsWith("{http://www.jcp.org/jcr/1.0}")) {
            String resolvedOperator = OPERATORS.get(operator.toLowerCase().trim());
            if (resolvedOperator != null) {
                operator = resolvedOperator;
            }
        }
        return operator;
    }
    /**
     * Sets the sequence (a comma-separated string) of dynamic operand types to
     * be applied on the operand1 (left operand of the comparison).
     *
     * @param appliedOperandsSequence the sequence (a comma-separated string) of
     *            dynamic operand types to be applied on the operand1 (left
     *            operand of the comparison)
     */
    public void setOperandTypes(String appliedOperandsSequence) {
        if (StringUtils.isEmpty(appliedOperandsSequence)) {
            throw new IllegalArgumentException("appliedOperands attribute value is required for this tag.");
        }
        List<OperandType> types = new LinkedList<OperandType>();
        appliedOperandsSequence = appliedOperandsSequence.toUpperCase();
        if (appliedOperandsSequence.contains(",")) {
            for (String op : Patterns.COMMA.split(appliedOperandsSequence)) {
                types.add(OperandType.valueOf(op.trim()));
            }
        } else {
            final OperandType opType = OperandType.valueOf(appliedOperandsSequence);
            if (opType == OperandType.PROPERTYVALUE) {
                types = DEF_OPERANDS;
            } else {
                types.add(opType);
            }
        }
        this.operandTypes = types;
    }
}
