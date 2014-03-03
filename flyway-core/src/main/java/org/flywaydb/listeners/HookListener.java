/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.listeners;

/**
 * This is the observable listener that will fire as a hook on the
 * migration lifecycle.  By implementing this interface you can
 * perform actions before or after a migration is run.
 * 
 * @author Dan Bunker
 */
public interface HookListener {
	/**
	 * This is the main method that will run when the hook fires.
	 * Add you custom action code here.
	 */
	public void run();
}
