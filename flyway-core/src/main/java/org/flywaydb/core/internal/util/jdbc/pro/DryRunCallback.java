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
package org.flywaydb.core.internal.util.jdbc.pro;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.BaseFlywayCallback;

import java.sql.Connection;

public class DryRunCallback extends BaseFlywayCallback {
    private final DryRunStatementInterceptor dryRunStatementInterceptor;

    public DryRunCallback(DryRunStatementInterceptor dryRunStatementInterceptor) {
        this.dryRunStatementInterceptor = dryRunStatementInterceptor;
    }

    @Override
    public void beforeClean(Connection connection) {
        dryRunStatementInterceptor.interceptCommand("clean");
    }

    @Override
    public void beforeMigrate(Connection connection) {
        dryRunStatementInterceptor.interceptCommand("migrate");
    }

    @Override
    public void beforeEachMigrate(Connection connection, MigrationInfo info) {
        dryRunStatementInterceptor.interceptCommand("migrate -> " + (info.getVersion() == null
                ? info.getDescription() + " [repeatable]"
                : "v" + info.getVersion()));
    }

    @Override
    public void beforeUndo(Connection connection) {
        dryRunStatementInterceptor.interceptCommand("undo");
    }

    @Override
    public void beforeEachUndo(Connection connection, MigrationInfo info) {
        dryRunStatementInterceptor.interceptCommand("undo -> v" + info.getVersion());
    }

    @Override
    public void beforeValidate(Connection connection) {
        dryRunStatementInterceptor.interceptCommand("validate");
    }

    @Override
    public void beforeBaseline(Connection connection) {
        dryRunStatementInterceptor.interceptCommand("baseline");
    }

    @Override
    public void beforeRepair(Connection connection) {
        dryRunStatementInterceptor.interceptCommand("repair");
    }

    @Override
    public void beforeInfo(Connection connection) {
        dryRunStatementInterceptor.interceptCommand("info");
    }
}
