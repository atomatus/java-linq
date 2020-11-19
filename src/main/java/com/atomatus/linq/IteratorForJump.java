package com.atomatus.linq;

import java.util.Iterator;
import java.util.Objects;

final class IteratorForJump<I> implements Iterator<I> {

    private final CollectionHelper.FunctionGet<Iterator<I>> colFun;
    private final int count;
    private Iterator<I> iterator;
    private int index;

    IteratorForJump(CollectionHelper.FunctionGet<Iterator<I>> colFun, int count) {
        if(count < 0) {
            throw new IndexOutOfBoundsException();
        }

        this.colFun = Objects.requireNonNull(colFun);
        this.count = count;
    }

    private void checkInit(){
        if(iterator == null) {
            //creating a new list to avoid concorrence.
            iterator = colFun.get();
            while(iterator.hasNext() && index < count){
                iterator.next();
                index++;
            }
        }
    }

    @Override
    public boolean hasNext() {
        this.checkInit();
        return iterator.hasNext();
    }

    @Override
    public I next() {
        this.checkInit();
        return iterator.next();
    }
}
