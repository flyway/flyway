/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.Cache;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.Statement;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

public class CacheCallback implements Callback {

    private static final Log LOG = LogFactory.getLog(CacheCallback.class);

    @Override
    public boolean supports(Event event, Context context) {
        return true;
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return false;
    }

    @Override
    public void handle(Event event, Context context) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("event", event.getId());
            setMigrationInfo(map, context.getMigrationInfo());
            setStatementInfo(map, context.getStatement());
            LOG.debug(new JSONObject(map).toString());
        } catch (JSONException jsonException) {
            LOG.error("Exception while geneneration json", jsonException);
        }
    }

    private void setMigrationInfo(Map<String, Object> map, MigrationInfo migrationInfo) {
        if (nonNull(migrationInfo)) {
            Map migrationInfoMap = new HashMap<>();
            migrationInfoMap.put("type", migrationInfo.getType());
            migrationInfoMap.put("description", migrationInfo.getDescription());
            migrationInfoMap.put("script", migrationInfo.getScript());
            migrationInfoMap.put("executionTime", migrationInfo.getExecutionTime());
            migrationInfoMap.put("state", migrationInfo.getState());
            map.put("migrationInfo", new JSONObject(migrationInfoMap));
        }
    }

    private void setStatementInfo(Map<String, Object> map, Statement statement) throws JSONException {
        if (nonNull(statement)) {
            Map statementInfoMap = new HashMap<>();
            statementInfoMap.put("errors", new JSONArray(Optional.ofNullable(statement.getErrors()).orElse(emptyList()).stream().map(Error::getMessage).toArray()));
            statementInfoMap.put("warnings", new JSONArray(Optional.ofNullable(statement.getWarnings()).orElse(emptyList()).stream().map(Warning::getMessage).toArray()));
            map.put("statement", new JSONObject(statementInfoMap));
        }
    }
}
