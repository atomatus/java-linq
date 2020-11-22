package com.atomatus.linq;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Iterator;
import java.util.Objects;

@SuppressWarnings({"unchecked", "Duplicates", "SpellCheckingInspection"})
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

    private static int subInt(Integer n0, Integer n1) {
        return n0 - n1;
    }

    private static long subLong(Long n0, Long n1) {
        return n0 - n1;
    }

    private static float subFloat(Float n0, Float n1) {
        return n0 - n1;
    }

    private static double subDouble(Double n0, Double n1) {
        return n0 - n1;
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
        return arr.length == 0 ? null :
                IteratorForReduce.reduce(arr, (acc, curr) -> sum(acc, fun.mount(curr)),
                        fun.mount(arr[0]), 1);
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

    //region sub
    static <N extends Number> N sub(N n0, N n1) {
        if (n0 instanceof Short || n1 instanceof Short) {
            return calc(n0, n1, Number::intValue, IteratorForMath::defaultInt, IteratorForMath::subInt);
        } else if (n0 instanceof Integer || n1 instanceof Integer) {
            return calc(n0, n1, Number::intValue, IteratorForMath::defaultInt, IteratorForMath::subInt);
        } else if (n0 instanceof Long || n1 instanceof Long) {
            return calc(n0, n1, Number::longValue, IteratorForMath::defaultLong, IteratorForMath::subLong);
        } else if (n0 instanceof Float || n1 instanceof Float) {
            return calc(n0, n1, Number::floatValue, IteratorForMath::defaultFloat, IteratorForMath::subFloat);
        } else if (n0 instanceof BigInteger || n1 instanceof BigInteger) {
            return calc(n0, n1, IteratorForMath::defaultBigInteger, BigInteger::subtract);
        } else if (n0 instanceof BigDecimal || n1 instanceof BigDecimal) {
            return calc(n0, n1, IteratorForMath::defaultBigDecimal, BigDecimal::subtract);
        } else {
            return calc(n0, n1, Number::doubleValue, IteratorForMath::defaultDouble, IteratorForMath::subDouble);
        }
    }
    //endregion

    //region pow
    @SuppressWarnings("SameParameterValue")
    static <N extends Number> N pow(N base, Number exp) {
        if (base instanceof Short) {
            return calc(base, exp, Number::intValue,
                    IteratorForMath::defaultInt,
                    (b, e) -> b ^ e);
        } else if (base instanceof Integer) {
            return calc(base, exp, Number::intValue,
                    IteratorForMath::defaultInt,
                    (b, e) -> b ^ e);
        } else if (base instanceof Long) {
            return calc(base, exp, Number::longValue,
                    IteratorForMath::defaultLong,
                    (b, e) -> b ^ e);
        } else if (base instanceof Float) {
            return calc(base, exp,
                    Number::floatValue,
                    IteratorForMath::defaultFloat,
                    (b, e) -> ((Double) Math.pow(b.doubleValue(), e.doubleValue())).floatValue());
        }else if (base instanceof BigInteger) {
             return calc(base, exp,
                     n -> BigInteger.valueOf(n.longValue()),
                     IteratorForMath::defaultBigInteger,
                     (b, e) -> b.pow(e.intValue()));
        } else if (base instanceof BigDecimal) {
            return calc(base, exp,
                    n -> BigDecimal.valueOf(n.doubleValue()),
                    IteratorForMath::defaultBigDecimal,
                    (b, e) -> b.pow(e.intValue()));
        } else {
            return calc(base, exp,
                    Number::doubleValue,
                    IteratorForMath::defaultDouble,
                    Math::pow);
        }
    }
    //endregion

    //region amplitude

    /**
     * In relation to a time series, the amplitude of a fluctuation is the value of the ordinate at its peak or
     * trough taken from some mean value or trend line. Sometimes the difference between values at peak and
     * trough is referred to as an "amplitude".
     * <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHMAAAAwCAYAAAAij0UkAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAE1ElEQVR4nO2bL3DiWBzHv725GXCpo+7qwAWXutSlLnUPl3WpAxdkXeqoSx114FJzQ8XNsA5ccoqcShxxYVU49T0B2+W6ty2lsL158z4zzJDkPd6P93l/fgnDEUlCIQW/fHQAiv2hZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZErE7jKTW5xVj3Dame4xnG8skyG6F6c4OjrBRfcByRLIHq5wdtzARecO0/wgzW7PYor7zjlOjo5wenmDx2wVc6txjGari2G8+PkxcScKhqLOWq1CmAHnu33I66QBjYpGMSrXhzbt3uxQre1ARK8O/taekCTLyKclBofrj1fYTebMp2H4DH2dqDkc7zemDVIGRoWaGLH44I76EZFXJ35rc1KM2bZcjoqPi2UHmQVDodMZlSxHDjUY7KWvVCnH9CyTpvn9y/InL1ZNA4MVTafxwR31QyKPddSoGxa9SfmhobxdZuTTMHtMSTLt0cC3ZfAgpD0aqNDs/9/m5FdWS60mQn6sSvLXt+2wCwyvh2heT3EKAKdNNGtfMI0z4KKx/w0dQPb4Gbn2NxaPUyw/XaJ6kFaA5K6FzjDH8vmFahOd+1tcnvx3vWX8iOlCw5fPj4hxibMDxbcVb1IfedRrOi3bpm3btG2LugZW7MHLo3LHZbaMfFrOgFFgsqIJhm8a+nOGrqA3OuCMLsZsW22OJj511Oi+vGNsVuTIE2yH6V7DeYPMOQe2zvazfWHi1oi6x2h9nPY9BukeIttMKOYBzYpG8Rab5ZzpQffYOUPHoheVJGf0dbDmjresW3B+gNi2lllO2jTEgM9jKPomUbHYL0jOQ4q6QTcYrfbUnVl31NPAmTMwK9TscN1+yr5dpyFcCkunIXz2PIembrOfkmTEoO2z5wmK3ozlbEBRN+hHKUPXYRC9vyef3ybN/DpRc9eZfclZz2TddOgKk7rp0vfbFIZOJyxYpgMK3eX4aznLZdsxqdv9d/XbVjKLSUCh16g7A842J8d8zMDRWYFGwx1wVo7omD7fcydYRgN6QqdWt+mPUpJkOurRMTSiolP4q4ESeSbdMckypLBW97ppz6IISbJgUZAsB7StPguSRShoCo+99y67xYR9z2Zd0+kEE85JlrOQvqiv+sEJOJ6THLs0/RnJOfuWzUFJMvJotSckZ/TNtfixS2tdLrDeupX8m60SoOOzKwzjq+8vnJzj6j7G1f36OL9DftJYJUc7Um22cDNs4Wbj3OlFB/cXHdxvnIufKlRRxRLL1dsVywyPwylQjZGvU5Ljyw6a3S7Q+EEmsy3HZ/h084BPmwE2LtEdXqI73DiXPAWIp6ytegx8n2I9lau+cHUb9vtsNomBZuNgGee2LIbXGFZbaLXOnwZW/hDj/OYMD90HfMCDtp/CfmUen2L5eIv7gz+XzJFkObI4QRbHyPIYcZYhjnPkSYJqo4HF3RW6d5+R51P88fstrqcNnF+0cJZ00Rlm75oBr7NElmTI4xhZFiPOckzjDEkcI08S/PXnFHGeYBrn63LJ6jvkGbJk98iOSPVfE1lQP4FJhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkT8A7YI56A7YlkCAAAAAElFTkSuQmCC"/>
     */
    static <IN, OUT extends Number> OUT amplitude(Iterator<IN> iterator, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(iterator);
        Objects.requireNonNull(fun);
        OUT min = null, max = null;
        while (iterator.hasNext()) {
            OUT next = fun.mount(iterator.next());
            min = minValid(min, next);
            max = maxValid(max, next);
        }

        return sub(max, min);
    }

    static <IN, OUT extends Number> OUT amplitude(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(fun);
        OUT min = min(arr, fun);
        OUT max = max(arr, fun);
        return sub(max, min);
    }

    static <IN extends Number> IN amplitude(Iterator<IN> iterator) {
        return IteratorForMath.amplitude(iterator, IteratorForMath::InToOutEquals);
    }

    static <IN extends Number> IN amplitude(IN[] arr) {
        return IteratorForMath.amplitude(arr, IteratorForMath::InToOutEquals);
    }
    //endregion

    //region average
    /**
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     * <img  src="data:image/png;base64,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" />
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

    //region median
    private static <IN, OUT extends Number> IteratorForSort.SortCollection<OUT> sort(Iterator<IN> iterator, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(iterator);
        Objects.requireNonNull(fun);
        IteratorForSort.SortCollection<OUT> sort = new IteratorForSort.SortCollection<>();
        while(iterator.hasNext()) {
            sort.push(fun.mount(iterator.next()));
        }
        return sort;
    }

    private static <IN, OUT extends Number> IteratorForSort.SortCollection<OUT> sort(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(fun);
        IteratorForSort.SortCollection<OUT> sort = new IteratorForSort.SortCollection<>();
        for (IN in : arr) {
            sort.push(fun.mount(in));
        }
        return sort;
    }

    private static <OUT extends Number> OUT median(IteratorForSort.SortCollection<OUT> sort) {
        // Caso par, somar os dois valores mais ao centro do conjunto ordenado e dividir por dois.
        if(sort.len % 2 == 0) {
            int midlInd = sort.len / 2;
            int midrInd = midlInd + 1;
            OUT midl = (OUT) sort.values[midlInd];
            OUT midr = (OUT) sort.values[midrInd];
            return divide(sum(midl, midr), 2);
        }

        // Caso impar, encontrar o valor no centro do conjunto ordenao;
        return (OUT) sort.values[sort.len / 2];
    }

    /**
     * Arrange the data points from smallest to largest.<br/>
     * <ul>
     * <li>If the number of data points is odd, the median is the middle data point in the list;</li>
     * <li>If the number of data points is even, the median is the average of the two middle data points in the list.</li>
     * </ul>
     */
    static <IN, OUT extends Number> OUT median(Iterator<IN> iterator, CollectionHelper.FunctionMount<IN, OUT> fun) {
        return median(sort(iterator, fun));
    }

    static <IN, OUT extends Number> OUT median(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> fun) {
        return median(sort(arr, fun));
    }

    static <IN extends Number> IN median(Iterator<IN> iterator) {
        return median(iterator, IteratorForMath::InToOutEquals);
    }

    static <IN extends Number> IN median(IN[] arr) {
        return median(arr, IteratorForMath::InToOutEquals);
    }
    //endregion

    //region mode
    /**
     * In statistics, the mode is the most commonly observed value in a set of data.
     */
    static <IN, OUT extends Number> OUT mode(Iterator<IN> iterator, CollectionHelper.FunctionMount<IN, OUT> fun) {
        return IterableResultFactory.getInstanceForGroup(() -> iterator, fun).size().maxEntry().getKey();
    }

    static <IN, OUT extends Number> OUT mode(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> fun) {
        return IterableResultFactory.getInstanceForGroupArray(arr, fun).size().maxEntry().getKey();
    }

    static <IN extends Number> IN mode(Iterator<IN> iterator) {
        return mode(iterator, IteratorForMath::InToOutEquals);
    }

    static <IN extends Number> IN mode(IN[] arr) {
        return mode(arr, IteratorForMath::InToOutEquals);
    }
    //endregion

    //region variance

    /**
     * <h2>What Is Population Variance?</h2>
     * <p>
     * The term variance refers to a statistical measurement of the spread between numbers
     * in a data set. More specifically, variance measures how far each number in the set
     * is from the mean and, therefore, from every other number in the set.
     * Variance is often depicted by this symbol: σ2. It is used by both analysts and
     * traders to determine volatility and market security.
     * The square root of the variance is the standard deviation (σ), which helps determine
     * the consistency of an investment's returns over a period of time.
     * </p><br/>
     * <h2>Understanding Population Variance</h2>
     * <p>
     * In statistics, variance measures variability from the average or mean. <br/>
     * It is calculated by taking the differences between each number in the data
     * set and the mean, then squaring the differences to make them positive,
     * and finally dividing the sum of the squares by the number of values in the data set.
     * </p><br/>
     * <p>
     * Variance is calculated using the following formula:<br/>
     * <img src="data:image/png;base64,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">
     * </p><br/>
     * <p>
     * <ul>
     * <li>A large variance indicates that numbers in the set are far from the mean and far from each other.</li>
     * <li>A small variance, on the other hand, indicates the opposite.</li>
     * <li>A variance value of zero, though, indicates that all values within a set of numbers are identical.</li>
     * </ul>
     * </p>
     *
     * @param iterator target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return population variance result
     */
    static <IN, OUT extends Number> OUT variance(Iterator<IN> iterator, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(iterator);
        Objects.requireNonNull(fun);
        OUT acc = null;
        int count = 0; // N
        while (iterator.hasNext()) {
            acc = sum(acc, fun.mount(iterator.next()));
            count++;
        }

        if(count > 0) {
            OUT mu  = divide(acc, count); //μ or x̄
            OUT sum = sum(iterator, e -> pow(sub(fun.mount(e), mu), 2)); //Σ(x - μ)²
            return divide(sum, count); //σ² = Σ(x - μ)² / N
        }

        return null;
    }

    static <IN, OUT extends Number> OUT variance(IN[] arr, CollectionHelper.FunctionMount<IN, OUT> fun) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(fun);
        OUT acc = null;
        int count = arr.length; // N
        for (IN in : arr) acc = sum(acc, fun.mount(in));

        if(count > 0) {
            OUT mu  = divide(acc, count); //μ or x̄
            OUT sum = sum(arr, e -> pow(sub(fun.mount(e), mu), 2)); //Σ(x - μ)²
            return divide(sum, count); //σ² = Σ(x - μ)² / N
        }

        return null;
    }

    static <IN extends Number> IN variance(Iterator<IN> iterator) {
        return variance(iterator, IteratorForMath::InToOutEquals);
    }

    static <IN extends Number> IN variance(IN[] arr) {
        return variance(arr, IteratorForMath::InToOutEquals);
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
    static <E> int compare(E e0, E e1) {
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
