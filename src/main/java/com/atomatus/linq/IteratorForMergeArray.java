package com.atomatus.linq;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

final class IteratorForMergeArray<I> implements Iterator<I>, CollectionHelper.FunctionMount<I, I> {

    private int index;
    private Iterator<I> curr;
    private final I[][] args;

    IteratorForMergeArray(I[][] args) {
        if(Objects.requireNonNull(args).length == 0) {
            throw new IndexOutOfBoundsException();
        }
        this.args = args;
    }

    IteratorForMergeArray(Iterable<I> iterable, I[][] args) {
        this(args);
        this.curr = Objects.requireNonNull(iterable).iterator();
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = false;
        if(index < args.length && (curr == null || !(hasNext = curr.hasNext()))) {
            curr = new IteratorForSelectArray<>(args[index++], this);
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

    @Override
    public I mount(I i) {
        return i;
    }
}
