package com.atomatus.linq;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Iterator;
import java.util.Objects;

@SuppressWarnings({"unchecked", "Duplicates"})
final class IteratorForMath {

    interface CalculateFunction<N> {
        N calc(N n0, N n1);
    }

    //region default Values
    private static int defaultInt() {
        return 0;
    }

    private static long defaultLong() {
        return 0L;
    }

    private static float defaultFloat() {
        return 0f;
    }

    private static double defaultDouble() {
        return 0d;
    }

    private static BigInteger defaultBigInteger() {
        return BigInteger.ZERO;
    }

    private static BigDecimal defaultBigDecimal() {
        return BigDecimal.ZERO;
    }

    private static BigInteger bigIntegerValue(Number n) {
        return n instanceof BigInteger ? (BigInteger) n :
                n instanceof BigDecimal ? ((BigDecimal) n).toBigInteger() :
                        BigInteger.valueOf(n.longValue());
    }

    private static BigDecimal bigDecimalValue(Number n) {
        return n instanceof BigDecimal ? (BigDecimal) n :
                n instanceof BigInteger ? new BigDecimal((BigInteger) n) :
                        BigDecimal.valueOf(n.doubleValue());
    }

    private static int sumInt(Integer n0, Integer n1) {
        return n0 + n1;
    }

    private static long sumLong(Long n0, Long n1) {
        return n0 + n1;
    }

    private static float sumFloat(Float n0, Float n1) {
        return n0 + n1;
    }

    private static double sumDouble(Double n0, Double n1) {
        return n0 + n1;
    }

    private static int divideInt(Integer n0, Integer n1) {
        return n0 / n1;
    }

    private static long divideLong(Long n0, Long n1) {
        return n0 / n1;
    }

    private static float divideFloat(Float n0, Float n1) {
        return n0 / n1;
    }

    private static double divideDouble(Double n0, Double n1) {
        return n0 / n1;
    }

    private static <IN> IN InToOutEquals(IN in) {
        return in;
    }
    //endregion

    //region parse
    static <IN, OUT extends Number> OUT parseNumber(IN in, Class<OUT> resultClass) {
        if (resultClass.isInstance(in)) {
            return (OUT) in;
        } else if (in instanceof Number) {
            return parseNumber((Number) in, resultClass);
        } else {
            return parseNumber(in.toString(), resultClass);
        }
    }

    private static <OUT extends Number> OUT parseNumber(Number n, Class<OUT> resultClass) {
        if (resultClass == Byte.class) {
            return (OUT) valueOrDefault(n, Number::byteValue, IteratorForMath::defaultInt);
        } else if (resultClass == Short.class) {
            return (OUT) valueOrDefault(n, Number::shortValue, IteratorForMath::defaultInt);
        } else if (resultClass == Integer.class) {
            return (OUT) valueOrDefault(n, Number::intValue, IteratorForMath::defaultInt);
        } else if (resultClass == Long.class) {
            return (OUT) valueOrDefault(n, Number::longValue, IteratorForMath::defaultLong);
        } else if (resultClass == Float.class) {
            return (OUT) valueOrDefault(n, Number::floatValue, IteratorForMath::defaultFloat);
        } else if (resultClass == Double.class) {
            return (OUT) valueOrDefault(n, Number::doubleValue, IteratorForMath::defaultDouble);
        } else if (resultClass == BigInteger.class || BigInteger.class.isAssignableFrom(resultClass)) {
            return (OUT) valueOrDefault(n, IteratorForMath::bigIntegerValue, IteratorForMath::defaultBigInteger);
        } else if (resultClass == BigDecimal.class || BigDecimal.class.isAssignableFrom(resultClass)) {
            return (OUT) valueOrDefault(n, IteratorForMath::bigDecimalValue, IteratorForMath::defaultBigDecimal);
        } else {
            return (OUT) valueOrDefault(n, Number::doubleValue, IteratorForMath::defaultDouble);
        }
    }

    private static <OUT extends Number> OUT parseNumber(String n, Class<OUT> resultClass) {
        if (resultClass == Byte.class) {
            return (OUT) Byte.valueOf(n);
        } else if (resultClass == Short.class) {
            return (OUT) Short.valueOf(n);
        } else if (resultClass == Integer.class) {
            return (OUT) Integer.valueOf(n);
        } else if (resultClass == Long.class) {
            return (OUT) Long.valueOf(n);
        } else if (resultClass == Float.class) {
            return (OUT) Float.valueOf(n);
        } else if (resultClass == Double.class) {
            return (OUT) Double.valueOf(n);
        } else if (resultClass == BigInteger.class || BigInteger.class.isAssignableFrom(resultClass)) {
            return (OUT) new BigInteger(n);
        } else if (resultClass == BigDecimal.class || BigDecimal.class.isAssignableFrom(resultClass)) {
            return (OUT) new BigDecimal(n, MathContext.DECIMAL128);
        } else {
            return (OUT) Double.valueOf(n);
        }
    }
    //endregion

