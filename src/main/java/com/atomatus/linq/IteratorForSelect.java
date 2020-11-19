package com.atomatus.linq;

import java.util.Iterator;
import java.util.Objects;

final class IteratorForSelect<IN, OUT> implements Iterator<OUT> {

    private final CollectionHelper.FunctionGet<Iterator<IN>> iteratorFun;
    private final CollectionHelper.FunctionMount<IN, OUT> mount;
    private Iterator<IN> iterator;

    IteratorForSelect(CollectionHelper.FunctionGet<Iterator<IN>> iteratorFun, CollectionHelper.FunctionMount<IN, OUT> mount){
        this.iteratorFun = Objects.requireNonNull(iteratorFun);
        this.mount = Objects.requireNonNull(mount);
    }

    private void checkInit(){
        if(iterator == null){
            iterator = iteratorFun.get();
        }
    }

    @Override
    public boolean hasNext() {
        checkInit();
        return iterator.hasNext();
    }

    @Override
    public OUT next() {
        checkInit();
        return mount.mount(iterator.next());
    }
}
