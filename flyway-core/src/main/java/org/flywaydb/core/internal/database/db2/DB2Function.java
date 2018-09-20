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
package org.flywaydb.core.internal.database.db2;

import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Function;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Db2-specific function.
 */
public class DB2Function extends Function {

	//Functions with Decfloat and Binary cannot be dropped with specified arguments, but may be dropped without arguments if their
	//names are unique => This means that if you have overloaded functions with these types, the drop statements will fail.
	// A possible workaround is to use unique names for functions with these types.
	private static final Collection<String> typesWithLength = Arrays.asList(
			"character",
			"char",
			"varchar",
			"graphic",
			"vargraphic",
			"decimal",
			"float",
			"varbinary");

	/**
	 * Creates a new Db2 function.
	 *
	 * @param jdbcTemplate The Jdbc Template for communicating with the DB.
	 * @param database    The database-specific support.
	 * @param schema       The schema this function lives in.
	 * @param name         The name of the function.
	 * @param args         The arguments of the function.
	 */
	DB2Function(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name, String... args) {
		super(jdbcTemplate, database, schema, name, args);
	}

	@Override
	protected void doDrop() throws SQLException {
		try {
			jdbcTemplate.execute("DROP FUNCTION "
					+ database.quote(schema.getName(), name)
					+ "(" + argsToCommaSeparatedString(args) + ")");
		} catch (SQLException e) {
			//Fallback - try to drop it without arguments - will catch most cases that are not supported above, including
			//functions with types "decfloat(16)", "decfloat(34)" and "binary large object"
			jdbcTemplate.execute("DROP FUNCTION " + database.quote(schema.getName(), name));
		}
	}

	/**
	 * Creates a comma separated string of the arguments, with special treatment of types that have or may have length. In that
	 * case we need to add empty parenthesis to get a match.
	 *
	 * @param args The argument list
	 * @return A comma separated string that can be included in the function drop statement
	 */
	private String argsToCommaSeparatedString(String[] args) {
		StringBuilder buf = new StringBuilder();
		for (String arg : args) {
			if (buf.length() > 0) {
				buf.append(",");
			}
			buf.append(arg);
			if (typesWithLength.contains(arg.toLowerCase())) {
				buf.append("()"); //Add parenthesis to match on "wildcard" length
			}
		}
		return buf.toString();
	}

	@Override
	public String toString() {
		return super.toString() + "(" + StringUtils.arrayToCommaDelimitedString(args) + ")";
	}
}