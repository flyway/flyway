/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.core.internal.reports.json;

        import com.google.gson.Gson;
        import com.google.gson.GsonBuilder;
        import com.google.gson.JsonObject;
        import org.flywaydb.core.api.output.InfoResult;
        import org.flywaydb.core.api.output.MigrateResult;
        import org.flywaydb.core.internal.util.LocalDateTimeSerializer;

        import java.time.LocalDateTime;

public class MigrateResultDeserializer implements HtmlResultDeserializer<MigrateResult> {

    @Override
    public boolean canDeserialize(JsonObject jsonObject) {
        return jsonObject.get("operation").getAsString().equals("migrate");
    }

    @Override
    public MigrateResult deserialize(JsonObject jsonObject) {
        final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer()).create();
        return gson.fromJson(jsonObject, MigrateResult.class);
    }
}