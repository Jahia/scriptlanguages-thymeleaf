package org.jahia.services.render.scripting.thymeleaf;

import org.thymeleaf.Arguments;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.messageresolver.MessageResolution;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by loom on 05.05.15.
 */
public class ThymeLeafMessageResolver extends AbstractMessageResolver {

    private static final Object[] EMPTY_MESSAGE_PARAMETERS = new Object[0];

    @Override
    public MessageResolution resolveMessage(Arguments arguments, String key, Object[] messageParameters) {
        checkInitialized();

        final Locale locale = arguments.getContext().getLocale();
        // here we use reflection to bypass class loading issues on the LocalizationContext class
        Object localizationContextObject = arguments.getContext().getVariables().get("javax.servlet.jsp.jstl.fmt.localizationContext" + ".request");
        Method getResourceBundleMethod = null;
        String message = null;
        try {
            getResourceBundleMethod = localizationContextObject.getClass().getMethod("getResourceBundle", null);
            ResourceBundle resourceBundle = (ResourceBundle) getResourceBundleMethod.invoke(localizationContextObject, null);

            final String messageValue = resourceBundle.getString(key);

            final MessageFormat messageFormat = new MessageFormat(messageValue, locale);
            message = messageFormat.format((messageParameters != null? messageParameters : EMPTY_MESSAGE_PARAMETERS));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (message == null) {
            return null;
        }

        return new MessageResolution(message);

    }
}
