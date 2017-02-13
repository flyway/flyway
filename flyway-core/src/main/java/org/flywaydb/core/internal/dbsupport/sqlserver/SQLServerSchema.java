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
package org.flywaydb.core.internal.dbsupport.sqlserver;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLServer implementation of Schema.
 */
public class SQLServerSchema extends Schema<SQLServerDbSupport> {
    /**
     * SQL server object types for which we support automatic clean-up. Those types can be used in conjunction with the
     * {@code sys.ojects} catalog. The full list of object types is available in the
     * <a href="https://msdn.microsoft.com/en-us/library/ms190324.aspx">MSDN documentation</a> (see the {@code type}
     * column description.
     */
    private enum ObjectType {
        /**
         * Aggregate function (CLR).
         */
        AGGREGATE("AF"),
        /**
         * CHECK constraint
         */
        CHECK_CONSTRAINT("C"),
        /**
         * DEFAULT constraint.
         */
        DEFAULT_CONSTRAINT("D"),
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
         * Assembly (CLR) table-valued function
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
         * Synonym.
         */
        SYNONYM("SN"),
        /**
         * Table-valued function.
         */
        TABLE_VALUED_FUNCTION("TF"),
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

        final String code;

        ObjectType(String code) {
            assert code != null;
            this.code = code;
        }
    }

    /**
     * SQL server object meta-data.
     */
    private class DBObject {
        /**
         * The object name.
         */
        final String name;
        /**
         * The object id.
         */
        final long objectId;

        DBObject(long objectId, String name) {
            assert name != null;
            this.objectId = objectId;
            this.name = name;
        }
    }

    /**
     * Creates a new SQLServer schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public SQLServerSchema(JdbcTemplate jdbcTemplate, SQLServerDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
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
                    "SELECT t.name FROM sys.types t INNER JOIN sys.schemas s ON t.schema_id = s.schema_id" +
                    " WHERE t.is_user_defined = 1 AND s.name = ? " +
                    "Union " +
                    "SELECT name FROM sys.assemblies WHERE is_user_defined=1" +
                    ") R", name);
            empty = objectCount == 0;
        }

        return empty;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
        jdbcTemplate.execute("DROP SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        List<DBObject> tables = queryDBObjects(ObjectType.USER_TABLE);

        for (String statement : cleanForeignKeys(tables)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanDefaultConstraints(tables)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanProcedure()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanViews()) {
            jdbcTemplate.execute(statement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        for (String statement : cleanFunctions()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanAggregates()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanTypes()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanAssemblies()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanSynonyms()) {
            jdbcTemplate.execute(statement);
        }

        if (jdbcTemplate.getMetaData().getDatabaseMajorVersion() >= 11) {
            for (String statement : cleanSequences()) {
                jdbcTemplate.execute(statement);
            }
        }
    }

    /**
     * Query objects with any of the given types.
     *
     * @param types the object types to be queried
     * @return the found objects
     * @throws SQLException when the retrieval failed
     */
    private List<DBObject> queryDBObjects(ObjectType... types) throws SQLException {
        return queryDBObjectsWithParent(null, types);
    }

