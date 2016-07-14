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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.configuration.MongoFlywayConfiguration;

import com.mongodb.MongoClient;

public class MongoFlywayConfigurationForTests implements MongoFlywayConfiguration {

	private ClassLoader classLoader;
	private String[] locations = new String[0];

	public MongoFlywayConfigurationForTests(ClassLoader classLoader, String[] locations) {
		this.classLoader = classLoader;
		this.locations = locations;
	}

	public static MongoFlywayConfigurationForTests create() {
		return new MongoFlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), new String[0]);
	}

	public static MongoFlywayConfigurationForTests create(String... locations) {
		return new MongoFlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), locations);
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

	@Override
	public boolean isSkipDefaultResolvers() {
		return false;
	}

	public boolean isSkipDefaultCallbacks() {
		return false;
	}

	@Override
	public MigrationVersion getTarget() {
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
		return null;
	}

	@Override
	public MongoClient getMongoClient() {
		return null;
	}

	@Override
	public MongoFlywayCallback[] getMongoCallbacks() {
		return null;
	}
}
