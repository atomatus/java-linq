package com.atomatus.linq;

import com.atomatus.util.DateHelper;
import com.atomatus.util.DecimalHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Generated result from collection, set or array interation or filter using {@link CollectionHelper}
 * or another {@link IterableResult}.
 *
 * @param <E> iterable element type
 * @author Carlos Matos
 */
public abstract class IterableResult<E> implements Iterable<E> {

    private int count = -1;

    /**
     * Convert current iterable result to array.
     *
     * @return new array within iterable elements reference.
     */
    public E[] toArray() {
        return CollectionHelper.toArray(this);
    }

    /**
     * Convert current iterable result to list.
     *
     * @return new list within iterable elements reference.
     */
    public List<E> toList() {
        return CollectionHelper.toList(this);
    }

    /**
     * Convert current iterable result to set.
     *
     * @return new set within iterable elements reference.
     */
    public Set<E> toSet() {
        return CollectionHelper.toSet(this);
    }

    /**
     * Filter current iterable by condition
     *
     * @param where filter condition
     * @return a new iterable result within elements filtered
     */
    public IterableResult<E> filter(CollectionHelper.CompareEntryValid<E> where) {
        return CollectionHelper.filter(this, where);
    }

    /**
     * Filter current iterable result recovering only non null elements.
     *
     * @return a new iterable result within elements filtered by non null condition.
     */
    public IterableResult<E> nonNull() {
        return CollectionHelper.nonNull(this);
    }

    /**
     * Generate an iterable result grouping elements by groupfun key result.
     *
     * @param groupFun group function to get grouping key.
     * @param <K>      key type
     * @return an instance of iterable result group whithin set values grouped by key.
     */
    public <K> IterableResultGroup<K, E> groupBy(CollectionHelper.FunctionMount<E, K> groupFun) {
        return CollectionHelper.groupBy(this, groupFun);
    }

    /**
     * Generate an iterable result grouping elements by equals objects.
     * @return an instance of iterable result group whithin set values grouped by equals objects.
     */
    public IterableResultGroup<E, E> group() {
        return CollectionHelper.groupBy(this, e -> e);
    }

    /**
     * Generate an iterable result within a set of values recovered from mount function.
     *
     * @param mount mount function to get new data
     * @param <OUT> element mounted from target iterator.
     * @return new iterable wihitin set of values from mount function.
     */
    public <OUT> IterableResult<OUT> select(CollectionHelper.FunctionMount<E, OUT> mount) {
        return CollectionHelper.select(this, mount);
    }

    /**
     * Union of two or more collections.
     *
     * @param args one or more iterable.
     * @return new iterable result.
     */
    @SafeVarargs
    public final IterableResult<E> merge(Iterable<? extends E>... args) {
        return IterableResultFactory.getInstanceForMerge(this, args);
    }

    /**
     * Union of two or more collections.
     *
     * @param args one or more arrays.
     * @return new iterable result.
     */
    @SafeVarargs
    public final IterableResult<E> merge(E[]... args) {
        return IterableResultFactory.getInstanceForMergeArray(this, args);
    }

    /**
     * Intersection of two or more collections.
     *
     * @param args one or more iterable.
     * @return new iterable result.
     */
    @SafeVarargs
    public final IterableResult<E> intersection(Iterable<E>... args) {
        return CollectionHelper.intersection(
                CollectionHelper.add(args, this));
    }

    /**
     * Intersection of two or more collections.
     *
     * @param args one or more iterable.
     * @return new iterable result.
     */
    @SafeVarargs
    public final IterableResult<E> intersection(E[]... args) {
        if(args.length > 1){
            return CollectionHelper.intersection(args).intersection(this);
        }

        Iterable<E>[] arr = CollectionHelper.select(args,
                e -> (Iterable<E>) () -> new IteratorForSelectArray<>(e)).toArray();
        return intersection(arr);
    }

    /**
     * Reduce method execute the reduceFun function to generate an accumulate result for each element on collection.
     *
     * @param reduceFun reduce function
     * @param acc       initial accumulate value, maybe null. When null first value of collection is the first accumulate.
     * @param <OUT>     accumulate type, indicate same IN type when accumulate start null.
     * @return final accumulate result.
     */
    public <OUT> OUT reduce(CollectionHelper.FunctionReduce<E, OUT> reduceFun, OUT acc) {
        return CollectionHelper.reduce(this, reduceFun, acc);
    }

