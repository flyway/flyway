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
package org.flywaydb.core.internal.resolver.spring;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resolver.AbstractMongoMigrationExecutor;
import org.flywaydb.core.api.migration.spring.SpringMongoMigration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.MongoClient;

/**
 * Adapter for executing migrations implementing SpringMongoMigration.
 */
public class SpringMongoMigrationExecutor extends AbstractMongoMigrationExecutor {

	/**
	 * The name of the database to execute migrations against.
	 */
	private final String databaseName;
	
	/**
	 * The SpringMongoMigration to execute.
	 */
	private final SpringMongoMigration springMongoMigration;

	/**
	 * Creates a new SpringMongoMigrationExecutor.
	 *
	 * @param springMongoMigration The Spring Jdbc Migration to execute.
	 */
	public SpringMongoMigrationExecutor(String databaseName, SpringMongoMigration springMongoMigration) {
		this.databaseName = databaseName;
		this.springMongoMigration = springMongoMigration;
	}

	@Override
	public void execute(MongoClient client) {
		try {
			springMongoMigration.migrate(new MongoTemplate(client, databaseName));
		} catch (Exception e) {
			throw new FlywayException("Spring MongoDB migration failed!", e);
		}
	}

	@Override
	public boolean executeInTransaction() {
		return true;
	}
}
