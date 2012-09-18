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
package com.googlecode.flyway.sample.webapp;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationInfos;
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
 * Servlet for querying the migration info.
 */
public class InfoServlet extends HttpServlet {
    /**
     * The datasource to use.
     */
    private final DataSource dataSource = Environment.createDataSource();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException("POST not supported");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Flyway flyway = new Flyway();
        flyway.setLocations("db.migration",
                "db/more/migrations",
                "com.googlecode.flyway.sample.migration",
                "com/googlecode/flyway/sample/webapp/migration");
        flyway.setDataSource(dataSource);

        MigrationInfos migrationInfos = flyway.info();

        response.setContentType("application/json");

        PrintWriter writer = response.getWriter();
        writer.print("{\"status\":\"OK\", \"rows\":[");
        boolean first = true;
        for (MigrationInfo migrationInfo : migrationInfos.all()) {
            if (!first) {
                writer.print(",");
            }

            writer.print("{\"version\":\"" + migrationInfo.getVersion() + "\",");
            
            String description = migrationInfo.getDescription() == null ? "" : migrationInfo.getDescription();
            writer.print("\"description\":\"" + description + "\",");
            writer.print("\"script\":\"" + migrationInfo.getScript() + "\",");
            writer.print("\"type\":\"" + migrationInfo.getType() + "\",");

            writer.print("\"installedOn\":\"" + DateUtils.formatDateAsIsoString(migrationInfo.getInstalledOn()) + "\",");
            writer.print("\"state\":\"" + migrationInfo.getState().name() + "\"}");

            first = false;
        }
        writer.print("]}");
    }
}
