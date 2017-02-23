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

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RunWith(Parameterized.class)
public class FlywayPerformanceMediumTest {
    private static final String DIR = "target/performance/" + System.currentTimeMillis();

    @Parameterized.Parameter
    public int scriptCount;

    @Parameterized.Parameters(name = "scriptCount={0}")
    public static Iterable<?> data() {
        return Arrays.asList(1000);
    }

    @Before
    public void generateLotsOfInstallerScripts() throws IOException {
        //noinspection ResultOfMethodCallIgnored
        new File(DIR).mkdirs();
        Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        for (int i = (scriptCount - 500); i < scriptCount; i++) {
            final int j = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        IOUtils.write("SELECT " + j + " FROM DUAL", new FileOutputStream(DIR + "/V" + j + "__Test.sql"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Test
    public void testPerformance() throws IOException, SQLException {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_db_perf;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("filesystem:" + DIR);
        flyway.migrate();
    }
}
