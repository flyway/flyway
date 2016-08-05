/**
 * Copyright 2010-2016 Boxfuse GmbH
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
 * Created on 04/08/16.
 *
 * @author Reda.Housni-Alaoui
 */
public class Triplet<L, C, R> {

    private Pair<L, R> pair;

    private C center;

    public static <L,C,R> Triplet<L, C, R> of(L left, C center, R right){
        Triplet<L, C, R> triplet = new Triplet<L, C, R>();
        triplet.pair = Pair.of(left, right);
        triplet.center = center;
        return triplet;
    }

    /**
     * @return The left side of the pair.
     */
    public L getLeft() {
        return pair.getLeft();
    }

    public C getCenter(){
        return center;
    }

    /**
     * @return The right side of the pair.
     */
    public R getRight() {
        return pair.getRight();
    }

}
