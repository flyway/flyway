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

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoCredential;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ValidationLevel;
import com.mongodb.client.model.ValidationOptions;
import com.mongodb.client.model.CreateCollectionOptions;

/**
 * The metadata table used to track all applied migrations for MongoDB.
 */
public class MongoMetaDataTable implements FlywayMetaDataTable {
	private static final Log LOG = LogFactory.getLog(MongoMetaDataTable.class);

	/**
	 * Name for the metadata collection.
	 */
	private String tableName;
	
	/**
	 * Mongo client for interacting with the DB.
	 */
	private MongoClient client;
	
	/**
	 * Mongo database containing the metadata collection.
	 */
	private MongoDatabase mongoDatabase;
	
	/**
	 * Metadata collection in MongoDB.
	 */
	private MongoCollection<BasicDBObject> metadataCollection;
	
	/**
	 * Creates a new instance of a MongoMetaDataTable.
	 *
	 * @param client The MongoClient used to interact with the database.
	 */
	public MongoMetaDataTable(MongoClient client, String dbName, String tableName) {
		boolean collectionExists = false;
		this.client = client;
		this.tableName = tableName;
	  this.mongoDatabase = client.getDatabase(dbName);
		
		if (!(collectionExists = metadataCollectionExists())) {
		  this.metadataCollection = mongoDatabase.getCollection(tableName, BasicDBObject.class);
		} else {
			// Database types used in the metadata collection schema.
			BasicDBObject dbInt = new BasicDBObject("$type", "int");
			BasicDBObject dbString = new BasicDBObject("$type", "string");
			BasicDBObject dbBool = new BasicDBObject("$type", "bool");
			BasicDBObject dbDate = new BasicDBObject("$type", "date");
			// Database object for setting a valid mongo schema.
			BasicDBObject validSchema = new BasicDBObject().
				append(MetaDataDocument.INSTALLED_RANK, dbInt).
				append(MetaDataDocument.VERSION, dbString).
				append(MetaDataDocument.DESCRIPTION, dbString).
				append(MetaDataDocument.TYPE, dbString).
				append(MetaDataDocument.SCRIPT, dbString).
				append(MetaDataDocument.CHECKSUM, dbInt).
				append(MetaDataDocument.INSTALLED_BY, dbString).
				append(MetaDataDocument.INSTALLED_ON, dbDate).
				append(MetaDataDocument.EXECUTION_TIME, dbInt).
				append(MetaDataDocument.SUCCESS, dbBool);
			ValidationOptions validator = new ValidationOptions().
				validator(validSchema).
				validationLevel(ValidationLevel.STRICT);
			CreateCollectionOptions collectionOptions = new CreateCollectionOptions().
				validationOptions(validator);

			if (!collectionExists) mongoDatabase.createCollection(tableName, collectionOptions);
			this.metadataCollection = mongoDatabase.getCollection(tableName, BasicDBObject.class);
		}
	}
	
	@Override
	public void lock() {
		
	}

	@Override
	public void addAppliedMigration(AppliedMigration appliedMigration) {
		AppliedMigration rankAdjusted = new AppliedMigration(calculateInstalledRank(), appliedMigration.getVersion(),
																												 appliedMigration.getDescription(), appliedMigration.getType(), appliedMigration.getScript(),
																												 appliedMigration.getChecksum(), appliedMigration.getInstalledOn(), appliedMigration.getInstalledBy(),
																												 appliedMigration.getExecutionTime(), appliedMigration.isSuccess());
		
	  metadataCollection.insertOne(new MetaDataDocument(rankAdjusted));
	}

  @Override
	public boolean hasAppliedMigrations() {
		int count = 0;
		BasicDBObject query = new BasicDBObject(MetaDataDocument.TYPE,
																						new BasicDBObject("$regex", "[^(BASELINE)]"));

		for (BasicDBObject dbo : metadataCollection.find(query)) {
			count++;
		}
		
		return count > 0;
	}
	
