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

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.util.DateUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet for querying the history of the DB instance.
 */
public class HistoryServlet extends HttpServlet {
    /**
     * The datasource to use.
     */
    private final DataSource dataSource = Environment.createDataSource();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException("POST not supported");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Flyway flyway = new Flyway();
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
            
            String description = row.getDescription() == null ? "" : row.getDescription();
            writer.print("\"description\":\"" + description + "\",");
            writer.print("\"script\":\"" + row.getScript() + "\",");
            writer.print("\"type\":\"" + row.getMigrationType() + "\",");

            writer.print("\"installedOn\":\"" + DateUtils.formatDateAsIsoString(row.getInstalledOn()) + "\",");
            writer.print("\"state\":\"" + row.getState().name() + "\"}");

            first = false;
        }
        writer.print("]}");
    }
}
