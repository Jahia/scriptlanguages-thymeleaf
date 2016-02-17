package org.jahia.services.render.scripting.thymeleaf.support.expression.variables;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.utils.Patterns;
import org.jahia.utils.Url;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RangeIterator;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by smomin on 2/16/16.
 */
public class FunctionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionUtils.class);

    /**
     *
     * @param attributes
     * @return
     */
    public static String attributes(final Map<String, Object> attributes) {
        final StringBuilder out = new StringBuilder();
        for (final Map.Entry<String, Object> attr : attributes.entrySet()) {
            out.append(attr.getKey()).append("=\"")
                    .append(attr.getValue() != null ? attr.getValue().toString() : "")
                    .append("\" ");
        }
        return out.toString();
    }

    /**
     * Checks if the provided target object can be found in the source. The
     * search is done, depending on the source parameter type. It can be either
     * {@link String}, {@link Collection} or an array of objects.
     *
     * @param source the source to search in
     * @param target the object to search for
     * @return <code>true</code> if the target object is present in the source
     */
    public static boolean contains(final Object source,
                                   final Object target) {
        if (source == null) {
            throw new IllegalArgumentException("The source cannot be null");
        }
        boolean found = false;
        if (source instanceof Collection<?>) {
            found = ((Collection<?>) source).contains(target);
        } else if (source instanceof Object[]) {
            found = ArrayUtils.contains((Object[]) source, target);
        } else {
            found = target != null ? source.toString().contains(target.toString()) : false;
        }

        return found;
    }

    /**
     *
     * @param initString
     * @param searchString
     * @return
     */
    public static int countOccurences(final String initString,
                                      final String searchString) {
        final String[] fullString = ("||||" + initString + "||||").split(searchString);
        return fullString.length - 1;
    }

    /**
     * Decode facet filter URL parameter
     * @param inputString enocded facet filter URL query parameter
     * @return decoded facet filter parameter
     */
    public static String decodeUrlParam(final String inputString) {
        return Url.decodeUrlParam(inputString);
    }

    /**
     *
     * @param value
     * @param defaultValue
     * @return
     */
    public static Object defaultValue(final Object value, final Object defaultValue) {
        return (value != null && (!(value instanceof String) || (((String) value)
                .length() > 0))) ? value : defaultValue;
    }

    /**
     *
     * @param localeToDisplay
     * @param localeUsedForRendering
     * @return
     */
    public static java.lang.String displayLocaleNameWith(final Locale localeToDisplay,
                                                         final Locale localeUsedForRendering) {
        return WordUtils.capitalizeFully(localeToDisplay.getDisplayName(localeUsedForRendering));
    }

    /**
     *
     * @param localeToDisplay
     * @return
     */
    public static String getLangIcon(final Locale localeToDisplay) {
        if("".equals(localeToDisplay.getCountry()))
            return "flag_" + localeToDisplay.getLanguage().toLowerCase() + "_on";
        else
            return "flag_" + Patterns.SPACE.matcher(localeToDisplay.getDisplayCountry(Locale.ENGLISH).toLowerCase()).replaceAll("_");
    }

    /**
     * Encode facet filter URL parameter
     * @param inputString facet filter parameter
     * @return filter encoded for URL query parameter usage
     */
    public static String encodeUrlParam(final String inputString) {
        return Url.encodeUrlParam(inputString);
    }

    /**
     * Checks if the current object is iterable so that it can be used in an c:forEach
     * tag.
     *
     * @param o the object to be checked if it is iterable
     * @return if the current object is iterable return true otherwise false
     */
    public static Boolean isIterable(final Object o) {
        boolean isIt = false;
        if (o instanceof Object[] || o instanceof boolean[] || o instanceof byte[]
                || o instanceof char[] || o instanceof short[] || o instanceof int[]
                || o instanceof long[] || o instanceof float[] || o instanceof double[]
                || o instanceof Collection<?> || o instanceof Iterator<?>
                || o instanceof Enumeration<?> || o instanceof Map<?, ?> || o instanceof String) {
            isIt = true;
        }

        return isIt;
    }

    /**
     * Joins the elements of the provided array/collection/iterator into a single String containing the provided elements with specified
     * separator.
     *
     * @param elements
     *            the set of values to join together, may be null
     * @param separator
     *            the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null elements input
     */
    public static String join(final Object elements,
                              final String separator) {
        if (elements == null) {
            return null;
        }

        if (elements instanceof Object[]) {
            return StringUtils.join((Object[]) elements, separator);
        } else if (elements instanceof Collection<?>) {
            return StringUtils.join((Collection<?>) elements, separator);
        } else if (elements instanceof Iterator<?>) {
            return StringUtils.join((Iterator<?>) elements, separator);
        } else if (elements instanceof Enumeration<?>) {
            return StringUtils.join(EnumerationUtils.toList((Enumeration<?>) elements), separator);
        } else if (elements instanceof Map<?, ?>) {
            return StringUtils.join(((Map<?, ?>) elements).keySet(), separator);
        } else if (elements instanceof String) {
            return (String) elements;
        }

        throw new IllegalArgumentException("Cannot handle the elements of type " + elements.getClass().getName());
    }

    /**
     *
     * @param obj
     * @return
     * @throws Exception
     */
    public static long length(final Object obj) throws Exception {
        if (obj != null && obj instanceof RangeIterator) {
            return JCRContentUtils.size((RangeIterator) obj);
        }
        else {
            if (obj == null) return 0;
            if (obj instanceof String) return ((String)obj).length();
            if (obj instanceof Collection) return ((Collection)obj).size();
            if (obj instanceof Map) return ((Map)obj).size();

            int count = 0;
            if (obj instanceof Iterator) {
                Iterator iter = (Iterator)obj;
                count = 0;
                while (iter.hasNext()) {
                    count++;
                    iter.next();
                }
                return count;
            }
            if (obj instanceof Enumeration) {
                Enumeration enum_ = (Enumeration)obj;
                count = 0;
                while (enum_.hasMoreElements()) {
                    count++;
                    enum_.nextElement();
                }
                return count;
            }
            try {
                count = Array.getLength(obj);
                return count;
            } catch (IllegalArgumentException ex) {}
            return count;
        }
    }

    /**
     *
     * @param pattern
     * @param str
     * @return
     */
    public static boolean matches(final String pattern, final String str) {
        return Pattern.compile(pattern).matcher(str).matches();
    }

    /**
     *
     * @param txt
     * @return
     */
    public static String removeCacheTags(final String txt) {
        return AggregateCacheFilter.removeCacheTags(txt);
    }

    /**
     *
     * @param initString
     * @param separator
     * @return
     */
    public static String removeDuplicates(final String initString, final String separator) {
        final String[] fullString = initString.split(separator);
        final StringBuilder finalString = new StringBuilder();
        String tmpString = initString;
        for (final String s : fullString) {
            if (tmpString.contains(s)) {
                finalString.append(s);
                if (finalString.length() > 0) {
                    finalString.append(separator);
                }
                tmpString = tmpString.replaceAll(s, "");
            }
        }
        return finalString.toString();
    }

    /**
     *
     * @param value
     * @return
     */
    public static String removeHtmlTags(final String value) {
        if (value == null || value.length() == 0) {
            return value;
        }
        final Source source = new Source(value);
        final TextExtractor textExtractor = source.getTextExtractor();
        textExtractor.setExcludeNonHTMLElements(true);
        textExtractor.setConvertNonBreakingSpaces(false);
        textExtractor.setIncludeAttributes(false);
        return textExtractor.toString();
    }

    /**
     * Reverse the content of a list. Only works with some List.
     *
     * @param list List<T> list to be reversed.
     * @return <code>java.util.List</code> the reversed list.
     */
    public static <T> List<T> reverse(final Collection<T> list) {
        final List<T> copy = new ArrayList<T>();
        copy.addAll(list);
        Collections.reverse(copy);
        return copy;
    }

    /**
     *
     * @param it
     * @param <T>
     * @return
     */
    public static <T> Iterator<T> reverse(final Iterator<T> it) {
        final List<T> copy = new ArrayList<T>();
        while (it.hasNext()) {
            copy.add(it.next());
        }
        Collections.reverse(copy);
        return copy.iterator();
    }

    /**
     *
     * @param orderedMap
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> reverse(final Map<String, T> orderedMap) {
        if (orderedMap == null || orderedMap.isEmpty()) {
            return orderedMap;
        }
        final LinkedHashMap<String, T> reversed = new LinkedHashMap<String, T>(orderedMap.size());
        final ListIterator<String> li = new LinkedList<String>(orderedMap.keySet())
                .listIterator(orderedMap.size());
        while (li.hasPrevious()) {
            final String key = li.previous();
            reversed.put(key, orderedMap.get(key));
        }
        return reversed;
    }

    /**
     *
     * @param value
     * @param appendix1
     * @param appendix2
     * @return
     */
    public static String stringConcatenation(final String value, final String appendix1, final String appendix2) {
        final StringBuilder buff = new StringBuilder();
        if (value != null) {
            buff.append(value);
        }
        if (appendix1 != null) {
            buff.append(appendix1);
        }
        if (appendix2 != null) {
            buff.append(appendix2);
        }
        return buff.toString();
    }

    /**
     *
     * @param s
     * @return
     */
    public static String sqlEncode(final String s) {
        return JCRContentUtils.sqlEncode(s);
    }

    /**
     *
     * @param s
     * @return
     */
    public static String xpathPathEncode(final String s) {
        return JCRContentUtils.stringToJCRPathExp(s);
    }

    /**
     *
     * @param req
     * @param moduleId
     * @return
     */
    public static String modulePath(final HttpServletRequest req,
                                    final String moduleId) {
        return req.getContextPath() + "/modules/" + moduleId;
    }

    /**
     *
     * @param dateToParse
     * @param pattern
     * @param locale
     * @return
     */
    public static String formatISODate(final String dateToParse,
                                       final String pattern,
                                       final Locale locale){
        try{
            final DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(dateToParse);
            return DateTimeFormat.forPattern(pattern).withLocale(locale != null ? locale : Locale.ENGLISH)
                    .print(!dateToParse.endsWith("Z") ? dateTime : dateTime.toDateTime(DateTimeZone.forID("UTC")));
        } catch (Exception e) {
            LOGGER.debug("Unable to parse date:"+dateToParse,e);
            return null;
        }
    }
}
