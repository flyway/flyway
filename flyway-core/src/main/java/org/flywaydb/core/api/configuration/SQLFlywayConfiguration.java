/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.api.configuration;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.callback.SQLFlywayCallback;
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;

import com.mongodb.MongoClient;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Abstract class for SQL flyway configuration. Can be used to provide configuration data to migrations and callbacks.
 */
public interface SQLFlywayConfiguration extends FlywayConfiguration {

	/**
	 * Retrieves the dataSource to use to access the database. Must have the necessary privileges to execute ddl.
	 *
	 * @return The dataSource to use to access the database. Must have the necessary privileges to execute ddl.
	 */
	DataSource getDataSource();

	/**
	 * Retrieves the version to tag an existing schema with when executing baseline.
	 * Gets the callbacks for lifecycle notifications.
	 *
	 * @return The callbacks for lifecycle notifications. An empty array if none. (default: none)
	 * @deprecated This field is deprecated, use the getSqlCallbacks method instead.
	 */
	@Deprecated
	FlywayCallback[] getCallbacks();
	
	/**
	 * Gets the callbacks for SQL lifecycle notifications.
	 *
	 * @return The callbacks for SQL lifecycle notifications. An empty array if none. (default: none)
	 */
	SQLFlywayCallback[] getSqlCallbacks();

	/**
	 * Retrieves the file name suffix for sql migrations.
	 * <p/>
	 * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
	 * which using the defaults translates to V1_1__My_description.sql</p>
	 *
	 * @return The file name suffix for sql migrations. (default: .sql)
	 */
	String getSqlMigrationSuffix();

	/**
	 * Retrieves the file name prefix for repeatable sql migrations.
	 * <p/>
	 * <p>Repeatable sql migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
	 * which using the defaults translates to R__My_description.sql</p>
	 *
	 * @return The file name prefix for repeatable sql migrations. (default: R)
	 */
	String getRepeatableSqlMigrationPrefix();

	/**
	 * Retrieves the file name separator for sql migrations.
	 * <p/>
	 * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
	 * which using the defaults translates to V1_1__My_description.sql</p>
	 *
	 * @return The file name separator for sql migrations. (default: __)
	 */
	String getSqlMigrationSeparator();

	/**
	 * Retrieves the file name prefix for sql migrations.
	 * <p/>
	 * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
	 * which using the defaults translates to V1_1__My_description.sql</p>
	 *
	 * @return The file name prefix for sql migrations. (default: V)
	 */
	String getSqlMigrationPrefix();

	/**
	 * Checks whether placeholders should be replaced.
	 *
	 * @return Whether placeholders should be replaced. (default: true)
	 */
	boolean isPlaceholderReplacement();

	/**
	 * Retrieves the suffix of every placeholder.
	 *
	 * @return The suffix of every placeholder. (default: } )
	 */
	String getPlaceholderSuffix();

	/**
	 * Retrieves the prefix of every placeholder.
	 *
	 * @return The prefix of every placeholder. (default: ${ )
	 */
	String getPlaceholderPrefix();

	/**
	 * Retrieves the map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
	 *
	 * @return The map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
	 */
	Map<String, String> getPlaceholders();

	/**
	 * Retrieves the schemas managed by Flyway.  These schema names are case-sensitive.
	 * <p>Consequences:</p>
	 * <ul>
	 * <li>The first schema in the list will be automatically set as the default one during the migration.</li>
	 * <li>The first schema in the list will also be the one containing the metadata table.</li>
	 * <li>The schemas will be cleaned in the order of this list.</li>
	 * </ul>
	 *
	 * @return The schemas managed by Flyway. (default: The default schema for the datasource connection)
	 */
	String[] getSchemas();

	/**
	 * Retrieves the encoding of Sql migrations.
	 *
	 * @return The encoding of Sql migrations. (default: UTF-8)
	 */
	String getEncoding();
}
