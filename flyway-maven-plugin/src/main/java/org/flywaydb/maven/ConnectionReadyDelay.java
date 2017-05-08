/**
 * Copyright 2010-2016 Boxfuse GmbH
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.maven;

import org.flywaydb.core.internal.util.logging.Log;

import java.util.concurrent.TimeUnit;

/**
 * A delay-retry mechanism to wait for database connection.
 *
 * @author nick.ilkevich@gmail.com
 */
class ConnectionReadyDelay {

    private final int maxRetries;
    private final Log log;
    private int retries;

    ConnectionReadyDelay(final Log log, final Integer maxRetries) {
        this.log = log;
        this.maxRetries = (maxRetries != null && maxRetries > 0) ? maxRetries : 0;
        this.retries = 0;
    }


    void waitTillConnectionReady(ConnectionChecker checker) throws Exception {
        try {
            checker.check();
        } catch (Exception e) {
            retries++;

            if (retries > maxRetries) {
                throw e;
            }

            log.info(String.format("[%s/%s] Retry... %s", retries, maxRetries, e.getMessage()));
            sleep();
            waitTillConnectionReady(checker);
        }
    }


    void sleep() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
    }

    /**
     * Interface to checking connection
     */
    interface ConnectionChecker {
        void check() throws Exception;
    }

}
