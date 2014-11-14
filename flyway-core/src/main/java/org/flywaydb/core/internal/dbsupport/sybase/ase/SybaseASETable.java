/**
 * 
 */
package org.flywaydb.core.internal.dbsupport.sybase.ase;

import java.sql.SQLException;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

/**
 * Sybase table for flyway
 *
 */
public class SybaseASETable extends Table {

	/**
	    * Creates a new Sybase table.
	    *
	    * @param jdbcTemplate The Jdbc Template for communicating with the DB.
	    * @param dbSupport    The database-specific support.
	    * @param schema       The schema this table lives in.
	    * @param name         The name of the table.
	    */
	public SybaseASETable(JdbcTemplate jdbcTemplate, DbSupport dbSupport,
			Schema schema, String name) {
		super(jdbcTemplate, dbSupport, schema, name);
	}
	
	@Override
	protected boolean doExists() throws SQLException {
		return exists(null, getSchema(), getName());
	}

	@Override
	protected void doLock() throws SQLException {
		jdbcTemplate.execute("LOCK TABLE " + this + " IN EXCLUSIVE MODE WAIT 10");
		
	}

	@Override
	protected void doDrop() throws SQLException {
		jdbcTemplate.execute("DROP TABLE " + getName());
		
	}
	
	/*
	 * Since Sybase ASE does not support schema, dropping out the schema name for toString method
	 * @see org.flywaydb.core.internal.dbsupport.SchemaObject#toString()
	 */
	@Override
    public String toString() {
        return name;
    }

}
