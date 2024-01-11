package org.flywaydb.core.internal.reports.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.flywaydb.core.api.output.InfoResult;
import org.flywaydb.core.internal.util.LocalDateTimeSerializer;

import java.time.LocalDateTime;

public class InfoResultDeserializer implements HtmlResultDeserializer<InfoResult> {

    @Override
    public boolean canDeserialize(JsonObject jsonObject) {
        return jsonObject.get("operation").getAsString().equals("info");
    }

    @Override
    public InfoResult deserialize(JsonObject jsonObject) {
        final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer()).create();
        return gson.fromJson(jsonObject, InfoResult.class);
    }
}