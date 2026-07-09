/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.callback;

import java.util.Optional;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.GenericCallback;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resolver.script.ScriptMigrationResolver;

public class PreConnectCallbackExecutor {
    private final Configuration configuration;
    private final ScriptMigrationResolver<Event> scriptMigrationResolver;

    public PreConnectCallbackExecutor(final ResourceProvider resourceProvider,
        final Configuration configuration,
        final ParsingContext parsingContext,
        final StatementInterceptor statementInterceptor) {
        this.configuration = configuration;
        this.scriptMigrationResolver = new ScriptMigrationResolver<>(resourceProvider,
            configuration,
            parsingContext,
            statementInterceptor);
    }

    public void executeCallbacks() {
        scriptMigrationResolver.resolveCallbacks((final String id) -> Optional.ofNullable(Event.fromId(id)));
        final Context context = new SimpleContext(configuration);
        for (final GenericCallback<Event> callback : scriptMigrationResolver.scriptCallbacks) {
            if (callback.supports(Event.BEFORE_CONNECT, context)) {
                callback.handle(Event.BEFORE_CONNECT, context);
            }
        }
    }
}
