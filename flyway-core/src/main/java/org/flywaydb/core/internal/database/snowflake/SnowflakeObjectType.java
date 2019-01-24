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
package org.flywaydb.core.internal.database.snowflake;

public enum SnowflakeObjectType {

    SCHEMAS("SCHEMAS", "SCHEMA"),
    OBJECTS("OBJECTS", "OBJECT"),
    TABLES("TABLES", "TABLE"),
    VIEWS("VIEWS", "VIEW"),
    FILE_FORMATS("FILE FORMATS", "FILE FORMAT"),
    SEQUENCES("SEQUENCES", "SEQUENCE"),
    STAGES("STAGES", "STAGE"),
    PIPES("PIPES", "PIPE"),
    FUNCTIONS("USER FUNCTIONS", "FUNCTION");

    private String showType;
    private String createDropType;

    SnowflakeObjectType(String showType, String createDropType) {
        this.showType = showType;
        this.createDropType = createDropType;
    }

    String getShowType() {
        return showType;
    }

    String getCreateDropType() {
        return createDropType;
    }

}
