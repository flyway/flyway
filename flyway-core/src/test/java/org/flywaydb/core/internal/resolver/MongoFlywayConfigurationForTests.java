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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.configuration.MongoFlywayConfiguration;

import com.mongodb.MongoClient;

import java.util.Map;

public class MongoFlywayConfigurationForTests implements MongoFlywayConfiguration {

  private ClassLoader classLoader;
  private String[] locations = new String[0];
  private String encoding;
  private String dbName;
  private String mongoMigrationPrefix;
  private String repeatableMongoMigrationPrefix;
  private String mongoMigrationSeparator;
  private String mongoMigrationSuffix;
  private boolean skipDefaultResolvers;

	public MongoFlywayConfigurationForTests(ClassLoader classLoader, String[] locations, String encoding, String dbName,
                                          String mongoMigrationPrefix, String repeatableMongoMigrationPrefix,
                                          String mongoMigrationSeparator, String mongoMigrationSuffix) {
		this.classLoader = classLoader;
		this.locations = locations;
		this.encoding = encoding;
    this.dbName = dbName;
		this.mongoMigrationPrefix = mongoMigrationPrefix;
    this.repeatableMongoMigrationPrefix = repeatableMongoMigrationPrefix;
		this.mongoMigrationSeparator = mongoMigrationSeparator;
		this.mongoMigrationSuffix = mongoMigrationSuffix;
	}

	public static MongoFlywayConfigurationForTests create() {
		return new MongoFlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(),
      new String[0], "UTF-8", "mongoMigrationTest", "V", "R", "__", ".js");
	}

  public static MongoFlywayConfigurationForTests createWithPrefix(String prefix) {
    return new MongoFlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(),
      new String[0], "UTF-8", "mongoMigrationTest", prefix , "R", "__", ".js");
  }

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
	@Override
	public MigrationVersion getBaselineVersion() {
		return null;
	}

	@Override
	public String getBaselineDescription() {
		return null;
	}

	@Override
	public MigrationResolver[] getResolvers() {
		return null;
	}

  public void setSkipDefaultResolvers(boolean skipDefaultResolvers) {
    this.skipDefaultResolvers = skipDefaultResolvers;
  }

  @Override
  public boolean isSkipDefaultResolvers() {
    return skipDefaultResolvers;
  }

  public boolean isSkipDefaultCallbacks() {
    return false;
  }

  @Override
  public MigrationVersion getTarget() {
    return null;
  }

  @Override
  public String getInstalledBy() {
    return null;
  }

  @Override
  public String getEncoding() {
    return encoding;
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
  public boolean isPlaceholderReplacement() {
    return false;
  }

  @Override
  public String getPlaceholderSuffix() {
    return null;
  }

  @Override
  public String getPlaceholderPrefix() {
    return null;
  }

  @Override
  public Map<String, String> getPlaceholders() {
    return null;
  }

  @Override
  public String getTable() {
    return null;
  }

  @Override
  public String[] getLocations() {
    return this.locations;
  }

  @Override
  public String getDatabaseName() {
    return dbName;
  }

  @Override
  public MongoClient getMongoClient() {
    return null;
  }

  @Override
  public MongoFlywayCallback[] getMongoCallbacks() {
    return null;
  }

  @Override
  public boolean isBaselineOnMigrate() {
    return false;
  }

  @Override
  public boolean isOutOfOrder() {
      return false;
  }

  @Override
  public boolean isIgnoreMissingMigrations() {
  return false;
  }

  @Override
  public boolean isIgnoreFutureMigrations() {
    return false;
  }

  @Override
  public boolean isValidateOnMigrate() {
    return false;
  }

  @Override
  public boolean isCleanOnValidationError() {
    return false;
  }

  @Override
  public boolean isCleanDisabled() {
    return false;
  }

  @Override
  public boolean isAllowMixedMigrations() {
    return false;
  }

  @Override
  public boolean isMixed() {
    return false;
  }

  @Override
  public boolean isGroup() {
    return false;
  }

  public void setRepeatableMongoMigrationPrefix(String repeatablePrefix) {
    this.repeatableMongoMigrationPrefix = repeatablePrefix;
  }

}
