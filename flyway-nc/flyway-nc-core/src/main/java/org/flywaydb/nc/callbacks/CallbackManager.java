/*-
 * ========================LICENSE_START=================================
 * flyway-nc-core
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
package org.flywaydb.nc.callbacks;

import java.util.Collection;
import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.internal.nc.CallbackHandler;
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.core.internal.parser.ParsingContext;

@CustomLog
public class CallbackManager {
    private final List<? extends CallbackHandler> callbackHandlers;

    public CallbackManager(final Configuration configuration, final Collection<LoadableResourceMetadata> resources) {
        callbackHandlers = configuration.getPluginRegister().getInstancesOf(CallbackHandler.class);

        if (callbackHandlers.isEmpty()){
            LOG.warn("Native Connectors Mode is set but no callback handlers loaded");
            return;
        }

        if (!configuration.isSkipDefaultCallbacks()) {
            callbackHandlers.forEach(x -> x.registerCallbacks(resources));
        }
    }

    public void handleEvent(final Event event,
        final NativeConnectorsDatabase database,
        final Configuration configuration,
        final ParsingContext parsingContext) {
        callbackHandlers.forEach(x -> x.handleEvent(event, database, configuration, parsingContext));
    }
}
