/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core;

import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.extensibility.EventTelemetryModel;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TelemetrySpan {
    public static <T, U extends EventTelemetryModel> T trackSpan(@Nullable final U telemetryModel,
        final Function<U, T> codeToTrack) {
        if (telemetryModel != null) {
            try (telemetryModel) {
                return codeToTrack.apply(telemetryModel);
            } catch (final Exception e) {
                telemetryModel.setException(e);
                //noinspection ProhibitedExceptionThrown
                throw e;
            }
        } else {
            return codeToTrack.apply(null);
        }
    }
}
