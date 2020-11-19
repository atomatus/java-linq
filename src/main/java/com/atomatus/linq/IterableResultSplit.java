package com.atomatus.linq;

import java.util.*;

final class IterableResultSplit extends IterableResult<String> implements Iterator<String> {

    private String str, next;
    private int index;
    private boolean done;
    private final int length;
    private final char splitter;
    private final List<String> result;

    public IterableResultSplit(String str, char splitter) {
        this.str        = Objects.requireNonNull(str);
        this.length     = str.length();
        this.splitter   = splitter;
        this.result     = new ArrayList<>();
    }

    private boolean checkNext() {
        boolean noNext;
        if ((noNext = next == null) && !done) {
            for(int i=index;;){
                if(i == length) {
                    next = str.substring(index, index = i);
                    return done = result.add(next);
                }
                else if(str.charAt(i++) == splitter) {
                    next = str.substring(index, (index = i) - 1);
                    return result.add(next);
                }
            }
        }
        return !noNext;
    }

    @Override
    public boolean hasNext() {
        synchronized (this) {
            return checkNext();
        }
    }

    @Override
    public String next() {
        synchronized (this) {
            if (checkNext()) {
                String aux = next;
                next = null;
                return aux;
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    @Override
    public Iterator<String> iterator() {
        synchronized (this) {
            if(done) {
                str = next = null;
                return result.iterator();
            } else {
                index = 0;
                result.clear();
                return this;
            }
        }
    }

    @Override
    public int count() {
        synchronized (this) {
            if (done) {
                return result.size();
            } else {
                return super.count();
            }
        }
    }
}
