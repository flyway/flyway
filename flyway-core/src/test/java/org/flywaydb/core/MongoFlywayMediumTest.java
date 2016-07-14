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
package org.flywaydb.core;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.lang.Thread;
import java.lang.Runnable;
import java.net.ServerSocket;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

@SuppressWarnings({"JavaDoc"})
public class MongoFlywayMediumTest {
	private static final Log LOG = LogFactory.getLog(MongoFlywayMediumTest.class);

	private MongoClient client;
	private final String testDb = "flyway_test";
	private final String dataDir = System.getProperty("user.home") + "/data";
	private List<Process> subProcesses = new ArrayList<Process>();

	private Integer findOpenPort() throws IOException {
		Integer portNumber = null;
    ServerSocket socket = null;

		try {
			socket = new ServerSocket(0);
		} catch (IOException e) {
			return null;
		} finally {
      portNumber = socket.getLocalPort();
			socket.close();
    }

		return portNumber;
  }

	private MongoFlyway build() {
		String mongoPropPrefix = "flyway.mongo.";
		Properties props = new Properties();
		MongoFlyway flyway = new MongoFlyway();

		client = new MongoClient("localhost", 27016);
		props.setProperty(mongoPropPrefix + "locations", "db.migrations.mongo");
		props.setProperty(mongoPropPrefix + "validateOnMigrate", "false");
		flyway.configure(props);
		flyway.setDatabaseName(testDb);
		flyway.setMongoClient(client);

		return flyway;
	}

	@Before
	public void initialize() {
		try {
			subProcesses.add(new ProcessBuilder("mkdir", "-p", dataDir + "/db").start());
			subProcesses.add(new ProcessBuilder("mongod", "--port", "27016", "--dbpath", dataDir + "/db").start());
		} catch (IOException e) {
			LOG.error("Could not initialize MongoDB Flyway testbed: ", e);
		}
	}

	@After
  public void cleanup() {
		MongoDatabase db = client.getDatabase(testDb);
		for (String name : db.listCollectionNames()) db.getCollection(name).drop();
		db.drop();

		try {
		  subProcesses.add(new ProcessBuilder("rm", "-rf", dataDir).start());
		} catch (IOException e) {
			LOG.error("Exiting subprocesses went wrong: ", e);
		} finally {
			Runnable destroyer = new Runnable() {
					@Override
					public void run() {
						for (Process p : subProcesses) p.destroy();
					}
				};
			new Thread(destroyer).start();

			try {				
				for (Process p : subProcesses) p.waitFor();
			} catch (InterruptedException e) {}
		}
	}
	
	@Test
	public void baseline() {
		MongoFlyway flyway = build();

		try {
			flyway.baseline();
		} catch (FlywayException e) {
			fail("Mongo baseline failed:" + e.getLocalizedMessage()); 
			return;
		}

		assertEquals(MigrationType.BASELINE, flyway.info().current().getType());
	}

	@Test
	public void migrate() {
		MongoFlyway flyway = build();

		try {
			flyway.baseline();
			flyway.migrate();
		} catch (FlywayException e) {
			fail("Mongo baseline failed:" + e.getLocalizedMessage()); 
			return;
		}

		MigrationInfo current = flyway.info().current();
		assertEquals(MigrationVersion.fromVersion("1.1"), current.getVersion());
		assertEquals(MigrationType.MONGODB, current.getType());
		assertEquals(MigrationState.SUCCESS, current.getState());
	}

	@Test(expected = FlywayException.class)
	public void validateNotApplied() {
		MongoFlyway flyway = build();

		flyway.validate();
	}

	@Test
	public void validateApplied() {
		MongoFlyway flyway = build();

		try {
			flyway.baseline();
			flyway.migrate();
			flyway.validate();
		} catch (FlywayException e) {
			fail("Validation failed: " + e.getLocalizedMessage());
		}

		assertTrue(true);
	}
}
