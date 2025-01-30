/*-
 * ========================LICENSE_START=================================
 * flyway-experimental-callbacks
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.experimental.callbacks;

import static org.flywaydb.verb.ErrorUtils.calculateErrorMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.CustomLog;
import lombok.Value;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.verb.executors.Executor;
import org.flywaydb.verb.executors.ExecutorFactory;
import org.flywaydb.verb.readers.Reader;
import org.flywaydb.verb.readers.ReaderFactory;

@CustomLog
public class CallbackManager {
    private final Collection<Callback> callbacks = new ArrayList<>();
    
    public CallbackManager(final Collection<LoadableResourceMetadata> resources, final boolean skipDefaultCallbacks) {
        if (!skipDefaultCallbacks) {
            registerCallbacks(resources);
        }
    }

    private void registerCallbacks(final Collection<LoadableResourceMetadata> resources) {
        resources.stream()
            .map(resource -> {
                Event event = Event.fromId(resource.prefix());
                if (event != null) {
                    LOG.debug("Callback registered: " + event.name() + " from " + resource.loadableResource().getAbsolutePath());
                    return new Callback(resource, event);
                }
                return null;
            })
            .filter(Objects::nonNull)
            .forEach(callbacks::add);
    }

    public void handleEvent(final Event event, final ExperimentalDatabase database, final Configuration configuration, final ParsingContext parsingContext) {
        callbacks.stream().filter(x -> x.supports(event)).forEach(x -> handleEvent(x, database, configuration, parsingContext));
    }

    private void handleEvent(final Callback callback, final ExperimentalDatabase database, final Configuration configuration, final ParsingContext parsingContext) {
        final Reader<Object> reader = ReaderFactory.getReader(database, configuration);
        final Stream<Object> executionUnits = reader.read(configuration,
            database,
            parsingContext,
            callback.getLoadableResourceMetadata().loadableResource(),
            callback.getLoadableResourceMetadata().sqlScriptMetadata());
        
        final Executor<Object> executor = ExecutorFactory.getExecutor(database, configuration);

        LOG.info("Callback executed: " + callback.getEvent().name() + " from " +  callback.getPhysicalLocation());

        executionUnits.forEach(executionUnit -> {
            try {
                executor.execute(database, executionUnit, configuration);
            } catch (Exception e) {
                final String title = "Error while executing " + callback.getEvent().getId()
                    + " callback: Script " + callback.getFileName() + " failed";
                final String errorMessage = calculateErrorMessage(e,
                    title,
                    callback.getLoadableResourceMetadata().loadableResource(),
                    callback.getPhysicalLocation(),
                    executor,
                    executionUnit,
                    "Message    : " + e.getMessage() + "\n");

                throw new FlywayException(errorMessage);
            }
        });
        executor.finishExecution(database, configuration);
    }
}

@Value
class Callback {
    LoadableResourceMetadata loadableResourceMetadata;
    Event event;

    public String getFileName() {
        return loadableResourceMetadata.loadableResource().getFilename();
    }

    public String getPhysicalLocation() {
        return loadableResourceMetadata.loadableResource().getAbsolutePath();
    }

    boolean supports(final Event event) {
        return this.event.equals(event);
    }
}
