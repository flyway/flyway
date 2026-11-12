/*-
 * ========================LICENSE_START=================================
 * flyway-nc-callbacks
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import org.flywaydb.core.api.callback.CallbackEvent;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.GenericCallback;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.internal.callback.InternalCallback;
import org.flywaydb.core.internal.callback.SimpleContext;
import org.flywaydb.core.internal.nc.CallbackHandler;
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.core.internal.parser.ParsingContext;

public class InternalCallbackHandler implements CallbackHandler {
    private final Collection<InternalCallback<?>> callbacks = new ArrayList<>();

    @Override
    public void registerCallbacks(final Configuration configuration,
        final Collection<LoadableResourceMetadata> resources,
        final Function<String, ? extends CallbackEvent<?>> eventResolver) {
        callbacks.clear();
        callbacks.addAll(configuration.getPluginRegister()
            .getInstancesOf(InternalCallback.class)
            .stream()
            .map(x -> (InternalCallback<?>) x)
            .toList());
    }

    @Override
    public void handleEvent(final CallbackEvent<?> event,
        final NativeConnectorsDatabase database,
        final Configuration configuration,
        final ParsingContext parsingContext) {
        handleEvent(event, database, configuration, parsingContext, null);
    }

    @Override
    public void handleEvent(final CallbackEvent<?> event,
        final NativeConnectorsDatabase database,
        final Configuration configuration,
        final ParsingContext parsingContext,
        final OperationResult operationResult) {
        final Context context = new SimpleContext(configuration, null, null, operationResult, database);
        callbacks.stream()
            .filter(x -> x.supportsEventType((Class<? extends CallbackEvent<?>>) event.getClass()))
            .map(x -> (GenericCallback) x)
            .filter(x -> x.supports(event, context))
            .forEach(x -> x.handle(event, context));
    }
}
