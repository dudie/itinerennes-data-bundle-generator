package fr.itinerennes.bundler.tasks.framework;

/*
 * [license]
 * Itinerennes data resources generator
 * ~~~~
 * Copyright (C) 2013 - 2014 Dudie
 * ~~~~
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * [/license]
 */

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
