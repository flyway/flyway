/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.gradle.task;

import org.flywaydb.core.internal.configuration.ConfigUtils;

import java.util.Map;

public class GcsmConfiguration {
    public static final String GCSM_PROJECT = "flyway.gcsm.project";
    public static final String GCSM_SECRETS = "flyway.gcsm.secrets";

    private String gcsmProject;
    private String[] gcsmSecrets;

    public void extract(Map<String, String> conf) {
        ConfigUtils.putIfSet(conf, GCSM_PROJECT, gcsmProject);
        ConfigUtils.putArrayIfSet(conf, GCSM_SECRETS, gcsmSecrets);
    }

    public String getGcsmProject() {
        return gcsmProject;
    }

    public void setGcsmProject(String gcsmProject) {
        this.gcsmProject = gcsmProject;
    }

    public String[] getGcsmSecrets() {
        return gcsmSecrets;
    }

    public void setGcsmSecrets(String[] gcsmSecrets) {
        this.gcsmSecrets = gcsmSecrets;
    }
}