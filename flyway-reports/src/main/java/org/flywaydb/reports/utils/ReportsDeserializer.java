/*-
 * ========================LICENSE_START=================================
 * flyway-reports
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
package org.flywaydb.reports.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.reports.json.HtmlResultDeserializer;

class ReportsDeserializer extends JsonDeserializer {
    private final PluginRegister pluginRegister;

    public ReportsDeserializer(final PluginRegister pluginRegister) {
        this.pluginRegister = pluginRegister;
    }

    @Override
    public Object deserialize(final JsonParser p, final DeserializationContext ctxt)
        throws IOException, JacksonException {
        final JsonNode reportElement = ctxt.readTree(p);
        if (reportElement.has("operation")) {
            final String operation = reportElement.get("operation").asText();
            final List<HtmlResultDeserializer> deserializers = pluginRegister.getInstancesOf(HtmlResultDeserializer.class);
            final HtmlResultDeserializer matchedDeserializer = deserializers.stream()
                .filter(x -> x.operationKey().equals(operation)).findFirst().orElseThrow(()-> new FlywayException(
                    "Unable to find matching deserializer for " + operation));
            return ctxt.readTreeAsValue(reportElement, matchedDeserializer.getDeserializingClass());
        }
        throw new FlywayException("Unable to deserialize report. Corrupt json report file");
    }
}
