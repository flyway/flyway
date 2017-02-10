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
package org.flywaydb.core;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.flywaydb.core.api.configuration.MongoFlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.MongoScriptFlywayCallback;
import org.flywaydb.core.internal.command.MongoBaseline;
import org.flywaydb.core.internal.command.MongoClean;
import org.flywaydb.core.internal.command.MongoMigrate;
import org.flywaydb.core.internal.command.MongoRepair;
import org.flywaydb.core.internal.command.MongoValidate;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.mongo.MongoDatabaseUtil;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.MongoMetaDataTable;
import org.flywaydb.core.internal.resolver.CompositeMongoMigrationResolver;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.ConfigurationInjectionUtils;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.VersionPrinter;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Scanner;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This is the centre point of Flyway, and for most users, the only class they will ever have to deal with.
 * <p>
 * It is THE public API from which all important Flyway functions such as clean, validate and migrate can be called.
 * </p>
 */
public class MongoFlyway implements MongoFlywayConfiguration {
  private static final Log LOG = LogFactory.getLog(MongoFlyway.class);

  /**
  * Property name prefix for placeholders that are configured through properties.
  */
  private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

  /**
   * The locations to scan recursively for migrations.
   * <p/>
   * <p>The location type is determined by its prefix.
   * Unprefixed locations or locations starting with {@code classpath:} point to a package on the
   * classpath and may contain both javaScript and java-based migrations.
   * Locations starting with {@code filesystem:} point to a directory on the filesystem and may
   * only contain javascript migrations.</p>
   * <p/>
   * (default: db/migration)
   */
  private Locations locations = new Locations("db/migration");

  /**
   * The encoding of Mongo migrations. (default: UTF-8)
   */
  private String encoding = "UTF-8";

  /**
   * <p>The name of the schema metadata table that will be used by Flyway. (default: schema_version)</p><p> By default
   * (single-schema mode) the metadata table is placed in the default schema for the connection provided by the
   * datasource. </p> <p> When the <i>flyway.schemas</i> property is set (multi-schema mode), the metadata table is
   * placed in the first schema of the list. </p>
   */
  private String table = "schema_version";

  /**
   * The name of the database to connect to in Mongo.
   */
  private String databaseName;

  /**
   * The target version up to which Flyway should consider migrations. Migrations with a higher
   * version number will be ignored. The special value {@code current} designates the current
   * version of the schema (default: the latest version)
   */
  private MigrationVersion target = MigrationVersion.LATEST;

  /**
   * Whether placeholders should be replaced. (default: true)
   */
  private boolean placeholderReplacement = true;

  /**
   * The map of &lt;placeholder, replacementValue&gt; to apply to mongo js migration scripts.
   */
  private Map<String, String> placeholders = new HashMap<String, String>();

  /**
   * The prefix of every placeholder. (default: ${ )
   */
  private String placeholderPrefix = "${";

  /**
   * The suffix of every placeholder. (default: } )
   */
  private String placeholderSuffix = "}";

  /**
   * The file name prefix for Mongo migrations. (default: V)
   * <p/>
   * <p>Mongo JavaScript migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
   * which using the defaults translates to V1_1__My_description.js</p>
   */
  private String mongoMigrationPrefix = "V";

  /**
   * The file name prefix for repeatable Mongo migrations. (default: R)
   * <p/>
   * <p>Repeatable Mongo JavaScript migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
   * which using the defaults translates to R__My_description.js</p>
   */
  private String repeatableMongoMigrationPrefix = "R";

  /**
   * The file name separator for Mongo JavaScript migrations. (default: __)
   * <p/>
   * <p>Mongo JavaScript migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
   * which using the defaults translates to V1_1__My_description.js</p>
   */
  private String mongoMigrationSeparator = "__";

  /**
   * The file name suffix for Mongo JavaScript migrations. (default: .js)
   * <p/>
   * <p>Mongo JavaScript migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
   * which using the defaults translates to V1_1__My_description.js</p>
   */
  private String mongoMigrationSuffix = ".js";

	/**
	 * Ignore missing migrations when reading the metadata table. These are migrations that were performed by an
	 * older deployment of the application that are no longer available in this version. For example: we have migrations
	 * available on the classpath with versions 1.0 and 3.0. The metadata table indicates that a migration with version 2.0
	 * (unknown to us) has also been applied. Instead of bombing out (fail fast) with an exception, a
	 * warning is logged and Flyway continues normally. This is useful for situations where one must be able to deploy
	 * a newer version of the application even though it doesn't contain migrations included with an older one anymore.
	 *
	 * {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception.
	 * (default: {@code false})
	 */
	private boolean ignoreMissingMigrations;

	/**
	 * Ignore future migrations when reading the metadata table. These are migrations that were performed by a
	 * newer deployment of the application that are not yet available in this version. For example: we have migrations
	 * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
	 * (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a
	 * warning is logged and Flyway continues normally. This is useful for situations where one must be able to redeploy
	 * an older version of the application after the database has been migrated by a newer one. (default: {@code true})
	 */
	private boolean ignoreFutureMigrations = true;

	/**
	 * Whether to automatically call validate or not when running migrate. (default: {@code true})
	 */
	private boolean validateOnMigrate = true;

	/**
	 * Whether to automatically call clean or not when a validation error occurs. (default: {@code false})
	 * <p> This is exclusively intended as a convenience for development. Even tough we strongly recommend
	 * not to change migration scripts once they have been checked into SCM and run, this provides a
	 * way of dealing with this case in a smooth manner. The database will be wiped clean automatically,
	 * ensuring that the next migration will bring you back to the state checked into SCM.</p>
	 * <p><b>Warning ! Do not enable in production !</b></p>
	 */
	private boolean cleanOnValidationError;

