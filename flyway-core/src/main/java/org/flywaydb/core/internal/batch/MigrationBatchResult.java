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
package org.flywaydb.core.internal.batch;

/**
 * Created on 07/08/16.
 *
 * @author Reda.Housni-Alaoui
 */
public class MigrationBatchResult {

    private int numberOfAppliedMigrations;
    private boolean done;

    public int getNumberOfAppliedMigrations() {
        return numberOfAppliedMigrations;
    }

    public void setNumberOfAppliedMigrations(int numberOfAppliedMigrations) {
        this.numberOfAppliedMigrations = numberOfAppliedMigrations;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
