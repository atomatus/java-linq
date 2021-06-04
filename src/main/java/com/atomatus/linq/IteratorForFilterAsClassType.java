package com.atomatus.linq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

final class IteratorForFilterAsClassType<I, E, C, S extends Collection<? extends I>> implements Iterator<E> {

    private final Class<C> classType;
    private final CollectionHelper.FunctionGet<S> getCollection;
    private final CollectionHelper.CompareEntryForClass<I, C> checkEntryValidForClassType;
    private final CollectionHelper.FunctionMount<I, E> mountElementFun;

    private Iterator<I> iterator;

    IteratorForFilterAsClassType(Class<C> classType,
                                 CollectionHelper.FunctionGet<S> getCollection,
                                 CollectionHelper.CompareEntryForClass<I, C> checkEntryValidForClassType,
                                 CollectionHelper.FunctionMount<I, E> mountElementFun){
        this.classType = Objects.requireNonNull(classType);
        this.getCollection = Objects.requireNonNull(getCollection);
        this.checkEntryValidForClassType = checkEntryValidForClassType;
        this.mountElementFun = mountElementFun;
    }

    private void checkInitList(){
        if(iterator == null){
            synchronized (classType) {
                //creating a new list to avoid concurrence.
                S set = getCollection.get();
                Objects.requireNonNull(set);
                iterator = new ArrayList<>(set).iterator();
            }
        }
    }

    private I getNext() {
        if(iterator == null || !iterator.hasNext()){
            return null;
        }

        I entry = iterator.next();
        return ((checkEntryValidForClassType != null && checkEntryValidForClassType.isValidFor(entry, classType)) ||
                classType.isInstance(entry)) ? entry : this.getNext();
    }

    @Override
    public boolean hasNext() {
        this.checkInitList();
        return iterator.hasNext();
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
        this.checkInitList();
        I next = getNext();
        return next == null ? null :
                mountElementFun != null ?
                        mountElementFun.mount(next) :
                        (E) next;
    }
}
