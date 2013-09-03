package fr.itinerennes.bundler.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Process {@link Property} annotations.
 * 
 * @author Jérémie Huchet
 */
public final class PropertyAnnotations {

    /**
     * Process annotations of the given instance. Same effect as {@link #process(String, Object)
     * process(String, null)}
     * 
     * @param o
     *            an object with {@link Property} annotations
     */
    public static void process(final Object o) {

        process(o.getClass().getSimpleName() + ".properties", o);
    }

    /**
     * Process annotations of the given instance.
     * 
     * @param filename
     *            the property filename to search in classpath
     * @param o
     *            an object with {@link Property} annotations
     */
    public static void process(final String filename, final Object o) {

        final Properties props = getProperties(filename);
        try {
            final InputStream in = PropertyAnnotations.class.getResourceAsStream(filename);
            if (in == null) {
                throw new FileNotFoundException(filename + " not found in classpath");
            }
            props.load(in);
        } catch (final IOException e) {
            final StringBuilder msg = new StringBuilder();
            msg.append("Unable to load properties. ");
            msg.append("Can't read file '").append(filename).append("'.");
            throw new RuntimeException(msg.toString(), e);
        }

        final Field[] fields = o.getClass().getDeclaredFields();
        for (final Field field : fields) {
            if (field.isAnnotationPresent(Property.class)) {
                final Property propHolder = field.getAnnotation(Property.class);
                loadProperty(o, field, propHolder, props);
            }
        }
    }

    /**
     * Loads properties from a file available in classpath.
     * 
     * @param filename
     *            a filename of a properties file to load
     * @return the loaded properties
     */
    private static Properties getProperties(final String filename) {

        final Properties props = new Properties();
        try {
            final InputStream in = PropertyAnnotations.class.getResourceAsStream(filename);
            if (in == null) {
                throw new FileNotFoundException(filename + " not found in classpath");
            }
            props.load(in);
        } catch (final IOException e) {
            final StringBuilder msg = new StringBuilder();
            msg.append("Unable to load properties. ");
            msg.append("Can't read file '").append(filename).append("'.");
            throw new RuntimeException(msg.toString(), e);
        }
        return props;
    }

    /**
     * Sets a field value.
     * <p>
     * If the annotation doesn't specify the property key, then the field name is used as the
     * property key.
     * <p>
     * If the annotation specifies the properties file to use, then the value is extracted from this
     * file.
     * 
     * @param o
     *            the object owning the field
     * @param field
     *            the field to set
     * @param propHolder
     *            the {@link Property} annotation
     * @param props
     *            the global properties to use
     */
    private static void loadProperty(final Object o, final Field field, final Property propHolder,
            final Properties props) {

        // get the property key
        final String propName;
        if ("".equals(propHolder.value())) {
            propName = field.getName();
        } else {
            propName = propHolder.value();
        }

        // get the property value
        final String value;
        if (!"".equals(propHolder.file())) {
            final Properties p = getProperties(propHolder.file());
            value = p.getProperty(propName);
        } else if (props.containsKey(propName)) {
            value = props.getProperty(propName);
        } else {
            value = null;
        }

        // set the field value
        AccessibleObject.setAccessible(new AccessibleObject[] { field }, true);
        try {
            field.set(o, value);
        } catch (final IllegalArgumentException e) {
            final String msg = String.format("Unable to set value '%s' in field '%s.%s'", value,
                    field.getDeclaringClass().getName(), field.getName());
            throw new RuntimeException(msg, e);
        } catch (final IllegalAccessException e) {
            final String msg = String.format("Unable to set value '%s' in field '%s.%s'", value,
                    field.getDeclaringClass().getName(), field.getName());
            throw new RuntimeException(msg, e);
        }
    }
}
