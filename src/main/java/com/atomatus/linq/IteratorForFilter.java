package com.atomatus.linq;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

final class IteratorForFilter<I> implements Iterator<I> {

    private final CollectionHelper.FunctionGet<Iterator<I>> iteratorFun;
    private final CollectionHelper.CompareEntryValid<I> where;

    private Iterator<I> iterator;
    private I data;

    IteratorForFilter(CollectionHelper.FunctionGet<Iterator<I>> iteratorFun,
                             CollectionHelper.CompareEntryValid<I> where) {
        this.iteratorFun = Objects.requireNonNull(iteratorFun);
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

    private void checkInit() {
        if(iterator == null){
            iterator = iteratorFun.get();
        }
    }

    @Override
    public boolean hasNext() {
        checkInit();
        return hasData() ||
                (iterator.hasNext() &&
                        (pushIf(iterator.next()) || hasNext()));
    }

    @Override
    public I next() {
        checkInit();
        if(hasData() || hasNext()) {
            return pop();
        } else {
            throw new NoSuchElementException();
        }
    }
}
