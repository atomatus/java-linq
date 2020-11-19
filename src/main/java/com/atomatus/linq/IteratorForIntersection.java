package com.atomatus.linq;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

final class IteratorForIntersection<I> implements Iterator<I> {

    private int index;
    private final Iterable<I>[] args;
    private Iterator<I> curr;
    private I next;

    IteratorForIntersection(Iterable<I>[] args) {
        if(Objects.requireNonNull(args).length == 0) {
            throw new IndexOutOfBoundsException();
        }
        this.args = args;
    }

    @Override
    public boolean hasNext() {
        if(next != null){
            return true;
        } else if(index < args.length) {
            if(curr == null) {
                curr = args[index++].iterator();
                if(!curr.hasNext()){
                    curr = null;
                    return hasNext();
                }
            }

            if(curr.hasNext()) {
                I next = curr.next();
                if(next == null){
                    return hasNext();
                }

                for(int i=index, l = args.length; i < l; i++) {
                    for (I n : args[i]) {
                        if (next == n || next.equals(n)) {
                            this.next = next;
                            return true;
                        }
                    }
                }
                return hasNext();
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public I next() {
        if(hasNext()) {
            I n = next;
            next = null;
            return n;
        } else {
            throw new NoSuchElementException();
        }
    }
}
