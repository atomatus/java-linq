package com.atomatus.linq;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;


final class IteratorForGroup<K, V> extends IteratorForMap<K, IterableResult<V>> implements IterableResultGroup.IteratorGroup<K, V> {

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

    private final CollectionHelper.FunctionGet<Iterator<V>> iteratorFun;
    private final CollectionHelper.FunctionMount<V, K> groupFun;
    private final CollectionHelper.CompareEntryValid<Map.Entry<K, IterableResult<V>>> filter;
    private final CollectionHelper.FunctionGet<Iterable<Map.Entry<K, IterableResult<V>>>> otherGroupEntries;
    private final IteratorForGroupCalculator<K, V> calculator;
    private final int limitCount;

    IteratorForGroup(CollectionHelper.FunctionGet<Iterator<V>> iteratorFun, CollectionHelper.FunctionMount<V, K> groupFun){
        this(Objects.requireNonNull(iteratorFun), Objects.requireNonNull(groupFun), null, null,
                IterableMapForFunctionGet.NO_LIMIT);
    }

    IteratorForGroup(V[] arr, CollectionHelper.FunctionMount<V, K> groupFun){
        this(() -> new IteratorForSelectArray<>(arr), Objects.requireNonNull(groupFun), null, null,
                IterableMapForFunctionGet.NO_LIMIT);
    }

    IteratorForGroup(CollectionHelper.FunctionGet<Iterable<Map.Entry<K, IterableResult<V>>>> otherGroupEntries,
                     CollectionHelper.CompareEntryValid<Map.Entry<K, IterableResult<V>>> filter){
        this(null, null, Objects.requireNonNull(filter), Objects.requireNonNull(otherGroupEntries),
                IterableMapForFunctionGet.NO_LIMIT);
    }

    IteratorForGroup(CollectionHelper.FunctionGet<Iterable<Map.Entry<K, IterableResult<V>>>> otherGroupEntries, int limitCount){
        this(null, null, null, Objects.requireNonNull(otherGroupEntries), limitCount);
    }

    IteratorForGroup(CollectionHelper.FunctionGet<Iterator<V>> iteratorFun,
                     CollectionHelper.FunctionMount<V, K> groupFun,
                     CollectionHelper.CompareEntryValid<Map.Entry<K, IterableResult<V>>> filter,
                     CollectionHelper.FunctionGet<Iterable<Map.Entry<K, IterableResult<V>>>> otherGroupEntries,
                     int limitCount) {
        this.iteratorFun        = iteratorFun;
        this.groupFun           = groupFun;
        this.filter             = filter;
        this.otherGroupEntries  = otherGroupEntries;
        this.limitCount         = limitCount;
        this.calculator         = new IteratorForGroupCalculator<>(this::getResult);
    }

    @Override
    protected IterableMap<K, IterableResult<V>> initResult() {
        return new IterableMapForFunctionGet<>(iteratorFun, groupFun, filter, otherGroupEntries, limitCount);
    }

    /**
     * Generate an iterable result with the amount of items in each entry.
     *
     * @return iterable result map within amount of items size in each entry.
     */
    @Override
    public IterableResultMap<K, Integer> size() {
        return calculator.size();
    }

    /**
     * Apply summation operation in a sequence of any kind of number.
     *
     * @param resultClass number type class.
     * @param <N>         number type
     * @return summation result
     */
    @Override
    public <N extends Number> IterableResultMap<K, N> sum(Class<N> resultClass) {
        return calculator.sum(resultClass);
    }

    /**
     * Apply summation operation in a sequence of any kind of number.
     *
     * @param mountFun function to get a target number in element.
     * @param <N>      number type
     * @return summation result
     */
    @Override
    public <N extends Number> IterableResultMap<K, N> sum(CollectionHelper.FunctionMount<V, N> mountFun) {
        return calculator.sum(mountFun);
    }

    /**
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     *
     * @param resultClass result number class type
     * @param <N>         result number type
     * @return result number
     */
    @Override
    public <N extends Number> IterableResultMap<K, N> average(Class<N> resultClass) {
        return calculator.average(resultClass);
    }

    /**
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     *
     * @param mountFun function to get target number
     * @param <N>      result number type
     * @return result number
     */
    @Override
    public <N extends Number> IterableResultMap<K, N> average(CollectionHelper.FunctionMount<V, N> mountFun) {
        return calculator.average(mountFun);
    }

    /**
     * A mean is a mathematical term, that describes the average of a sample.<br>
     * In Statistics, the definition of the mean is similar to the average.<br>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     *
     * @param resultClass number type class.
     * @return result number
     */
    @Override
    public <N extends Number> IterableResultMap<K, N> mean(Class<N> resultClass) {
        return calculator.mean(resultClass);
    }

    /**
     * A mean is a mathematical term, that describes the average of a sample.<br>
     * In Statistics, the definition of the mean is similar to the average.<br>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     *
     * @param mountFun function to get target number
     * @param <N>      result number type
     * @return result number
     */
    @Override
    public <N extends Number> IterableResultMap<K, N> mean(CollectionHelper.FunctionMount<V, N> mountFun) {
        return calculator.mean(mountFun);
    }

    /**
     * Recover minimum value of collection
     *
     * @return minimum value
     */
    @Override
    public IterableResultMap<K, V> min() {
        return calculator.min();
    }

    /**
     * Recover minimum value of collection
     *
     * @param mountFun function to get Comparable element target
     * @param <C>      result comparable element
     * @return minimum value
     */
    @Override
    public <C extends Comparable<C>> IterableResultMap<K, V> min(CollectionHelper.FunctionMount<V, C> mountFun) {
        return calculator.min(mountFun);
    }

    /**
     * Recover maximum value of collection
     *
     * @return maximum value
     */
    @Override
    public IterableResultMap<K, V> max() {
        return calculator.max();
    }

    /**
     * Recover maximum value of collection
     *
     * @param mountFun function to get Comparable element target
     * @param <C>      result comparable element
     * @return maximum value
     */
    @Override
    public <C extends Comparable<C>> IterableResultMap<K, V> max(CollectionHelper.FunctionMount<V, C> mountFun) {
        return calculator.max(mountFun);
    }

    @Override
    public IterableResultGroup<K, V> sample(CollectionHelper.CompareEntryValid<K> checkFun) {
        return calculator.sample(checkFun);
    }

    @Override
    public IterableResultGroup<K, V> amount(int count) {
        return calculator.amount(count);
    }

    @Override
    public Object[][] toTable() {
        return IteratorFor2DMatrixFactory
                .getInstanceForEntrySet(this.toSet())
                .toMatrix();
    }
}