    /**
     * Query objects with any of the given types and parent (if non-null).
     *
     * @param parent the parent object or {@code null} if unspecified
     * @param types  the object types to be queried
     * @return the found objects
     * @throws SQLException when the retrieval failed
     */
    private List<DBObject> queryDBObjectsWithParent(DBObject parent, ObjectType... types) throws SQLException {
        assert types != null && types.length > 0;
        StringBuilder query = new StringBuilder("SELECT obj.object_id, obj.name FROM sys.objects AS obj " +
                "LEFT JOIN sys.extended_properties AS eps " +
                "ON obj.object_id = eps.major_id " +
                "AND eps.class = 1 " +    // Class 1 = objects and columns (we are only interested in objects).
                "AND eps.minor_id = 0 " + // Minor ID, always 0 for objects.
                "AND eps.name='microsoft_database_tools_support' " + // Select all objects generated from MS database
                // tools.
                "WHERE SCHEMA_NAME(obj.schema_id) = '" + name + "'  " +
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

        if (parent != null) {
            // Apply the parent selection if one was given.
            query.append(" AND obj.parent_object_id = ").append(parent.objectId);
        }

        query.append(" order by create_date desc");

        return jdbcTemplate.query(query.toString(), new RowMapper<DBObject>() {
            @Override
            public DBObject mapRow(ResultSet rs) throws SQLException {
                return new DBObject(rs.getLong("object_id"), rs.getString("name"));
            }
        });
    }

    /**
     * Cleans the foreign keys in this schema.
     *
     * @param tables the tables to be cleaned
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanForeignKeys(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<String>();
        for (DBObject table : tables) {
            List<DBObject> fks = queryDBObjectsWithParent(table, ObjectType.FOREIGN_KEY,
                    ObjectType.CHECK_CONSTRAINT);
            for (DBObject fk : fks) {
                statements.add("ALTER TABLE " + dbSupport.quote(name, table.name) + " DROP CONSTRAINT " +
                        dbSupport.quote(fk.name));
            }
        }
        return statements;
    }

    /**
     * Cleans the default constraints in this schema.
     *
     * @param tables the tables to be cleaned
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanDefaultConstraints(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<String>();
        for (DBObject table : tables) {
            List<DBObject> dfs = queryDBObjectsWithParent(table, ObjectType.DEFAULT_CONSTRAINT);
            for (DBObject df : dfs) {
                statements.add("ALTER TABLE " + dbSupport.quote(name, table.name) + " DROP CONSTRAINT " + dbSupport.quote(df.name));
            }

        }
        return statements;
    }

    /**
     * Cleans the procedures in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanProcedure() throws SQLException {
        List<String> statements = new ArrayList<String>();
        List<DBObject> procedures = queryDBObjects(ObjectType.STORED_PROCEDURE, ObjectType.CLR_STORED_PROCEDURE);
        for (DBObject procedure : procedures) {
            statements.add("DROP PROCEDURE " + dbSupport.quote(name, procedure.name));
        }

        return statements;
    }

    /**
     * Cleans the functions in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanFunctions() throws SQLException {
        List<String> statements = new ArrayList<String>();
        List<DBObject> functions = queryDBObjects(ObjectType.SCALAR_FUNCTION, ObjectType.CLR_SCALAR_FUNCTION,
                ObjectType.CLR_TABLE_VALUED_FUNCTION, ObjectType.TABLE_VALUED_FUNCTION,
                ObjectType.INLINED_TABLE_FUNCTION);
        for (DBObject function : functions) {
            statements.add("DROP FUNCTION " + dbSupport.quote(name, function.name));
        }
        return statements;
    }

    /**
     * Cleans the aggregates in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanAggregates() throws SQLException {
        List<String> statements = new ArrayList<String>();
        List<DBObject> aggregates = queryDBObjects(ObjectType.AGGREGATE);
        for (DBObject aggregate : aggregates) {
            statements.add("DROP AGGREGATE " + dbSupport.quote(name, aggregate.name));
        }
        return statements;
    }

    /**
     * Cleans the views in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanViews() throws SQLException {
        List<String> statements = new ArrayList<String>();
        List<DBObject> views = queryDBObjects(ObjectType.VIEW);
        for (DBObject view : views) {
            statements.add("DROP VIEW " + dbSupport.quote(name, view.name));
        }

        return statements;
    }

    /**
     * Cleans the types in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanTypes() throws SQLException {
        List<String> typeNames =
                jdbcTemplate.queryForStringList(
                        "SELECT t.name FROM sys.types t INNER JOIN sys.schemas s ON t.schema_id = s.schema_id" +
                                " WHERE t.is_user_defined = 1 AND s.name = ?",
                        name
                );

        List<String> statements = new ArrayList<String>();
        for (String typeName : typeNames) {
            statements.add("DROP TYPE " + dbSupport.quote(name, typeName));
        }
        return statements;
    }

    /**
     * Cleans the CLR assemblies in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanAssemblies() throws SQLException {
        List<String> assemblyNames =
                jdbcTemplate.queryForStringList(
                        "SELECT * FROM sys.assemblies WHERE is_user_defined=1"
                );
        List<String> statements = new ArrayList<String>();
        for (String assemblyName : assemblyNames) {
            statements.add("DROP ASSEMBLY " + dbSupport.quote(assemblyName));
        }
        return statements;
    }

    /**
     * Cleans the synonyms in this schema.ter
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanSynonyms() throws SQLException {
        List<String> statements = new ArrayList<String>();
        List<DBObject> synonyms = queryDBObjects(ObjectType.SYNONYM);
        for (DBObject synonym : synonyms) {
            statements.add("DROP SYNONYM " + dbSupport.quote(name, synonym.name));
        }

        return statements;
    }

    /**
     * Cleans the sequences in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanSequences() throws SQLException {
        List<String> statements = new ArrayList<String>();
        List<DBObject> sequences = queryDBObjects(ObjectType.SEQUENCE_OBJECT);
        for (DBObject sequence : sequences) {
            statements.add("DROP SEQUENCE " + dbSupport.quote(name, sequence.name));
        }

        return statements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = new ArrayList<String>();
        for (DBObject table : queryDBObjects(ObjectType.USER_TABLE)) {
            tableNames.add(table.name);
        }

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new SQLServerTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new SQLServerTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