    //region sum
    static <IN, OUT extends Number> OUT sum(Iterator<IN> iterator, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(fun);
        return IteratorForReduce.reduce(iterator, (acc, curr) -> sum(acc, fun.mount(curr)),
                fun.mount(iterator.hasNext() ? iterator.next() : null));
    }

    static <IN, OUT extends Number> OUT sum(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(fun);
        return IteratorForReduce.reduce(arr, (acc, curr) -> sum(acc, fun.mount(curr)));
    }

    static <IN extends Number> IN sum(Iterator<IN> iterator) {
        return IteratorForMath.sum(iterator, IteratorForMath::InToOutEquals);
    }

    static <IN extends Number> IN sum(IN[] arr) {
        return IteratorForMath.sum(arr, IteratorForMath::InToOutEquals);
    }

    private static <N extends Number> N sum(N n0, N n1) {
        if (n0 instanceof Short || n1 instanceof Short) {
            return calc(n0, n1, Number::intValue, IteratorForMath::defaultInt, IteratorForMath::sumInt);
        } else if (n0 instanceof Integer || n1 instanceof Integer) {
            return calc(n0, n1, Number::intValue, IteratorForMath::defaultInt, IteratorForMath::sumInt);
        } else if (n0 instanceof Long || n1 instanceof Long) {
            return calc(n0, n1, Number::longValue, IteratorForMath::defaultLong, IteratorForMath::sumLong);
        } else if (n0 instanceof Float || n1 instanceof Float) {
            return calc(n0, n1, Number::floatValue, IteratorForMath::defaultFloat, IteratorForMath::sumFloat);
        } else if (n0 instanceof BigInteger || n1 instanceof BigInteger) {
            return calc(n0, n1, IteratorForMath::defaultBigInteger, BigInteger::add);
        } else if (n0 instanceof BigDecimal || n1 instanceof BigDecimal) {
            return calc(n0, n1, IteratorForMath::defaultBigDecimal, BigDecimal::add);
        } else {
            return calc(n0, n1, Number::doubleValue, IteratorForMath::defaultDouble, IteratorForMath::sumDouble);
        }
    }
    //endregion

    //region average
    /**
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     */
    static <IN, OUT extends Number> OUT average(Iterator<IN> iterator, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(iterator);
        Objects.requireNonNull(fun);
        OUT acc = null;
        int count = 0;
        while (iterator.hasNext()) {
            acc = sum(acc, fun.mount(iterator.next()));
            count++;
        }
        return count > 0 ? divide(acc, count) : null;
    }

    static <IN, OUT extends Number> OUT average(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(fun);
        OUT acc = null;
        int count = 0;
        for (IN i : arr) {
            acc = sum(acc, fun.mount(i));
            count++;
        }
        return count > 0 ? divide(acc, count) : null;
    }

    static <IN extends Number> IN average(Iterator<IN> iterator) {
        return IteratorForMath.average(iterator, IteratorForMath::InToOutEquals);
    }

    static <IN extends Number> IN average(IN[] arr) {
        return IteratorForMath.average(arr, IteratorForMath::InToOutEquals);
    }
    //endregion

    //region mean

    /**
     * A mean is a mathematical term, that describes the average of a sample.<br/>
     * In Statistics, the definition of the mean is similar to the average.<br/>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     */
    static <IN, OUT extends Number> OUT mean(Iterator<IN> iterator, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(iterator);
        Objects.requireNonNull(fun);
        OUT min = null, max = null;
        while (iterator.hasNext()) {
            OUT next = fun.mount(iterator.next());
            min = minValid(min, next);
            max = maxValid(max, next);
        }
        return divide(sum(min, max), 2);
    }

    static <IN, OUT extends Number> OUT mean(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(fun);
        OUT min = min(arr, fun);
        OUT max = max(arr, fun);
        return divide(sum(min, max), 2);
    }

    static <IN extends Number> IN mean(Iterator<IN> iterator) {
        return IteratorForMath.mean(iterator, IteratorForMath::InToOutEquals);
    }

    static <IN extends Number> IN mean(IN[] arr) {
        return IteratorForMath.mean(arr, IteratorForMath::InToOutEquals);
    }
    //endregion

