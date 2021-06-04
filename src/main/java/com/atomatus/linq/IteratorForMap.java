package com.atomatus.linq;

import java.lang.reflect.Array;
import java.util.*;

abstract class IteratorForMap<K, V> implements IterableResultMap.IteratorMap<K, V> {

    private static class LazyReadOnlyMap<IN, K, V> implements IterableMap<K, V> {

        private Set<Entry<K, V>> set;
        private transient Set<K> keySet;
        private transient Collection<V> values;
        private final Iterable<IN> input;
        private final CollectionHelper.FunctionMount<IN, Entry<K, V>> mountFun;

        LazyReadOnlyMap(Iterable<IN> input,
                        CollectionHelper.FunctionMount<IN, Entry<K, V>> mountFun) {
            this.input = input;
            this.mountFun = mountFun;
        }

        private synchronized Set<Entry<K, V>> getSet() {
            if (set == null) {
                set = CollectionHelper.select(input, mountFun).toSet();
            }
            return set;
        }

        @Override
        public int size() {
            return getSet().size();
        }

        @Override
        public boolean isEmpty() {
            return getSet().isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V get(Object key) {
            for (Entry<K, V> entry : getSet()) {
                K k = entry.getKey();
                if (k == key || key.equals(k)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        @Override
        public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<K> keySet() {
            if (keySet == null) {
                keySet = CollectionHelper.select(getSet(), Entry::getKey).toSet();
            }
            return keySet;
        }

        @Override
        public Collection<V> values() {
            if (values == null) {
                values = CollectionHelper.select(getSet(), Entry::getValue).toList();
            }
            return values;
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return getSet();
        }

        @Override
        public void foreach(CollectionHelper.ForEachEntryConsumer<Entry<K, V>> action) {
            CollectionHelper.foreach(getSet(), action);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder().append("[");
            List<Entry<K, V>> list = new ArrayList<>(getSet());
            char eq     = '=';
            String sep  = ", ";
            for (int i = 0, l = list.size(), j = l - 1; i < l; i++) {
                Entry<K, V> e = list.get(i);
                sb.append(e.getKey())
                        .append(eq)
                        .append(IteratorForJoin.toString(e.getValue()));
                if (i < j) {
                    sb.append(sep);
                }
            }
            return sb.append("]").toString();
        }

        @Override
        public Iterator<K> iteratorKeys() {
            return CollectionHelper.select(getSet(), Entry::getKey).iterator();
        }

        @Override
        public Iterator<V> iteratorValues() {
            return CollectionHelper.select(getSet(), Entry::getValue).iterator();
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private Entry<K, V> compareEntry(CollectionHelper.FunctionComparer<Number> comparer) {
            return CollectionHelper.reduce(getSet(), (acc, curr) -> {
                V v0 = acc.getValue();
                V v1 = curr.getValue();
                if(v1 == null) {
                    return acc;
                } else if(v0 == null) {
                    return curr;
                } else if(v0 instanceof Comparable<?>) {
                    return comparer.compare(((Comparable)v0).compareTo(v1), 0) ? acc : curr;
                } else if(v0 instanceof Number) {
                    return comparer.compare((Number) v0, (Number) v1) ? acc : curr;
                } else if(v0 instanceof Collection<?>) {
                    Collection<?> c0 = (Collection<?>) v0;
                    Collection<?> c1 = (Collection<?>) v1;
                    return comparer.compare(c0.size(), c1.size()) ? acc : curr;
                } else if(v0.getClass().isArray() && v1.getClass().isArray()) {
                    return comparer.compare(Array.getLength(v0), Array.getLength(v1)) ? acc : curr;
                } else {
                    throw new UnsupportedOperationException(
                            String.format("Object of type \"%1$s\" can not be compared!",
                                    v0.getClass().getSimpleName()));
                }
            });
        }

        @Override
        public Entry<K, V> minEntry() {
            return compareEntry((a, b) -> a.doubleValue() < b.doubleValue());
        }

        @Override
        public Entry<K, V> maxEntry() {
            return compareEntry((a, b) -> a.doubleValue() > b.doubleValue());
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return getSet().iterator();
        }
    }

    static <IN, K, V> IteratorForMap<K, V> getInstanceForLazyReadOnlyMap(Iterable<IN> input,
                                                                         CollectionHelper.FunctionMount<IN, Map.Entry<K, V>> mountFun) {
        return new IteratorForMap<K, V>() {
            @Override
            protected IterableMap<K, V> initResult() {
                return new LazyReadOnlyMap<>(input, mountFun);
            }
        };
    }

    private IterableMap<K, V> result;
    private Iterator<Map.Entry<K, V>> iterator;

    protected final synchronized IterableMap<K, V> getResult() {
        if (result == null) {
            result = this.initResult();
        }
        return result;
    }

    protected final synchronized Iterator<Map.Entry<K, V>> getIterator() {
        if(iterator == null) {
            iterator = getResult().iterator();
        }
        return iterator;
    }

    protected abstract IterableMap<K, V> initResult();

    @Override
    public IterableResult<K> keySet() {
        return new IterableResult<K>() {
            @Override
            public Iterator<K> iterator() {
                return getResult().iteratorKeys();
            }
        };
    }

    @Override
    public IterableResult<V> values() {
        return new IterableResult<V>() {
            @Override
            public Iterator<V> iterator() {
                return getResult().iteratorValues();
            }
        };
    }

    @Override
    public boolean hasNext() {
        return getIterator().hasNext();
    }

    @Override
    public Map.Entry<K, V> next() {
        return getIterator().next();
    }

    @Override
    public int count() {
        return getResult().size();
    }

    @Override
    public boolean isEmpty() {
        return getResult().isEmpty();
    }

    @Override
    public Map<K, V> toMap() {
        return new HashMap<>(getResult());
    }

    @Override
    public Set<Map.Entry<K, V>> toSet() {
        return new HashSet<>(getResult().entrySet());
    }

    @Override
    public Object[][] toTable() {
        return IteratorFor2DMatrixFactory
                .getInstanceForMap(this.toMap())
                .toMatrix();
    }

    @Override
    public V get(K key) {
        return getResult().get(key);
    }

    @Override
    public Map.Entry<K, V> minEntry() {
        return getResult().minEntry();
    }

    @Override
    public Map.Entry<K, V> maxEntry() {
        return getResult().maxEntry();
    }

    @Override
    public void foreach(CollectionHelper.ForEachEntryConsumer<Map.Entry<K, V>> action) {
        getResult().foreach(action);
    }

}
