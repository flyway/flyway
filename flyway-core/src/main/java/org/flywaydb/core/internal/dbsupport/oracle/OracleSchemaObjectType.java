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
package org.flywaydb.core.internal.dbsupport.oracle;

import org.flywaydb.core.internal.dbsupport.SchemaObjectType;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;


/**
 * Oracle-specific schema object types. This enum encapsulates logic of finding and dropping of objects of different
 * types in a schema.
 */
public enum OracleSchemaObjectType implements SchemaObjectType<OracleSchema> {

    // Tables, except for XML tables, nested tables, IOT overflow tables and other secondary objects.
    TABLE("TABLE", "CASCADE CONSTRAINTS PURGE") {
        @Override
        public List<String> getObjectNames(OracleSchema schema) throws SQLException {
            String tablesQuery =
                    "SELECT TABLE_NAME, OWNER\n" +
                            "FROM ALL_TABLES\n" +
                            "WHERE OWNER = ?\n" +
                            "  AND (IOT_TYPE IS NULL OR IOT_TYPE NOT LIKE '%OVERFLOW%')\n" +
                            "  AND NESTED != 'YES'\n" +
                            "  AND SECONDARY != 'Y'\n";

            boolean referencePartitionedTablesExist = schema.getDbSupport().queryReturnsRows(
                    "SELECT * FROM ALL_PART_TABLES WHERE OWNER = ? AND PARTITIONING_TYPE = 'REFERENCE'",
                    schema.getName());

            if (referencePartitionedTablesExist) {
                tablesQuery = "WITH TABLES AS (\n" +
                                tablesQuery +
                                ")\n" +
                                "SELECT t.TABLE_NAME\n" +
                                "FROM TABLES t\n" +
                                "  LEFT JOIN ALL_PART_TABLES pt\n" +
                                "    ON t.OWNER = pt.OWNER\n" +
                                "   AND t.TABLE_NAME = pt.TABLE_NAME\n" +
                                "   AND pt.PARTITIONING_TYPE = 'REFERENCE'\n" +
                                "  LEFT JOIN ALL_CONSTRAINTS fk\n" +
                                "    ON pt.OWNER = fk.OWNER\n" +
                                "   AND pt.TABLE_NAME = fk.TABLE_NAME\n" +
                                "   AND pt.REF_PTN_CONSTRAINT_NAME = fk.CONSTRAINT_NAME\n" +
                                "   AND fk.CONSTRAINT_TYPE = 'R'\n" +
                                "  LEFT JOIN ALL_CONSTRAINTS puk\n" +
                                "    ON fk.R_OWNER = puk.OWNER\n" +
                                "   AND fk.R_CONSTRAINT_NAME = puk.CONSTRAINT_NAME\n" +
                                "   AND puk.CONSTRAINT_TYPE IN ('P', 'U')\n" +
                                "  LEFT JOIN TABLES p\n" +
                                "    ON puk.OWNER = p.OWNER\n" +
                                "   AND puk.TABLE_NAME = p.TABLE_NAME\n" +
                                "START WITH p.TABLE_NAME IS NULL\n" +
                                "CONNECT BY PRIOR t.TABLE_NAME = p.TABLE_NAME\n" +
                                "ORDER BY LEVEL DESC";
            }

            return schema.getJdbcTemplate().queryForStringList(tablesQuery, schema.getName());
        }
    },

    // XML tables, seen in a separate data dictionary view (if XDB is installed)
    XML_TABLE("TABLE", "CASCADE CONSTRAINTS PURGE") {
        @Override
        public List<String> getObjectNames(OracleSchema schema) throws SQLException {
            return schema.getDbSupport().isXmlDbAvailable()
                    ? schema.getJdbcTemplate().queryForStringList(
                    "SELECT TABLE_NAME FROM ALL_XML_TABLES WHERE OWNER = ? " +
                            // ALL_XML_TABLES shows objects in RECYCLEBIN, ignore them
                            "AND TABLE_NAME NOT LIKE 'BIN$________________________$_'",
                    schema.getName())
                    : Collections.<String>emptyList();
        }
    },

