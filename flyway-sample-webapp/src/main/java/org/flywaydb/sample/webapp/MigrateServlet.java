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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for Migrating the DB instance.
 */
public class MigrateServlet extends HttpServlet {
    /**
     * The Flyway instance to use.
     */
    private final Flyway flyway = Environment.createFlyway();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int successCount = flyway.migrate();

        response.setContentType("application/json");
        response.getWriter().print("{\"status\":\"OK\", \"count\":" + successCount + "}");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException("GET not supported");
    }
}
