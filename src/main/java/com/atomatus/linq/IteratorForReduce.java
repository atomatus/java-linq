package com.atomatus.linq;

import java.util.Iterator;
import java.util.Objects;

final class IteratorForReduce {

    @SuppressWarnings({"unchecked", "CatchMayIgnoreException"})
    static <IN, OUT> OUT reduce(Iterator<IN> iterator,
                                CollectionHelper.FunctionReduce<IN, OUT> reduceFun,
                                OUT acc) {
        Objects.requireNonNull(iterator);
        Objects.requireNonNull(reduceFun);
        boolean first = acc == null;
        while (iterator.hasNext()) {
            IN next = iterator.next();
            if(first) {
                first = false;
                try {
                    acc = (OUT) next;
                    continue;
                }catch (ClassCastException ex) { }
            }
            acc = reduceFun.reduce(acc, next);
        }
        return acc;
    }

    static <IN, OUT> OUT reduce(Iterable<IN> iterable,
                                CollectionHelper.FunctionReduce<IN, OUT> reduceFun,
                                OUT acc) {
        return reduce(Objects.requireNonNull(iterable).iterator(), reduceFun, acc);
    }

    @SuppressWarnings({"unchecked"})
    static <IN, OUT> OUT reduce(IN[] arr, CollectionHelper.FunctionReduce<IN, OUT> reduceFun, OUT acc, int offset) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(reduceFun);

        for (int i=offset, l=arr.length; i < l; i++) {
            if(i == 0 && acc == null) {
                try {
                    acc = (OUT) arr[i];
                    continue;
                }catch (ClassCastException ignored) { }
            }
            acc = reduceFun.reduce(acc, arr[i]);
        }
        return acc;
    }

    static <IN, OUT> OUT reduce(IN[] arr, CollectionHelper.FunctionReduce<IN, OUT> reduceFun, OUT acc) {
        return reduce(arr, reduceFun, acc, 0);
    }

    @SuppressWarnings("unchecked")
    static <IN, OUT> OUT reduce(IN[] arr, CollectionHelper.FunctionReduce<IN, OUT> reduceFun) {
        Objects.requireNonNull(arr);
        return arr.length == 0 ? null : reduce(arr, reduceFun, (OUT) arr[0], 1);
    }
}
