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
package org.flywaydb.core.internal.database.derby;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.ExecutableSqlScript;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.util.List;

/**
 * Derby-specific SQL script.
 */
class DerbySqlScript extends ExecutableSqlScript {
    DerbySqlScript(Configuration configuration, LoadableResource sqlScriptResource, boolean mixed




            , PlaceholderReplacer placeholderReplacer) {
        super(configuration, sqlScriptResource, mixed



                , placeholderReplacer
        );
    }

    @Override
    protected SqlStatementBuilder createSqlStatementBuilder() {
        return new DerbySqlStatementBuilder(Delimiter.SEMICOLON);
    }
}