package org.flywaydb.core.internal.util;

import java.util.Iterator;

/**
 * Iterator that can be used to close underlying resources.
 *
 * @param <T> The typo of element to iterate on.
 */
public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {
}