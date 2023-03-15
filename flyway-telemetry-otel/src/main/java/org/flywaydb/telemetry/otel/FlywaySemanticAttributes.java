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

import io.opentelemetry.api.common.AttributeKey;

public final class FlywaySemanticAttributes {
    public static final AttributeKey<String> APPLICATION_VERSION = AttributeKey.stringKey("service.version");
    public static final AttributeKey<String> APPLICATION_EDITION = AttributeKey.stringKey("service.edition");
    public static final AttributeKey<String> DATABASE_ENGINE = AttributeKey.stringKey("database.engine");
    public static final AttributeKey<String> IS_REDGATE = AttributeKey.stringKey("redgate.employee");
    public static final AttributeKey<String> SESSION_ID = AttributeKey.stringKey("redgate.session");
    public static final AttributeKey<String> OPERATION_ID = AttributeKey.stringKey("redgate.operation");

}