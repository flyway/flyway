/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.sample.appengine;

import com.google.appengine.api.rdbms.AppEngineDriver;
import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.util.DateUtils;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet for querying the history of the Google Cloud SQL instance.
 */
public class HistoryServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException("GET not supported");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DriverDataSource dataSource = new DriverDataSource(
                new AppEngineDriver(),
                "jdbc:google:rdbms://flyway-test-project:flyway-sample/flyway_sample_appengine",
                "",
                "");

        Flyway flyway = new Flyway();
        flyway.setBasePackage("com.googlecode.flyway.sample.appengine.migration");
        flyway.setDataSource(dataSource);

        List<MetaDataTableRow> metaDataTableRows = flyway.history();

        response.setContentType("application/json");

        PrintWriter writer = response.getWriter();
        writer.print("{\"status\":\"OK\", \"rows\":[");
        boolean first = true;
        for (MetaDataTableRow row : metaDataTableRows) {
            if (!first) {
                writer.print(",");
            }

            writer.print("{\"version\":\"" + row.getVersion() + "\",");
            writer.print("\"description\":\"" + row.getDescription() + "\",");
            writer.print("\"installedOn\":\"" + DateUtils.formatDateAsIsoString(row.getInstalledOn()) + "\",");
            writer.print("\"state\":\"" + row.getState().name() + "\"}");

            first = false;
        }
        writer.print("]}");
    }
}
