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
package org.flywaydb.core.internal.metadatatable;

import java.util.Map;
import java.util.Date;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.MigrationType;

import com.mongodb.BasicDBObject;

public class MetaDataDocument extends BasicDBObject {

	/** Names of the fields in the MongoDB MetaData Collection. */
	static final String INSTALLED_RANK = "installed_rank";
	static final String VERSION = "version";
	static final String DESCRIPTION = "description";
	static final String TYPE = "type";
	static final String SCRIPT = "script";
	static final String CHECKSUM = "checksum";
	static final String INSTALLED_BY = "installed_by";
	static final String INSTALLED_ON = "installed_on";
	static final String EXECUTION_TIME = "execution_time";
	static final String SUCCESS = "success";

	/**
	 * Creates a new MongoDB document for the Metadata table.
	 *
	 * @param map A DB object to create the document from.
	 */
	public MetaDataDocument(Map map) {
		super(map);
	}
	
	/**
	 * Creates a new MongoDB document for the Metadata table.
	 *
	 * @param appliedMigration An {@link AppliedMigration} to build the document from.
	 */
	public MetaDataDocument(AppliedMigration appliedMigration) {
		String installBy = appliedMigration.getInstalledBy();
		Date installedOn = appliedMigration.getInstalledOn();
		MigrationVersion version = appliedMigration.getVersion();

		super.put("_id", appliedMigration.getInstalledRank());
		super.put(INSTALLED_RANK, appliedMigration.getInstalledRank());
		super.put(VERSION, (version == null) ? null : version.getVersion());
		super.put(DESCRIPTION, appliedMigration.getDescription());
		super.put(TYPE, appliedMigration.getType().toString());
		super.put(SCRIPT, appliedMigration.getScript());
		super.put(CHECKSUM, appliedMigration.getChecksum());
		super.put(INSTALLED_BY, (installBy == null) ? "" : installBy);
		super.put(INSTALLED_ON, (installedOn == null) ? new Date() : installedOn);
		super.put(EXECUTION_TIME, appliedMigration.getExecutionTime());
		super.put(SUCCESS, appliedMigration.isSuccess());
	}

	/**
	 * Creates a new MongoDB document for the Metadata table.
	 *
	 * @param installedRank The order in which this migration was applied amongst all others. (For out of order detection)
	 * @param version       The target version of this migration.
	 * @param description   The description of the migration.
	 * @param type          The type of migration (INIT, SQL, ...)
	 * @param script        The name of the script to execute for this migration, relative to its classpath location.
	 * @param checksum      The checksum of the migration. (Optional)
	 * @param installedOn   The timestamp when this migration was installed.
	 * @param installedBy   The user that installed this migration.
	 * @param executionTime The execution time (in millis) of this migration.
	 * @param success       Flag indicating whether the migration was successful or not.
	 */
	public MetaDataDocument(int installedRank, MigrationVersion version, String description,
							MigrationType type, String script, Integer checksum, Date installedOn,
							String installedBy, int executionTime, boolean success) {
		super.put("_id", installedRank);
		super.put(INSTALLED_RANK, installedRank);
		super.put(VERSION, (version != null) ? version.getVersion() : null);
		super.put(DESCRIPTION, description);
		super.put(TYPE, type.toString());
		super.put(SCRIPT, script);
		super.put(CHECKSUM, checksum);
		super.put(INSTALLED_ON, installedOn);
		super.put(INSTALLED_BY, installedBy);
		super.put(EXECUTION_TIME, executionTime);
		super.put(SUCCESS, success);
	}

	/**
	 * Converts this document into an {@link AppliedMigration}.
	 *
	 * @return This Mongo document as an {@link AppliedMigration}.
	 */
	public AppliedMigration toMigration() {
        return new AppliedMigration(getInstalledRank(), getVersion(), getDescription(), getType(), getScript(),
            getChecksum(), getInstalledOn(), getInstalledBy(), getExecutionTime(), isSuccess());
	}

	/**
	 * @return The order in which this migration was applied amongst all others. (For out of order detection)
	 */
	public int getInstalledRank() {
		return super.getInt(INSTALLED_RANK);
	}

	/**
	 * @return The target version of this migration. 'null' if this is a repeatable migration.
	 */
	public MigrationVersion getVersion() {
        if (super.getString(VERSION) != null) {
            return MigrationVersion.fromVersion(super.getString(VERSION));
        } else {
            return null;
        }
    }

	/**
	 * @return The description of the migration.
	 */
	public String getDescription() {
		return super.getString(DESCRIPTION);
	}

	/**
	 * @return The type of migration (BASELINE, SQL, ...)
	 */
	public MigrationType getType() {
		return MigrationType.valueOf(super.getString(TYPE));
	}

	/**
	 * @return The name of the script to execute for this migration, relative to its classpath location.
	 */
	public String getScript() {
		return super.getString(SCRIPT);
	}
	
	/**
	 * @return The checksum of the migration. (Optional)
	 */
	public Integer getChecksum() {
		int c = super.getInt(CHECKSUM, 0);
		return (c == 0) ? null : c;
	}

	/**
	 * @return The timestamp when this migration was installed.
	 */
	public Date getInstalledOn() {
		return super.getDate(INSTALLED_ON, new Date());
	}

	/**
	 * @return The user that installed this migration.
	 */
	public String getInstalledBy() {
		return super.getString(INSTALLED_BY, "");
	}

	/**
	 * @return The execution time (in millis) of this migration.
	 */
	public int getExecutionTime() {
		return super.getInt(EXECUTION_TIME);
	}

	/**
	 * @return Flag indicating whether the migration was successful or not.
	 */
	public boolean isSuccess() {
		return super.getBoolean(SUCCESS);
	}
}
