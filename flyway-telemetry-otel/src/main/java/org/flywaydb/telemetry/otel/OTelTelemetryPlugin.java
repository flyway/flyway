/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.telemetry.otel;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.extensibility.RootTelemetryModel;
import org.flywaydb.core.extensibility.TelemetryPlugin;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.telemetry.otel.exceptions.FlywaySanitizedException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OTelTelemetryPlugin implements TelemetryPlugin {
    private OpenTelemetry openTelemetry;
    private Tracer tracer;
    private Span rootSpan;
    private Context mainContext;
    private RootTelemetryModel rootTelemetryModel;
    private Scope scope;

    private void ensureConnected() {
        if (openTelemetry == null) {
            connect();
        }
    }

    @Override
    public void logRootDetails(RootTelemetryModel rootTelemetryModel) {
        ensureConnected();
        this.rootTelemetryModel = rootTelemetryModel;
    }

    private AttributesMap rootToAttributes(int requiredSize) {
        AttributesMap attributes = AttributesMap.create(requiredSize + 7, Integer.MAX_VALUE);
        attributes.put(SemanticAttributes.ENDUSER_ID, rootTelemetryModel.getUserId());
        attributes.put(FlywaySemanticAttributes.DATABASE_ENGINE, rootTelemetryModel.getDatabaseEngine());
        attributes.put(FlywaySemanticAttributes.APPLICATION_VERSION, rootTelemetryModel.getApplicationVersion());
        attributes.put(FlywaySemanticAttributes.APPLICATION_EDITION, rootTelemetryModel.getApplicationEdition());
        attributes.put(FlywaySemanticAttributes.IS_REDGATE, rootTelemetryModel.isRedgateEmployee());
        attributes.put(FlywaySemanticAttributes.SESSION_ID, rootTelemetryModel.getSessionId());
        attributes.put(FlywaySemanticAttributes.OPERATION_ID, rootTelemetryModel.getOperationId());
        return attributes;
    }

    @Override
    public void logEventDetails(EventTelemetryModel eventModel) {
        ensureConnected();
        addEvent(eventModel);
    }

    @Override
    public void close() throws Exception {
        try {
            ensureConnected();
            dumpLogs();
            rootSpan.end();
            scope.close();
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addEvent(EventTelemetryModel eventModel) {
        Set<Map.Entry<String, Object>> props = convertModelToMap(eventModel).entrySet();
        AttributesMap attributes = rootToAttributes(props.size());
        for (Map.Entry<String, Object> kvPair : props) {

            Object value = kvPair.getValue();
            String key = kvPair.getKey();

            if (!key.equals("name")) {
                String fullKey = eventModel.getName() + "." + key;
                if (value instanceof String ) {
                    attributes.put(AttributeKey.stringKey(fullKey), (String) value);
                } else if (value instanceof Boolean) {
                    attributes.put(AttributeKey.booleanKey(fullKey), (Boolean) value);
                } else if (value instanceof Integer) {
                    attributes.put(AttributeKey.longKey(fullKey), (Integer) value);
                } else if (value instanceof Long) {
                    attributes.put(AttributeKey.longKey(fullKey), (Long) value);
                } else if (value instanceof ArrayList<?>) {
                    if (((ArrayList<?>)value).size() > 0) {
                        if (((ArrayList<?>)value).get(0) instanceof String) {
                            attributes.put(AttributeKey.stringArrayKey(fullKey), (ArrayList<String>) value);
                        } else if (((ArrayList<?>) value).get(0) instanceof Long) {
                            attributes.put(AttributeKey.longArrayKey(fullKey), (ArrayList<Long>) value);
                        } else if (((ArrayList<?>) value).get(0) instanceof Boolean) {
                            attributes.put(AttributeKey.booleanArrayKey(fullKey), (ArrayList<Boolean>) value);
                        }
                    }
                }
            }
        }

        Span subSpan = tracer.spanBuilder(eventModel.getName())
                             .setAllAttributes(attributes)
                             .setParent(mainContext.with(rootSpan))
                             .setSpanKind(SpanKind.SERVER)
                             .setStartTimestamp(eventModel.getStartTime())
                             .startSpan();

        if(eventModel.getException() != null) {
            subSpan.setStatus(StatusCode.ERROR);
            rootSpan.recordException(new FlywaySanitizedException(eventModel.getException()));
        }

        subSpan.end();
        subSpan.storeInContext(mainContext);
        rootSpan.storeInContext(mainContext);
    }

    private void dumpLogs() {
        Attributes attributes = rootToAttributes(0);
        rootSpan.setAllAttributes(attributes);
    }

    private static Map<String, Object> convertModelToMap(Object bean) {
        Map<String, Object> result = new HashMap<>();
        for (Field f : bean.getClass().getDeclaredFields()) {
            try {
                String name = f.getName();
                String pascalName = name.substring(0, 1).toUpperCase() + name.substring(1);
                Method m = bean.getClass().getDeclaredMethod("get" + pascalName, null);
                Object o = m.invoke(bean, null);
                result.put(name, o);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return result;
    }

    private void connect() {
        openTelemetry = initOpenTelemetry();

        tracer = openTelemetry.getTracer("com.red-gate");

        rootSpan = tracer.spanBuilder("flyway")
                         .setSpanKind(SpanKind.SERVER)
                         .startSpan();

        mainContext = Context.current().with(rootSpan);
        scope = mainContext.makeCurrent();
    }

    static OpenTelemetry initOpenTelemetry() {

        SpanExporter azureSpanExporter = new AzureMonitorExporterBuilder()
                .connectionString("InstrumentationKey=34e2ae50-f8b8-4f38-bf0b-44c0b0a961af;IngestionEndpoint=https://appinsights.red-gate.com/;LiveEndpoint=https://appinsights.red-gate.com/")
                .buildTraceExporter();

        AttributesMap attributes = AttributesMap.create(2, Integer.MAX_VALUE);
        attributes.put(ResourceAttributes.SERVICE_NAME, "flyway-cli");
        attributes.put(ResourceAttributes.SERVICE_VERSION, VersionPrinter.getVersion());

        OpenTelemetrySdk openTelemetrySdk =
                OpenTelemetrySdk.builder()
                                .setTracerProvider(
                                        SdkTracerProvider.builder()
                                                         .addSpanProcessor(SimpleSpanProcessor.create(azureSpanExporter))
                                                         .setResource(Resource.create(attributes))
                                                         .build())
                                .buildAndRegisterGlobal();

        Runtime.getRuntime().addShutdownHook(new Thread(openTelemetrySdk::close));

        return openTelemetrySdk;
    }
}