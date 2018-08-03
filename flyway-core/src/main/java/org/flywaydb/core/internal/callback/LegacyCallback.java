/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.callback;

import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.FlywayCallback;

public class LegacyCallback implements Callback {
    private final FlywayCallback flywayCallback;

    public LegacyCallback(FlywayCallback flywayCallback) {
        this.flywayCallback = flywayCallback;
    }

    @Override
    public boolean supports(Event event, Context context) {
        return true;
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }

    @Override
    public void handle(Event event, Context context) {
        switch (event) {
            case BEFORE_CLEAN:
                flywayCallback.beforeClean(context.getConnection());
                return;
            case AFTER_CLEAN:
                flywayCallback.afterClean(context.getConnection());
                return;
            case BEFORE_MIGRATE:
                flywayCallback.beforeMigrate(context.getConnection());
                return;
            case BEFORE_EACH_MIGRATE:
                flywayCallback.beforeEachMigrate(context.getConnection(), context.getMigrationInfo());
                return;
            case AFTER_EACH_MIGRATE:
                flywayCallback.afterEachMigrate(context.getConnection(), context.getMigrationInfo());
                return;
            case AFTER_MIGRATE:
                flywayCallback.afterMigrate(context.getConnection());
                return;
            case BEFORE_VALIDATE:
                flywayCallback.beforeValidate(context.getConnection());
                return;
            case AFTER_VALIDATE:
                flywayCallback.afterValidate(context.getConnection());
                return;














            case BEFORE_INFO:
                flywayCallback.beforeInfo(context.getConnection());
                return;
            case AFTER_INFO:
                flywayCallback.afterInfo(context.getConnection());
                return;
            case BEFORE_BASELINE:
                flywayCallback.beforeBaseline(context.getConnection());
                return;
            case AFTER_BASELINE:
                flywayCallback.afterBaseline(context.getConnection());
                return;
            case BEFORE_REPAIR:
                flywayCallback.beforeRepair(context.getConnection());
                return;
            case AFTER_REPAIR:
                flywayCallback.afterRepair(context.getConnection());
                return;
            default:
                // Ignore other events
        }
    }
}