/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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

import lombok.CustomLog;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@CustomLog
public class MergeUtils {

    public static <T> T merge(T a, T b) {
        return b != null ? b : a;
    }

    public static <E, T extends Collection<E>> T merge(T a, T b) {
        return a == null ? b : (b != null && !b.isEmpty() ? b : a);
    }

    public static <K, V> Map<K, V> merge(Map<K, V> primary, Map<K, V> overrides, BiFunction<V, V, V> mergeFn) {
        if (primary == null) {
            return overrides;
        }

        Map<K, V> result = new HashMap<>(primary);

        if (overrides != null) {
            for (K key : overrides.keySet()) {
                if (primary.containsKey(key)) {
                    V mergedValue = mergeFn.apply(primary.get(key), overrides.get(key));
                    result.replace(key, mergedValue);
                } else {
                    result.put(key, overrides.get(key));
                }
            }
        }

        return result;
    }

    public static <T> void mergeModel(T source, T target) {
        Class<?> clas = source.getClass();
        Field[] fields = clas.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (!Modifier.isFinal(field.getModifiers())) {
                    field.setAccessible(true);
                    Object sourceValue = field.get(source);
                    Object targetValue = field.get(target);
                    Object value = (sourceValue != null) ? sourceValue : targetValue;
                    field.set(target, value);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to get value from field when merging model", e);
        }
    }
}