    /**
     * Reduce method execute the reduceFun function to generate an accumulate result for each element on collection.
     * Starting accumulate null.
     *
     * @param reduceFun reduce function
     * @param <OUT>     accumulate type, indicate same IN type when accumulate start null.
     * @return final accumulate result.
     */
    public <OUT> OUT reduce(CollectionHelper.FunctionReduce<E, OUT> reduceFun) {
        return CollectionHelper.reduce(this, reduceFun);
    }

    /**
     * Apply summation operation in a sequence of any kind of number.
     *
     * @param fun   function to get a target number in element.
     * @param <OUT> number type
     * @return summation result
     */
    public <OUT extends Number> OUT sum(CollectionHelper.FunctionMount<E, OUT> fun) {
        return CollectionHelper.sum(this, fun);
    }

    /**
     * Apply summation operation in a sequence of any kind of number (casting current elements to OUT Number type).
     *
     * @param <OUT> number type
     * @return summation result
     */
    @SuppressWarnings("unchecked")
    public <OUT extends Number> OUT sum() {
        return CollectionHelper.sum(this, e -> (OUT) e);
    }

    /**
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     *
     * @param fun   function to get target number
     * @param <OUT> result number type
     * @return result number
     */
    public <OUT extends Number> OUT average(CollectionHelper.FunctionMount<E, OUT> fun) {
        return CollectionHelper.average(this, fun);
    }

    /**
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     *
     * @param <OUT> result number type
     * @return result number
     */
    @SuppressWarnings("unchecked")
    public <OUT extends Number> OUT average() {
        return CollectionHelper.average(this, e -> (OUT) e);
    }

    /**
     * A mean is a mathematical term, that describes the average of a sample.<br/>
     * In Statistics, the definition of the mean is similar to the average.<br/>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     *
     * @param fun   function to get target number
     * @param <OUT> result number type
     * @return result number
     */
    public <OUT extends Number> OUT mean(CollectionHelper.FunctionMount<E, OUT> fun) {
        return CollectionHelper.mean(this, fun);
    }

    /**
     * A mean is a mathematical term, that describes the average of a sample.<br/>
     * In Statistics, the definition of the mean is similar to the average.<br/>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     *
     * @param <OUT> result number type
     * @return result number
     */
    @SuppressWarnings("unchecked")
    public <OUT extends Number> OUT mean() {
        return CollectionHelper.mean(this, e -> (OUT) e);
    }

    /**
     * Recover minimum value of collection
     *
     * @param fun   function to get Comparable element target
     * @param <OUT> result comparable element
     * @return minimum value
     */
    public <OUT extends Comparable<OUT>> OUT min(CollectionHelper.FunctionMount<E, OUT> fun) {
        return CollectionHelper.min(this, fun);
    }

    /**
     * Recover minimum value of collection
     *
     * @param <OUT> result comparable element
     * @return minimum value
     */
    @SuppressWarnings("unchecked")
    public <OUT extends Comparable<OUT>> OUT min() {
        return CollectionHelper.min(this, e -> (OUT) e);
    }

    /**
     * Recover maximum value of collection
     *
     * @param fun   function to get Comparable element target
     * @param <OUT> result comparable element
     * @return maximum value
     */
    public <OUT extends Comparable<OUT>> OUT max(CollectionHelper.FunctionMount<E, OUT> fun) {
        return CollectionHelper.max(this, fun);
    }

    /**
     * Recover maximum value of collection
     *
     * @param <OUT> collection comparable element
     * @return maximum value
     */
    @SuppressWarnings("unchecked")
    public <OUT extends Comparable<OUT>> OUT max() {
        return CollectionHelper.max(this, e -> (OUT) e);
    }

    /**
     * Recover distinct (non duplicated) element of collection
     *
     * @param mount function to mount output distinct value
     * @param <OUT> output type
     * @return new iterable result with distinct elements.
     */
    public <OUT> IterableResult<OUT> distinct(CollectionHelper.FunctionMount<E, OUT> mount) {
        return CollectionHelper.distinct(this, mount);
    }

    /**
     * Recover distinct (non duplicated) element of collection
     *
     * @return new iterable result with distinct elements.
     */
    public IterableResult<E> distinct() {
        return CollectionHelper.distinct(this);
    }

    /**
     * How like literally named, "jump" elements on collection
     * returning all others elements after offset count it.
     *
     * @param count count of elements will be discarted
     * @return new iterable result with non elements after offset count.
     */
    public IterableResult<E> jump(int count) {
        return CollectionHelper.jump(this, count);
    }

