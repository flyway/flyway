/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.ant;

/**
 * Nested &lt;placeholder&gt; element for the flyway:migrate Ant task.
 */
public class PlaceholderElement {
    /**
     * The name of the placeholder.
     */
    private String name;

    /**
     * The value of the placeholder.
     */
    private String value;

    /**
     * @return The name of the placeholder.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name of the placeholder.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The value of the placeholder.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The value of the placeholder.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
