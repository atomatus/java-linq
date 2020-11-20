package com.atomatus.linq;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

final class IteratorForTake<I> implements Iterator<I> {

    private final CollectionHelper.FunctionGet<Iterator<I>> colFun;
    private final int count;
    private Iterator<I> iterator;
    private int index;

    IteratorForTake(CollectionHelper.FunctionGet<Iterator<I>> colFun, int count) {
        if(count < 0) {
            throw new IndexOutOfBoundsException();
        }

        this.colFun = Objects.requireNonNull(colFun);
        this.count = count;
    }

    private void checkInit(){
        if(iterator == null){
            //creating a new list to avoid concurrence.
            iterator = colFun.get();
        }
    }

    @Override
    public boolean hasNext() {
        this.checkInit();
        return index < count && iterator.hasNext();
    }

    @Override
    public I next() {
        this.checkInit();
        if(index == count){
            throw new NoSuchElementException();
        }
        index++;
        return iterator.next();
    }
}
