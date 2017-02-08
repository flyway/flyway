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
package org.flywaydb.core.internal.util;

/**
 * A simple pair of values.
 */
public class Pair<L, R> {
    /**
     * The left side of the pair.
     */
    private L left;

    /**
     * The right side of the pair.
     */
    private R right;

    /**
     * Creates a new pair of these values.
     * @param left The left side of the pair.
     * @param right The right side of the pair.
     * @return The pair.
     */
    public static <L,R> Pair<L, R> of(L left, R right) {
        Pair<L, R> pair = new Pair<L, R>();
        pair.left = left;
        pair.right = right;
        return pair;
    }

    /**
     * @return The left side of the pair.
     */
    public L getLeft() {
        return left;
    }

    /**
     * @return The right side of the pair.
     */
    public R getRight() {
        return right;
    }
}
