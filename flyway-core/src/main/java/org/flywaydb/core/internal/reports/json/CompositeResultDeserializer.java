package org.flywaydb.core.internal.reports.json;

import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.output.HtmlResult;
import org.flywaydb.core.api.output.*;
import org.flywaydb.core.internal.plugin.PluginRegister;

import java.lang.reflect.Type;
import java.util.List;

@RequiredArgsConstructor
public class CompositeResultDeserializer implements JsonDeserializer<CompositeResult<HtmlResult>> {

    private final PluginRegister pluginRegister;
    @Override
    public CompositeResult<HtmlResult> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        CompositeResult<HtmlResult> result = new CompositeResult<>();

        final JsonObject obj = jsonElement.getAsJsonObject();
        final JsonElement individualResults = obj.get("individualResults");
        final JsonArray irArray = individualResults.getAsJsonArray();
        final List<HtmlResultDeserializer> deserializers = pluginRegister.getPlugins(HtmlResultDeserializer.class);

        for (Object object : irArray) {
            JsonObject jsonObject = (JsonObject) object;
            deserializers.stream()
                         .filter(d -> d.canDeserialize(jsonObject))
                         .findFirst()
                         .ifPresent(d -> result.individualResults.add(d.deserialize(jsonObject)));
        }

        return result;
    }
}