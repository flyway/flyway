package org.flywaydb.core.internal.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.output.CompositeResult;
import org.flywaydb.core.api.output.OperationResult;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.flywaydb.core.internal.util.FileUtils.createDirIfNotExists;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

    public static String jsonToFile(String filename, Object json) {

        File file= new File(filename);

        try {
            createDirIfNotExists(file);
        } catch (UnsupportedOperationException ignore) {

        }

        try (FileWriter fileWriter = new FileWriter(file)) {
            getGson().toJson(json, fileWriter);
            return file.getCanonicalPath();
        } catch (Exception e) {
            throw new FlywayException("Unable to write JSON to file: " + e.getMessage());
        }
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .create();
    }

    public static <T> List<T> toList(String json) {
        Type listType = new TypeToken<ArrayList<T>>() { }.getType();
        return getGson().fromJson(json, listType);
    }

    public static String getFromJson(String json, String key) {
        return getGson().fromJson(json, JsonObject.class).get(key).getAsString();
    }

    public static <T extends OperationResult> CompositeResult<T> appendIfExists(String filename, CompositeResult<T> json, JsonDeserializer<CompositeResult<T>> deserializer) {
        if (!Files.exists(Paths.get(filename))) {
            return json;
        }

        CompositeResult<T> existingObject;
        Type existingObjectType = new TypeToken<CompositeResult<T>>() { }.getType();

        try (FileReader reader = new FileReader(filename)) {

            existingObject = new GsonBuilder()
                    .registerTypeAdapter(existingObjectType, deserializer)
                    .create()
                    .fromJson(reader, existingObjectType);
        } catch (Exception e) {
            throw new FlywayException("Unable to read filename: " + filename, e);
        }

        if (existingObject == null) {
            return json;
        }

        existingObject.individualResults.addAll(json.individualResults);
        return existingObject;
    }

    public static Object parseJsonArray(String json) {
        return JsonParser.parseString(json).getAsJsonArray();
    }

    public static <T> T parseJson(String json, Class<T> clazz) {
        return getGson().fromJson(json, clazz);
    }

    public static String prettyPrint(String json) {
        String output;
        try {
            JsonReader reader = new JsonReader(new StringReader(json));
            reader.setLenient(true);
            output = getGson().newBuilder().setLenient().create().toJson(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (Exception ignore) {
            output = json;
        }
        output = output.replace("\\r\\n", System.lineSeparator());
        output = output.replace("\\n", System.lineSeparator());
        return output;
    }
}