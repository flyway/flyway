/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.google.code.flyway.core.sql;

import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.google.code.flyway.core.BaseMigration;
import com.google.code.flyway.core.DbSupport;
import com.google.code.flyway.core.SqlScript;

/**
 * Database migration based on a sql file.
 */
public class SqlMigration extends BaseMigration {
	/**
	 * The resource containing the sql script.
	 */
	private final Resource sqlScriptResource;

	/**
	 * A map of <placeholder, replacementValue> to apply to sql migration
	 * scripts.
	 */
	private final Map<String, String> placeholders;

	/**
	 * The support for database-specific extensions.
	 */
	private final DbSupport dbSupport;

	/**
	 * Creates a new sql script migration based on this sql script.
	 * 
	 * @param sqlScriptResource
	 *            The resource containing the sql script.
	 */
	public SqlMigration(Resource sqlScriptResource, Map<String, String> placeholders, DbSupport dbSupport) {
		initVersion(extractVersionStringFromFileName(sqlScriptResource.getFilename()));
		scriptName = "Sql File: " + sqlScriptResource.getFilename();

		this.sqlScriptResource = sqlScriptResource;
		this.placeholders = placeholders;
		this.dbSupport = dbSupport;
	}

	/**
	 * Extracts the sql file version string from this file name.
	 * 
	 * @param fileName
	 *            The file name to parse.
	 * @return The version string.
	 */
	/* private -> for testing */
	static String extractVersionStringFromFileName(String fileName) {
		int lastDirSeparator = fileName.lastIndexOf("/");
		int extension = fileName.lastIndexOf(".sql");

		return fileName.substring(lastDirSeparator + 1, extension);
	}

	@Override
	public void doMigrate(SimpleJdbcTemplate jdbcTemplate) {
		SqlScript sqlScript = dbSupport.createSqlScript(sqlScriptResource, placeholders);
		sqlScript.execute(jdbcTemplate);
	}
}