	/**
	 * Whether to disable clean. (default: {@code false})
	 * <p>This is especially useful for production environments where running clean can be quite a
	 * career limiting move.</p>
	 */
	private boolean cleanDisabled;

	/**
	 * The version to tag an existing schema with when executing baseline. (default: 1)
	 */
	private MigrationVersion baselineVersion = MigrationVersion.fromVersion("1");

	/**
	 * The description to tag an existing schema with when executing baseline.
	 * (default: &lt;&lt; Flyway Baseline &gt;&gt;)
	 */
	private String baselineDescription = "<< Flyway Baseline >>";

	/**
	 * <p>
	 * Whether to automatically call baseline when migrate is executed against a non-empty schema
	 * with no metadata table. This schema will then be initialized with the {@code baselineVersion}
	 * before executing the migrations. Only migrations above {@code baselineVersion} will then be
	 * applied.
	 * </p>
	 * <p>
	 * This is useful for initial Flyway production deployments on projects with an existing DB.
	 * </p>
	 * <p>
	 * Be careful when enabling this as it removes the safety net that ensures
	 * Flyway does not migrate the wrong database in case of a configuration mistake!
	 * (default: {@code false})
	 * </p>
	 */
	private boolean baselineOnMigrate;

	/**
	 * Allows migrations to be run "out of order".
	 * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
	 * it will be applied too instead of being ignored.</p>
	 * <p>(default: {@code false})</p>
	 */
	private boolean outOfOrder;

	/**
	 * This is a list of custom callbacks that fire before and after tasks are executed.  You can
	 * add as many custom callbacks as you want. (default: none)
	 */
	private MongoFlywayCallback[] callbacks = new MongoFlywayCallback[0];

	/**
	 * Whether Flyway should skip the default callbacks. If true, only custom callbacks are used.
	 * <p>(default: false)</p>
	 */
	private boolean skipDefaultCallbacks;

	/**
	 * The custom MigrationResolvers to be used in addition to the built-in ones for resolving
	 * Migrations to apply.
	 * <p>(default: none)</p>
	 */
	private MigrationResolver[] resolvers = new MigrationResolver[0];

	/**
	 * Whether Flyway should skip the default resolvers. If true, only custom resolvers are used.
	 * <p>(default: false)</p>
	 */
	private boolean skipDefaultResolvers;

	/**
	 * Whether MongoFlyway created the MongoClient.
	 */
	private boolean createdMongoClient;

	/**
	 * The Mongo client used to interact with the database.
	 */
	private MongoClient client;

	/**
	 * The ClassLoader to use for resolving migrations on the classpath.
	 * (default: Thread.currentThread().getContextClassLoader() )
	 */
	private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

	/**
	 * Whether to allow mixing transactional and non-transactional statements within the same migration.
	 *
	 * {@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})
	 */
	private boolean allowMixedMigrations;

  /**
   * The username that will be recorded in the metadata table as having applied the migration.
   * <p>
   * {@code null} for the current database user of the connection. (default: {@code null}).
   */
  private String installedBy;

	/**
	 * Creates a new instance of MongoFlyway. This is your starting point.
	 */
	public MongoFlyway() {
        // Do nothing
	}

