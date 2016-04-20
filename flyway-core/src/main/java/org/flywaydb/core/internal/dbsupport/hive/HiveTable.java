package org.flywaydb.core.internal.dbsupport.hive;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.SQLException;

public class HiveTable extends Table {
    private static final Log LOG = LogFactory.getLog(HiveTable.class);

    public HiveTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
        super(jdbcTemplate, dbSupport, schema, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return exists(null, schema, name);
    }

    @Override
    protected void doLock() throws SQLException {
        return; // lock is not supported
    }

    @Override
    protected void doDrop() throws SQLException {
        // We do not know if it is a table or a view...
        String tableOrView = dbSupport.quote(schema.getName(), name);
        try {
            jdbcTemplate.execute("DROP TABLE " + tableOrView);
        } catch (SQLException e) {
            if (e.getMessage().contains("Cannot drop a view with DROP TABLE"))
                jdbcTemplate.execute("DROP VIEW " + tableOrView);
            else
                throw e;
        }
    }


}
