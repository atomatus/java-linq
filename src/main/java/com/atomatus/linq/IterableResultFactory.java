package com.atomatus.linq;

import java.util.Collection;
import java.util.Iterator;

final class IterableResultFactory {

    static <I> IterableResult<I> getInstanceForFilter(CollectionHelper.FunctionGet<Iterator<I>> iteratorFun,
                                                      CollectionHelper.CompareEntryValid<I> where) {
        return new IterableResult<I>() {
            @Override
            public Iterator<I> iterator() {
                return new IteratorForFilter<>(iteratorFun, where);
            }
        };
    }

    static <I> IterableResult<I> getInstanceForFilterArray(I[] arr, CollectionHelper.CompareEntryValid<I> where) {
        return new IterableResult<I>() {
            @Override
            public Iterator<I> iterator() {
                return new IteratorForFilterArray<>(arr, where);
            }
        };
    }

    static <I, E, C, S extends Collection<? extends I>> IterableResult<E> getInstanceForFilterAsClassType(Class<C> classType,
                                                                                                          CollectionHelper.FunctionGet<S> getCollection,
                                                                                                          CollectionHelper.CompareEntryForClass<I, C> checkEntryValidForClassType,
                                                                                                          CollectionHelper.FunctionMount<I, E> mountElementFun) {
        return new IterableResult<E>() {
            @Override
            public Iterator<E> iterator() {
                return new IteratorForFilterAsClassType<>(classType, getCollection,
                        checkEntryValidForClassType, mountElementFun);
            }
        };
    }

    static <K, V> IterableResultGroup<K, V> getInstanceForGroup(CollectionHelper.FunctionGet<Iterator<V>> iteratorFun,
                                                                CollectionHelper.FunctionMount<V, K> groupFun) {
        return new IterableResultGroup<K, V>() {
            @Override
            protected IterableResultGroup.IteratorGroup<K, V> initIterator() {
                return new IteratorForGroup<>(iteratorFun, groupFun);
            }
        };
    }

    static <K, V> IterableResultGroup<K, V> getInstanceForGroupArray(V[] arr,
                                                                     CollectionHelper.FunctionMount<V, K> groupFun) {
        return new IterableResultGroup<K, V>() {
            @Override
            protected IterableResultGroup.IteratorGroup<K, V> initIterator() {
                return new IteratorForGroup<>(arr, groupFun);
            }
        };
    }

    static <IN, OUT> IterableResult<OUT> getInstanceForSelect(CollectionHelper.FunctionGet<Iterator<IN>> iteratorFun,
                                                              CollectionHelper.FunctionMount<IN, OUT> mount) {
        return new IterableResult<OUT>() {
            @Override
            public Iterator<OUT> iterator() {
                return new IteratorForSelect<>(iteratorFun, mount);
            }
        };
    }

    static <IN, OUT> IterableResult<OUT> getInstanceForSelectArray(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> mount) {
        return new IterableResult<OUT>() {
            @Override
            public Iterator<OUT> iterator() {
                return new IteratorForSelectArray<>(arr, mount);
            }
        };
    }


    static <I> IterableResult<I> getInstanceForMerge(Iterable<I> curr, Iterable<? extends I>[] args) {
        return new IterableResult<I>() {
            @Override
            public Iterator<I> iterator() {
                return new IteratorForMerge<>(curr, args);
            }
        };
    }

    static <I> IterableResult<I> getInstanceForMerge(Iterable<? extends I>[] args) {
        return new IterableResult<I>() {
            @Override
            public Iterator<I> iterator() {
                return new IteratorForMerge<>(args);
            }
        };
    }

    static <I> IterableResult<I> getInstanceForMerge(I[] arr, Iterable<? extends I>[] args) {
        return new IterableResult<I>() {
            @Override
            public Iterator<I> iterator() {
                return new IteratorForMerge<>(arr, args);
            }
        };
    }

    static <I> IterableResult<I> getInstanceForMergeArray(I[][] args) {
        return new IterableResult<I>() {
            @Override
            public Iterator<I> iterator() {
                return new IteratorForMergeArray<>(args);
            }
        };
    }

    static <I> IterableResult<I> getInstanceForMergeArray(Iterable<I> iterable, I[][] args) {
        return new IterableResult<I>() {
            @Override
            public Iterator<I> iterator() {
                return new IteratorForMergeArray<>(iterable, args);
            }
        };
    }

    static <I> IterableResult<I> getInstanceForIntersection(Iterable<I>[] args) {
        return new IterableResult<I>() {
            @Override
            public Iterator<I> iterator() {
                return new IteratorForIntersection<>(args);
            }
        };
    }

    static <I> IterableResult<I> getInstanceForIntersectionArray(I[][] args) {
        return new IterableResult<I>() {
            @Override
            public Iterator<I> iterator() {
                return new IteratorForIntersectionArray<>(args);
            }
        };
    }


    static <IN, OUT> IterableResult<OUT> getInstanceForDistinct(CollectionHelper.FunctionGet<Iterator<IN>> iteratorFun,
                                                                CollectionHelper.FunctionMount<IN, OUT> mount) {
        return new IterableResult<OUT>() {
            @Override
            public Iterator<OUT> iterator() {
                return new IteratorForDistinct<>(iteratorFun, mount);
            }
        };
    }

    static <IN, OUT> IterableResult<OUT> getInstanceForDistinctArray(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> mount) {
        return new IterableResult<OUT>() {
            @Override
            public Iterator<OUT> iterator() {
                return new IteratorForDistinctArray<>(arr, mount);
            }
        };
    }

    static <I> IterableResult<I> getInstanceForTake(CollectionHelper.FunctionGet<Iterator<I>> colFun, int count) {
        return new IterableResult<I>() {
            @Override
            public Iterator<I> iterator() {
                return new IteratorForTake<>(colFun, count);
            }
        };
    }

    static <I> IterableResult<I> getInstanceForJump(CollectionHelper.FunctionGet<Iterator<I>> colFun, int count) {
        return new IterableResult<I>() {
            @Override
            public Iterator<I> iterator() {
                return new IteratorForJump<>(colFun, count);
            }
        };
    }
}
