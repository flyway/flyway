/*-
 * ========================LICENSE_START=================================
 * flyway-nc-callbacks
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
package org.flywaydb.nc.callbacks;

import static org.flywaydb.core.internal.util.FileUtils.getParentDir;
import static org.flywaydb.nc.utils.ErrorUtils.calculateErrorMessage;

import java.nio.file.Paths;
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
import org.flywaydb.core.experimental.CallbackHandler;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.nc.utils.ErrorUtils;
import org.flywaydb.nc.utils.VerbUtils;
import org.flywaydb.nc.executors.NonJdbcExecutorExecutionUnit;
import org.flywaydb.core.experimental.Executor;
import org.flywaydb.nc.executors.ExecutorFactory;
import org.flywaydb.nc.executors.JdbcExecutor;
import org.flywaydb.core.experimental.Reader;
import org.flywaydb.nc.readers.ReaderFactory;

@CustomLog
public class NativeConnectorsCallbackHandler implements CallbackHandler {
    private final Collection<Callback> callbacks = new ArrayList<>();

    @Override
    public void handleEvent(final Event event,
        final ExperimentalDatabase database,
        final Configuration configuration,
        final ParsingContext parsingContext) {
        callbacks.stream()
            .filter(x -> x.supports(event))
            .forEach(x -> handleEvent(x, database, configuration, parsingContext));
    }

    private void handleEvent(final Callback callback,
        final ExperimentalDatabase database,
        final Configuration configuration,
        final ParsingContext parsingContext) {
        final Reader<Object> reader = ReaderFactory.getReader(database, configuration);
        final Stream<Object> executionUnits = reader.read(configuration,
            database,
            parsingContext,
            callback.getLoadableResourceMetadata().loadableResource(),
            callback.getLoadableResourceMetadata().sqlScriptMetadata());

        final Executor executor = ExecutorFactory.getExecutor(database, configuration);

        LOG.info("Callback executed: " + callback.getEvent().name() + " from " + callback.getPhysicalLocation());

        try (final EventTelemetryModel telemetryModel = new EventTelemetryModel(callback.getEvent().getId(),
            VerbUtils.getFlywayTelemetryManager(configuration))) {
            executionUnits.forEach(executionUnit -> {

                Object executionUnitObj;
                if (executor instanceof JdbcExecutor) {
                    executionUnitObj = executionUnit;
                } else {
                    final String parentDir = getParentDir(callback.getLoadableResourceMetadata()
                        .loadableResource()
                        .getAbsolutePath());
                    executionUnitObj = new NonJdbcExecutorExecutionUnit((String) executionUnit, parentDir);
                }

                try {
                    executor.execute(database, executionUnitObj, configuration);
                } catch (Exception e) {
                    final String title = "Error while executing "
                        + callback.getEvent().getId()
                        + " callback: "
                        + ErrorUtils.getScriptExecutionErrorMessageTitle(Paths.get(callback.getFileName())
                        .getFileName(), configuration.getCurrentEnvironmentName());
                    final String errorMessage = calculateErrorMessage(title,
                        callback.getLoadableResourceMetadata().loadableResource(),
                        callback.getPhysicalLocation(),
                        executor,
                        executionUnitObj,
                        "Message    : " + e.getMessage() + "\n");

                    throw new FlywayException(errorMessage);
                }
            });
            executor.finishExecution(database, configuration);
        }
    }

    @Override
    public void registerCallbacks(final Collection<LoadableResourceMetadata> resources) {
        callbacks.clear();
        resources.stream().map(resource -> {
            Event event = Event.fromId(resource.prefix());
            if (event != null) {
                LOG.debug("Callback registered: " + event.name() + " from " + resource.loadableResource()
                    .getAbsolutePath());
                return new Callback(resource, event);
            }
            return null;
        }).filter(Objects::nonNull).forEach(callbacks::add);
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
