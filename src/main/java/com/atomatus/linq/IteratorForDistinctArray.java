package com.atomatus.linq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

final class IteratorForDistinctArray<IN, OUT> implements Iterator<OUT> {

    private final IN[] array;
    private final CollectionHelper.FunctionMount<IN, OUT> mount;
    private Iterator<IN> iterator;
    private Iterator<OUT> distinct;

    IteratorForDistinctArray(IN[] array, CollectionHelper.FunctionMount<IN, OUT> mount){
        this.array = Objects.requireNonNull(array);
        this.mount = Objects.requireNonNull(mount);
    }

    @SuppressWarnings({"unchecked", "DuplicatedCode", "rawtypes"})
    private void checkInit(){
        if(distinct == null){
            List<OUT> acc = new ArrayList<>();
            for(IN curr : array) {
                OUT out = mount.mount(curr);
                if(out instanceof Comparable<?>) {
                    Comparable comp = (Comparable<?>) out;
                    if(CollectionHelper.all(acc, a -> comp.compareTo(a) != 0)) {
                        acc.add(mount.mount(curr));
                    }
                } else if(!acc.contains(out)) {
                    acc.add(mount.mount(curr));
                }
            }
            distinct = acc.iterator();
        }
    }

    @Override
    public boolean hasNext() {
        checkInit();
        return distinct.hasNext();
    }

    @Override
    public OUT next() {
        checkInit();
        return distinct.next();
    }
}
