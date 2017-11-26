/*
 * Copyright 2010-2017 Boxfuse GmbH
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