    /**
     * Take only amount of elements set on count.
     *
     * @param count count of elements
     * @return new iterable result with taked elements.
     */

    public IterableResult<E> take(int count) {
        return CollectionHelper.take(this, count);
    }

    /**
     * Count of elements on iterable result.
     *
     * @return count of elements
     */
    public int count() {
        if (count == -1) count = CollectionHelper.count(this);
        return count;
    }

    /**
     * Count element by condition.
     *
     * @param where condition to accept count element
     * @return count of element into condition.
     */
    public int count(CollectionHelper.CompareEntryValid<E> where) {
        return CollectionHelper.count(this, where);
    }

    /**
     * A simple foreach action.
     *
     * @param action action to recover each element on collection
     */
    public void foreach(CollectionHelper.ForEachEntryConsumer<E> action) {
        CollectionHelper.foreach(this, action);
    }

    /**
     * A simple foreach action with index.
     *
     * @param action action to recover each element on collection
     */
    public void foreachI(CollectionHelper.ForEachIterableEntryConsumer<E> action) {
        CollectionHelper.foreach(this, action);
    }

    /**
     * Check if all elements on iterable pass on test action.
     *
     * @param action check pass action
     * @return return true when all elements on collection pass on test action.
     */
    public boolean all(CollectionHelper.CompareEntryValid<E> action) {
        return CollectionHelper.all(this, action);
    }

    /**
     * Check if at least one element on iterable pass on test action.
     *
     * @param action check pass action
     * @return return true when at least one element on collection pass on test action.
     */
    public boolean any(CollectionHelper.CompareEntryValid<E> action) {
        return CollectionHelper.any(this, action);
    }

    /**
     * Join all values how unique string, wheter value is a collection, set or array bring up theses datas
     * to same level of current datas and join it, otherwise, set simple objects toString and join it too.
     * @param separator data separator
     * @return string result
     */
    public String join(String separator) {
        return IteratorForJoin.join(null, null, separator, this);
    }

    /**
     * Join all values how unique string, wheter value is a collection, set or array bring up theses datas
     * to same level of current datas and join it, otherwise, set simple objects toString and join it too.
     * @return string result
     */
    public String join() {
        return IteratorForJoin.join(null, null, ", ", this);
    }

    private <W> IterableResult<W> asWrapper(W defaultValue,
        CollectionHelper.FunctionMount<Number, W> fromNumber,
        CollectionHelper.FunctionMount<String, W> fromString) {
        return this.select(e -> {
            if(e == null) {
                return defaultValue;
            } else if(e instanceof Number){
                return fromNumber.mount((Number)e);
            } else {
                String aux = e.toString();
                return aux.length() > 0 ? fromString.mount(aux) : defaultValue;
            }
        });
    }

    /**
     * Convert current result values to Integer (default value is 0).
     * @return iterable result for integer values.
     */
    public IterableResult<Integer> asInteger() {
        return asWrapper(0,
                Number::intValue,
                Integer::parseInt);
    }

    /**
     * Convert current result values to Long (default value is 0).
     * @return iterable result for long values.
     */
    public IterableResult<Long> asLong() {
        return asWrapper(0L,
                Number::longValue,
                Long::parseLong);
    }

    /**
     * Convert current result values to Float (default value is 0).
     * @return iterable result for float values.
     */
    public IterableResult<Float> asFloat() {
        return asWrapper(0f,
                Number::floatValue,
                Float::parseFloat);
    }

    /**
     * Convert current result values to Double (default value is 0).
     * @return iterable result for double values.
     */
    public IterableResult<Double> asDouble() {
        return asWrapper(0d,
                Number::doubleValue,
                Double::parseDouble);
    }

    /**
     * Convert current result values to BigDecimal (default value is BigDecimal.Zero).
     * @return iterable result for bigDecimal values.
     */
    public IterableResult<BigDecimal> asBigDecimal() {
        return asWrapper(BigDecimal.ZERO,
                DecimalHelper::toBigDecimal,
                DecimalHelper::toBigDecimal);
    }

    /**
     * Convert current result values to Date (default value is null).
     * @return iterable result for Date values.
     */
    public IterableResult<Date> asDate() {
        return asWrapper(null,
                n -> new Date(n.longValue()),
                DateHelper.getInstance()::parseDate);
    }

    @Override
    public String toString() {
        return IteratorForJoin.join("[", "]", ", ", this);
    }
}
