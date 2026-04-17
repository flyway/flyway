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
package org.flywaydb.core.internal.reports;

public class ReportGenerationOutputMerger {

    public static ReportGenerationOutput merge(final ReportGenerationOutput first, final ReportGenerationOutput second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }

        final ReportDetails mergedDetails = mergeReportDetails(first.reportDetails, second.reportDetails);
        final Exception mergedException = mergeExceptions(first.aggregateException, second.aggregateException);

        return new ReportGenerationOutput(mergedDetails, mergedException);
    }

    private static ReportDetails mergeReportDetails(final ReportDetails first, final ReportDetails second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        final ReportDetails merged = new ReportDetails();
        merged.setJsonReportFilename(first.getJsonReportFilename() != null ? first.getJsonReportFilename() : second.getJsonReportFilename());
        merged.setHtmlReportFilename(first.getHtmlReportFilename() != null ? first.getHtmlReportFilename() : second.getHtmlReportFilename());
        merged.setSarifReportFilename(first.getSarifReportFilename() != null ? first.getSarifReportFilename() : second.getSarifReportFilename());
        return merged;
    }

    private static Exception mergeExceptions(final Exception first, final Exception second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        first.addSuppressed(second);
        return first;
    }
}
