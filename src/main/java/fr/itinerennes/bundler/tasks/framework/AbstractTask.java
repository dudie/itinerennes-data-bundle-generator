package fr.itinerennes.bundler.tasks.framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTask.class);

    private final AnnotationsHelper annotations = new AnnotationsHelper();

    public void run() {
        try {
            runAnnotatedMethods(PreExec.class);
            execute();
        } finally {
            runAnnotatedMethods(PostExec.class);
        }
    }

    private <T extends Annotation> void runAnnotatedMethods(final Class<T> annotationClass) {
        for (final Method m : annotations.getMethods(this.getClass(), annotationClass)) {
            try {
                LOGGER.debug("Attempting to invoke {}() for task {}", m.getName(), this.getClass().getSimpleName());
                m.invoke(this);
            } catch (final Exception e) {
                final String msg = String.format("Unable to invoke task method: %s#%s()", this.getClass().getName(), m.getName());
                LOGGER.error(msg, e);
                throw new IllegalStateException(msg, e);
            }
        }
    }

    protected abstract void execute();

    class AnnotationsHelper {

        <T extends Annotation> List<Method> getMethods(Class<?> clazz, Class<T> annotationClass) {

            final Method[] allMethods = clazz.getMethods();
            final List<Method> havingAnnotation = new ArrayList<Method>();

            for (final Method m : allMethods) {
                if (null != m.getAnnotation(annotationClass)) {
                    havingAnnotation.add(m);
                }
            }

            return havingAnnotation;
        }
    }
}
