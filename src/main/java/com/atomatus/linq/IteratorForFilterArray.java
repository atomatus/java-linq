package com.atomatus.linq;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

final class IteratorForFilterArray<I> implements Iterator<I> {

    private final I[] arr;
    private final CollectionHelper.CompareEntryValid<I> where;

    private I data;
    private int index;

    IteratorForFilterArray(I[] arr, CollectionHelper.CompareEntryValid<I> where){
        this.arr = Objects.requireNonNull(arr);
        this.where = Objects.requireNonNull(where);
    }

    private boolean pushIf(I i){
        boolean isValid = where.isValid(i);
        if(isValid){
            data = i;
        }
        return isValid;
    }

    private I pop(){
        I aux = data;
        data = null;
        return aux;
    }

    private boolean hasData(){
        return data != null;
    }

    @Override
    public boolean hasNext() {
        return hasData() ||
                (index < arr.length &&
                        (pushIf(arr[index++]) || hasNext()));
    }

    @Override
    public I next() {
        if(hasData() || hasNext()) {
            return pop();
        } else {
            throw new NoSuchElementException();
        }
    }
}
