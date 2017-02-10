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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;

/**
 * For instantiating Embedded MongoDB. MongoDB related tests can extend this class to get
 * access to in-memory MondoDB instance.
 */
public class EmbeddedMongoDb {

    private static final MongodStarter mongodStarter = MongodStarter.getDefaultInstance();
    private static String databaseName = "mongoFlywayTest";
    private int mongodPort;
    private String mongoUri;
    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private MongoClient mongoClient;

    @Before
    public void initialize() throws IOException {
        mongodPort = Network.getFreeServerPort();
        mongoUri = "mongodb://localhost:" + mongodPort + "/" + databaseName;
        mongodExe = mongodStarter.prepare(new MongodConfigBuilder()
                .version(Version.Main.V3_2)
                .net(new Net(mongodPort, Network.localhostIsIPv6()))
                .build());
        mongod = mongodExe.start();
        mongoClient = new MongoClient(new MongoClientURI(mongoUri));
    }

    @After
    public void tearDown() {
        mongod.stop();
        mongodExe.stop();
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public String getMongoUri() {
        return mongoUri;
    }

    public String getDatabaseName() {
        return databaseName;
    }

}
