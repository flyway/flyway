/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.extensibility;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.HtmlResult;

import java.util.Collections;

import static org.flywaydb.core.internal.reports.html.HtmlReportGenerator.getInformationalText;
import static org.flywaydb.core.internal.reports.html.HtmlReportGenerator.getSummaryStyle;

public interface HtmlRenderer<T extends HtmlResult> extends Plugin {
    String render(T result, Configuration config);

    String tabTitle(T result);

    Class<T> getType();

    default String getSummary(T result) {
        String separator = String.join("", Collections.nCopies(8, "&nbsp;"));
        return "<div " + getSummaryStyle() + ">\n" +
                "<div>" + getSummaryText(result, separator, true) + "</div>" +
                "<div>" + getInformationalText() + "</div>\n" +
                "</div>\n";
    }

   default String getSummaryText(T result, String separator, boolean addStyles) {
        return null;
    }
}