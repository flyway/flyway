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
import java.util.Collections;
import java.util.Objects;
import lombok.CustomLog;
import lombok.Value;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.internal.exception.FlywayMigrateException;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.verb.FileReadingWithPlaceholderReplacement;

@CustomLog
public class CallbackManager {
    private final Collection<Callback> callbacks = new ArrayList<>();

    public CallbackManager(Collection<LoadableResourceMetadata> resources) {
        registerCallbacks(resources);
    }

    private void registerCallbacks(Collection<LoadableResourceMetadata> resources) {
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

    public void handleEvent(Event event, ExperimentalDatabase database, Configuration configuration, ParsingContext parsingContext) {
        callbacks.stream().filter(x -> x.supports(event)).forEach(x -> handleEvent(x, database, configuration, parsingContext));
    }

    private void handleEvent(Callback callback, ExperimentalDatabase database, Configuration configuration, ParsingContext parsingContext) {
         final String executionUnit = FileReadingWithPlaceholderReplacement.readFile(configuration, parsingContext, callback.getPhysicalLocation());

         try {
             database.doExecute(executionUnit, configuration.isOutputQueryResults());
         } catch (Exception e) {
             String title = "Error while executing " + callback.getEvent().getId() + " callback: Script " + callback.getFileName() + " failed";
             String errorMessage = calculateErrorMessage(e, title,
                 callback.getLoadableResourceMetadata().loadableResource(),
                 callback.getPhysicalLocation(), null,
                 "Message    : " + e.getMessage() + "\n");

             throw new FlywayException(errorMessage);
         }

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

    boolean supports(Event event) {
        return this.event.equals(event);
    }
}
