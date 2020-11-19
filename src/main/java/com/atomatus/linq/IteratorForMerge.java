package com.atomatus.linq;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

final class IteratorForMerge<I> implements Iterator<I> {

    private int index;
    private Iterator<? extends I> curr;
    private final Iterable<? extends I>[] args;

    IteratorForMerge(Iterable<? extends I>[] args) {
        if(Objects.requireNonNull(args).length == 0) {
            throw new IndexOutOfBoundsException();
        }
        this.args = args;
    }

    IteratorForMerge(Iterable<I> curr, Iterable<? extends I>[] args) {
        this(args);
        this.curr = curr.iterator();
    }

    IteratorForMerge(I[] arr, Iterable<? extends I>[] args) {
        this(args);
        this.curr = new IteratorForSelectArray<>(arr);
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = false;
        if(index < args.length && (curr == null || !(hasNext = curr.hasNext()))) {
            curr = args[index++].iterator();
        }
        return hasNext || (curr != null && curr.hasNext());
    }

    @Override
    public I next() {
        if(hasNext()) {
            return curr.next();
        } else {
            throw new NoSuchElementException();
        }
    }
}
