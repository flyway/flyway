/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.google.code.flyway.core.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * measure execution time for log statements
 */
public class LogTimer {

    private long start;
    private long stop;
    private boolean stopped = false;

    public LogTimer() {
        start = System.currentTimeMillis();
    }

    public void stop() {
        stopped = true;
        stop = System.currentTimeMillis();
    }

    public String getFormatted() {
        if (!stopped) {
            stop();
        }
        return format(stop - start);
    }

    public static String format(long millis) {
        DateFormat formatter = new SimpleDateFormat("mm:ss,S");
        return formatter.format(new Date(millis));
    }
}
