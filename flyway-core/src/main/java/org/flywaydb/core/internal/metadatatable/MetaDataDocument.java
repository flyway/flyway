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
package org.flywaydb.core.internal.metadatatable;

import java.util.Map;
import java.util.Date;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.MigrationType;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

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
	 * The order in which this migration was applied amongst all others. (For out of order detection)
	 */
	private int installedRank;

	/**
	 * The target version of this migration. {@code null} if it is a repeatable migration.
	 */
	private MigrationVersion version;

	/**
	 * The description of the migration.
	 */
	private String description;

	/**
	 * The type of migration (BASELINE, SQL, ...)
	 */
	private MigrationType type;

	/**
	 * The name of the script to execute for this migration, relative to its classpath location.
	 */
	private String script;

	/**
	 * The checksum of the migration. (Optional)
	 */
	private Integer checksum;

	/**
	 * The timestamp when this migration was installed.
	 */
	private Date installedOn;

	/**
	 * The user that installed this migration.
	 */
	private String installedBy;

	/**
	 * The execution time (in millis) of this migration.
	 */
	private int executionTime;

	/**
	 * Flag indicating whether the migration was successful or not.
	 */
	private boolean success;

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
		String s = appliedMigration.getInstalledBy();
		Date d = appliedMigration.getInstalledOn();

		super.put("_id", appliedMigration.getInstalledRank());
		super.put(INSTALLED_RANK, appliedMigration.getInstalledRank());
		super.put(VERSION, appliedMigration.getVersion().getVersion());
		super.put(DESCRIPTION, appliedMigration.getDescription());
		super.put(TYPE, appliedMigration.getType().toString());
		super.put(SCRIPT, appliedMigration.getScript());
		super.put(CHECKSUM, appliedMigration.getChecksum());
		super.put(INSTALLED_BY, (s == null) ? "" : s);
		super.put(INSTALLED_ON, (d == null) ? new Date() : d);
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
		super.put(VERSION, version.getVersion());
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
	 * @returns This Mongo document as an {@link AppliedMigration}.
	 */
	public AppliedMigration toMigration() {
		return new AppliedMigration(getInstalledRank(), getVersion(), getDescription(), getType(), getScript(),
																getChecksum(), getInstalledOn(), getInstalledBy(), getExecutionTime(), isSuccess());
	}

	/**
	 * Update the installed_rank field in this document.
	 *
	 * @param installedRank The order in which this migration was applied amongst all others. (For out of order detection)
	 */
	public UpdateResult updateInstalledRank(MongoCollection collection, int installedRank) {
		BasicDBObject updateDoc = new BasicDBObject("$set", new BasicDBObject(INSTALLED_RANK, installedRank));

		return collection.updateOne(this, updateDoc);
	}

	/**
	 * @return The order in which this migration was applied amongst all others. (For out of order detection)
	 */
	public int getInstalledRank() {
		return super.getInt(INSTALLED_RANK);
	}

	/**
	 * Update the version field in this document.
	 *
	 * @param version The target version of this migration.
	 */
	public UpdateResult updateVersion(MongoCollection collection, MigrationVersion migrationVersion) {
		BasicDBObject updateDoc = new BasicDBObject("$set", new BasicDBObject(VERSION, migrationVersion.getVersion()));

		return collection.updateOne(this, updateDoc);
	}

	/**
	 * @return The target version of this migration.
	 */
	public MigrationVersion getVersion() {
		return MigrationVersion.fromVersion(super.getString(VERSION));
	}

	/**
	 * Update the description field in this document.
	 *
	 * @param version The description of this migration.
	 */
	public UpdateResult updateDescription(MongoCollection collection, String description) {
		BasicDBObject updateDoc = new BasicDBObject("$set", new BasicDBObject(DESCRIPTION, description));

		return collection.updateOne(this, updateDoc);
	}
	
	/**
	 * @return The description of the migration.
	 */
	public String getDescription() {
		return super.getString(DESCRIPTION);
	}

	/**
	 * Update the type field in this document.
	 *
	 * @param type The migration type for this document.
	 */
	public UpdateResult updateType(MongoCollection collection, MigrationType type) {
		BasicDBObject updateDoc = new BasicDBObject("$set", new BasicDBObject(TYPE, type.toString()));

		return collection.updateOne(this, updateDoc);
	}
	
	/**
	 * @return The type of migration (BASELINE, SQL, ...)
	 */
	public MigrationType getType() {
		return MigrationType.valueOf(super.getString(TYPE));
	}

	/**
	 * Update the script field in this document.
	 *
	 * @param script The name of the script to execute for this migration, relative to its classpath location.
	 */
	public UpdateResult updateScript(MongoCollection collection, String script) {
		BasicDBObject updateDoc = new BasicDBObject("$set", new BasicDBObject(SCRIPT, script));

		return collection.updateOne(this, updateDoc);
	}
	
	/**
	 * @return The name of the script to execute for this migration, relative to its classpath location.
	 */
	public String getScript() {
		return super.getString(SCRIPT);
	}
	
	/**
	 * Update the checksum field in this document.
	 *
	 * @param checksum The checksum of the migration. (Optional)
	 */
	public UpdateResult updateChecksum(MongoCollection collection, Integer checksum) {
		BasicDBObject updateDoc = new BasicDBObject("$set", new BasicDBObject(CHECKSUM, checksum));

		return collection.updateOne(this, updateDoc);
	}
	
	/**
	 * @return The checksum of the migration. (Optional)
	 */
	public Integer getChecksum() {
		int c = super.getInt(CHECKSUM, 0);
		return (c == 0) ? null : c;
	}

	/**
	 * Update the installed_on field in this document.
	 *
	 * @param date The timestamp when this migration was installed.
	 */
	public UpdateResult updateInstalledOn(MongoCollection collection, Date date) {
		BasicDBObject updateDoc = new BasicDBObject("$set", new BasicDBObject(INSTALLED_ON, date));

		return collection.updateOne(this, updateDoc);
	}
	
	/**
	 * @return The timestamp when this migration was installed.
	 */
	public Date getInstalledOn() {
		return super.getDate(INSTALLED_ON, new Date());
	}

	/**
	 * Update the installed_by field in this document.
	 *
	 * @param installedBy The user that installed this migration.
	 */
	public UpdateResult updateInstalledBy(MongoCollection collection, String installedBy) {
		BasicDBObject updateDoc = new BasicDBObject("$set", new BasicDBObject(INSTALLED_BY, installedBy));

		return collection.updateOne(this, updateDoc);
	}
		
	/**
	 * @return The user that installed this migration.
	 */
	public String getInstalledBy() {
		return super.getString(INSTALLED_BY, "");
	}

	/**
	 * Update the installed_by field in this document.
	 *
	 * @param installedBy The user that installed this migration.
	 */
	public UpdateResult updateExecutionTime(MongoCollection collection, int executionTime) {
		BasicDBObject updateDoc = new BasicDBObject("$set", new BasicDBObject(EXECUTION_TIME, executionTime));

		return collection.updateOne(this, updateDoc);
	}
	
	/**
	 * @return The execution time (in millis) of this migration.
	 */
	public int getExecutionTime() {
		return super.getInt(EXECUTION_TIME);
	}

	/**
	 * Update the success field in this document.
	 *
	 * @param success Flag indicating whether the migration was successful or not.
	 */
	public UpdateResult updateExecutionTime(MongoCollection collection, boolean success) {
		BasicDBObject updateDoc = new BasicDBObject("$set", new BasicDBObject(SUCCESS, success));

		return collection.updateOne(this, updateDoc);
	}
	
	/**
	 * @return Flag indicating whether the migration was successful or not.
	 */
	public boolean isSuccess() {
		return super.getBoolean(SUCCESS);
	}
}
