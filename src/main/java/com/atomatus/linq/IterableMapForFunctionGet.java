package com.atomatus.linq;

import java.util.*;

final class IterableMapForFunctionGet<K, V> implements IterableMap<K, IterableResult<V>> {

    private static class IEntry<J, E> {

        private final J key;
        private final E value;
        private final IterableResult<E> iterableResult;
        private final boolean found;

        private IEntry(J key, E value, IterableResult<E> iterableResult, boolean found) {
            this.key = key;
            this.value = value;
            this.iterableResult = iterableResult;
            this.found = found;
        }

        public IEntry(J key, E value, IterableResult<E> iterableResult) {
            this(key, value, iterableResult, true);
        }

        public IEntry(Entry<J, IterableResult<E>> entry) {
            this(entry.getKey(), null, entry.getValue());
        }

        public Entry<J, IterableResult<E>> toMapEntry(){
            return new Entry<J, IterableResult<E>>(){

                @Override
                public J getKey() {
                    return key;
                }

                @Override
                public IterableResult<E> getValue() {
                    return iterableResult;
                }

                @Override
                public IterableResult<E> setValue(IterableResult<E> value) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public J getKey() {
            return key;
        }

        public E getValue() {
            return value;
        }

        public boolean isFound() {
            return found;
        }

        public IterableResult<E> getIterableResult() {
            return iterableResult;
        }

        static <S, I> IEntry<S, I> notFound(){
            return new IEntry<>(null, null, null, false);
        }
    }

    private static class IterableResultInternal<I> extends IterableResult<I>{

        private final List<I> list;

        {
            list = new ArrayList<>();
        }

        int size(){
            return list.size();
        }

        boolean add(I v){
            return list.add(v);
        }

        @Override
        public Iterator<I> iterator() {
            return list.iterator();
        }

        @Override
        public String toString() {
            return IteratorForJoin.toString(list);
        }
    }

    private final class LocalIterator<I> implements Iterator<I>, CollectionHelper.CompareEntryValid<IEntry<K, V>> {

        private final CollectionHelper.FunctionMount<IEntry<K, V>, I> mountFun;
        private final Iterator<Entry<K, IterableResult<V>>> iterator;

        private List<I> readed;
        private IEntry<K, V> e;

        LocalIterator( CollectionHelper.FunctionMount<IEntry<K, V>, I> mountFun) {
            this.mountFun   = mountFun;
            this.iterator   = map == null ? null : map.entrySet().iterator();
            this.readed     = new ArrayList<>();
        }

        private boolean checkNext(){
            synchronized (locker) {
                e = e != null ? e : getNextValidEntry(iterator, this);
                boolean hasNext = e.found;

                if(!hasNext && readed != null){
                    readed.clear();
                    readed = null;
                }

                return hasNext;
            }
        }

        @Override
        public boolean hasNext() {
            return checkNext();
        }

        @Override
        public I next() {
            if((e != null && !e.found) || (e == null && !checkNext())){
                throw new NoSuchElementException();
            }

            I i = mountFun.mount(e);
            e = null;
            readed.add(i);
            return i;
        }

        @Override
        public boolean isValid(IEntry<K, V> e) {
            return !readed.contains(mountFun.mount(e));
        }
    }

    static final int NO_LIMIT;

    private transient Map<K, IterableResult<V>> map;
    private final Object locker;

    private final CollectionHelper.FunctionGet<Iterator<V>> iteratorFun;
    private final CollectionHelper.FunctionMount<V, K> groupFun;
    private final CollectionHelper.CompareEntryValid<Entry<K, IterableResult<V>>> filter;
    private final CollectionHelper.FunctionGet<Iterable<Entry<K, IterableResult<V>>>> otherGroupEntries;
    private final boolean isIteratorGroup;
    private final int limitCount;

    private Iterator<V> iterator;
    private Iterator<Entry<K, IterableResult<V>>> iteratorOtherGroup;
    private boolean hasNext;

    static {
        NO_LIMIT = -1;
    }

    IterableMapForFunctionGet(CollectionHelper.FunctionGet<Iterator<V>> iteratorFun,
                              CollectionHelper.FunctionMount<V, K> groupFun,
                              CollectionHelper.CompareEntryValid<Entry<K, IterableResult<V>>> filter,
                              CollectionHelper.FunctionGet<Iterable<Entry<K, IterableResult<V>>>> otherGroupEntries,
                              int limitCount){
        this.iteratorFun        = iteratorFun;
        this.groupFun           = groupFun;
        this.filter             = filter;
        this.otherGroupEntries  = otherGroupEntries;
        this.limitCount         = limitCount;
        this.isIteratorGroup    = iteratorFun != null && groupFun != null;
        this.hasNext            = true;
        this.locker             = new Object();
    }

    //region getEntry and getFullMap
    private IEntry<K, V> getEntryForIteratorGroup(boolean findAll,
                                                  IEntry<K, V> found,
                                                  Map<K, IterableResult<V>> map,
                                                  CollectionHelper.CompareEntryValid<IEntry<K, V>> checkFun){
        if(!hasNext){
            return found;
        } else if(iterator == null) {
            iterator = iteratorFun.get();
        }

        V v;
        K k;
        IterableResult<V> iri;
        IEntry<K, V> e;
        while (hasNext = iterator.hasNext()) {
            v   = iterator.next();
            k   = groupFun.mount(v);
            iri = map.get(k);
            if (iri == null) {
                iri = new IterableResultInternal<>();
                map.put(k, iri);
            } else if(limitCount != NO_LIMIT && ((IterableResultInternal<V>)iri).size() == limitCount) {
                continue;
            }

            if(((IterableResultInternal<V>) iri).add(v)) {
                e = new IEntry<>(k, v, iri);
                if (checkFun.isValid(e)) {
                    found = e;
                    if (!findAll) {
                        return found;
                    }
                }
            }
        }

        iterator = null;
        return found;
    }

    private IEntry<K, V> getEntryForOtherGroupFiltered(boolean findAll,
                                                       IEntry<K, V> found,
                                                       Map<K, IterableResult<V>> map,
                                                       CollectionHelper.CompareEntryValid<IEntry<K, V>> checkFun){
        if(!hasNext){
            return found;
        } else if(iteratorOtherGroup == null) {
            Iterable<Entry<K, IterableResult<V>>> other = otherGroupEntries.get();
            iteratorOtherGroup = other.iterator();
        }

        IEntry<K, V> e;
        Entry<K, IterableResult<V>> entry;
        while (hasNext = iteratorOtherGroup.hasNext()) {
            entry = iteratorOtherGroup.next();
            if(filter == null || filter.isValid(entry)) {
                e = new IEntry<>(entry);
                map.put(e.key, limitCount != NO_LIMIT ? e.iterableResult.take(limitCount) : e.iterableResult);
                if(checkFun.isValid(e)) {
                    found = e;
                    if(!findAll){
                        return found;
                    }
                }
            }
        }

        return found;
    }

    private IEntry<K, V> getEntryForIteratorOrOtherGroupFiltered(boolean findAll,
                                                                 IEntry<K, V> found,
                                                                 Map<K, IterableResult<V>> map,
                                                                 CollectionHelper.CompareEntryValid<IEntry<K, V>> checkFun) {
        if(isIteratorGroup) {
            return getEntryForIteratorGroup(findAll, found, map, checkFun);
        } else {
            return getEntryForOtherGroupFiltered(findAll, found, map, checkFun);
        }
    }

    private IEntry<K, V> getEntry(boolean findAll, boolean readMap, CollectionHelper.CompareEntryValid<IEntry<K, V>> checkFun) {
        synchronized (locker) {
            //region find on current map
            IEntry<K, V> found = IEntry.notFound();
            if(map == null){
                map = new HashMap<>(); //first getEntry request.
            } else if(readMap) {
                IEntry<K, V> e;
                for (Entry<K, IterableResult<V>> entry : map.entrySet()) {
                    e = new IEntry<>(entry);
                    if (checkFun.isValid(e)) {
                        found = e;
                        if (!findAll) {
                            return found;
                        } else {
                            break;//continue to find all on iterator or from otherGroup filtered.
                        }
                    }
                }
            }
            //endregion

            //region find on iterator or another group filtered
            return getEntryForIteratorOrOtherGroupFiltered(findAll, found, map, checkFun);
            //endregion
        }
    }

    private IEntry<K, V> getEntry(CollectionHelper.CompareEntryValid<IEntry<K, V>> checkFun){
        return getEntry(false, true, checkFun);
    }

    private IEntry<K, V> getNextValidEntry(Iterator<Entry<K, IterableResult<V>>> iterator,
                                           CollectionHelper.CompareEntryValid<IEntry<K, V>> checkFun){
        synchronized (locker) {

            //region find on iterator
            if(iterator != null) {
                while (iterator.hasNext()) {
                    IEntry<K, V> e = new IEntry<>(iterator.next());
                    if (checkFun.isValid(e)) {
                        return e;
                    }
                }
            }
            else if(map == null){
                map = new HashMap<>(); //first getEntry request.
            }
            //endregion

            //region find on iterator or another group filtered
            return getEntryForIteratorOrOtherGroupFiltered(false, IEntry.notFound(), map, checkFun);
            //endregion
        }
    }

    private synchronized Map<K, IterableResult<V>> getFullMap(){
        getEntry(true, false, entry -> false);
        return map;
    }
    //endregion

    //region IterableMap
    @Override
    public Iterator<K> iteratorKeys() {
        return new LocalIterator<>(IEntry::getKey);
    }

    @Override
    public Iterator<IterableResult<V>> iteratorValues() {
        return new LocalIterator<>(IEntry::getIterableResult);
    }

    @Override
    public Iterator<Entry<K, IterableResult<V>>> iterator() {
        return new LocalIterator<>(IEntry::toMapEntry);
    }

    @Override
    public void foreach(CollectionHelper.ForEachEntryConsumer<Entry<K, IterableResult<V>>> action) {
        CollectionHelper.foreach(getFullMap().entrySet(), action);
    }
    //endregion

    //region map
    @Override
    public int size() {
        return this.getFullMap().size();
    }

    @Override
    public boolean isEmpty() {
        return getFullMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getEntry(entry -> Objects.equals(key, entry.key)).found;
    }

    @Override
    public boolean containsValue(Object value) {
        return getEntry(entry -> Objects.equals(value, entry.value)).found;
    }

    @Override
    public IterableResult<V> get(Object key) {
        return getFullMap().get(key);
    }

    private Entry<K, IterableResult<V>> compareEntry(CollectionHelper.FunctionComparer<Integer> comparer) {
        return CollectionHelper.reduce(getFullMap().entrySet(),(acc, curr) -> {
            IterableResult<V> v0 = acc.getValue();
            IterableResult<V> v1 = curr.getValue();
            int c0 = v0 == null ? 0 : v0.count();
            int c1 = v1 == null ? 0 : v1.count();
            return comparer.compare(c0, c1) ? acc : curr;
        });
    }

    @Override
    public Entry<K, IterableResult<V>> minEntry() {
        return compareEntry((a, b) -> a < b);
    }

    @Override
    public Entry<K, IterableResult<V>> maxEntry() {
        return compareEntry((a, b) -> a > b);
    }

    //region UnsupportedOperationException
    @Override
    public IterableResult<V> put(K key, IterableResult<V> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IterableResult<V> remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends IterableResult<V>> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
    //endregion

    @Override
    public Set<K> keySet() {
        return getFullMap().keySet();
    }

    @Override
    public Collection<IterableResult<V>> values() {
        return getFullMap().values();
    }

    @Override
    public Set<Entry<K, IterableResult<V>>> entrySet() {
        return getFullMap().entrySet();
    }
    //endregion

    //region toString
    @Override
    public String toString() {
        synchronized (locker) {
            if(map == null){
                return "null";
            }
        }
        return getFullMap().toString();
    }
    //endregion

}
