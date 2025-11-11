/*-
 * ========================LICENSE_START=================================
 * flyway-reports
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
package org.flywaydb.reports.api.extensibility;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.HtmlResult;
import java.util.List;
import org.flywaydb.core.extensibility.Plugin;

public interface HtmlRenderer<T extends HtmlResult> extends Plugin {
    String render(T result, Configuration config);
    String tabTitle(T result, Configuration config);
    Class<T> getType();
    default List<HtmlReportSummary> getHtmlSummary(final T result, final Configuration config) {
        return null;
    }
}
