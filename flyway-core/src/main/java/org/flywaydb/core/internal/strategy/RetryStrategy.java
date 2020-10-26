/*
 * Copyright © Red Gate Software Ltd 2010-2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.strategy;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.SqlCallable;

import java.sql.SQLException;

/**
 * A class that retries a Callable a given number of times until success is obtained.
 */
public class RetryStrategy {

    /**
     * We hard-code a default of 50 retries here, but this may be overridden by configuration.
     */
    private static int numberOfRetries = 50;
    private static boolean unlimitedRetries;

    private int numberOfRetriesRemaining;

    /**
     * A class that retries a Callable a given number of times until success is obtained.
     */
    public RetryStrategy() {
        numberOfRetriesRemaining = numberOfRetries;
    }

    /**
     * Set the number of retries that are to be attempted before giving up.
     * @param retries The number of retries to attempt. To try forever, use -1.
     */
    public static void setNumberOfRetries(int retries) {
        numberOfRetries = retries;
        unlimitedRetries = (retries < 0);
    }

    private boolean hasMoreRetries() {
        return (unlimitedRetries || numberOfRetriesRemaining > 0);
    }

    private void nextRetry() {
        if (!unlimitedRetries) {
            numberOfRetriesRemaining--;
        }
    }

    private int nextWaitInMilliseconds() {
        return 1000;
    }

    /**
     * Keep retrying a Callable with a potentially varying wait on each iteration, until one of the following happens:
     * - the callable returns {@code true};
     * - an InterruptedException happens
     * - the number of retries is exceeded.
     *
     * @param callable               The callable to retry
     * @param interruptionMessage    The message to relay if interruption happens
     * @param retriesExceededMessage The message to relay if the number of retries is exceeded
     *
     * @throws SQLException
     */
    public void doWithRetries(SqlCallable<Boolean> callable, String interruptionMessage, String retriesExceededMessage) throws SQLException {
        while (!callable.call()) {
            try {
                Thread.sleep(nextWaitInMilliseconds());
            } catch (InterruptedException e) {
                throw new FlywayException(interruptionMessage, e);
            }

            if (!hasMoreRetries()) {
                throw new FlywayException(retriesExceededMessage);
            }
            nextRetry();
        }
    }
}