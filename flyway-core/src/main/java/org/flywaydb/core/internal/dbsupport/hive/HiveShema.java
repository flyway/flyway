package org.flywaydb.core.internal.dbsupport.hive;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

import java.sql.SQLException;
import java.util.List;

public class HiveShema extends Schema<HiveDbSupport> {
    public HiveShema(JdbcTemplate jdbcTemplate, HiveDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
        jdbcTemplate.execute("DROP SCHEMA " + dbSupport.quote(name) + " RESTRICT");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForStringList("SHOW SCHEMAS").contains(name);
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return allTables().length == 0;
    }

    @Override
    protected void doClean() throws SQLException {
        for (Table table: allTables())
            table.drop();
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList("SHOW TABLES IN "+ dbSupport.quote(name));

        Table[] tables =  new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new HiveTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new HiveTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
