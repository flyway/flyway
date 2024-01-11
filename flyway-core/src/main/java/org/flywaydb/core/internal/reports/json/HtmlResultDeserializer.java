package org.flywaydb.core.internal.reports.json;

import com.google.gson.JsonObject;
import org.flywaydb.core.api.output.HtmlResult;
import org.flywaydb.core.extensibility.Plugin;

public interface HtmlResultDeserializer<T extends HtmlResult> extends Plugin {

    boolean canDeserialize(JsonObject jsonObject);

    T deserialize(JsonObject jsonObject);
}