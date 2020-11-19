package com.atomatus.linq;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

final class IteratorForIntersectionArray<I> implements Iterator<I> {

    private int index, cIndex;
    private final I[][] args;
    private I next;

    IteratorForIntersectionArray(I[][] args) {
        if (Objects.requireNonNull(args).length == 0) {
            throw new IndexOutOfBoundsException();
        }
        this.args = args;
    }

    private boolean exists(I cand, I[][] args, int index, int length) {
        if (index == length) {
            return true;
        }

        I[] arr = args[index];
        for (I i : arr) {
            if (i != null && (i == cand || i.equals(cand))) {
                return exists(cand, args, index + 1, length);
            }
        }

        return false;
    }

    @Override
    public boolean hasNext() {
        if (next != null) {
            return true;
        } else if (index < args.length) {
            I[] curr = args[index];
            int len = curr.length;

            if (len == 0) {
                index++;
                return hasNext();
            } else if (cIndex < len) {
                I cand = curr[cIndex++];
                if (exists(cand, args, index + 1, args.length)) {
                    next = cand;
                    return true;
                } else {
                    return hasNext();
                }
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public I next() {
        if (hasNext()) {
            I n = next;
            next = null;
            return n;
        } else {
            throw new NoSuchElementException();
        }
    }
}
