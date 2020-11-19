package com.atomatus.linq;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

final class IteratorForSelectArray<IN, OUT> implements Iterator<OUT> {

    private final class DefaultFunctionMount implements CollectionHelper.FunctionMount<IN, OUT> {
        @Override
        @SuppressWarnings("unchecked")
        public OUT mount(IN in) {
            return (OUT) in;
        }
    }

    private final IN[] arr;
    private final CollectionHelper.FunctionMount<IN, OUT> mount;

    private OUT next;
    private int index;

    IteratorForSelectArray(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> mount) {
        this.arr = Objects.requireNonNull(arr);
        this.mount = Objects.requireNonNull(mount);
    }

    IteratorForSelectArray(IN[] arr){
        this.arr = arr;
        this.mount = new DefaultFunctionMount();
    }

    @Override
    public boolean hasNext() {
        return index < arr.length;
    }

    @Override
    public OUT next() {
        if(index == arr.length){
            throw new NoSuchElementException();
        }
        return mount.mount(arr[index++]);
    }
}
