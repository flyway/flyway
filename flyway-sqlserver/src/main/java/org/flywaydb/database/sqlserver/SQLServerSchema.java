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
package org.flywaydb.database.sqlserver;

import lombok.CustomLog;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CustomLog
public class SQLServerSchema extends Schema<SQLServerDatabase, SQLServerTable> {
    protected final String databaseName;

    /**
     * SQL Server object types for which we support automatic clean-up. These types can be used in conjunction with the
     * {@code sys.objects} catalog. The full list of object types is available in the
     * <a href="https://msdn.microsoft.com/en-us/library/ms190324.aspx">MSDN documentation</a>
     * (see the {@code type} column description.)
     */
    protected enum ObjectType {
        /**
         * Aggregate function (CLR).
         */
        AGGREGATE("AF"),
        /**
         * CHECK constraint.
         */
        CHECK_CONSTRAINT("C"),
        /**
         * DEFAULT constraint.
         */
        DEFAULT_CONSTRAINT("D"),
        /**
         * PRIMARY KEY constraint.
         */
        PRIMARY_KEY("PK"),
        /**
         * FOREIGN KEY constraint.
         */
        FOREIGN_KEY("F"),
        /**
         * In-lined table-function.
         */
        INLINED_TABLE_FUNCTION("IF"),
        /**
         * Scalar function.
         */
        SCALAR_FUNCTION("FN"),
        /**
         * Assembly (CLR) scalar-function.
         */
        CLR_SCALAR_FUNCTION("FS"),
        /**
         * Assembly (CLR) table-valued function.
         */
        CLR_TABLE_VALUED_FUNCTION("FT"),
        /**
         * Stored procedure.
         */
        STORED_PROCEDURE("P"),
        /**
         * Assembly (CLR) stored-procedure.
         */
        CLR_STORED_PROCEDURE("PC"),
        /**
         * Rule (old-style, stand-alone).
         */
        RULE("R"),
        /**
         * Synonym.
         */
        SYNONYM("SN"),
        /**
         * Table-valued function.
         */
        TABLE_VALUED_FUNCTION("TF"),
        /**
         * Assembly (CLR) DML trigger.
         */
        ASSEMBLY_DML_TRIGGER("TA"),
        /**
         * SQL DML trigger.
         */
        SQL_DML_TRIGGER("TR"),
        /**
         * Unique Constraint.
         */
        UNIQUE_CONSTRAINT("UQ"),
        /**
         * User table.
         */
        USER_TABLE("U"),
        /**
         * View.
         */
        VIEW("V"),
        /**
         * Sequence object.
         */
        SEQUENCE_OBJECT("SO");

        public final String code;

        ObjectType(String code) {
            assert code != null;
            this.code = code;
        }
    }

    /**
     * SQL Server object meta-data.
     */
    public static class DBObject {
        public final String name;
        public final long objectId;

        public DBObject(long objectId, String name) {
            assert name != null;
            this.objectId = objectId;
            this.name = name;
        }
    }

