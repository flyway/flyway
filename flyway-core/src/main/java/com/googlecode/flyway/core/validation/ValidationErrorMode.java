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
package com.googlecode.flyway.core.validation;

/**
 * Mode for handling validation errors.
 */
public enum ValidationErrorMode {
    /**
     * Throw an exception and fail.
     */
    FAIL,

    /**
     * Cleans the database.<br/>
     * <br/>
     * <p>
     * This is exclusively intended as a convenience for development. Even tough we strongly recommend not to change
     * migration scripts once they have been checked into SCM and run, this provides a way of dealing with this case in
     * a smooth manner. The database will be wiped clean automatically, ensuring that the next migration will bring you
     * back to the state checked into SCM.
     * </p>
     * <br/>
     * <b>Warning ! Do not use in produktion !</b>
     */
    CLEAN
}
