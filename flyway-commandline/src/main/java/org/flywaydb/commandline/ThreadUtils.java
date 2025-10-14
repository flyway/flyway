/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.commandline;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

class ThreadUtils {
    static void terminate(final int exitCode, final AutoCloseable flywayTelemetryHandle) {
        try {
            flywayTelemetryHandle.close();
        } catch(final Exception ignore){}

        if (exitCode != 0) {
            System.exit(exitCode);
        }

        final Thread t = new Thread(() -> {
            try {
                Thread.sleep(500);
                for (int i = 1; i <= 5; i++) {
                    stopThreads();
                    Thread.sleep(100);
                }
                Runtime.getRuntime().halt(0);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private static void stopThreads(){
        final Thread current = Thread.currentThread();
        final ThreadGroup root = getRootThreadGroup();

        final Thread[] threads = snapshotThreads(root, current);
        if (threads.length == 0) {
            return;
        }

        // First pass: interrupt eligible threads
        for (final Thread eligibleThread : threads) {
            eligibleThread.interrupt();
        }

        // Second pass: wait for them to finish (bounded)
        final long deadline = System.nanoTime() + Duration.ofSeconds(1).toNanos();
        for (final Thread eligibleThread : threads) {
            final long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                break;
            }

            // join with remaining time
            final long ms = Math.max(0, remaining / 1_000_000);
            final int ns = (int) Math.max(0, remaining % 1_000_000);
            try {
                eligibleThread.join(ms, ns);
            } catch (final InterruptedException ignore) {
            }
        }
    }

    private static Thread[] snapshotThreads(final ThreadGroup root, final Thread current) {
        int size;
        Thread[] threads;
        int n = root.activeCount();
        do {
            size = n * 2;
            threads = new Thread[size];
            n = root.enumerate(threads, true);
        } while (size > n);

        final Collection<String> knownSystemThreads = new HashSet<>(Arrays.asList(
            "Finalizer", "Reference Handler", "Signal Dispatcher", "Common-Cleaner"));

        return Arrays.stream(threads)
            .filter(Objects::nonNull)
            .filter(t -> t != current)
            .filter(t -> !t.isDaemon())
            .filter(t -> !knownSystemThreads.contains(t.getName()))
            .toArray(Thread[]::new);
    }

    private static ThreadGroup getRootThreadGroup() {
        ThreadGroup current = Thread.currentThread().getThreadGroup();
        ThreadGroup parent = current.getParent();
        while (parent != null) {
            current = parent;
            parent = current.getParent();
        }
        return current;
    }
}
