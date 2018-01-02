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
package org.flywaydb.core.internal.util;

import java.util.Arrays;

/**
 * A simple pair of values.
 */
public class Pair<L, R> implements Comparable<Pair<L, R>> {
    /**
     * The left side of the pair.
     */
    private final L left;

    /**
     * The right side of the pair.
     */
    private final R right;

    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Creates a new pair of these values.
     *
     * @param left  The left side of the pair.
     * @param right The right side of the pair.
     * @return The pair.
     */
    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<L, R>(left, right);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return left.equals(pair.left) && right.equals(pair.right);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{left, right});
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(Pair<L, R> o) {
        if (left instanceof Comparable<?>) {
            int l = ((Comparable<L>) left).compareTo(o.left);
            if (l != 0) {
                return l;
            }
        }
        if (right instanceof Comparable<?>) {
            int r = ((Comparable<R>) right).compareTo(o.right);
            if (r != 0) {
                return r;
            }
        }
        return 0;
    }
}