	@Override
	public List<AppliedMigration> allAppliedMigrations() {
		List<AppliedMigration> appliedMigrations = new ArrayList<AppliedMigration>();
		for (BasicDBObject dbo : metadataCollection.find()) {
			appliedMigrations.add(new MetaDataDocument(dbo).toMigration());
		}

		return appliedMigrations;
	}

	@Override
	public void addBaselineMarker(MigrationVersion initVersion, String initDescription) {
		MongoCredential dbCreds = null;
		String username = "";
		
		try {
			dbCreds = client.getCredentialsList().get(0);
			username = dbCreds.getUserName();
		} catch (Exception e) {
			LOG.info("Continuing to baseline without Mongo credentials");
		}
		
		MetaDataDocument baselineMarker = new MetaDataDocument(0, initVersion, initDescription,
																													 MigrationType.BASELINE, initDescription, null, new Date(),
																													 username, 0, true);

		metadataCollection.insertOne(baselineMarker);
	}

  @Override
	public boolean hasBaselineMarker() {
		boolean cond = false;

		if (!metadataCollectionExists()) {
			return cond;
		}
		
		for (BasicDBObject dbo : metadataCollection.find()) {
			MetaDataDocument mdd = new MetaDataDocument(dbo);
			
			if (mdd.getType() == MigrationType.BASELINE) {
				cond = true;
				break;
			}
		}
		
		return cond;
	}

  @Override
	public AppliedMigration getBaselineMarker() {
		AppliedMigration baselineMarker = null;
		
		for (AppliedMigration am : allAppliedMigrations()) {
			if (am.getType() == MigrationType.BASELINE) {
			  baselineMarker = am;
				break;
			}
		}
		
		return baselineMarker;
	}

  @Override
	public void removeFailedMigrations() {
		for (BasicDBObject dbo : metadataCollection.find()) {
			MetaDataDocument mdd = new MetaDataDocument(dbo);

			if (!mdd.isSuccess()) metadataCollection.deleteOne(mdd);
		}
	}

  @Override
	public void updateChecksum(MigrationVersion version, Integer checksum) {
		String searchVersion = version.getVersion();
		
		for (BasicDBObject dbo : metadataCollection.find()) {
			MetaDataDocument mdd = new MetaDataDocument(dbo);
			String docVersion = mdd.getVersion().getVersion();
			
			if (docVersion.equals(searchVersion)) {
				mdd.updateChecksum(metadataCollection, checksum);
			  return;
			}
		}
	}

  @Override
	public boolean upgradeIfNecessary() {
		boolean isNecessary = false;
		String oldRank = "version_rank";
	  BasicDBObject searchQuery = new BasicDBObject(oldRank,
																									new BasicDBObject("$regex", "\\d+(\\.\\d+)*"));
		FindIterable<BasicDBObject> upgrades = metadataCollection.find(searchQuery, BasicDBObject.class);
		
		for (BasicDBObject dbo : upgrades) {
			MetaDataDocument mdd = new MetaDataDocument(dbo);
			Integer rankValue = mdd.getInt(oldRank);
			
			if (rankValue == null) continue;
			else {
				BasicDBObject upgradeQuery = new BasicDBObject(oldRank, rankValue);
				mdd.removeField(oldRank);
				mdd.put(MetaDataDocument.INSTALLED_RANK, rankValue);
				
				metadataCollection.updateOne(upgradeQuery, mdd);
				isNecessary = true;
			}
		}

		return isNecessary;
	}

	private Integer calculateInstalledRank() {
		int max = 0;

		for (BasicDBObject dbo : metadataCollection.find()) {
			MetaDataDocument doc = new MetaDataDocument(dbo);
			Integer rank = doc.getInstalledRank();
			max = (rank <= max) ? max : rank;
		}

		return max + 1;
	}
	
	private boolean metadataCollectionExists() {
		boolean collectionExists = false;
		
		for (String name : mongoDatabase.listCollectionNames()) {
			collectionExists |= name.equals(tableName);
		}

		return collectionExists;
	}
}