	@Override
	public String[] getLocations() {
		String[] result = new String[locations.getLocations().size()];
		for (int i = 0; i < locations.getLocations().size(); i++) {
			result[i] = locations.getLocations().get(i).toString();
		}
		return result;
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public String getTable() {
		return table;
	}

	@Override
	public MigrationVersion getTarget() {
		return target;
	}

    /**
     * Checks whether placeholders should be replaced.
     *
     * @return Whether placeholders should be replaced. (default: true)
     */
    public boolean isPlaceholderReplacement() {
        return placeholderReplacement;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    @Override
    public String getPlaceholderPrefix() {
        return placeholderPrefix;
    }

    @Override
    public String getPlaceholderSuffix() {
        return placeholderSuffix;
    }

	@Override
	public String getMongoMigrationPrefix() {
		return mongoMigrationPrefix;
	}

	@Override
    public String getRepeatableMongoMigrationPrefix() {
        return repeatableMongoMigrationPrefix;
    }

	@Override
	public String getMongoMigrationSeparator() {
		return mongoMigrationSeparator;
	}

	@Override
	public String getMongoMigrationSuffix() {
		return mongoMigrationSuffix;
	}

  @Override
  public boolean isIgnoreMissingMigrations() {
    return ignoreMissingMigrations;
  }

	/**
	 * Ignore future migrations when reading the metadata table. These are migrations that were performed by a
	 * newer deployment of the application that are not yet available in this version. For example: we have migrations
	 * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
	 * (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a
	 * warning is logged and Flyway continues normally. This is useful for situations where one must be able to redeploy
	 * an older version of the application after the database has been migrated by a newer one.
	 *
	 * @return {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception.
	 * (default: {@code true})
	 */
	public boolean isIgnoreFutureMigrations() {
		return ignoreFutureMigrations;
	}

	/**
	 * Whether to automatically call validate or not when running migrate.
	 *
	 * @return {@code true} if validate should be called. {@code false} if not. (default: {@code true})
	 */
	public boolean isValidateOnMigrate() {
		return validateOnMigrate;
	}

	/**
	 * Whether to automatically call clean or not when a validation error occurs.
	 * <p> This is exclusively intended as a convenience for development. Even tough we strongly recommend
	 * not to change migration scripts once they have been checked into SCM and run, this provides a
	 * way of dealing with this case in a smooth manner. The database will be wiped clean automatically,
	 * ensuring that the next migration will bring you back to the state checked into SCM.</p>
	 * <p><b>Warning ! Do not enable in production !</b></p>
	 *
	 * @return {@code true} if clean should be called. {@code false} if not. (default: {@code false})
	 */
	public boolean isCleanOnValidationError() {
		return cleanOnValidationError;
	}

	/**
	 * Whether to disable clean.
	 * <p>This is especially useful for production environments where running clean can be quite a career limiting move.</p>
	 *
	 * @return {@code true} to disabled clean. {@code false} to leave it enabled.  (default: {@code false})
	 */
	public boolean isCleanDisabled() {
		return cleanDisabled;
	}

	@Override
	public MigrationVersion getBaselineVersion() {
		return baselineVersion;
	}

	@Override
	public String getBaselineDescription() {
		return baselineDescription;
	}

	/**
	 * <p>
	 * Whether to automatically call baseline when migrate is executed against a non-empty schema
	 * with no metadata table. This schema will then be initialized with the {@code baselineVersion}
	 * before executing the migrations. Only migrations above {@code baselineVersion} will then be applied.
	 * </p>
	 * <p>
	 * This is useful for initial Flyway production deployments on projects with an existing DB.
	 * </p>
	 * <p>
	 * Be careful when enabling this as it removes the safety net that ensures
	 * Flyway does not migrate the wrong database in case of a configuration mistake!
	 * </p>
	 *
	 * @return {@code true} if baseline should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})
	 */
	public boolean isBaselineOnMigrate() {
		return baselineOnMigrate;
	}

	/**
	 * Allows migrations to be run "out of order".
	 * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
	 * it will be applied too instead of being ignored.</p>
	 *
	 * @return {@code true} if outOfOrder migrations should be applied, {@code false} if not. (default: {@code false})
	 */
	public boolean isOutOfOrder() {
		return outOfOrder;
	}

	@Override
	public MigrationResolver[] getResolvers() {
		return resolvers;
	}

	@Override
	public boolean isSkipDefaultResolvers() {
		return skipDefaultResolvers;
	}

	@Override
	public String getDatabaseName() {
		return databaseName;
	}
	
	@Override
	public MongoClient getMongoClient() {
		return client;
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

  @Override
  public boolean isAllowMixedMigrations() {
      return allowMixedMigrations;
  }

  @Override
  public String getInstalledBy() {
    return installedBy;
  }

  /**
   * The username that will be recorded in the metadata table as having applied the migration.
   *
   * @param installedBy The username or {@code null} for the current database user of the connection. (default: {@code null}).
   */
  public void setInstalledBy(String installedBy) {
    if ("".equals(installedBy)) {
      installedBy = null;
    }
    this.installedBy = installedBy;
  }

  /**
   * Ignore missing migrations when reading the metadata table. These are migrations that were performed by an
   * older deployment of the application that are no longer available in this version. For example: we have migrations
   * available on the classpath with versions 1.0 and 3.0. The metadata table indicates that a migration with version 2.0
   * (unknown to us) has also been applied. Instead of bombing out (fail fast) with an exception, a
   * warning is logged and Flyway continues normally. This is useful for situations where one must be able to deploy
   * a newer version of the application even though it doesn't contain migrations included with an older one anymore.
   *
   * @param ignoreMissingMigrations {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception.
   * (default: {@code false})
   */
  public void setIgnoreMissingMigrations(boolean ignoreMissingMigrations) {
    this.ignoreMissingMigrations = ignoreMissingMigrations;
  }

	/**
	 * Whether to ignore future migrations when reading the metadata table. These are migrations
	 * that were performed by a newer deployment of the application that are not yet available in
	 * this version. For example: we have migrations available on the classpath up to version 3.0.
	 * The metadata table indicates that a migration to version 4.0 (unknown to us) has already been
	 * applied. Instead of bombing out (fail fast) with an exception, a warning is logged and Flyway
	 * continues normally. This is useful for situations where one must be able to redeploy an
	 * older version of the application after the database has been migrated by a newer one.
	 *
	 * @param ignoreFutureMigrations {@code true} to continue normally and log a warning, {@code false} to fail
	 *                               fast with an exception. (default: {@code true})
	 */
	public void setIgnoreFutureMigrations(boolean ignoreFutureMigrations) {
		this.ignoreFutureMigrations = ignoreFutureMigrations;
	}

	/**
	 * Whether to automatically call validate or not when running migrate.
	 *
	 * @param validateOnMigrate {@code true} if validate should be called. {@code false} if not.
	 *                          (default: {@code true})
	 */
	public void setValidateOnMigrate(boolean validateOnMigrate) {
		this.validateOnMigrate = validateOnMigrate;
	}

	/**
	 * Whether to automatically call clean or not when a validation error occurs.
	 * <p> This is exclusively intended as a convenience for development. Even tough we strongly recommend
	 * not to change migration scripts once they have been checked into SCM and run, this provides a
	 * way of dealing with this case in a smooth manner. The database will be wiped clean automatically,
	 * ensuring that the next migration will bring you back to the state checked into SCM.</p>
	 * <p><b>Warning ! Do not enable in production !</b></p>
	 *
	 * @param cleanOnValidationError {@code true} if clean should be called. {@code false} if not. (default: {@code false})
	 */
	public void setCleanOnValidationError(boolean cleanOnValidationError) {
		this.cleanOnValidationError = cleanOnValidationError;
	}

	/**
	 * Whether to disable clean.
	 * <p>This is especially useful for production environments where running clean can be quite a
	 * career limiting move.</p>
	 *
	 * @param cleanDisabled {@code true} to disabled clean. {@code false} to leave it enabled.
	 *                      (default: {@code false})
	 */
	public void setCleanDisabled(boolean cleanDisabled) {
		this.cleanDisabled = cleanDisabled;
	}

	/**
	 * Sets the locations to scan recursively for migrations.
	 * <p/>
	 * <p>The location type is determined by its prefix.
	 * Unprefixed locations or locations starting with {@code classpath:} point to a package on the
	 * classpath and may contain both JavaScript and java-based migrations.
	 * Locations starting with {@code filesystem:} point to a directory on the filesystem and may
	 * only contain JavaScript migrations.</p>
	 *
	 * @param locations Locations to scan recursively for migrations. (default: db/migration)
	 */
	public void setLocations(String... locations) {
		this.locations = new Locations(locations);
	}

	/**
	 * Sets the encoding of Mongo JavaScript migrations.
	 *
	 * @param encoding The encoding of Mongo JavaScript migrations. (default: UTF-8)
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

    /**
     * Sets whether placeholders should be replaced.
     *
     * @param placeholderReplacement Whether placeholders should be replaced. (default: true)
     */
    public void setPlaceholderReplacement(boolean placeholderReplacement) {
        this.placeholderReplacement = placeholderReplacement;
    }

    /**
     * Sets the placeholders to replace in mongo js migration scripts.
     *
     * @param placeholders The map of &lt;placeholder, replacementValue&gt; to apply to mongo js
     *                     migration scripts.
     */
    public void setPlaceholders(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

    /**
     * Sets the prefix of every placeholder.
     *
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        if (!StringUtils.hasLength(placeholderPrefix)) {
            throw new FlywayException("placeholderPrefix cannot be empty!");
        }
        this.placeholderPrefix = placeholderPrefix;
    }

    /**
     * Sets the suffix of every placeholder.
     *
     * @param placeholderSuffix The suffix of every placeholder. (default: } )
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        if (!StringUtils.hasLength(placeholderSuffix)) {
            throw new FlywayException("placeholderSuffix cannot be empty!");
        }
        this.placeholderSuffix = placeholderSuffix;
    }

	/**
	 * Sets the file name prefix for Mongo JavaScript migrations.
	 * <p/>
	 * <p>Mongo JavaScript migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
	 * which using the defaults translates to V1_1__My_description.js</p>
	 *
	 * @param mongoMigrationPrefix The file name prefix for Mongo JavaScript migrations (default: V)
	 */
	public void setMongoMigrationPrefix(String mongoMigrationPrefix) {
		this.mongoMigrationPrefix = mongoMigrationPrefix;
	}

    /**
     * Sets the file name prefix for repeatable Mongo migrations.
     * <p/>
     * <p>Repeatable Mongo migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to R__My_description.js</p>
     *
     * @param repeatableMongoMigrationPrefix The file name prefix for repeatable Mongo migrations (default: R)
     */
    public void setRepeatableMongoMigrationPrefix(String repeatableMongoMigrationPrefix) {
        this.repeatableMongoMigrationPrefix = repeatableMongoMigrationPrefix;
    }

	/**
	 * Sets the file name separator for Mongo JavaScript migrations.
	 * <p/>
	 * <p>Mongo JavaScript migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
	 * which using the defaults translates to V1_1__My_description.js</p>
	 *
	 * @param mongoMigrationSeparator The file name separator for Mongo JavaScript migrations (default: __)
	 */
	public void setMongoMigrationSeparator(String mongoMigrationSeparator) {
		if (!StringUtils.hasLength(mongoMigrationSeparator)) {
			throw new FlywayException("mongoMigrationSeparator cannot be empty!");
		}

		this.mongoMigrationSeparator = mongoMigrationSeparator;
	}

	/**
	 * Sets the file name suffix for Mongo JavaScript migrations.
	 * <p/>
	 * <p>Mongo JavaScript migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
	 * which using the defaults translates to V1_1__My_description.js</p>
	 *
	 * @param mongoMigrationSuffix The file name suffix for Mongo JavaScript migrations (default: .js)
	 */
	public void setMongoMigrationSuffix(String mongoMigrationSuffix) {
		this.mongoMigrationSuffix = mongoMigrationSuffix;
	}

	/**
	 * <p>Sets the name of the schema metadata table that will be used by Flyway.</p>
	 * <p> By default (single-schema mode) the metadata table is placed in the default schema for
	 * the connection provided by the datasource. </p>
	 * <p> When the <i>flyway.schemas</i> property is set (multi-schema mode), the metadata table is
	 * placed in the first schema of the list. </p>
	 *
	 * @param table The name of the schema metadata table that will be used by flyway.
	 *              (default: schema_version)
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * Sets the target version up to which Flyway should consider migrations. Migrations with a
	 * higher version number will be ignored.
	 *
	 * @param target The target version up to which Flyway should consider migrations.
	 *               (default: the latest version)
	 */
	public void setTarget(MigrationVersion target) {
		this.target = target;
	}

	/**
	 * Sets the target version up to which Flyway should consider migrations.
	 * Migrations with a higher version number will be ignored.
	 *
	 * @param target The target version up to which Flyway should consider migrations.
	 *               The special value {@code current} designates the current version of the schema.
	 *               (default: the latest version)
	 */
	public void setTargetAsString(String target) {
		this.target = MigrationVersion.fromVersion(target);
	}

	/**
	 * Sets the MongoClient to use. Must have the necessary privileges to execute ddl.
	 *
     * Providing a MongoClient through this method will override the client created by using
     * the mongo uri. Use this method to retain the control over MongoClient instance.
     *
	 * @param client The MongoClient to use. Must have the necessary privileges to execute ddl.
	 */
	public void setMongoClient(MongoClient client) {
		this.client = client;
		createdMongoClient = false;
	}

	/**
	 * Sets the MongoClientURI to use.
	 *
     * To use a custom ClassLoader, setClassLoader() must be called prior to calling this method.
     *
	 * @param uri   MongoClient URI used to connect to a MongoDB database server.
	 */
	public void setMongoClientUri(String uri) {
        MongoClientURI mongoUri = new MongoClientURI(uri);
        this.databaseName = mongoUri.getDatabase();
        if (databaseName == null) {
            throw new FlywayException("Cannot find database from Mongo URI!");
        }
        this.client = new MongoClient(mongoUri);
        createdMongoClient = true;
	}

	/**
	 * Sets the ClassLoader to use for resolving migrations on the classpath.
	 *
	 * @param classLoader The ClassLoader to use for resolving migrations on the classpath.
	 *                    (default: Thread.currentThread().getContextClassLoader() )
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Sets the version to tag an existing schema with when executing baseline.
	 *
	 * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
	 */
	public void setBaselineVersion(MigrationVersion baselineVersion) {
		this.baselineVersion = baselineVersion;
	}

	/**
	 * Sets the version to tag an existing schema with when executing baseline.
	 *
	 * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
	 */
	public void setBaselineVersionAsString(String baselineVersion) {
		this.baselineVersion = MigrationVersion.fromVersion(baselineVersion);
	}

	/**
	 * Sets the description to tag an existing schema with when executing baseline.
	 *
	 * @param baselineDescription The description to tag an existing schema with when executing baseline.
	 *                            (default: &lt;&lt; Flyway Baseline &gt;&gt;)
	 */
	public void setBaselineDescription(String baselineDescription) {
		this.baselineDescription = baselineDescription;
	}

	/**
	 * <p>
	 * Whether to automatically call baseline when migrate is executed against a non-empty schema
	 * with no metadata table. This schema will then be baselined with the {@code baselineVersion}
	 * before executing the migrations. Only migrations above {@code baselineVersion} will then be applied.
	 * </p>
	 * <p>
	 * This is useful for initial Flyway production deployments on projects with an existing DB.
	 * </p>
	 * <p>
	 * Be careful when enabling this as it removes the safety net that ensures
	 * Flyway does not migrate the wrong database in case of a configuration mistake!
	 * </p>
	 *
	 * @param baselineOnMigrate {@code true} if baseline should be called on migrate for non-empty schemas,
	 * {@code false} if not. (default: {@code false})
	 */
	public void setBaselineOnMigrate(boolean baselineOnMigrate) {
		this.baselineOnMigrate = baselineOnMigrate;
	}

	/**
	 * Allows migrations to be run "out of order".
	 * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
	 * it will be applied too instead of being ignored.</p>
	 *
	 * @param outOfOrder {@code true} if outOfOrder migrations should be applied, {@code false} if not.
	 *                   (default: {@code false})
	 */
	public void setOutOfOrder(boolean outOfOrder) {
		this.outOfOrder = outOfOrder;
	}

	@Override
	public MongoFlywayCallback[] getMongoCallbacks() {
		return callbacks;
	}

	@Override
	public boolean isSkipDefaultCallbacks() {
		return skipDefaultCallbacks;
	}

	/**
	 * Set the callbacks for lifecycle notifications.
	 *
	 * @param callbacks The callbacks for lifecycle notifications. (default: none)
	 */
	public void setMongoCallbacks(MongoFlywayCallback... callbacks) {
		this.callbacks = callbacks;
	}

	/**
	 * Set the callbacks for lifecycle notifications.
	 *
	 * @param callbacks The fully qualified class names of the callbacks for lifecycle notifications.
	 *                  (default: none)
	 */
	public void setMongoCallbacksAsClassNames(String... callbacks) {
		List<MongoFlywayCallback> callbackList = ClassUtils.instantiateAll(callbacks, classLoader);
		setMongoCallbacks(callbackList.toArray(new MongoFlywayCallback[callbacks.length]));
	}

	/**
	 * Whether Flyway should skip the default callbacks. If true, only custom callbacks are used.
	 *
	 * @param skipDefaultCallbacks Whether default built-in callbacks should be skipped. <p>(default: false)</p>
	 */
	public void setSkipDefaultCallbacks(boolean skipDefaultCallbacks) {
		this.skipDefaultCallbacks = skipDefaultCallbacks;
	}

	/**
	 * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving
	 * migrations to apply.
	 *
	 * @param resolvers The custom MigrationResolvers to be used in addition to the built-in ones
	 *                  for resolving Migrations to apply. (default: empty list)
	 */
	public void setResolvers(MigrationResolver... resolvers) {
		this.resolvers = resolvers;
	}

	/**
	 * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
	 *
	 * @param resolvers The fully qualified class names of the custom MigrationResolvers to be used in addition
	 *                  to the built-in ones for resolving Migrations to apply. (default: empty list)
	 */
	public void setResolversAsClassNames(String... resolvers) {
		List<MigrationResolver> resolverList = ClassUtils.instantiateAll(resolvers, classLoader);
		setResolvers(resolverList.toArray(new MigrationResolver[resolvers.length]));
	}

	/**
	 * Whether Flyway should skip the default resolvers. If true, only custom resolvers are used.
	 *
	 * @param skipDefaultResolvers Whether default built-in resolvers should be skipped. <p>(default: false)</p>
	 */
	public void setSkipDefaultResolvers(boolean skipDefaultResolvers) {
		this.skipDefaultResolvers = skipDefaultResolvers;
	}

	/**
	 * <p>Starts the database migration. All pending migrations will be applied in order.
	 * Calling migrate on an up-to-date database has no effect.</p>
	 * <img src="https://flywaydb.org/assets/balsamiq/command-migrate.png" alt="migrate">
	 *
	 * @return The number of successfully applied migrations.
	 * @throws FlywayException when the migration failed.
	 */
	public int migrate() throws FlywayException {
		return execute(new Command<Integer>() {
			public Integer execute(MongoClient client, MigrationResolver migrationResolver,
								   MongoMetaDataTable metaDataTable, MongoFlywayCallback[] flywayCallbacks) {
				if (validateOnMigrate) {
					doValidate(client, migrationResolver, metaDataTable, flywayCallbacks, true);
				}

                if (!metaDataTable.exists()) {
                    boolean databaseIsEmpty = MongoDatabaseUtil.empty(client, databaseName);
                    if (!databaseIsEmpty) {
                        if (baselineOnMigrate) {
                            new MongoBaseline(client, metaDataTable, baselineVersion, baselineDescription, flywayCallbacks).baseline();
                        } else {
                            throw new FlywayException("Found non-empty MongoDB instance without metadata table! Use"
                                    + " baseline() or set baselineOnMigrate to true to initialize the metadata table.");
                        }
                    }
                }

				MongoMigrate mongoMigrate = new MongoMigrate(client, metaDataTable,
						migrationResolver, ignoreFutureMigrations, MongoFlyway.this);
				return mongoMigrate.migrate();
			}
		});
	}

	/**
	 * <p>Validate applied migrations against resolved ones (on the filesystem or classpath)
	 * to detect accidental changes that may prevent the schema(s) from being recreated exactly.</p>
	 * <p>Validation fails if</p>
	 * <ul>
	 *     <li>differences in migration names, types or checksums are found</li>
	 *     <li>versions have been applied that aren't resolved locally anymore</li>
	 *     <li>versions have been resolved that haven't been applied yet</li>
	 * </ul>
	 *
	 * <img src="https://flywaydb.org/assets/balsamiq/command-validate.png" alt="validate">
	 *
	 * @throws FlywayException when the validation failed.
	 */
	public void validate() throws FlywayException {
		execute(new Command<Void>() {
			public Void execute(MongoClient client, MigrationResolver migrationResolver,
								MongoMetaDataTable metaDataTable, MongoFlywayCallback[] flywayCallbacks) {
				doValidate(client, migrationResolver, metaDataTable, flywayCallbacks, false);
				return null;
			}
		});
	}

	/**
	 * Performs the actual validation. All set up must have taken place beforehand.
	 *
	 * @param client                  The mongo client to interact with the database.
	 * @param migrationResolver       The migration resolver.
	 * @param metaDataTable           The metadata table.
	 * @param flywayCallbacks         Callbacks to fire off before and after validation.
	 * @param pending                 Whether pending migrations are ok.
	 */
	private void doValidate(MongoClient client, MigrationResolver migrationResolver,
							MongoMetaDataTable metaDataTable, MongoFlywayCallback[] flywayCallbacks, boolean pending) {
		String validationError = new MongoValidate(client, metaDataTable, migrationResolver, target,
				outOfOrder, pending, ignoreFutureMigrations, ignoreFutureMigrations, flywayCallbacks).validate();

		if (validationError != null) {
			if (cleanOnValidationError) {
				new MongoClean(client, flywayCallbacks, cleanDisabled).clean();
			} else {
				throw new FlywayException("Validate failed: " + validationError);
			}
		}
	}

	/**
	 * <p>Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.
	 * The schemas are cleaned in the order specified by the {@code schemas} property.</p>
	 * <img src="https://flywaydb.org/assets/balsamiq/command-clean.png" alt="clean">
	 *
	 * @throws FlywayException when the clean fails.
	 */
	public void clean() {
		execute(new Command<Void>() {
			public Void execute(MongoClient client, MigrationResolver migrationResolver,
								MongoMetaDataTable metaDataTable, MongoFlywayCallback[] flywayCallbacks) {
				new MongoClean(client, flywayCallbacks, cleanDisabled).clean();
				return null;
			}
		});
	}

	/**
	 * <p>Retrieves the complete information about all the migrations including applied, pending and
	 * current migrations with details and status.</p>
	 * <img src="https://flywaydb.org/assets/balsamiq/command-info.png" alt="info">
	 *
	 * @return All migrations sorted by version, oldest first.
	 * @throws FlywayException when the info retrieval failed.
	 */
	public MigrationInfoService info() {
		return execute(new Command<MigrationInfoService>() {
			public MigrationInfoService execute(MongoClient client, MigrationResolver migrationResolver,
                                          MongoMetaDataTable metaDataTable, MongoFlywayCallback[] flywayCallbacks) {
				for (final MongoFlywayCallback callback : flywayCallbacks) {
					callback.beforeInfo(client);
				}

				MigrationInfoServiceImpl migrationInfoService =
					new MigrationInfoServiceImpl(migrationResolver, metaDataTable, target, outOfOrder, true, true, true);
				migrationInfoService.refresh();

				for (final MongoFlywayCallback callback : flywayCallbacks) {
					callback.afterInfo(client);
				}

				return migrationInfoService;
			}
		});
	}

	/**
	 * <p>Baselines an existing database, excluding all migrations up to and including baselineVersion.</p>
	 * <p/>
	 * <img src="https://flywaydb.org/assets/balsamiq/command-baseline.png" alt="baseline">
	 *
	 * @throws FlywayException when the schema baselining failed.
	 */
	public void baseline() throws FlywayException {
		execute(new Command<Void>() {
			public Void execute(MongoClient client, MigrationResolver migrationResolver,
								MongoMetaDataTable metaDataTable, MongoFlywayCallback[] flywayCallbacks) {
				new MongoBaseline(client, metaDataTable, baselineVersion, baselineDescription, flywayCallbacks).baseline();
				return null;
			}
		});
	}

	/**
	 * Repairs the Flyway metadata table. This will perform the following actions:
	 * <ul>
	 * <li>Remove any failed migrations on databases without DDL transactions (User objects left behind must still be cleaned up manually)</li>
	 * <li>Correct wrong checksums</li>
	 * </ul>
	 * <img src="https://flywaydb.org/assets/balsamiq/command-repair.png" alt="repair">
	 *
	 * @throws FlywayException when the metadata table repair failed.
	 */
	public void repair() throws FlywayException {
		execute(new Command<Void>() {
			public Void execute(MongoClient client, MigrationResolver migrationResolver,
								MongoMetaDataTable metaDataTable, MongoFlywayCallback[] flywayCallbacks) {
				new MongoRepair(client, migrationResolver, metaDataTable, flywayCallbacks).repair();
				return null;
			}
		});
	}

    /**
     * Creates the MigrationResolver.
     *
     * @param scanner   The Scanner for resolving migrations.
     * @return A new, fully configured, MigrationResolver instance.
     */
    private MigrationResolver createMigrationResolver(Scanner scanner) {
        for (MigrationResolver resolver : resolvers) {
            ConfigurationInjectionUtils.injectFlywayConfiguration(resolver, this);
        }

        return new CompositeMongoMigrationResolver(scanner, locations, this, createPlaceholderReplacer(), resolvers);
    }

    /**
     * @return A new, fully configured, PlaceholderReplacer.
     */
    private PlaceholderReplacer createPlaceholderReplacer() {
        if (placeholderReplacement) {
            return new PlaceholderReplacer(placeholders, placeholderPrefix, placeholderSuffix);
        }
        return PlaceholderReplacer.NO_PLACEHOLDERS;
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration.
     * Property names are documented in the flyway maven plugin.
     * <p/>
     * <p>To use a custom ClassLoader, setClassLoader() must be called prior to calling this method.</p>
     *
     * @param properties Properties used for configuration.
     * @throws FlywayException when the configuration failed.
     */
	@SuppressWarnings("ConstantConditions")
	public void configure(Properties properties) {
        Map<String, String> props = new HashMap<String, String>();
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      props.put(entry.getKey().toString(), entry.getValue().toString());
    }

    String uriProp = props.remove("flyway.mongoUri");

    if (StringUtils.hasText(uriProp)) {
      setMongoClientUri(uriProp);
    } else {
      LOG.warn("Incomplete MongoDB configuration! flyway.mongoUri must be set.");
    }

    String locationsProp = props.remove("flyway.locations");
    if (locationsProp != null) {
      setLocations(StringUtils.tokenizeToStringArray(locationsProp, ","));
    }
    String placeholderReplacementProp = props.remove("flyway.placeholderReplacement");
    if (placeholderReplacementProp != null) {
      setPlaceholderReplacement(Boolean.parseBoolean(placeholderReplacementProp));
    }
    String placeholderPrefixProp = props.remove("flyway.placeholderPrefix");
    if (placeholderPrefixProp != null) {
      setPlaceholderPrefix(placeholderPrefixProp);
    }
    String placeholderSuffixProp = props.remove("flyway.placeholderSuffix");
    if (placeholderSuffixProp != null) {
      setPlaceholderSuffix(placeholderSuffixProp);
    }
    String mongoMigrationPrefixProp = props.remove("flyway.mongoMigrationPrefix");
    if (mongoMigrationPrefixProp != null) {
      setMongoMigrationPrefix(mongoMigrationPrefixProp);
    }
    String repeatableMongoMigrationPrefixProp = props.remove("flyway.repeatableMongoMigrationPrefix");
    if (repeatableMongoMigrationPrefixProp != null) {
      setRepeatableMongoMigrationPrefix(repeatableMongoMigrationPrefixProp);
    }
    String mongoMigrationSeparatorProp = props.remove("flyway.mongoMigrationSeparator");
    if (mongoMigrationSeparatorProp != null) {
      setMongoMigrationSeparator(mongoMigrationSeparatorProp);
    }
    String mongoMigrationSuffixProp = props.remove("flyway.mongoMigrationSuffix");
    if (mongoMigrationSuffixProp != null) {
      setMongoMigrationSuffix(mongoMigrationSuffixProp);
    }
    String encodingProp = props.remove("flyway.encoding");
    if (encodingProp != null) {
      setEncoding(encodingProp);
    }
    String tableProp = props.remove("flyway.table");
    if (tableProp != null) {
      setTable(tableProp);
    }
    String cleanOnValidationErrorProp = props.remove("flyway.cleanOnValidationError");
    if (cleanOnValidationErrorProp != null) {
      setCleanOnValidationError(Boolean.parseBoolean(cleanOnValidationErrorProp));
    }
    String cleanDisabledProp = props.remove("flyway.cleanDisabled");
    if (cleanDisabledProp != null) {
      setCleanDisabled(Boolean.parseBoolean(cleanDisabledProp));
    }
    String validateOnMigrateProp = props.remove("flyway.validateOnMigrate");
    if (validateOnMigrateProp != null) {
      setValidateOnMigrate(Boolean.parseBoolean(validateOnMigrateProp));
    }
    String baselineVersionProp = props.remove("flyway.baselineVersion");
    if (baselineVersionProp != null) {
      setBaselineVersion(MigrationVersion.fromVersion(baselineVersionProp));
    }
    String baselineDescriptionProp = props.remove("flyway.baselineDescription");
    if (baselineDescriptionProp != null) {
      setBaselineDescription(baselineDescriptionProp);
    }
    String baselineOnMigrateProp = props.remove("flyway.baselineOnMigrate");
    if (baselineOnMigrateProp != null) {
      setBaselineOnMigrate(Boolean.parseBoolean(baselineOnMigrateProp));
    }
    String ignoreMissingMigrationsProp = props.remove("flyway.ignoreMissingMigrations");
    if (ignoreMissingMigrationsProp != null) {
      setIgnoreMissingMigrations(Boolean.parseBoolean(ignoreMissingMigrationsProp));
    }
    String ignoreFutureMigrationsProp = props.remove("flyway.ignoreFutureMigrations");
    if (ignoreFutureMigrationsProp != null) {
      setIgnoreFutureMigrations(Boolean.parseBoolean(ignoreFutureMigrationsProp));
    }
    String targetProp = props.remove("flyway.target");
    if (targetProp != null) {
      setTarget(MigrationVersion.fromVersion(targetProp));
    }
    String outOfOrderProp = props.remove("flyway.outOfOrder");
    if (outOfOrderProp != null) {
      setOutOfOrder(Boolean.parseBoolean(outOfOrderProp));
    }
    String resolversProp = props.remove("flyway.resolvers");
    if (StringUtils.hasLength(resolversProp)) {
      setResolversAsClassNames(StringUtils.tokenizeToStringArray(resolversProp, ","));
    }
    String skipDefaultResolversProp = props.remove("flyway.skipDefaultResolvers");
    if (skipDefaultResolversProp != null) {
      setSkipDefaultResolvers(Boolean.parseBoolean(skipDefaultResolversProp));
    }
    String callbacksProp = props.remove("flyway.callbacks");
    if (StringUtils.hasLength(callbacksProp)) {
      setMongoCallbacksAsClassNames(StringUtils.tokenizeToStringArray(callbacksProp, ","));
    }
    String skipDefaultCallbacksProp = props.remove("flyway.skipDefaultCallbacks");
    if (skipDefaultCallbacksProp != null) {
      setSkipDefaultCallbacks(Boolean.parseBoolean(skipDefaultCallbacksProp));
    }

    Map<String, String> placeholdersFromProps = new HashMap<String, String>(placeholders);
    Iterator<Map.Entry<String, String>> iterator = props.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, String> entry = iterator.next();
      String propertyName = entry.getKey();

      if (propertyName.startsWith(PLACEHOLDERS_PROPERTY_PREFIX)
          && propertyName.length() > PLACEHOLDERS_PROPERTY_PREFIX.length()) {
        String placeholderName = propertyName.substring(PLACEHOLDERS_PROPERTY_PREFIX.length());
        String placeholderValue = entry.getValue();
        placeholdersFromProps.put(placeholderName, placeholderValue);
        iterator.remove();
      }
    }
    setPlaceholders(placeholdersFromProps);

    String installedByProp = props.remove("flyway.installedBy");
    if (installedByProp != null) {
      setInstalledBy(installedByProp);
    }

    for (String key : props.keySet()) {
      if (key.startsWith("flyway.")) {
        LOG.warn("Unknown configuration property: " + key);
      }
    }
	}

	/**
	 * Executes this command with proper resource handling and cleanup.
	 *
	 * @param command The command to execute.
	 * @param <T>     The type of the result.
	 * @return The result of the command.
	 */
	/*private -> testing*/ <T> T execute(Command<T> command) {
		T result;

		VersionPrinter.printVersion();

		try {
			if (client == null) {
				throw new FlywayException("Unable to connect to the database. Configure the Mongo URI!");
			}

			Scanner scanner = new Scanner(classLoader);
			MigrationResolver migrationResolver = createMigrationResolver(scanner);
			Set<MongoFlywayCallback> flywayCallbacks = new LinkedHashSet<MongoFlywayCallback>(Arrays.asList(callbacks));
			if (!skipDefaultCallbacks) {
				flywayCallbacks.add(new MongoScriptFlywayCallback(scanner, locations, createPlaceholderReplacer(), this));
			}

			MongoFlywayCallback[] flywayCallbacksArray = flywayCallbacks.toArray(new MongoFlywayCallback[flywayCallbacks.size()]);
			for (MongoFlywayCallback callback : flywayCallbacks) {
				ConfigurationInjectionUtils.injectFlywayConfiguration(callback, this);
			}

			MongoMetaDataTable metaDataTable = new MongoMetaDataTable(client, databaseName, table);
			if (metaDataTable.upgradeIfNecessary()) {
				new MongoRepair(client, migrationResolver, metaDataTable, flywayCallbacksArray).repairChecksumsAndDescriptions();
				LOG.info("Metadata table " + table + " successfully upgraded to the Flyway 4.0 format.");
			}
			result = command.execute(client, migrationResolver, metaDataTable, flywayCallbacksArray);
		} finally {
			if ((client instanceof MongoClient) && createdMongoClient) {
				client.close();
			}
		}
		return result;
	}

	/**
	 * A Flyway command that can be executed.
	 *
	 * @param <T> The result type of the command.
	 */
	interface Command<T> {
		/**
		 * Execute the operation.
		 *
		 * @param client            The Mongo client used to interact with the database.
		 * @param migrationResolver The migration resolver to use.
		 * @param metaDataTable     The metadata table.
		 * @param flywayCallbacks   The callbacks to use.
		 */
		T execute(MongoClient client, MigrationResolver migrationResolver, MongoMetaDataTable metaDataTable, MongoFlywayCallback[] flywayCallbacks);
	}
}
