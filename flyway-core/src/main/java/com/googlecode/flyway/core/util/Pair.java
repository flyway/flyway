package com.googlecode.flyway.core.util;

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
