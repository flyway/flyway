package org.flywaydb.database.saphana;

import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SAP HANA implementation of Schema.
 */
public class SAPHANASchema extends Schema<SAPHANADatabase, SAPHANATable> {
    /**
     * Creates a new SAP HANA schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param name The name of the schema.
     */
    SAPHANASchema(JdbcTemplate jdbcTemplate, SAPHANADatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYS.SCHEMAS WHERE SCHEMA_NAME=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("select count(*) from sys.tables where schema_name = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from sys.views where schema_name = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from sys.sequences where schema_name = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from sys.synonyms where schema_name = ?", name);
        return objectCount == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name) + " RESTRICT");
    }

    @Override
    protected void doClean() throws SQLException {
        for (String dropStatement : generateDropStatements("SYNONYM")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatementsForViews()) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatements("TABLE")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatements("SEQUENCE")) {
            jdbcTemplate.execute(dropStatement);
        }
    }

    /**
     * Generates DROP statements for this type of object in this schema.
     *
     * @param objectType The type of object.
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatements(String objectType) throws SQLException {
        List<String> dropStatements = new ArrayList<>();
        for (String dbObject : getDbObjects(objectType)) {
            dropStatements.add("DROP " + objectType + " " + database.quote(name, dbObject) + " CASCADE");
        }
        return dropStatements;
    }

    /**
     * Generates DROP statements for all views in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForViews() throws SQLException {
        List<String> dropStatements = new ArrayList<>();
        for (String dbObject : getDbObjects("VIEW")) {
            dropStatements.add("DROP VIEW " + database.quote(name, dbObject));
        }
        return dropStatements;
    }

    private List<String> getDbObjects(String objectType) throws SQLException {
        return jdbcTemplate.queryForStringList(
                "select " + objectType + "_NAME from SYS." + objectType + "S where SCHEMA_NAME = ?", name);
    }

    @Override
    protected SAPHANATable[] doAllTables() throws SQLException {
        List<String> tableNames = getDbObjects("TABLE");
        SAPHANATable[] tables = new SAPHANATable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new SAPHANATable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new SAPHANATable(jdbcTemplate, database, this, tableName);
    }
}