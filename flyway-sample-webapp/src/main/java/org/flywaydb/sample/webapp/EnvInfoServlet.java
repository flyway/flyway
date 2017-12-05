/*
 * Copyright 2010-2017 Boxfuse GmbH
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
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet for serving the home page.
 */
public class EnvInfoServlet extends HttpServlet {
    /**
     * The Flyway instance to use.
     */
    private final Flyway flyway = Environment.createFlyway();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException("POST not supported");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String appserver;
        FeatureDetector featureDetector = new FeatureDetector(Thread.currentThread().getContextClassLoader());
        if (featureDetector.isJBossVFSv2Available()) {
            appserver = "JBoss 5";
        } else if (featureDetector.isJBossVFSv3Available()) {
            appserver = "JBoss 6+";
        } else {
            appserver = "Other";
        }

        String database = ((DriverDataSource) flyway.getDataSource()).getUrl();

        PrintWriter writer = response.getWriter();
        writer.print("{\"status\":\"OK\"," +
                " \"appserver\":\"" + appserver + "\"," +
                " \"database\":\"" + database + "\"}");
    }
}
