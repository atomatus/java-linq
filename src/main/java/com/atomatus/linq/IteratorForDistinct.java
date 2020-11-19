package com.atomatus.linq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

final class IteratorForDistinct<IN, OUT> implements Iterator<OUT> {

    private final CollectionHelper.FunctionGet<Iterator<IN>> iteratorFun;
    private final CollectionHelper.FunctionMount<IN, OUT> mount;
    private Iterator<IN> iterator;
    private Iterator<OUT> distinct;

    IteratorForDistinct(CollectionHelper.FunctionGet<Iterator<IN>> iteratorFun, CollectionHelper.FunctionMount<IN, OUT> mount){
        this.iteratorFun = Objects.requireNonNull(iteratorFun);
        this.mount = Objects.requireNonNull(mount);
    }

    @SuppressWarnings({"unchecked", "rawtypes", "DuplicatedCode"})
    private void checkInit(){
        if(iterator == null){
            iterator = Objects.requireNonNull(iteratorFun.get());
            List<OUT> acc = new ArrayList<>();
            while(iterator.hasNext()) {
                IN curr = iterator.next();
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
