package org.flywaydb.core.internal.strategy;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BackoffStrategy {

    private int current;
    private final int exponent;
    private final int interval;

    /**
     * @return The current value of the counter and immediately updates it with the next value
     */
    public int next() {
        int temp = current;
        current = Math.min(current * exponent, interval);
        return temp;
    }

    /**
     * @return The current value of the counter without updating it
     */
    public int peek() {
        return current;
    }
}