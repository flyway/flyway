/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.sample.webapp;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.internal.util.DateUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet for querying the migration info.
 */
public class InfoServlet extends HttpServlet {
    /**
     * The Flyway instance to use.
     */
    private final Flyway flyway = Environment.createFlyway();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException("POST not supported");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        MigrationInfoService migrationInfoService = flyway.info();

        response.setContentType("application/json");

        PrintWriter writer = response.getWriter();
        writer.print("{\"status\":\"OK\", \"rows\":[");
        boolean first = true;
        for (MigrationInfo migrationInfo : migrationInfoService.all()) {
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
