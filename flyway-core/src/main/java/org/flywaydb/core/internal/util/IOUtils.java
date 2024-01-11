package org.flywaydb.core.internal.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IOUtils {
    /**
     * Closes this closeable and never fails while doing so.
     *
     * @param closeable The closeable to close. Can be {@code null}.
     */
    public static void close(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (Exception ignored) {}
    }
}