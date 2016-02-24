package org.jahia.services.render.scripting.thymeleaf.core.jcr.node;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRValueFactoryImpl;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.ScriptingConstants;
import org.jahia.services.render.scripting.thymeleaf.core.jcr.AbstractJCRService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by smomin on 2/22/16.
 */
public class JCRFilterService extends AbstractJCRService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JCRFilterService.class);
    private static final String EQ = "eq";
    private List<JCRNodeWrapper> nodeList;

    private Object list;
    private String properties;
    private JCRNodeWrapper node;

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param languageCode
     * @param properties
     * @param node
     * @param list
     */
    public JCRFilterService(final RenderContext renderContext,
                            final Resource currentResource,
                            final String languageCode,
                            final String properties,
                            final JCRNodeWrapper node,
                            final Object list) {
        super(renderContext, currentResource, languageCode);
        this.properties = properties;
        this.node = node;
        this.list = list;
    }

    /**
     *
     */
    public void init() {
        if (list instanceof Collection) {
            this.nodeList = new ArrayList<JCRNodeWrapper>((Collection<? extends JCRNodeWrapper>) list);
        } else if (list instanceof Iterator) {
            this.nodeList = new ArrayList<JCRNodeWrapper>();
            final Iterator iterator = (Iterator) list;
            while (iterator.hasNext()) {
                final JCRNodeWrapper e = (JCRNodeWrapper) iterator.next();
                this.nodeList.add(e);
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    public List<JCRNodeWrapper> doProcess() {
        init();
        try {
            final JSONObject jsonObject = new JSONObject(properties);
            final String uuid = (String) jsonObject.get(ScriptingConstants.ATTR_UUID);
            if (uuid.equals(node.getIdentifier())) {
                final String name = (String) jsonObject.get(ScriptingConstants.ATTR_NAME);
                final String valueAsString = (String) jsonObject.get(ScriptingConstants.ATTR_VALUE);

                String op = getValueOrDefaultIfMissingOrEmpty(jsonObject, ScriptingConstants.ATTR_OP, EQ);

                // check for negation operator
                boolean isNegated = false;
                if (op.startsWith("!")) {
                    isNegated = true; // we want the negated usual outcome
                    op = op.substring(1); // remove negation operator
                }

                // if we don't have a type, assume "String"
                final String type = getValueOrDefaultIfMissingOrEmpty(jsonObject, ScriptingConstants.ATTR_TYPE,
                        ScriptingConstants.ATTR_STRING);

                // optional format
                SimpleDateFormat dateFormat = null;
                final String format = getValueOrDefaultIfMissingOrEmpty(jsonObject,
                        ScriptingConstants.ATTR_FORMAT, null);
                if (format != null) {
                    dateFormat = new SimpleDateFormat(format);
                }

                // backward compatibility with previously documented "date" type where appropriate JCR type should be "Date"
                final boolean isLowerCaseDate = "date".equals(type);
                final Value value = isLowerCaseDate ? null : JCRValueFactoryImpl.getInstance()
                        .createValue(valueAsString, PropertyType.valueFromName(type));

                final List<JCRNodeWrapper> res = new ArrayList<JCRNodeWrapper>();
                for (final JCRNodeWrapper jcrNodeWrapper : nodeList) {
                    final JCRPropertyWrapper property = jcrNodeWrapper.getProperty(name);
                    if (property != null) {
                        if (EQ.equals(op)) {
                            // backward compatibility
                            if (isLowerCaseDate) {
                                if (dateFormat != null && dateFormat.format(property.getDate()
                                        .getTime()).equals(valueAsString)) {
                                    res.add(jcrNodeWrapper);
                                }
                            } else if (property.isMultiple()) {
                                final JCRValueWrapper[] values = property.getValues();
                                for (final Value wrappedValue : values) {
                                    if (wrappedValue.equals(value)) {
                                        res.add(jcrNodeWrapper);
                                    }
                                }
                            } else if (property.getValue().equals(value)) {
                                res.add(jcrNodeWrapper);
                            }
                        }
                    }
                }

                // if we had negated the operation, remove all matching elements from the original nodeList
                if (isNegated) {
                    nodeList.removeAll(res);
                }
            }
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return nodeList;
    }


    /**
     * @param jsonObject
     * @param paramName
     * @param defaultValue
     * @return
     */
    private String getValueOrDefaultIfMissingOrEmpty(final JSONObject jsonObject,
                                                     final String paramName,
                                                     final String defaultValue) {
        String paramValue = null;
        try {
            paramValue = (String) jsonObject.get(paramName);
        } catch (JSONException e) {
            // ignore exception indicating missing value, this is an annoying idiom of the JSON.org API,
            // would be better if it conformed to returning null if no value was associated to the key
        }
        return (paramValue == null || paramValue.isEmpty()) ? defaultValue : paramValue;
    }
}