    //region divide
    static <N extends Number> N divide(N acc, int divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Division by zero");
        } else if (acc instanceof Integer) {
            return calc(acc, divisor, Number::intValue, IteratorForMath::defaultInt, IteratorForMath::divideInt);
        } else if (acc instanceof Long) {
            return calc(acc, divisor, Number::longValue, IteratorForMath::defaultLong, IteratorForMath::divideLong);
        } else if (acc instanceof Float) {
            return calc(acc, divisor, Number::floatValue, IteratorForMath::defaultFloat, IteratorForMath::divideFloat);
        } else if (acc instanceof Double) {
            return calc(acc, divisor, Number::doubleValue, IteratorForMath::defaultDouble, IteratorForMath::divideDouble);
        } else if (acc instanceof BigInteger) {
            return calc(acc, divisor, IteratorForMath::bigIntegerValue, IteratorForMath::defaultBigInteger, BigInteger::divide);
        } else if (acc instanceof BigDecimal) {
            return calc(acc, divisor, IteratorForMath::bigDecimalValue, IteratorForMath::defaultBigDecimal, BigDecimal::divide);
        } else {
            return calc(acc, divisor, Number::doubleValue, IteratorForMath::defaultDouble, IteratorForMath::divideDouble);
        }
    }
    //endregion

    //region min
    @SuppressWarnings("rawtypes")
    private static <E> int compare(E e0, E e1) {
        return e0 == null ? (e1 == null ? 0 : -1) :
                (e1 == null ? 1 :
                        (e0 instanceof Comparable ? ((Comparable) e0).compareTo(e1) :
                                (e0 instanceof Number ? Double.compare(((Number) e0).doubleValue(), ((Number) e1).doubleValue()) :
                                        (e0.equals(e1) ? 0 : 1))));
    }

    private static <E> E minValid(E e0, E e1){
        return e0 == null || compare(e0, e1) == 1 ? e1 : e0;
    }

    static <IN, OUT> OUT min(Iterator<IN> iterator, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(iterator);
        Objects.requireNonNull(fun);
        return CollectionHelper.reduce(iterator, (acc, curr) -> minValid(acc, fun.mount(curr)));
    }

    static <IN, OUT> OUT min(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(fun);
        return CollectionHelper.reduce(arr, (acc, curr) -> minValid(acc, fun.mount(curr)));
    }

    static <E> E min(Iterator<E> iterator) {
        return min(iterator, IteratorForMath::InToOutEquals);
    }

    static <E> E min(E[] arr) {
        return min(arr, IteratorForMath::InToOutEquals);
    }
    //endregion

    //region max

    private static <E> E maxValid(E e0, E e1){
        return e0 == null || compare(e0, e1) == -1 ? e1 : e0;
    }

    static <IN, OUT> OUT max(Iterator<IN> iterator, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(iterator);
        Objects.requireNonNull(fun);
        return CollectionHelper.reduce(iterator, (acc, curr) -> maxValid(acc, fun.mount(curr)));
    }

    static <IN, OUT> OUT max(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(fun);
        return CollectionHelper.reduce(arr, (acc, curr) -> maxValid(acc,fun.mount(curr)));
    }

    static <E> E max(Iterator<E> iterator) {
        return max(iterator, IteratorForMath::InToOutEquals);
    }

    static <E> E max(E[] arr) {
        return max(arr, IteratorForMath::InToOutEquals);
    }
    //endregion

    //region calc
    private static <IN extends Number, OUT> IN calc(IN n0, IN n1,
                                                    CollectionHelper.FunctionGet<OUT> funcDef,
                                                    CalculateFunction<OUT> calcFun) {
        return (IN) calcFun.calc(
                valueOrDefault(n0, null, funcDef),
                valueOrDefault(n1, null, funcDef));
    }

    private static <I extends Number, J extends Number, K extends Number> I calc(I n0, J n1,
                                                                                 CollectionHelper.FunctionMount<Number, K> funMount,
                                                                                 CollectionHelper.FunctionGet<K> funcDef,
                                                                                 CalculateFunction<K> calcFun) {
        return (I) calcFun.calc(valueOrDefault(n0, funMount, funcDef), valueOrDefault(n1, funMount, funcDef));
    }

    private static <E, I> I valueOrDefault(E e, CollectionHelper.FunctionMount<E, I> funMount, CollectionHelper.FunctionGet<I> funcDef) {
        return e == null ? funcDef.get() : funMount != null ? funMount.mount(e) : (I) e;
    }
    //endregion
}
