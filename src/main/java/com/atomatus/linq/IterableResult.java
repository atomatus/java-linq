package com.atomatus.linq;

import com.atomatus.util.DateHelper;
import com.atomatus.util.DecimalHelper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Generated result from collection, set or array iteration or filter using {@link CollectionHelper}
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
     * Generate an iterable result grouping elements by groupFun key result.
     *
     * @param groupFun group function to get grouping key.
     * @param <K>      key type
     * @return an instance of iterable result group within set values grouped by key.
     */
    public <K> IterableResultGroup<K, E> groupBy(CollectionHelper.FunctionMount<E, K> groupFun) {
        return CollectionHelper.groupBy(this, groupFun);
    }

    /**
     * Generate an iterable result grouping elements by equals objects.
     * @return an instance of iterable result group within set values grouped by equals objects.
     */
    public IterableResultGroup<E, E> group() {
        return CollectionHelper.groupBy(this, e -> e);
    }

    /**
     * Generate an iterable result within a set of values recovered from mount function.
     *
     * @param mount mount function to get new data
     * @param <OUT> element mounted from target iterator.
     * @return new iterable within set of values from mount function.
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
     * <p>
     * In relation to a time series, the amplitude of a fluctuation is the value of the ordinate at its peak or
     * trough taken from some mean value or trend line. Sometimes the difference between values at peak and
     * trough is referred to as an "amplitude".
     * </p>
     * <p>
     * In Statistics, the total amplitude At of a set of values
     * is the difference between the highest and lowest value of the sample.
     * </p>
     * @param fun   function to get target number
     * @param <OUT> result number type
     * @return result number
     */
    public <OUT extends Number> OUT amplitude(CollectionHelper.FunctionMount<E, OUT> fun) {
        return CollectionHelper.amplitude(this, fun);
    }

    /**
     * <p>
     * In relation to a time series, the amplitude of a fluctuation is the value of the ordinate at its peak or
     * trough taken from some mean value or trend line. Sometimes the difference between values at peak and
     * trough is referred to as an "amplitude".
     * </p>
     * <p>
     * In Statistics, the total amplitude At of a set of values
     * is the difference between the highest and lowest value of the sample.
     * </p>
     * @param <OUT> result number type
     * @return result number
     */
    @SuppressWarnings("unchecked")
    public <OUT extends Number> OUT amplitude() {
        return CollectionHelper.amplitude(this, e -> (OUT) e);
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
     * Arrange the data points from smallest to largest.<br/>
     * <ul>
     * <li>If the number of data points is odd, the median is the middle data point in the list;</li>
     * <li>If the number of data points is even, the median is the average of the two middle data points in the list.</li>
     * </ul>
     * @param fun   function to get target number
     * @param <OUT> result number type
     * @return result number
     */
    public <OUT extends Number> OUT median(CollectionHelper.FunctionMount<E, OUT> fun) {
        return CollectionHelper.median(this, fun);
    }

    /**
     * Arrange the data points from smallest to largest.<br/>
     * <ul>
     * <li>If the number of data points is odd, the median is the middle data point in the list;</li>
     * <li>If the number of data points is even, the median is the average of the two middle data points in the list.</li>
     * </ul>
     * @param <OUT> result number type
     * @return result number
     */
    @SuppressWarnings("unchecked")
    public <OUT extends Number> OUT median() {
        return CollectionHelper.median(this, e -> (OUT) e);
    }

    /**
     * In statistics, the mode is the most commonly observed value in a set of data.
     * @param fun   function to get target number
     * @param <OUT> result number type
     * @return result number
     */
    public <OUT extends Number> OUT mode(CollectionHelper.FunctionMount<E, OUT> fun) {
        return CollectionHelper.mode(this, fun);
    }

    /**
     * In statistics, the mode is the most commonly observed value in a set of data.
     * @param <OUT> result number type
     * @return result number
     */
    @SuppressWarnings("unchecked")
    public <OUT extends Number> OUT mode() {
        return CollectionHelper.mode(this, e -> (OUT) e);
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
     * <h2>Population Variance</h2>
     * <p>
     * In statistics, variance measures variability from the average or mean. <br/>
     * It is calculated by taking the differences between each number in the data
     * set and the mean, then squaring the differences to make them positive,
     * and finally dividing the sum of the squares by the number of values in the data set.
     * </p><br/>
     * <ul>
     * <li>A large variance indicates that numbers in the set are far from the mean and far from each other.</li>
     * <li>A small variance, on the other hand, indicates the opposite.</li>
     * <li>A variance value of zero, though, indicates that all values within a set of numbers are identical.</li>
     * </ul>
     * </p>
     * @param fun   function to get target number
     * @param <OUT> result number type
     * @return population variance result
     */
    public <OUT extends Number> OUT variance(CollectionHelper.FunctionMount<E, OUT> fun) {
        return CollectionHelper.variance(this, fun);
    }

    /**
     * <h2>Population Variance</h2>
     * <p>
     * In statistics, variance measures variability from the average or mean. <br/>
     * It is calculated by taking the differences between each number in the data
     * set and the mean, then squaring the differences to make them positive,
     * and finally dividing the sum of the squares by the number of values in the data set.
     * </p><br/>
     * <ul>
     * <li>A large variance indicates that numbers in the set are far from the mean and far from each other.</li>
     * <li>A small variance, on the other hand, indicates the opposite.</li>
     * <li>A variance value of zero, though, indicates that all values within a set of numbers are identical.</li>
     * </ul>
     * </p>
     * @param <OUT> result number type
     * @return population variance result
     */
    @SuppressWarnings("unchecked")
    public <OUT extends Number> OUT variance() {
        return CollectionHelper.variance(this, e -> (OUT) e);
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
     * @param count count of elements will be ignored.
     * @return new iterable result with non elements after offset count.
     */
    public IterableResult<E> jump(int count) {
        return CollectionHelper.jump(this, count);
    }

    /**
     * Take only amount of elements set on count.
     *
     * @param count count of elements
     * @return new iterable result with got elements.
     */
    public IterableResult<E> take(int count) {
        return CollectionHelper.take(this, count);
    }

    /**
     * Sort all elements in iterable as ascendant mode (from smallest to biggest).
     * @return new iterable result sorted.
     */
    public IterableResult<E> asc() {
        return CollectionHelper.asc(this);
    }

    /**
     * Sort all elements in iterable as descendant mode (from biggest to smallest).
     * @return new iterable result sorted.
     */
    public IterableResult<E> desc() {
        return CollectionHelper.desc(this);
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
     * Join all values how unique string, whether value is a collection, set or array bring up theses data
     * to same level of current data and join it, otherwise, set simple objects toString and join it too.
     * @param separator data separator
     * @return string result
     */
    public String join(String separator) {
        return IteratorForJoin.join(null, null, separator, this);
    }

    /**
     * Join all values how unique string, whether value is a collection, set or array bring up theses data
     * to same level of current data and join it, otherwise, set simple objects toString and join it too.
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
     * Convert current result values to Short (default value is 0).
     * @return iterable result for short values.
     */
    public IterableResult<Short> asShort() {
        return asWrapper((short) 0,
                Number::shortValue,
                Short::parseShort);
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
     * Convert current result values to BigInteger (default value is BigInteger.Zero).
     * @return iterable result for BigInteger values.
     */
    public IterableResult<BigInteger> asBigInteger() {
        return asWrapper(BigInteger.ZERO,
                e -> BigInteger.valueOf(e.longValue()),
                e -> DecimalHelper.toBigDecimal(e).toBigInteger());
    }

    /**
     * Convert current result values to Calendar (default value is null).
     * @return iterable result for Calendar values.
     */
    public IterableResult<Calendar> asCalendar() {
        return asWrapper(null,
                n -> {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(n.longValue());
                    return c;
                },
                DateHelper.getInstance()::parseCalendar);
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

    /**
     * Convert current result values to String (default value is null).
     * @return iterable result for String values.
     */
    public IterableResult<String> asString() {
        return asWrapper(null,
                Number::toString,
                String::toString);
    }

    @Override
    public String toString() {
        return IteratorForJoin.join("[", "]", ", ", this);
    }
}
