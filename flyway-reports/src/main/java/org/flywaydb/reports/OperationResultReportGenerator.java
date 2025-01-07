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
package org.flywaydb.reports;

import static org.flywaydb.reports.utils.OperationsReportUtils.filterHtmlResults;
import static org.flywaydb.reports.utils.OperationsReportUtils.getAggregateExceptions;
import static org.flywaydb.reports.utils.OperationsReportUtils.writeReport;

import java.time.LocalDateTime;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.internal.reports.ReportDetails;
import org.flywaydb.core.internal.reports.ReportGenerationOutput;
import org.flywaydb.core.internal.reports.ResultReportGenerator;

public class OperationResultReportGenerator implements ResultReportGenerator {

    @Override
    public ReportGenerationOutput generateReport(final OperationResult operationResult,
        final Configuration configuration,
        final LocalDateTime executionTime) {
        ReportDetails reportDetails = new ReportDetails();
        Exception aggregateException = null;
        
        final OperationResult filteredResults = filterHtmlResults(operationResult);
        if (filteredResults != null) {
            reportDetails = writeReport(configuration, filteredResults, executionTime);
            aggregateException = getAggregateExceptions(filteredResults);
        }
        
        return new ReportGenerationOutput(reportDetails, aggregateException);
    }
}
