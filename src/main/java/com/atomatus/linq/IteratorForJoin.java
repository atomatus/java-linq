package com.atomatus.linq;

import java.util.Iterator;
import java.util.Objects;

final class IteratorForJoin {

    private static Object getValue(String prefix, String suffix, String separator, Object e) {
        if (e == null) {
            return "null";
        } else {
            Class<?> clazz = e.getClass();
            if (clazz.isArray()) {
                if (clazz == Object[].class) {
                    return join(prefix, suffix, separator, (Object[]) e);
                } else {
                    return join(prefix, suffix, separator, CollectionHelper.toArray(e));
                }
            } else if (Iterable.class.isAssignableFrom(clazz)) {
                return join(prefix, suffix, separator, (Iterable<?>) e);
            } else if (Iterator.class.isAssignableFrom(clazz)) {
                return join(prefix, suffix, separator, (Iterator<?>) e);
            } else {
                return e;
            }
        }
    }

    private static Object getValue(String separator, Object e) {
        return getValue(null, null, separator, e);
    }

    static String join(String prefix, String suffix, String separator, Iterator<?> it) {
        if (!Objects.requireNonNull(it).hasNext()) {
            return "";
        }

        //noinspection DuplicatedCode
        prefix = prefix == null ? "" : prefix;
        suffix = suffix == null ? "" : suffix;
        separator = separator == null ? "" : separator;

        StringBuilder sb = new StringBuilder();
        sb.append(prefix);

        boolean hasNext;
        do {
            Object e = it.next();
            sb.append(getValue(separator, e));
            if (hasNext = it.hasNext()) {
                sb.append(separator);
            }
        } while (hasNext);

        return sb
                .append(suffix)
                .toString();
    }

    static String join(String prefix, String suffix, String separator, Iterable<?> it) {
        Objects.requireNonNull(it);
        return join(prefix, suffix, separator, it.iterator());
    }

    static String join(String prefix, String suffix, String separator, Object... arr) {
        if (Objects.requireNonNull(arr).length == 0) {
            return "";
        }

        //noinspection DuplicatedCode
        prefix = prefix == null ? "" : prefix;
        suffix = suffix == null ? "" : suffix;
        separator = separator == null ? "" : separator;

        StringBuilder sb = new StringBuilder();
        sb.append(prefix);

        for (int i = 0, l = arr.length, j = l - 1; i < l; i++) {
            sb.append(getValue(separator, arr[i]));
            if (i < j) {
                sb.append(separator);
            }
        }

        return sb
                .append(suffix)
                .toString();
    }

    static String toString(Object e) {
        return getValue("[", "]", ", ", e).toString();
    }
}
