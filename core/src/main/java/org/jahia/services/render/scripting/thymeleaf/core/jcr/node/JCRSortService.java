package org.jahia.services.render.scripting.thymeleaf.core.jcr.node;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.thymeleaf.core.jcr.AbstractJCRService;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by smomin on 2/22/16.
 */
public class JCRSortService extends AbstractJCRService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JCRSortService.class);
    private Collection<JCRNodeWrapper> nodeList;

    private Object list;
    private String properties;

    /**
     *
     * @param renderContext
     * @param currentResource
     * @param languageCode
     * @param properties
     * @param list
     */
    public JCRSortService(final RenderContext renderContext,
                          final Resource currentResource,
                          final String languageCode,
                          final String properties,
                          final Object list) {
        super(renderContext, currentResource, languageCode);
        this.properties = properties;
        this.list = list;
    }

    @Override
    public void init() {
        if (list instanceof Collection) {
            this.nodeList = (Collection) list;
        } else if (list instanceof Iterator) {
            this.nodeList = new ArrayList<JCRNodeWrapper>();
            final Iterator<?> iterator = (Iterator<?>) list;
            while (iterator.hasNext()) {
                final JCRNodeWrapper e = (JCRNodeWrapper) iterator.next();
                this.nodeList.add(e);
            }
        }
    }

    @Override
    public List<JCRNodeWrapper> doProcess() {
        final List<JCRNodeWrapper> nodeList = new ArrayList<JCRNodeWrapper>(
                this.nodeList != null ? this.nodeList : Collections.<JCRNodeWrapper>emptyList());
        final String[] props = Patterns.COMMA.split(properties);
        Collections.sort(nodeList, new NodeComparator(props));
        return nodeList;
    }

    /**
     *
     */
    private final class NodeComparator implements Comparator<JCRNodeWrapper> {
        private String[] props;

        NodeComparator(final String[] props) {
            if (props == null) {
                throw new IllegalArgumentException("Should provide a valid array of properties to compare from");
            }
            this.props = props;
        }

        public int compare(final JCRNodeWrapper o1, final JCRNodeWrapper o2) {
            // first make sure that we return 0 if both are equal
            if (o1.equals(o2)) {
                return 0;
            }

            // if we don't have length, be consistent with equals (i.e. compare paths)
            if (props.length == 0) {
                return o1.getPath().compareToIgnoreCase(o2.getPath());
            }

            // default value is true, if the corresponding property has not been provided at the beginning of the nodeList
            // (templates developed before this change)
            boolean ignoreCase = true;
            int startIndex = 0;

            // if we have an odd number of "properties", the first one is a boolean indicating whether the sorting is case-sensitive or not
            if (props.length % 2 == 1) {
                ignoreCase = Boolean.valueOf(props[0]);
                startIndex = 1;
            }

            int result = 0;
            int power = props.length / 2;
            for (int i = startIndex; i < props.length; i += 2) {
                String prop = props[i];
                final String dir = props[i + 1];

                // we use a multiplier for each property in order to denote the importance of a given prop in the ordering
                int powerOf10 = (int) Math.pow(10, power--);
                int multiplier = "desc".equals(dir) ? -powerOf10 : powerOf10;
                String referenceProp = null;
                try {
                    prop = prop.trim();
                    if (prop.length() > 0) {
                        if (prop.contains(";")) {
                            String[] split = Patterns.SEMICOLON.split(prop);
                            prop = split[0];
                            referenceProp = split[1];
                        }
                        final boolean o1HasProp = o1.hasProperty(prop);
                        final boolean o2HasProp = o2.hasProperty(prop);
                        int r;
                        if (!o1HasProp && o2HasProp) {
                            r = -multiplier;
                        } else if (!o2HasProp && o1HasProp) {
                            r = multiplier;
                        } else {
                            Property p1 = o1.getProperty(prop);
                            Property p2 = o2.getProperty(prop);
                            if (referenceProp != null) {
                                p1 = p1.getNode().getProperty(referenceProp);
                                p2 = p2.getNode().getProperty(referenceProp);
                            }
                            switch (p1.getType()) {
                                case PropertyType.DATE:
                                    r = p1.getDate().compareTo(p2.getDate());
                                    break;
                                case PropertyType.DECIMAL:
                                case PropertyType.LONG:
                                case PropertyType.DOUBLE:
                                    r = Double.compare(p1.getDouble(), p2.getDouble());
                                    break;
                                default:
                                    final Collator collator = Collator.getInstance(Locale
                                            .forLanguageTag(o1.getLanguage()));
                                    if (ignoreCase) {
                                        collator.setStrength(Collator.TERTIARY);
                                    } else {
                                        collator.setStrength(Collator.SECONDARY);
                                    }
                                    r = collator.compare(p1.getString(), p2.getString());
                                    break;
                            }
                        }
                        result += r * multiplier;
                    }
                } catch (RepositoryException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            return result;
        }
    }
}