    public SQLServerSchema(JdbcTemplate jdbcTemplate, SQLServerDatabase database, String databaseName, String name) {
        super(jdbcTemplate, database, name);
        this.databaseName = databaseName;
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        boolean empty = queryDBObjects(ObjectType.SCALAR_FUNCTION, ObjectType.AGGREGATE,
                                       ObjectType.CLR_SCALAR_FUNCTION, ObjectType.CLR_TABLE_VALUED_FUNCTION, ObjectType.TABLE_VALUED_FUNCTION,
                                       ObjectType.STORED_PROCEDURE, ObjectType.CLR_STORED_PROCEDURE, ObjectType.USER_TABLE,
                                       ObjectType.SYNONYM, ObjectType.SEQUENCE_OBJECT, ObjectType.FOREIGN_KEY, ObjectType.VIEW).isEmpty();
        if (empty) {
            int objectCount = jdbcTemplate.queryForInt("SELECT count(*) FROM " +
                                                               "( " +
                                                               "SELECT t.name FROM sys.types t INNER JOIN sys.schemas s ON t.schema_id = s.schema_id " +
                                                               "WHERE t.is_user_defined = 1 AND s.name = ? " +
                                                               "Union " +
                                                               "SELECT name FROM sys.assemblies WHERE is_user_defined=1" +
                                                               ") R", name);
            empty = objectCount == 0;
        }
        return empty;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        for (String statement : getCleanStatementsBeforeFirstTableDrop(queryDBObjects(ObjectType.USER_TABLE))) {
            executeIgnoringDependencyErrors(statement);
        }
        dropTablesIgnoringErrors(allTables());
        for (String statement : getCleanStatementsBeforeSecondTableDrop(queryDBObjects(ObjectType.USER_TABLE))) {
            executeIgnoringDependencyErrors(statement);
        }
        dropTablesIgnoringErrors(allTables());
        for (String statement : getCleanStatementsAfterLastTableDrop(queryDBObjects(ObjectType.USER_TABLE))) {
            executeIgnoringDependencyErrors(statement);
        }

        for (String statement : getCleanStatementsBeforeFirstTableDrop(queryDBObjects(ObjectType.USER_TABLE))) {
            jdbcTemplate.execute(statement);
        }
        dropTablesIgnoringErrors(allTables());
        for (String statement : getCleanStatementsBeforeSecondTableDrop(queryDBObjects(ObjectType.USER_TABLE))) {
            jdbcTemplate.execute(statement);
        }
        dropTables(allTables());
        for (String statement : getCleanStatementsAfterLastTableDrop(queryDBObjects(ObjectType.USER_TABLE))) {
            jdbcTemplate.execute(statement);
        }
    }

    private List<String> getCleanStatementsBeforeFirstTableDrop(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();
        statements.addAll(cleanTriggers());
        statements.addAll(cleanForeignKeys(tables));
        return statements;
    }

    private List<String> getCleanStatementsBeforeSecondTableDrop(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();

        statements.addAll(cleanForeignKeys(tables));
        statements.addAll(cleanPrimaryKeys(tables));
        statements.addAll(cleanDefaultConstraints(tables));
        statements.addAll(cleanUniqueConstraints(tables));
        statements.addAll(cleanComputedColumns(tables));
        statements.addAll(cleanObjects("PROCEDURE", ObjectType.STORED_PROCEDURE, ObjectType.CLR_STORED_PROCEDURE));
        statements.addAll(cleanObjects("VIEW", ObjectType.VIEW));
        statements.addAll(cleanObjects("FUNCTION",
                                       ObjectType.SCALAR_FUNCTION,
                                       ObjectType.CLR_SCALAR_FUNCTION,
                                       ObjectType.CLR_TABLE_VALUED_FUNCTION,
                                       ObjectType.TABLE_VALUED_FUNCTION,
                                       ObjectType.INLINED_TABLE_FUNCTION));

        return statements;
    }

    private List<String> getCleanStatementsAfterLastTableDrop(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();

        statements.addAll(cleanIndexes(tables));
        statements.addAll(cleanObjects("AGGREGATE", ObjectType.AGGREGATE));
        statements.addAll(cleanSynonyms());
        statements.addAll(cleanRules());
        statements.addAll(cleanObjects("DEFAULT", ObjectType.DEFAULT_CONSTRAINT));
        statements.addAll(cleanXmlSchemaCollections());




            statements.addAll(cleanObjects("SEQUENCE", ObjectType.SEQUENCE_OBJECT));




        return statements;
    }

    private void dropTables(SQLServerTable[] allTables) throws SQLException {
        for (SQLServerTable table : allTables) {
            table.dropSystemVersioningIfPresent();
        }
        for (SQLServerTable table : allTables) {
            table.drop();
        }
    }

    private void dropTablesIgnoringErrors(SQLServerTable[] allTables) {
        try {
            dropTables(allTables);
        } catch (Exception ignored) {
        }
    }

    private void executeIgnoringDependencyErrors(String statement) {
        try {
            jdbcTemplate.execute(statement);
        } catch (SQLException e) {
            LOG.debug("Ignoring dependency-related error: " + e.getMessage());
        }
    }