    // Queue tables, have related objects and should be dropped separately prior to other related types.
    QUEUE_TABLE("TABLE") {
        @Override
        public List<String> getObjectNames(OracleSchema schema) throws SQLException {
            return schema.getJdbcTemplate().queryForStringList(
                    "SELECT QUEUE_TABLE FROM ALL_QUEUE_TABLES WHERE OWNER = ?",
                    schema.getName()
            );
        }
        @Override
        public String generateDropStatement(OracleSchema schema, String objectName) {
            return "BEGIN DBMS_AQADM.DROP_QUEUE_TABLE('" + super.fullObjName(schema, objectName) + "', FORCE => TRUE); END;";
        }
    },

    // Materialized view logs, seen as ordinary tables but should be dropped in a special way.
    MATERIALIZED_VIEW_LOG("TABLE") {
        @Override
        public List<String> getObjectNames(OracleSchema schema) throws SQLException {
            return schema.getJdbcTemplate().queryForStringList(
                    "SELECT MASTER FROM ALL_MVIEW_LOGS WHERE LOG_OWNER = ?",
                    schema.getName()
            );
        }
        @Override
        public String generateDropStatement(OracleSchema schema, String objectName) {
            return "DROP MATERIALIZED VIEW LOG ON " + super.fullObjName(schema, objectName);
        }
    },

    // All indexes, except for domain indexes, should be dropped after tables (if any left).
    INDEX("INDEX") {
        @Override
        public List<String> getObjectNames(OracleSchema schema) throws SQLException {
            return schema.getJdbcTemplate().queryForStringList(
                    "SELECT INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ? AND INDEX_TYPE NOT LIKE '%DOMAIN%'",
                    schema.getName()
            );
        }
    },

    // Domain indexes, have related objects and should be dropped separately prior to tables.
    DOMAIN_INDEX("INDEX", "FORCE") {
        @Override
        public List<String> getObjectNames(OracleSchema schema) throws SQLException {
            return schema.getJdbcTemplate().queryForStringList(
                    "SELECT INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ? AND INDEX_TYPE LIKE '%DOMAIN%'",
                    schema.getName()
            );
        }
    },

    // Clusters.
    CLUSTER("CLUSTER", "INCLUDING TABLES CASCADE CONSTRAINTS"),

    // Views, including XML views.
    VIEW("VIEW", "CASCADE CONSTRAINTS"),

    // Materialized views, leave tables as they may be referenced.
    MATERIALIZED_VIEW("MATERIALIZED VIEW", "PRESERVE TABLE"),

    // Local synonyms.
    SYNONYM("SYNONYM", "FORCE"),

    // Sequences, no filtering for identity sequences, since they get dropped along with master tables..
    SEQUENCE("SEQUENCE"),

    // Procedures, functions, packages.
    PROCEDURE("PROCEDURE"),
    FUNCTION("FUNCTION"),
    PACKAGE("PACKAGE"),

    // Triggers of all types, should be dropped at first, because invalid DDL triggers may break the whole clean.
    TRIGGER("TRIGGER"),

    // Types, should be dropped at the end.
    TYPE("TYPE", "FORCE"),

    // Java sources, cause cascade drop of object classes (only those compiled from sources).
    JAVA_SOURCE("JAVA SOURCE"),

    // Scheduler jobs.
    SCHEDULER_JOB("JOB") {
        @Override
        public String generateDropStatement(OracleSchema schema, String objectName) {
            return "BEGIN DBMS_SCHEDULER.DROP_JOB('" + super.fullObjName(schema, objectName) + "', FORCE => TRUE); END;";
        }
    }
    // TODO: to be extended
    ;


    private final String name;
    private final String dropOptions;

    /**
     * Instantiates a new object type value.
     * @param name object type name
     * @param dropOptions extra options for drop statement
     */
    OracleSchemaObjectType(String name, String dropOptions) {
        this.name = name;
        this.dropOptions = dropOptions;
    }

    OracleSchemaObjectType(String name) {
        this(name, "");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getObjectNames(OracleSchema schema) throws SQLException {
        return schema.getJdbcTemplate().queryForStringList(
                "SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_TYPE = ?",
                schema.getName(), this.getName()
        );
    }

    @Override
    public String generateDropStatement(OracleSchema schema, String objectName) {
        return "DROP " + name + " " + fullObjName(schema, objectName) + " " + dropOptions;
    }

    @Override
    public void dropObject(OracleSchema schema, String objectName) throws SQLException {
        schema.getJdbcTemplate().execute(generateDropStatement(schema, objectName));
    }

    String fullObjName(OracleSchema schema, String objectName) {
        return schema.getDbSupport().quote(schema.getName(), objectName);
    }

}