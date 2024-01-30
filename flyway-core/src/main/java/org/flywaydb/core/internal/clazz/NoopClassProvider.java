package org.flywaydb.core.internal.clazz;

import org.flywaydb.core.api.ClassProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * ClassProvider that does nothing.
 */
public enum NoopClassProvider implements ClassProvider {
    INSTANCE;

    @Override
    public Collection<Class<?>> getClasses() {
        return Collections.emptyList();
    }
}