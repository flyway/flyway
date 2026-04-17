/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.core.api.logging;

public record ProgressBar(Progress progress) {
    private static final String PROGRESS_FILL = "#";
    private static final String PROGRESS_EMPTY = "-";
    private static final float PROGRESS_BAR_SIZE = 80f;
    private static final float PERCENTAGE_FACTOR = 100f;

    public record Progress(int percent, String message) {}

    @Override
    public String toString() {
        return PROGRESS_FILL.repeat(convertToBarSize(Math.max(0, progress.percent)))
            + PROGRESS_EMPTY.repeat(convertToBarSize(Math.max(0, 100 - progress.percent)))
            + " "
            + progress.percent
            + "%\t:\t"
            + progress.message;
    }

    private int convertToBarSize(final int percent) {
        return (int) ((Math.max(0, percent) / PERCENTAGE_FACTOR) * PROGRESS_BAR_SIZE);
    }
}