    /**
     * Query objects with any of the given types.
     *
     * @param types The object types to be queried.
     * @return The found objects.
     * @throws SQLException when the retrieval failed.
     */
    protected List<DBObject> queryDBObjects(ObjectType... types) throws SQLException {
        return queryDBObjectsWithParent(null, types);
    }

    /**
     * Query objects with any of the given types and parent (if non-null).
     *
     * @param parent The parent object or {@code null} if unspecified.
     * @param types The object types to be queried.
     * @return The found objects.
     * @throws SQLException when the retrieval failed.
     */
    private List<DBObject> queryDBObjectsWithParent(DBObject parent, ObjectType... types) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT obj.object_id, obj.name FROM sys.objects AS obj " +
                                                        "LEFT JOIN sys.extended_properties AS eps " +
                                                        "ON obj.object_id = eps.major_id " +
                                                        "AND eps.class = 1 " +    // Class 1 = objects and columns (we are only interested in objects).
                                                        "AND eps.minor_id = 0 " + // Minor ID, always 0 for objects.
                                                        "AND eps.name='microsoft_database_tools_support' " + // Select all objects generated from MS database tools.
                                                        "WHERE SCHEMA_NAME(obj.schema_id) = '" + name + "' " +
                                                        "AND eps.major_id IS NULL " + // Left Excluding JOIN (we are only interested in user defined entries).
                                                        "AND obj.is_ms_shipped = 0 " + // Make sure we do not return anything MS shipped.
                                                        "AND obj.type IN (" // Select the object types.
        );

        // Build the types IN clause.
        boolean first = true;
        for (ObjectType type : types) {
            if (!first) {
                query.append(", ");
            }
            query.append("'").append(type.code).append("'");
            first = false;
        }
        query.append(")");

        // Apply the parent selection if one was given.
        if (parent != null) {
            query.append(" AND obj.parent_object_id = ").append(parent.objectId);
        }

        query.append(" order by create_date desc, object_id desc");

        return jdbcTemplate.query(query.toString(), rs -> new DBObject(rs.getLong("object_id"), rs.getString("name")));
    }

    private List<String> cleanPrimaryKeys(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();
        for (DBObject table : tables) {
            for (DBObject pk : queryDBObjectsWithParent(table, ObjectType.PRIMARY_KEY)) {
                statements.add("ALTER TABLE " + database.quote(name, table.name) + " DROP CONSTRAINT " + database.quote(pk.name));
            }
        }
        return statements;
    }

    private List<String> cleanForeignKeys(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();
        for (DBObject table : tables) {
            for (DBObject fk : queryDBObjectsWithParent(table, ObjectType.FOREIGN_KEY, ObjectType.CHECK_CONSTRAINT)) {
                statements.add("ALTER TABLE " + database.quote(name, table.name) + " DROP CONSTRAINT " + database.quote(fk.name));
            }
        }
        return statements;
    }

    /**
     * @param tables The tables to be cleaned.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanComputedColumns(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();
        for (DBObject table : tables) {
            String tableName = database.quote(name, table.name);
            List<String> columns = jdbcTemplate.queryForStringList("" +
                                                                           "SELECT name " +
                                                                           "FROM sys.computed_columns " +
                                                                           "WHERE object_id=OBJECT_ID(N'" + tableName + "')");
            for (String column : columns) {
                statements.add("ALTER TABLE " + tableName + " DROP COLUMN " + database.quote(column));
            }
        }
        return statements;
    }

    /**
     * @param tables The tables to be cleaned.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanIndexes(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();
        for (DBObject table : tables) {
            String tableName = database.quote(name, table.name);
            List<String> indexes = jdbcTemplate.queryForStringList("" +
                                                                           "SELECT name FROM sys.indexes " +
                                                                           "WHERE object_id=OBJECT_ID(N'" + tableName + "') " +
                                                                           "AND is_primary_key = 0 AND is_unique_constraint = 0 AND name IS NOT NULL");
            for (String index : indexes) {
                statements.add("DROP INDEX " + database.quote(index) + " ON " + tableName);
            }
        }
        return statements;
    }

    /**
     * @param tables The tables to be cleaned.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanDefaultConstraints(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();
        for (DBObject table : tables) {
            String tableName = database.quote(name, table.name);
            List<String> indexes = jdbcTemplate.queryForStringList("" +
                                                                           "SELECT i.name FROM sys.indexes i " +
                                                                           "JOIN sys.index_columns ic on i.index_id = ic.index_id " +
                                                                           "JOIN sys.columns c ON ic.column_id = c.column_id AND i.object_id = c.object_id " +
                                                                           "WHERE i.object_id=OBJECT_ID(N'" + tableName + "') " +
                                                                           "AND is_primary_key = 0 AND is_unique_constraint = 1 AND i.name IS NOT NULL " +
                                                                           "GROUP BY i.name " +
                                                                           // We can't delete the unique ROWGUIDCOL constraint from a table which has a FILESTREAM column.
                                                                           // It will auto-delete when the table is dropped.
                                                                           "HAVING MAX(CAST(is_rowguidcol AS INT)) = 0 OR MAX(CAST(is_filestream AS INT)) = 0");
            for (String index : indexes) {
                statements.add("ALTER TABLE " + tableName + " DROP CONSTRAINT " + database.quote(index));
            }
        }
        return statements;
    }

    /**
     * @param tables The tables to be cleaned.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanUniqueConstraints(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();
        for (DBObject table : tables) {
            for (DBObject df : queryDBObjectsWithParent(table, ObjectType.DEFAULT_CONSTRAINT)) {
                statements.add("ALTER TABLE " + database.quote(name, table.name) + " DROP CONSTRAINT " + database.quote(df.name));
            }
        }
        return statements;
    }

    /**
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    protected List<String> cleanTriggers() throws SQLException {
        List<String> statements = new ArrayList<>();
        if (database.supportsTriggers()) {
            List<String> triggerNames = jdbcTemplate.queryForStringList("" +
                                                                                "SELECT * " +
                                                                                "FROM sys.triggers " +
                                                                                "WHERE is_ms_shipped=0 AND parent_id=0 AND parent_class_desc='DATABASE'");
            for (String triggerName : triggerNames) {
                statements.add("DROP TRIGGER " + database.quote(triggerName) + " ON DATABASE");
            }
        }
        return statements;
    }

    /**
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    protected List<String> cleanSynonyms() throws SQLException {
        List<String> statements = new ArrayList<>();
        if (database.supportsSynonyms()) {
            statements.addAll(cleanObjects("SYNONYM", ObjectType.SYNONYM));
        }
        return statements;
    }

    /**
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    protected List<String> cleanRules() throws SQLException {
        List<String> statements = new ArrayList<>();
        if (database.supportsRules()) {
            statements.addAll(cleanObjects("RULE", ObjectType.RULE));
        }
        return statements;
    }

    private List<String> cleanXmlSchemaCollections() throws SQLException {
        List<String> xscNames = jdbcTemplate.queryForStringList("SELECT name FROM sys.xml_schema_collections WHERE schema_id = SCHEMA_ID(?)", name);
        return xscNames.stream().map(xscName -> "DROP XML SCHEMA COLLECTION " + database.quote(name, xscName)).collect(Collectors.toList());
    }

    /**
     * @param dropQualifier The type of DROP statement to issue.
     * @param objectTypes The type of objects to drop.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    protected List<String> cleanObjects(String dropQualifier, ObjectType... objectTypes) throws SQLException {
        return queryDBObjects(objectTypes).stream()
                .map(dbObject -> "DROP " + dropQualifier + " " + database.quote(name, dbObject.name))
                .collect(Collectors.toList());
    }

    @Override
    protected SQLServerTable[] doAllTables() throws SQLException {
        return queryDBObjects(ObjectType.USER_TABLE).stream()
                .map(table -> new SQLServerTable(jdbcTemplate, database, databaseName, this, table.name))
                .toArray(SQLServerTable[]::new);

    }

    @Override
    public Table getTable(String tableName) {
        return new SQLServerTable(jdbcTemplate, database, databaseName, this, tableName);
    }
}