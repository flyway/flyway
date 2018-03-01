/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.api.callback;

/**
 * This is the main callback interface that should be implemented to handle Flyway lifecycle events.
 */
public interface Callback {
    /**
     * Whether this callback supports this event or not. This is primarily meant as a way to optimize event handling
     * by avoiding unnecessary connection state setups for events that will not be handled anyway.
     *
     * @param event   The event to check.
     * @param context The context for this event.
     * @return {@code true} if it can be handled, {@code false} if not.
     */
    boolean supports(Event event, Context context);

    /**
     * Whether this event can be handled in a transaction or whether it must be handled outside a transaction instead.
     * In the vast majority of the cases the answer will be
     * {@code true}. Only in the rare cases where non-transactional statements are executed should this return {@code false}.
     * This method is called before {@link #handle(Event, Context)} in order to determine in advance whether a transaction
     * can be used or not.
     *
     * @param event   The event to check.
     * @param context The context for this event.
     * @return {@code true} if it can be handled within a transaction (almost all cases). {@code false} if it must be
     * handled outside a transaction instead (very rare).
     */
    boolean canHandleInTransaction(Event event, Context context);

    /**
     * Handles this Flyway lifecycle event.
     *
     * @param event   The event to handle.
     * @param context The context for this event.
     */
    void handle(Event event, Context context);
}