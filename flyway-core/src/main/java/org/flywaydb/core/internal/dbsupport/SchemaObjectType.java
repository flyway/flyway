/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport;

import java.sql.SQLException;
import java.util.List;

/**
 * A schema object type. This interface is to be implemented by database-specific enums of supported object types.
 */
public interface SchemaObjectType<S extends Schema> {

    /**
     * Returns object type name as it is named in the data dictionary, e.g. "TABLE", "MATERIALIZED VIEW", etc.
     *
     * @return this type name
     */
    String getName();

    /**
     * Returns the list of object names of this type in the specified schema.
     *
     * @param schema the schema whose objects should be listed
     * @return the list of objects in order guaranteeing successful dropping
     * @throws SQLException if retrieving of object list failed
     */
    List<String> getObjectNames(S schema) throws SQLException;

    /**
     * Returns the drop statement for the specified object of this type.
     *
     * @param schema the owner of the object
     * @param objectName the object name
     * @return drop statement
     */
    String generateDropStatement(S schema, String objectName);

    /**
     * Drops the specified object of this type.
     *
     * @param schema the owner of the object
     * @param objectName the object name
     * @throws SQLException if dropping of object failed
     */
    void dropObject(S schema, String objectName) throws SQLException;
}
