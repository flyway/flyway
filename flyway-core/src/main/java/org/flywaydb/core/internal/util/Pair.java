package org.flywaydb.core.internal.util;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@RequiredArgsConstructor(staticName = "of")
@Getter
public class Pair<L, R> implements Comparable<Pair<L, R>> {
    private final L left;
    private final R right;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return left.equals(pair.left) && right.equals(pair.right);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] {left, right});
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