package com.atomatus.linq;

import java.util.Map;
import java.util.Objects;

final class IteratorForGroupCalculator<K, V> implements IterableResultGroup.IteratorGroupCalculator<K, V> {

    static class EntryMapForMount<K, E, OUT> implements Map.Entry<K, OUT> {

        private final K key;
        private final E inValue;
        private final CollectionHelper.FunctionMount<E, OUT> mountFun;
        private transient OUT outValue;

        EntryMapForMount(Map.Entry<K, E> entry,
                         CollectionHelper.FunctionMount<E, OUT> mountFun) {
            this.key = Objects.requireNonNull(entry).getKey();
            this.inValue = entry.getValue();
            this.mountFun = Objects.requireNonNull(mountFun);
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public OUT getValue() {
            return outValue == null ? (outValue = mountFun.mount(inValue)) : outValue;
        }

        @Override
        public OUT setValue(OUT value) {
            throw new UnsupportedOperationException("Read only entry: value can not be changed!");
        }

        @Override
        public String toString() {
            return String.valueOf(key) + '=' + IteratorForJoin.toString(getValue());
        }
    }

    private final CollectionHelper.FunctionGet<Iterable<Map.Entry<K, IterableResult<V>>>> getMapEntries;

    public IteratorForGroupCalculator(CollectionHelper.FunctionGet<Iterable<Map.Entry<K, IterableResult<V>>>> getMapEntries) {
        this.getMapEntries = Objects.requireNonNull(getMapEntries);
    }

    private <N> IterableResultMap<K, N> calc(CollectionHelper.FunctionMount<IterableResult<V>, N> calcFun) {
        return new IterableResultMap<K, N>() {
            @Override
            protected IterableResultMap.IteratorMap<K, N> initIterator() {
                return IteratorForMap.getInstanceForLazyReadOnlyMap(getMapEntries.get(),
                        e -> new IteratorForGroup.EntryMapForMount<>(e, calcFun));
            }
        };
    }

    @Override
    public IterableResultMap<K, Integer> size() {
        return new IterableResultMap<K, Integer>() {
            @Override
            protected IterableResultMap.IteratorMap<K, Integer> initIterator() {
                return IteratorForMap.getInstanceForLazyReadOnlyMap(getMapEntries.get(),
                        e -> new IteratorForGroup.EntryMapForMount<>(e, CollectionHelper::count));
            }
        };
    }

    @Override
    public <N extends Number> IterableResultMap<K, N> sum(Class<N> resultClass) {
        Objects.requireNonNull(resultClass);
        return sum(i -> IteratorForMath.parseNumber(i, resultClass));
    }

    @Override
    public <N extends Number> IterableResultMap<K, N> sum(CollectionHelper.FunctionMount<V, N> mountFun) {
        Objects.requireNonNull(mountFun);
        return calc(list -> IteratorForMath.sum(list.iterator(), mountFun));
    }

    @Override
    public <N extends Number> IterableResultMap<K, N> average(Class<N> resultClass) {
        Objects.requireNonNull(resultClass);
        return average(i -> IteratorForMath.parseNumber(i, resultClass));
    }

    @Override
    public <N extends Number> IterableResultMap<K, N> average(CollectionHelper.FunctionMount<V, N> mountFun) {
        Objects.requireNonNull(mountFun);
        return calc(list -> IteratorForMath.average(list.iterator(), mountFun));
    }

    @Override
    public <N extends Number> IterableResultMap<K, N> mean(Class<N> resultClass) {
        Objects.requireNonNull(resultClass);
        return mean(i -> IteratorForMath.parseNumber(i, resultClass));
    }

    @Override
    public <N extends Number> IterableResultMap<K, N> mean(CollectionHelper.FunctionMount<V, N> mountFun) {
        Objects.requireNonNull(mountFun);
        return calc(list -> IteratorForMath.mean(list.iterator(), mountFun));
    }

    @Override
    public IterableResultMap<K, V> min() {
        return calc(list -> IteratorForMath.min(list.iterator()));
    }

    @Override
    public <C extends Comparable<C>> IterableResultMap<K, V> min(CollectionHelper.FunctionMount<V, C> mountFun) {
        Objects.requireNonNull(mountFun);
        return calc(list -> IteratorForMath.min(list.iterator(), (v) -> new Comparable<C>() {
            final V value = v;
            final C c = mountFun.mount(v);

            @Override
            public int compareTo(C o) {
                return o.compareTo(c);
            }
        }).value);
    }

    @Override
    public IterableResultMap<K, V> max() {
        return calc(list -> IteratorForMath.max(list.iterator()));
    }

    @Override
    public <C extends Comparable<C>> IterableResultMap<K, V> max(CollectionHelper.FunctionMount<V, C> mountFun) {
        Objects.requireNonNull(mountFun);
        return calc(list -> IteratorForMath.max(list.iterator(), (v) -> new Comparable<C>() {
            final V value = v;
            final C c = mountFun.mount(v);

            @Override
            public int compareTo(C o) {
                return o.compareTo(c);
            }
        }).value);
    }

    @Override
    public IterableResultGroup<K, V> sample(CollectionHelper.CompareEntryValid<K> checkFun) {
        Objects.requireNonNull(checkFun);
        return new IterableResultGroup<K, V>() {
            @Override
            protected IteratorGroup<K, V> initIterator() {
                return new IteratorForGroup<>(getMapEntries, entry -> checkFun.isValid(entry.getKey()));
            }
        };
    }

    @Override
    public IterableResultGroup<K, V> amount(int count) {
        return new IterableResultGroup<K, V>() {
            @Override
            protected IteratorGroup<K, V> initIterator() {
                return new IteratorForGroup<>(getMapEntries, count);
            }
        };
    }
}
