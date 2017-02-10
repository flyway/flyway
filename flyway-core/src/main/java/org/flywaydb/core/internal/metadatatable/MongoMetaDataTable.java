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

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.internal.util.mongo.MongoDatabaseUtil;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

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
  private String collectionName;


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
   * @param dbName the database name where metadata collection will be created.
   * @param collectionName the metadata collection name.
   */
  public MongoMetaDataTable(MongoClient client, String dbName, String collectionName) {
    this.client = client;
    this.collectionName = collectionName;
      this.mongoDatabase = client.getDatabase(dbName);

    if (metadataCollectionExists()) {
      this.metadataCollection = mongoDatabase.getCollection(collectionName, BasicDBObject.class);
    } else {
      createMetadataCollection(dbName);
    }
  }
	
	@Override
	public void addAppliedMigration(AppliedMigration appliedMigration) {
    AppliedMigration rankAdjusted = new AppliedMigration(
      calculateInstalledRank(), appliedMigration.getVersion(),
      appliedMigration.getDescription(), appliedMigration.getType(), appliedMigration.getScript(),
      appliedMigration.getChecksum(), appliedMigration.getInstalledOn(),
      appliedMigration.getInstalledBy(), appliedMigration.getExecutionTime(),
      appliedMigration.isSuccess());
    metadataCollection.insertOne(new MetaDataDocument(rankAdjusted));
    LOG.debug("MetaData collection " + collectionName + " successfully updated to reflect changes");
	}

  @Override
  public boolean exists() {
    return hasSchemasMarker() || hasBaselineMarker() || hasAppliedMigrations();
  }

  @Override
  public boolean hasAppliedMigrations() {
    Document query = Document.parse("{type: {$not: {$in: ['BASELINE', 'SCHEMA', 'INIT'] }}}");
    FindIterable foundMigrations = metadataCollection.find(query);
    return foundMigrations.iterator().hasNext();
  }

  @Override
  public List<AppliedMigration> allAppliedMigrations() {
    List<AppliedMigration> appliedMigrations = new ArrayList<AppliedMigration>();
    if (!metadataCollectionExists()) {
      return appliedMigrations;
    }
    for (BasicDBObject dbo : metadataCollection.find()) {
      appliedMigrations.add(new MetaDataDocument(dbo).toMigration());
    }
    return appliedMigrations;
  }

	private void addSchemaMarker() {
    addAppliedMigration(new AppliedMigration(
      MigrationVersion.fromVersion("0"), "<< Flyway Schema Creation >>", MigrationType.SCHEMA, "", null, 0, true));
  }

    @Override
    public boolean hasSchemasMarker() {
      if (!metadataCollectionExists()) {
          return false;
      }
      Document query = Document.parse("{ type: 'SCHEMA' }");
      FindIterable databaseMarkers = metadataCollection.find(query);
      return databaseMarkers.iterator().hasNext();
    }

	@Override
	public void addBaselineMarker(MigrationVersion baselineVersion, String baselineDescription) {
		MongoCredential dbCreds;
		String username = "";
		
		try {
			dbCreds = client.getCredentialsList().get(0);
			username = dbCreds.getUserName();
		} catch (Exception e) {
			LOG.info("Continuing to baseline without Mongo credentials");
		}
		
		MetaDataDocument baselineMarker = new MetaDataDocument(0, baselineVersion, baselineDescription,
				MigrationType.BASELINE, baselineDescription, null, new Date(), username, 0, true);

		metadataCollection.insertOne(baselineMarker);
	}

  	@Override
	public boolean hasBaselineMarker() {
		if (!metadataCollectionExists()) {
			return false;
		}
		Document query = Document.parse("{ type: { $in: ['INIT', 'BASELINE'] }}");
		FindIterable baselineMarkers = metadataCollection.find(query);
		return baselineMarkers.iterator().hasNext();
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
    Document filter = Document.parse("{" + MetaDataDocument.SUCCESS + ": false}");
    DeleteResult result = metadataCollection.deleteMany(filter);
    Long deleteCount = result.getDeletedCount();
    if (deleteCount > 0) {
      LOG.debug("Repaired " + deleteCount + " failed migration(s) in metadata collection " + collectionName);
    } else {
      LOG.info("Repair of failed migration in metadata table " + collectionName + " not necessary. No failed migration detected.");
    }
	}

  @Override
  public void update(MigrationVersion version, String description,  Integer checksum) {
    LOG.info("Repairing metadata for version " + version + " (Description: " + description + ", Checksum: " + checksum + ")  ...");
    String searchVersion = version.getVersion();
    BasicDBObject filter = new BasicDBObject().append(MetaDataDocument.VERSION, searchVersion);
    BasicDBObject newData= new BasicDBObject().
                                  append(MetaDataDocument.CHECKSUM, checksum).
                                  append(MetaDataDocument.DESCRIPTION, description);
    BasicDBObject update = new BasicDBObject().append("$set", newData);
    metadataCollection.findOneAndUpdate(filter, update);
	}

	@Override
	public boolean upgradeIfNecessary() {
    Document filter = Document.parse("{ version_rank: {$exists: true} }");
    Document renameQuery = Document.parse("{$rename: {version_rank: '" + MetaDataDocument.INSTALLED_RANK + "'}}");
    UpdateResult result = metadataCollection.updateMany(filter, renameQuery);
    return (result.getModifiedCount() > 0);
	}

	private Integer calculateInstalledRank() {
    BasicDBObject descendingRank = new BasicDBObject().append(MetaDataDocument.INSTALLED_RANK, -1);
    BasicDBObject dbo = metadataCollection.find().sort(descendingRank).first();
    if (dbo == null) {
      return 1;
    } else {
      return new MetaDataDocument(dbo).getInstalledRank() + 1;
    }
	}

  /**
   * Checks if the metadata collection exists in MongoDB.
   *
   * @return True if it does, false if not.
   */
	private boolean metadataCollectionExists() {
    return MongoDatabaseUtil.hasCollection(client, mongoDatabase.getName(), collectionName);
	}

  /**
   * Creates the metadata collection.
   *
   * @param dbName  The database where this metadata collection will be created.
   */
  private void createMetadataCollection(String dbName) {
    LOG.info("In MongoDB " + dbName + ", creating Metadata collection: " + collectionName);
    // Database types used in the metadata collection schema.
    BasicDBObject dbInt = new BasicDBObject("$type", "int");
    BasicDBObject dbString = new BasicDBObject("$type", "string");
    BasicDBObject dbBool = new BasicDBObject("$type", "bool");
    BasicDBObject dbDate = new BasicDBObject("$type", "date");
    BasicDBObject dbNull = new BasicDBObject("$type", "null");
    BasicDBObject checksumTypeInt = new BasicDBObject(MetaDataDocument.CHECKSUM, dbInt);
    BasicDBObject checksumTypeNull = new BasicDBObject(MetaDataDocument.CHECKSUM, dbNull);
    BasicDBObject[] checksumType = {checksumTypeInt, checksumTypeNull};
    BasicDBObject versionTypeString = new BasicDBObject(MetaDataDocument.VERSION, dbString);
    BasicDBObject versionTypeNull = new BasicDBObject(MetaDataDocument.VERSION, dbNull);
    BasicDBObject[] versionType = {versionTypeString, versionTypeNull};
    // Database object for setting a valid mongo schema.
    BasicDBObject validSchema = new BasicDBObject().
                                  append(MetaDataDocument.INSTALLED_RANK, dbInt).
                                  append("$or", versionType).
                                  append(MetaDataDocument.DESCRIPTION, dbString).
                                  append(MetaDataDocument.TYPE, dbString).
                                  append(MetaDataDocument.SCRIPT, dbString).
                                  append("$or", checksumType).
                                  append(MetaDataDocument.INSTALLED_BY, dbString).
                                  append(MetaDataDocument.INSTALLED_ON, dbDate).
                                  append(MetaDataDocument.EXECUTION_TIME, dbInt).
                                  append(MetaDataDocument.SUCCESS, dbBool);
    ValidationOptions validator = new ValidationOptions().
                                    validator(validSchema).
                                    validationLevel(ValidationLevel.STRICT);
    CreateCollectionOptions collectionOptions = new CreateCollectionOptions().
                                                  validationOptions(validator);

    Boolean dbExists = MongoDatabaseUtil.exists(client, dbName);
    if (dbExists) {
      LOG.debug("Database " + dbName + " already exists. Skipping database creation.");
    } else {
      LOG.info("Creating Mongo database " + dbName + " ...");
    }
    mongoDatabase.createCollection(collectionName, collectionOptions);
    LOG.debug("Metadata collection " + collectionName + " created.");
    this.metadataCollection = mongoDatabase.getCollection(collectionName, BasicDBObject.class);
    if (!dbExists) addSchemaMarker();
  }

  /**
   * @return The Mongo metadata collection name.
   */
  public String getCollectionName() {
    return collectionName;
	}

}
