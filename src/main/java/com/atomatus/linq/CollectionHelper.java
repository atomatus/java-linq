package com.atomatus.linq;

import com.atomatus.util.ArrayHelper;

import java.lang.reflect.Array;
import java.util.*;

/**
 * <p>
 * Helper for query actions (from any iterable, set or array) to
 * manipulate, filter, find, group elements on set or array fastestway for reducing code.
 * </p>
 * <p>
 * Every query action result generate an {@link IterableResult}, {@link IteratorForGroup} or {@link IteratorForMap},
 * all thus are tree schedules Iterator actions that will be mounted and executed only when do a directed request it.
 * </p>
 *
 * @author Carlos Matos
 */
public final class CollectionHelper {

    private CollectionHelper() {
    }

    //region Compare and Functions

    /**
     * Compare if entry is valid for class.
     *
     * @param <I> entry to be compared it.
     * @param <C> desired class type.
     */
    public interface CompareEntryForClass<I, C> {
        boolean isValidFor(I i, Class<C> classType);
    }

    /**
     * Check if current entry is valid.
     *
     * @param <I> target entry.
     */
    public interface CompareEntryValid<I> {
        boolean isValid(I i);
    }

    /**
     * Receive a entry value.
     *
     * @param <I> entry value.
     */
    public interface ForEachEntryConsumer<I> {
        /**
         * Current accept.
         *
         * @param i current element.
         */
        void accept(I i);
    }

    /**
     * Receive a entry value and index.
     *
     * @param <I> entry value.
     */
    public interface ForEachIterableEntryConsumer<I> {
        /**
         * Current accept.
         *
         * @param i     current element.
         * @param index current element index.
         */
        void accept(I i, int index);
    }

    /**
     * Receive a entry value and mount a element.
     *
     * @param <I> entry value.
     * @param <E> generated element from entry value.
     */
    public interface FunctionMount<I, E> {
        E mount(I i);
    }

    /**
     * Small function to get a element.
     *
     * @param <E>
     */
    public interface FunctionGet<E> {
        E get();
    }

    /**
     * Reduce function.
     *
     * @param <IN>
     * @param <OUT>
     */
    public interface FunctionReduce<IN, OUT> {
        OUT reduce(OUT acc, IN curr);
    }

    /**
     * Compare function.
     * @param <E>
     */
    public interface FunctionComparer<E> {
        boolean compare(E e0, E e1);
    }
    //endregion

    //region toArray

    /**
     * Create a new object array from current object (current object referecing a array).
     *
     * @param arr target object.
     * @return object array.
     */
    public static Object[] toArray(Object arr) {
        return ArrayHelper.toArray(arr);
    }

    /**
     * Create a new object array from current object (current object referecing a array).
     *
     * @param arr       target object.
     * @param clazzType array class type.
     * @param <E>       array type.
     * @return object array.
     */
    public static <E> E[] toArray(Object arr, Class<E> clazzType) {
        return ArrayHelper.toArray(arr, clazzType);
    }

    /**
     * Convert a collection to array.
     *
     * @param col target collection.
     * @param <C> collection type.
     * @param <I> collection element type.
     * @return a new array within collection's elements reference.
     */
    public static <C extends Iterable<? extends I>, I> I[] toArray(C col) {
        Objects.requireNonNull(col);
        int i = 0;
        int len = 100;
        I[] arr = null;

        for (I t : col) {
            if (arr == null) {
                arr = copyOf(len, t);
            } else if (i == arr.length) {
                arr = copyOf(arr.length + len, arr);
            }
            arr[i++] = t;
        }

        return arr == null ? copyOf(0) :
                i < arr.length ? copyOf(i, arr) : arr;
    }

    /**
     * Convert a collection to array.
     *
     * @param col  target collection.
     * @param size new array size.
     * @param <C>  collection type.
     * @param <I>  collection element type.
     * @return a new array within collection's elements reference.
     */
    public static <C extends Iterable<? extends I>, I> I[] toArray(C col, int size) {
        Objects.requireNonNull(col);
        int i = 0;
        I[] arr = copyOf(size);

        for (I t : col) {
            if (i == size) {
                break;
            }
            arr[i++] = t;
        }

        return arr;
    }

    /**
     * Generate a copy of array with a new length.
     *
     * @param length new array length.
     * @param array  target array or elements.
     * @param <E>    element type.
     * @return copy of array within elements reference.
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    private static <E> E[] copyOf(int length, E... array) {
        if (array.length > 0) {
            Class<?> type;
            if (array.length == 1) {
                type = array[0].getClass();
            } else {
                List<Class<?>> res = nonNull(array).<Class<?>>distinct(E::getClass).toList();
                type = res.size() == 1 ? array[0].getClass() : array.getClass().getComponentType();
            }

            E[] copy = (E[]) Array.newInstance(type, length);
            System.arraycopy(array, 0, copy, 0, Math.min(array.length, length));
            return copy;
        }

        return (E[]) new Object[0];
    }

    /**
     * Insert a new element at object array (on index).
     *
     * @param arr   target array
     * @param e     new element to array
     * @param index index on array
     * @param <E>   element type
     * @return new array.
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] insertAt(E[] arr, E e, int index) {
        Objects.requireNonNull(arr);
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }

        int length = arr.length;
        int newLength = Math.max(index, length) + 1;
        Class<?> newType = arr.getClass();
        E[] nArr = (newType == Object[].class) ?
                (E[]) new Object[newLength] :
                (E[]) Array.newInstance(newType.getComponentType(), newLength);

        boolean found = false;
        for (int i = 0, j = 0; i < length; j++, i++) {
            if (!found && (found = i == index)) {
                j++;
            }
            nArr[j] = arr[i];
        }
        nArr[index] = e;
        return nArr;
    }

    /**
     * Insert a new element at first index of object array.
     *
     * @param arr target array
     * @param e   new element to array
     * @param <E> element type
     * @return new array.
     */
    public static <E> E[] push(E[] arr, E e) {
        return insertAt(arr, e, 0);
    }

    /**
     * Insert a new element at end of object array.
     *
     * @param arr target array
     * @param e   new element to array
     * @param <E> element type
     * @return new array.
     */
    public static <E> E[] add(E[] arr, E e) {
        return insertAt(arr, e, arr.length);
    }
    //endregion

    //region toList

    /**
     * Convert any iterable to list.
     *
     * @param iterable target
     * @param <E>      element type
     * @return new list within iterable elements
     */
    public static <E> List<E> toList(Iterable<E> iterable) {
        List<E> list = new ArrayList<>();
        foreach(iterable, (ForEachEntryConsumer<E>) list::add);
        return list;
    }

    /**
     * Convert an array to list
     *
     * @param arr target
     * @param <E> element type
     * @return new list within iterable elements
     */
    public static <E> List<E> toList(E[] arr) {
        List<E> list = new ArrayList<>();
        foreach(arr, (ForEachEntryConsumer<E>) list::add);
        return list;
    }
    //endregion

    //region toSet

    /**
     * Convert any iterable to set.
     *
     * @param iterable target
     * @param <E>      element type
     * @return new set within iterable elements
     */
    public static <E> Set<E> toSet(Iterable<E> iterable) {
        Set<E> set = new HashSet<>();
        foreach(iterable, set::add);
        return set;
    }

    /**
     * Convert an array to set
     *
     * @param arr target
     * @param <E> element type
     * @return new set within iterable elements
     */
    public static <E> Set<E> toSet(E[] arr) {
        Set<E> set = new HashSet<>();
        foreach(arr, set::add);
        return set;
    }
    //endregion

    //region repeat
    private static <I, E> E[] loop(int count, I seed, FunctionMount<I, E> mountFun, FunctionGet<E> getFun) {
        if (count <= 0) throw new IllegalArgumentException("Count can not be less then one!");
        E[] arr = copyOf(count, seed == null ? getFun.get() : mountFun.mount(seed));
        for (int i = 1; i < count; i++) arr[i] = seed == null ? getFun.get() : mountFun.mount(seed);
        return arr;
    }

    /**
     * Generate an array with result of mounFun execution for each index.
     * @param count array size
     * @param seed input function seed.
     * @param mountFun function to generate element for each index.
     * @param <I> seed type
     * @param <E> element type
     * @return array generated.
     */
    public static <I, E> E[] repeat(int count, I seed, FunctionMount<I, E> mountFun) {
        return loop(count, seed, mountFun, null);
    }

    /**
     * Generate an array with result of mounFun execution for each index.
     * @param count array size
     * @param mountFun function to generate element for each index.
     * @param <E> element type
     * @return array generated.
     */
    public static <E> E[] repeat(int count, FunctionGet<E> mountFun) {
        return loop(count, null, null, mountFun);
    }

    /**
     * Generate an array with result of mounFun execution for each index.
     * @param count array size
     * @param mountFun function to generate element for each index.
     * @param <E> element type
     * @return array generated.
     */
    public static <E> E[] repeat(int count, FunctionMount<Integer, E> mountFun) {
        return repeat(count, count, mountFun);
    }

    /**
     * Generate a random array of number type specified.
     * @param count array size
     * @param clazz element number type
     * @param <E> element type
     * @return array generated.
     */
    @SuppressWarnings("unchecked")
    public static <E extends Number> E[] random(int count, Class<E> clazz) {
        if(clazz == Integer.class) {
            return  (E[]) repeat(count, count, new Random()::nextInt);
        } else if(clazz == Short.class) {
            Random r = new Random();
            return  (E[]) repeat(count, Math.min(Short.MAX_VALUE, count), (i) -> (short) r.nextInt(i));
        } else if(clazz == Long.class) {
            return (E[]) repeat(count, new Random()::nextLong);
        } else if(clazz == Double.class) {
            return (E[]) repeat(count, new Random()::nextDouble);
        } else if(clazz == Float.class) {
            return (E[]) repeat(count, new Random()::nextFloat);
        } else if(clazz == Byte.class) {
            Byte[] bytes = new Byte[count];
            Random r = new Random();
            for (int i = 0, len = bytes.length; i < len; ) {
                for (int rnd = r.nextInt(), n = Math.min(len - i, Integer.SIZE / Byte.SIZE); n-- > 0; rnd >>= Byte.SIZE) {
                    bytes[i++] = (byte) rnd;
                }
            }
            return (E[]) bytes;
        } else {
            throw new UnsupportedOperationException();
        }
    }
    //endregion

    //region sequence
    private static <E extends Number> E[] sequence(int count, E start, E add, FunctionReduce<E, E> addFun){
        if(count <= 0){
            throw new IllegalArgumentException("Count can not be less then one!");
        }
        E[] arr = copyOf(count, start);
        E curr = start;
        for(int i = 1; i < count; i++) {
            curr = arr[i] = addFun.reduce(curr, add);
        }
        return arr;
    }

    /**
     * Generate an array with a sequence of values specified.
     * @param count array size
     * @param start start value
     * @param add add value for each new input.
     * @param clazz element number type
     * @param <E> element type
     * @return a generated sequence array.
     */
    @SuppressWarnings("unchecked")
    public static <E extends Number> E[] sequence(int count, E start, E add, Class<E> clazz) {
        return sequence(count, start, add,
                clazz == Short.class ? (c, a) -> (E) Integer.valueOf(c.shortValue() + a.shortValue()) :
                        clazz == Integer.class ? (c, a) -> (E) Integer.valueOf(c.intValue() + a.intValue()) :
                                clazz == Long.class ? (c, a) -> (E) Long.valueOf(c.longValue() + a.longValue()) :
                                        clazz == Float.class ? (c, a) -> (E) Float.valueOf(c.floatValue() + a.floatValue()) :
                                                clazz == Double.class ? (c, a) -> (E) Double.valueOf(c.doubleValue() + a.doubleValue()) :
                                                        (c, a) -> (E) Byte.valueOf((byte)(c.byteValue() + a.byteValue())));
    }

    /**
     * Generate an array with a sequence of values specified.
     * @param count array size
     * @param start start value
     * @param clazz element number type
     * @param <E> element type
     * @return a generated sequence array.
     */
    public static <E extends Number> E[] sequence(int count, E start, Class<E> clazz) {
        return sequence(count, start, valueNumberOne(clazz), clazz);
    }

    /**
     * Generate an array with a sequence of values specified.
     * @param count array size
     * @param clazz element number type
     * @param <E> element type
     * @return a generated sequence array.
     */
    public static <E extends Number> E[] sequence(int count, Class<E> clazz) {
        return sequence(count, valueNumberZero(clazz), clazz);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Number> E valueNumberZero(Class<E> clazz) {
        return (E) (clazz == Short.class || clazz == Integer.class ? (Number) 0 :
                clazz == Long.class ? (Number) 0L :
                        clazz == Float.class ? (Number) 0F :
                                clazz == Double.class ? 0D : 0x0);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Number> E valueNumberOne(Class<E> clazz) {
        return (E) (clazz == Short.class || clazz == Integer.class ? (Number) 1 :
                        clazz == Long.class ? (Number) 1L :
                                clazz == Float.class ? (Number) 1F :
                                        clazz == Double.class ? 1D : 0x1);
    }
    //endregion

    //region filterAs
    //region filterAsIterable

    /**
     * Build an interable from any collection, filtering values and accepting only who is valid for classType and
     * recovering from a iterable mode.
     *
     * @param classType                   dessired class type.
     * @param getCollection               target collection.
     * @param checkEntryValidForClassType check if current entry is valid for classType.
     * @param mountElementFun             mount a element from current entry.
     * @param <I>                         current entry type, same type (or herintenced type) for collection S.
     * @param <E>                         mounted element from entry type.
     * @param <C>                         dessired class type.
     * @param <S>                         collection.
     * @return return a iterable of filtered element (E) of collection (S) within entries (I) valid for ClassType (C).
     */
    public static <I, E, C, S extends Collection<? extends I>> IterableResult<E> filterAsIterable(Class<C> classType,
                                                                                                  FunctionGet<S> getCollection,
                                                                                                  CompareEntryForClass<I, C> checkEntryValidForClassType,
                                                                                                  FunctionMount<I, E> mountElementFun) {
        return IterableResultFactory.getInstanceForFilterAsClassType(classType,
                getCollection,
                checkEntryValidForClassType,
                mountElementFun);
    }

    /**
     * Build an interable from any collection, filtering values and accepting only who is valid for classType and
     * recovering from a iterable mode.
     *
     * @param classType                   dessired class type.
     * @param set                         target collection.
     * @param checkEntryValidForClassType check if current entry is valid for classType.
     * @param mountElementFun             mount a element from current entry.
     * @param <I>                         current entry type, same type (or herintenced type) for collection S.
     * @param <E>                         mounted element from entry type.
     * @param <C>                         dessired class type.
     * @return return a iterable of filtered element (E) of collection within entries (I) valid for ClassType (C).
     */
    public static <I, E, C> IterableResult<E> filterAsIterable(Class<C> classType,
                                                               Collection<? extends I> set,
                                                               CompareEntryForClass<I, C> checkEntryValidForClassType,
                                                               FunctionMount<I, E> mountElementFun) {
        Objects.requireNonNull(set);
        return filterAsIterable(classType, () -> set, checkEntryValidForClassType, mountElementFun);
    }

    /**
     * Build an interable from any collection, filtering values and accepting only who is valid for function validate entry and
     * recovering from a iterable mode.
     *
     * @param set               target collection.
     * @param checkEntryIsValid check which entry is valid to be filtering.
     * @param mountElementFun   mount a element from each entry.
     * @param <I>               current entry type.
     * @param <E>               mounted element from entry type.
     * @return return a iterable of filtered element (E) of collection within entries (I) valid for compareEntryValid function.
     */
    public static <I, E> IterableResult<E> filterAsIterable(Collection<? extends I> set,
                                                            CompareEntryValid<I> checkEntryIsValid,
                                                            FunctionMount<I, E> mountElementFun) {
        Objects.requireNonNull(checkEntryIsValid);
        return filterAsIterable(Object.class, set, (i, clazz) -> checkEntryIsValid.isValid(i), mountElementFun);
    }

    /**
     * Build an interable from any collection, filtering values and accepting only who is valid for function validate entry and
     * recovering from a iterable mode.
     *
     * @param set               target collection.
     * @param checkEntryIsValid check which entry is valid to be filtering.
     * @param <I>               entry type.
     * @return return a iterable of filtered element of collection valid for compareEntryValid function.
     */
    public static <I> IterableResult<I> filterAsIterable(Collection<? extends I> set, CompareEntryValid<I> checkEntryIsValid) {
        return filterAsIterable(set, checkEntryIsValid, (i) -> i);
    }
    //endregion

    //region filterAsList
    private static <I> List<I> filterAsList(List<?> list, Class<I> classType) {
        Iterable<I> i = filterAsIterable(classType, list, null, null);
        List<I> result = new ArrayList<>();
        foreach(i, (ForEachEntryConsumer<I>) result::add);
        return result;
    }

    /**
     * Generate a list filtered by super class type of elements on target list.
     *
     * @param list      list of elements are super then target classType.
     * @param classType super class type
     * @param <I>       element type (herintence from super class type)
     * @return new list with elements are instanceof classType
     */
    public static <I> List<I> filterAsListS(List<? super I> list, Class<I> classType) {
        return filterAsList(list, classType);
    }

    /**
     * Generate a list filtered by extends class type of elements on target list.
     *
     * @param list      list of elements are extends then target classType.
     * @param classType extended class type
     * @param <I>       element type (herintence from extend class type)
     * @return new list with elements are instanceof classType
     */
    public static <I> List<I> filterAsListE(List<? extends I> list, Class<I> classType) {
        return filterAsList(list, classType);
    }
    //endregion

    //region filterAsCollection
    private static <I> Collection<I> filterAsCollection(Collection<?> col, Class<I> classType) {
        Iterable<I> i = filterAsIterable(classType, col, null, null);
        try {
            Collection<I> result = new ArrayList<>();
            foreach(i, result::add);
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate a list filtered by super class type of elements on target list.
     *
     * @param col       list of elements are super then target classType.
     * @param classType super class type
     * @param <I>       element type (herintence from super class type)
     * @return new list with elements are instanceof classType
     */
    public static <I> Collection<I> filterAsCollectionS(Collection<? super I> col, Class<I> classType) {
        return filterAsCollection(col, classType);
    }

    /**
     * Generate a list filtered by extends class type of elements on target list.
     *
     * @param col       list of elements are extends then target classType.
     * @param classType extended class type
     * @param <I>       element type (herintence from extend class type)
     * @return new list with elements are instanceof classType
     */
    public static <I> Collection<I> filterAsCollectionE(Collection<? extends I> col, Class<I> classType) {
        return filterAsCollection(col, classType);
    }
    //endregion

    //region filterAsSet
    private static <I> Set<I> filterAsSet(Set<?> set, Class<I> classType) {
        Iterable<I> i = filterAsIterable(classType, set, null, null);
        try {
            Set<I> result = new HashSet<>();
            foreach(i, result::add);
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate a set filtered by super class type of elements on target set.
     *
     * @param set       set of elements are super then target classType.
     * @param classType super class type
     * @param <I>       element type (herintence from super class type)
     * @return new set with elements are instanceof classType
     */
    public static <I> Set<I> filterAsSetS(Set<? super I> set, Class<I> classType) {
        return filterAsSet(set, classType);
    }

    /**
     * Generate a set filtered by extends class type of elements on target set.
     *
     * @param set       set of elements are extends then target classType.
     * @param classType extended class type
     * @param <I>       element type (herintence from extend class type)
     * @return new set with elements are instanceof classType
     */
    public static <I> Set<I> filterAsSetE(Set<? extends I> set, Class<I> classType) {
        return filterAsSet(set, classType);
    }
    //endregion
    //endregion

    //region filter

    /**
     * Filter an iterator by condition
     *
     * @param iterator target
     * @param where    filter condition
     * @param <I>      element type
     * @return a new iterable result within elements filtered
     */
    public static <I> IterableResult<I> filter(Iterator<I> iterator, CompareEntryValid<I> where) {
        return IterableResultFactory.getInstanceForFilter(() -> iterator, where);
    }

    /**
     * Filter an iterable by condition
     *
     * @param col   target
     * @param where filter condition
     * @param <I>   element type
     * @return a new iterable result within elements filtered
     */
    public static <I> IterableResult<I> filter(Iterable<I> col, CompareEntryValid<I> where) {
        return IterableResultFactory.getInstanceForFilter(col::iterator, where);
    }

    /**
     * Filter an array by condition
     *
     * @param arr   target
     * @param where filter condition
     * @param <I>   element type
     * @return a new iterable result within elements filtered
     */
    public static <I> IterableResult<I> filter(I[] arr, CompareEntryValid<I> where) {
        return IterableResultFactory.getInstanceForFilterArray(arr, where);
    }
    //endregion

    //region nonNull

    /**
     * Filter an iterator recovering only non null elements.
     *
     * @param iterator target
     * @param <I>      element type
     * @return a new iterable result within elements filtered by non null condition.
     */
    public static <I> IterableResult<I> nonNull(Iterator<I> iterator) {
        return IterableResultFactory.getInstanceForFilter(() -> iterator, Objects::nonNull);
    }

    /**
     * Filter an collection recovering only non null elements.
     *
     * @param col target
     * @param <I> element type
     * @return a new iterable result within elements filtered by non null condition.
     */
    public static <I> IterableResult<I> nonNull(Iterable<I> col) {
        return IterableResultFactory.getInstanceForFilter(col::iterator, Objects::nonNull);
    }

    /**
     * Filter an array recovering only non null elements.
     *
     * @param arr target
     * @param <I> element type
     * @return a new iterable result within elements filtered by non null condition.
     */
    public static <I> IterableResult<I> nonNull(I[] arr) {
        return IterableResultFactory.getInstanceForFilterArray(arr, Objects::nonNull);
    }
    //endregion

    //region groupBy

    /**
     * Generate an iterable result grouping elements by groupfun key result.
     *
     * @param iterator target.
     * @param groupFun group function to get grouping key.
     * @param <K>      key type
     * @param <V>      value type
     * @return an instance of toMap whitin set values grouped by key.
     */
    public static <K, V> IterableResultGroup<K, V> groupBy(Iterator<V> iterator, FunctionMount<V, K> groupFun) {
        return IterableResultFactory.getInstanceForGroup(() -> iterator, groupFun);
    }

    /**
     * Generate an iterable result grouping elements by groupfun key result.
     *
     * @param col      target.
     * @param groupFun group function to get grouping key.
     * @param <K>      key type
     * @param <V>      value type
     * @return an instance of toMap whitin set values grouped by key.
     */
    public static <K, V> IterableResultGroup<K, V> groupBy(Iterable<V> col, FunctionMount<V, K> groupFun) {
        return IterableResultFactory.getInstanceForGroup(col::iterator, groupFun);
    }

    /**
     * Generate an iterable result grouping elements by groupfun key result.
     *
     * @param arr      target.
     * @param groupFun group function to get grouping key.
     * @param <K>      key type
     * @param <V>      value type
     * @return an instance of toMap whitin array values grouped by key.
     */
    public static <K, V> IterableResultGroup<K, V> groupBy(V[] arr, FunctionMount<V, K> groupFun) {
        return IterableResultFactory.getInstanceForGroupArray(arr, groupFun);
    }
    //endregion

    //region select

    /**
     * Generate an iterable result within a set of values recovered from mount function.
     *
     * @param iterator iterator target.
     * @param mount    mount function to get new data
     * @param <IN>     element type
     * @param <OUT>    element mounted from target iterator.
     * @return new iterable wihitin set of values from mount function.
     */
    public static <IN, OUT> IterableResult<OUT> select(Iterator<IN> iterator, FunctionMount<IN, OUT> mount) {
        return IterableResultFactory.getInstanceForSelect(() -> iterator, mount);
    }

    /**
     * Generate an iterable result within a set of values recovered from mount function.
     *
     * @param col   target
     * @param mount mount function to get new data
     * @param <IN>  element type
     * @param <OUT> element mounted from target iterator.
     * @return new iterable wihitin set of values from mount function.
     */
    public static <IN, OUT> IterableResult<OUT> select(Iterable<IN> col, FunctionMount<IN, OUT> mount) {
        return IterableResultFactory.getInstanceForSelect(col::iterator, mount);
    }

    /**
     * Generate an iterable result within a set of values recovered from mount function.
     *
     * @param arr   target
     * @param mount mount function to get new data
     * @param <IN>  element type
     * @param <OUT> element mounted from target iterator.
     * @return new iterable wihitin set of values from mount function.
     */
    public static <IN, OUT> IterableResult<OUT> select(IN[] arr, FunctionMount<IN, OUT> mount) {
        return IterableResultFactory.getInstanceForSelectArray(arr, mount);
    }
    //endregion

    //region merge

    /**
     * Union of two or more collections.
     *
     * @param curr current iterable base.
     * @param args one or more iterable whitin compatible elements with current iterable base.
     * @param <I>  iterable base type
     * @return new iterable result.
     */
    @SafeVarargs
    public static <I> IterableResult<I> merge(Iterable<I> curr, Iterable<? extends I>... args) {
        return IterableResultFactory.getInstanceForMerge(curr, args);
    }

    /**
     * Union of two or more collections.
     *
     * @param args one or more iterable.
     * @param <I>  iterable base type
     * @return new iterable result.
     */
    @SafeVarargs
    public static <I> IterableResult<I> merge(Iterable<? extends I>... args) {
        return IterableResultFactory.getInstanceForMerge(args);
    }

    /**
     * Union of two or more collections.
     *
     * @param arr  current array base.
     * @param args one or more iterable whitin compatible elements with current array base.
     * @param <I>  iterable base type
     * @return new iterable result.
     */
    @SafeVarargs
    public static <I> IterableResult<I> merge(I[] arr, Iterable<? extends I>... args) {
        return IterableResultFactory.getInstanceForMerge(arr, args);
    }

    /**
     * Union of two or more collections.
     *
     * @param args one or more arrays.
     * @param <I>  array base type
     * @return new iterable result.
     */
    @SafeVarargs
    public static <I> IterableResult<I> merge(I[]... args) {
        return IterableResultFactory.getInstanceForMergeArray(args);
    }

    /**
     * Union of two or more collections.
     *
     * @param iterable current iterable base.
     * @param args     one or more arrays whitin compatible elements with current iterable base.
     * @param <I>      iterable base type
     * @return new iterable result.
     */
    @SafeVarargs
    public static <I> IterableResult<I> merge(Iterable<I> iterable, I[]... args) {
        return IterableResultFactory.getInstanceForMergeArray(iterable, args);
    }
    //endregion

    //region intersection

    /**
     * Intersection of two or more collections.
     *
     * @param args one or more iterable.
     * @param <I>  iterable base type
     * @return new iterable result.
     */
    @SafeVarargs
    public static <I> IterableResult<I> intersection(Iterable<I>... args) {
        return IterableResultFactory.getInstanceForIntersection(args);
    }

    /**
     * Intersection of two or more collections.
     *
     * @param args one or more iterable.
     * @param <I>  iterable base type
     * @return new iterable result.
     */
    @SafeVarargs
    public static <I> IterableResult<I> intersection(I[]... args) {
        return IterableResultFactory.getInstanceForIntersectionArray(args);
    }
    //endregion

    //region reduce

    /**
     * Reduce method execute the reduceFun function to generate an accumulate result for each element on collection.
     *
     * @param iterator  target
     * @param reduceFun reduce function
     * @param acc       initial accumulate value, maybe null. When null first value of collection is the first accumulate.
     * @param <IN>      collection element type
     * @param <OUT>     accumulate type, indicate same IN type when accumulate start null.
     * @return final accumulate result.
     */
    public static <IN, OUT> OUT reduce(Iterator<IN> iterator, FunctionReduce<IN, OUT> reduceFun, OUT acc) {
        return IteratorForReduce.reduce(iterator, reduceFun, acc);
    }

    /**
     * Reduce method execute the reduceFun function to generate an accumulate result for each element on collection.
     * Starting accumulate null.
     *
     * @param iterator  target
     * @param reduceFun reduce function
     * @param <IN>      collection element type
     * @param <OUT>     accumulate type, indicate same IN type when accumulate start null.
     * @return final accumulate result.
     */
    public static <IN, OUT> OUT reduce(Iterator<IN> iterator, FunctionReduce<IN, OUT> reduceFun) {
        return IteratorForReduce.reduce(iterator, reduceFun, null);
    }

    /**
     * Reduce method execute the reduceFun function to generate an accumulate result for each element on collection.
     *
     * @param col       target
     * @param reduceFun reduce function
     * @param acc       initial accumulate value, maybe null. When null first value of collection is the first accumulate.
     * @param <IN>      collection element type
     * @param <OUT>     accumulate type, indicate same IN type when accumulate start null.
     * @return final accumulate result.
     */
    public static <IN, OUT> OUT reduce(Iterable<IN> col, FunctionReduce<IN, OUT> reduceFun, OUT acc) {
        return IteratorForReduce.reduce(col, reduceFun, acc);
    }

    /**
     * Reduce method execute the reduceFun function to generate an accumulate result for each element on collection.
     * Starting accumulate null.
     *
     * @param col       target
     * @param reduceFun reduce function
     * @param <IN>      collection element type
     * @param <OUT>     accumulate type, indicate same IN type when accumulate start null.
     * @return final accumulate result.
     */
    public static <IN, OUT> OUT reduce(Iterable<IN> col, FunctionReduce<IN, OUT> reduceFun) {
        return IteratorForReduce.reduce(col, reduceFun, null);
    }

    /**
     * Reduce method execute the reduceFun function to generate an accumulate result for each element on collection.
     *
     * @param arr       target
     * @param reduceFun reduce function
     * @param acc       initial accumulate value, maybe null. When null first value of collection is the first accumulate.
     * @param <IN>      collection element type
     * @param <OUT>     accumulate type, indicate same IN type when accumulate start null.
     * @return final accumulate result.
     */
    public static <IN, OUT> OUT reduce(IN[] arr, FunctionReduce<IN, OUT> reduceFun, OUT acc) {
        return IteratorForReduce.reduce(arr, reduceFun, acc);
    }

    /**
     * Reduce method execute the reduceFun function to generate an accumulate result for each element on collection.
     * Starting accumulate null.
     *
     * @param arr       target
     * @param reduceFun reduce function
     * @param <IN>      collection element type
     * @param <OUT>     accumulate type, indicate same IN type when accumulate start null.
     * @return final accumulate result.
     */
    public static <IN, OUT> OUT reduce(IN[] arr, FunctionReduce<IN, OUT> reduceFun) {
        return IteratorForReduce.reduce(arr, reduceFun);
    }
    //endregion

    //region math
    //region sum

    /**
     * Apply summation operation in a sequence of any kind of number.
     *
     * @param iterator target
     * @param fun      function to get a target number in element.
     * @param <OUT>    number type
     * @return summation result
     */
    public static <IN, OUT extends Number> OUT sum(Iterator<IN> iterator, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.sum(iterator, fun);
    }

    /**
     * Apply summation operation in a sequence of any kind of number.
     *
     * @param col   target
     * @param fun   function to get a target number in element.
     * @param <OUT> number type
     * @return summation result
     */
    public static <IN, OUT extends Number> OUT sum(Iterable<IN> col, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.sum(col.iterator(), fun);
    }

    /**
     * Apply summation operation in a sequence of any kind of number.
     *
     * @param arr   target
     * @param fun   function to get a target number in element.
     * @param <OUT> number type
     * @return summation result
     */

    public static <IN, OUT extends Number> OUT sum(IN[] arr, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.sum(arr, fun);
    }

    /**
     * Apply summation operation in a sequence of any kind of number.
     *
     * @param iterator target
     * @return summation result
     */
    public static <IN extends Number> IN sum(Iterator<IN> iterator) {
        return IteratorForMath.sum(iterator);
    }

    /**
     * Apply summation operation in a sequence of any kind of number.
     *
     * @param col target
     * @return summation result
     */
    public static <IN extends Number> IN sum(Iterable<IN> col) {
        return IteratorForMath.sum(col.iterator());
    }

    /**
     * Apply summation operation in a sequence of any kind of number.
     *
     * @param arr target
     * @return summation result
     */
    public static <IN extends Number> IN sum(IN[] arr) {
        return IteratorForMath.sum(arr);
    }
    //endregion

    //region average

    /**
     * <p>
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     * </p>
     * <img  src="data:image/png;base64,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" />
     * @param iterator target
     * @param fun      function to get target number
     * @param <IN>     element type
     * @param <OUT>    result type
     * @return result number
     */
    public static <IN, OUT extends Number> OUT average(Iterator<IN> iterator, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.average(iterator, fun);
    }

    /**
     * <p>
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     * </p>
     * <img  src="data:image/png;base64,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" />
     * @param col   target
     * @param fun   function to get target number
     * @param <IN>  element type
     * @param <OUT> result type
     * @return result number
     */
    public static <IN, OUT extends Number> OUT average(Iterable<IN> col, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.average(col.iterator(), fun);
    }

    /**
     * <p>
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     * </p>
     * <img  src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAWMAAABlCAYAAACY7IJIAAAACXBIWXMAAA7EAAAOxAGVKw4bAAATgElEQVR4nO3da1hU5doH8H/6ttsMDmU7RHKuy2ADEQQeOaWoI4giCqhocooUUd8Okplp5i5BPOUB0CIVUXeCqJwpRQU8pMaM4lWADgRv4yaHbEBLHWfGNtp6P5CTI6YMs4a1Bu7fJ1mwnnWD8J81z3oOTzAMw4AQQginenFdACGEEApjQgjhBQpjQgjhAQpjQgjhAQpjQgjhAQpjQgjhAQpjQgjhAQpjQgjhAQpjQgjhgf/hugBCCAEAjVqDstJS/M78DltbWxQXFWFmeDjc3N25Lq1LUBgTQnhBYCnApUuXAABOL74Ii79bQKPRcFxV16FuCkIIbxwrK4OdnR1cXFxwvLwcbm49464YoDAmhPCEXC5Hr969ERwaAqlEgkFDBkNgKeC6rC5DYUwI4YWTx08gKjoaAJCfkwut9jaUSiXHVXWdJ2gJTUIIHygUCohEIt2/n376aQiFQo6r6joUxoQQwgPUTUEI6TJSiaRHdT0Ygoa2EUK6jOyiDK9Hv2ZUG5s/+xTjAgJYqog/KIwJIV1mVuxsVJ47h2Pl5bpj0TExsLN74ZHnNTe3oKggH1eu/AwrKyuT1sgV6jMmhHQpjVqDMb6+UKlUAAAnJydkHzjw2GFscrkcQeMnYPeeL+Dl7d0VpXYp6jMmhHQpgaUAW9I+031cX1+P9O3bH3uevb09goODIbsoM2V5nKE7Y0IIJ4oLi7Bk8WIAwN8tLLB1+7bH3vHK5XJckl+Cn7/fY9tXKpVofuBh4b11Lh78nIVAAAcHB0O/BVbRnTEhhBPBoSGYGjYNAHBbq8Xbb7wJhULxyHPs7e07FMQA0NraiqKCQsyYFoblHyzTW+eitbUVWzZvxoxpYSgqKESvXtxHId0ZE0I4o9VqMWVyMBobGwEAAwcORPb+/ej7j2dZu8bYUaPw9DN9UVBcpDumUWuweNEiLHp/Mezt7Vm7ljG4fzkghPRYFhYW2LF7FwSCtod3jY2NWJmYwOo1fEaMQF1tre6BIQAkfPQRr4IYoDAmhHBMJBLhvfcX6z4uOVSC0qNHWWvfy6utH/ro4SMAgKSERMTEzuZVEAPUTUEI4Yl34+NRcqgEACAQCLB7zxesLCzf1NQE/zFi+I4ehcGDB2PCxIm8C2KAwpgQwiMhQZNQX18PABAKhThx6hQry2jea/dAXi5vdw6hbgpCCG98tm2rrv9YpVJ1aPxxd0FhTAjhDZFIhH+t+BhA28iKsBnTjW5z4/oNmBkRDgA49fXXRrdnKhTGhBBeOXTwIIRCIXIK8jFgwACj2lqy6D0MHjIE4ZGREAqF+O6771iqkn0UxoQQ3sjLycX5c5XYnbnH6IXllyx6D57e3rpJImKxGLWyWjbKNAkKY0IIL6Qmp2D5smVYvW4tXFxcjGorKXElAgIDMW16mO7YCF9fXG1pgVQiMbZUk6AwJoRwrrysHFvT0pC0ejXGT5jQ6XYUCgWiwiOg1WraTZu26W8DANiSullvAsj9577k6NTpaxuLhrYRQjglk8kQHR4Bf39/rNu4odPtlJeVY1dGhu7jt+MX6BYeysvJRVlpqS6EhUIhFi9dwqvxxhTGhBDONDU1ITbmdbi+/DI2piR3+LzsrCxYWvZBcGgIK3VotVrszcyEQCBAeGQkK20airopCCGciY15HU899RRWrlpl0HkH9h+A2G8sa3VYWFigvu57DBoyhLU2DdXjtl2qqa7GrowMXLr0H9jb22PGzFd1b2VqqquxNe1z/PTTTxgjFmOs31ijZ+s0NTUZPTzn65MnsX3rNqjVari4uiA2Lk739qq8rBz792XjluoW/Pz9IPbzY+Wtl1arxZUrV4xqS6vVojA/Hwe/Ogi1Wo1JkydhUnAwbGza+u6ys7JwYP8BWFpaImhSEEJCpxg920qj1kB1S6W7Bt/I5XL0t+lv1PdZU12NL4u/xLmzZ2Hdzxqxc+bAfdAgWFhYQC6XIyM9HbKLMri4uiAkdAo8vTxZ/A7YoVFr8E78Avz3v78hv6iowz8PjVqDVUkrcfv2baNHW9xPq9WivqHB6AeHxuhxYezm7o53Fy/GOPFYuLi66C1m7ebujhs3bmDpsg9Y29Yl4eOPsX3HDqPaGDV6NFqaW7B82TJMmjxJLyA9vTyxOjERKZ9uYXWa5/81NGDdmrXIzN7b6TYsLCwQHhmJ5uYWbE1Lw6q1a/RC0tKyDywtLfH59m2s/WGdOXMa0goJln/8ESvtse2jD5fr9WV2hpu7O16ws8OYkb7o37+/Xlv29vZoaWlBVHS03kgCvpk7Zw7OV1Zi4MCBmDtnTofOudrSoltqM3BiIKv1VFdVYdiwYVCpVKyGvCF6XBgDbbN8nJyccP5cpd7xpIREzIqNZXV/LWtra1bamTY9DOvWrMHFCxf0jietSMCWrZ9z+or+ONOmh2FrWhqkFRJdnSqVChcuXDAq7B/GysoKwm66YeX9hEIhhnkMx/lK/d/hvJxciMViXgdxanKKru7GxkZdwBpi4At2rNYkqZBAdfMmbty4wVkY99g+4+DQEDQ2NkL5x9YrhQUF8BkxosO7CHBhpO9ISL6p0H2c9umnmPfmG7wOYqDtxc/Wtj/Ky8oAtHXdLFq4EDGzXue2MDPn7eMDlUqlGzebmpwChULB2QOojsjOysLWtDSj2xFasRuYc+fPw7qNGyASiVht1xA98s4YAMYHBmLDJ+tx+utTAIBn+vbldRADQGBQEEoOlUAmkyE/JxcR0VG8GprzKD4jRiA/Nw8KhQKJK1ZgY3IyZ3cg3YWHhwcAoPTIUUgqJBCJRLy+IwaAQUOGoLahvtPnq1QqyC5ehHW/fixW1dalxrUee2csEonwnLU10rZsAQDeBzHQdicEAG/P/19MnR5mNkEM/LnAd0xEJJYuW0ZBzAI3d3cIhUIUF7VtJ8T3IAZg9Ls4oVAIL29vs/rd76gee2cMAE6Ojvjmm28QOHEiK+1NCW4/5lF55QqmXGx//IMPPzT4KbdQKIStbX88+be/sdo1UV5Wjk83b9Y7dvv2bTQrle2+p2f79kXGv3cbfI3+trYAgEFDBrP2h5SanIITx4/rHdNqNFBrNO2Ou7i6YNWaNaxct6PmzpmDluYWvWM/Njbi4+X/goVAf/TAWwsWdOqGYIBIhLraWlZWNyMcY3qo3AM5zHg/f8bZwZE5euSIya4TFxvLWlspm5J1NV++fJm1dh+muqqKiZwZzlp7CxcsYDyGDGU8hgxlrc2HOXrkCLNyRYJJr2GMyJnhjKSigpW2igoKmeCJQYyzgyOzNzOTlTYJd3pkN0VeTi7OSiRYv2kjAEBaYbqFQ9gaTZGXkwuRSIQ33noLgGlrZtuSRe/h1fBwiMViqFQqyOVyk12rp4ymkMlkOHH8GDL3ZQMALl36D7cFdbF7D967kx4XxtlZWVAoFFi3cQPc3N3xnLU1Tp86xXVZj1RYUIBn+vbFtOlh8PX1BQCcOc3vmoG2gfTvxscjIDAQXt7eCAhsGxv6YBcCMUx5WTl2bNuGVWvXtg1xGz6c14umP+jrkydRXFjU6fOLC4swP24uixXxA2t9xkkJiairq3vk1wx4/nmjFgIxVl5OLpqbWxC/8B3dsVd8fFBcXAy5XM7LhwJ5Obl6Iz36/uNZODk5oeRQCZJWr2FlfzBT+XDpUsyZN0/Xv+3n7wehUAhJRQVmx8ZyXJ15Ki8rx9GSEmxKTdUd8/P3xydr10KhUHA6NKujhg/3wN3f73b6/ODQEOzauZPFiviBtTCeOj0Mqps32WqOdXk5uSjIz283ycDT2xvFxcX4sqhYL6SBtl/81YmJKDz4FSdP/zPS01Ff9327FzAPT0/U19ejoqKi3UMfmUyG1SuTUP/99wgOCeFkJppKpULSigSMEY9t96DR6cUXcf5c5UNnOqUmp+Dc2bNQq9X4Iiuzy37mMpkM7y6Ix6L3F2NcQIDuuFKpxKJ3FsLZ2bndzzE1OQXFBQVI372ry17EpRIJ9u/Lbjej08vnz5XJHvwd5huFQoG8nFxMmx6m9/+bkZ6O48fav2Pq189a74WnW+O609rUykrLmNmvxTDODo5M6ORgprqqSvc5qUTKRM4MZ5wdHJnBbu5MyqZkRqFQ6J0/0ucVo65/uKTE4HP2ZmYycbGxjLODIxMXG6tXU0F+vu4hntjXl9mxfTujvqXWfb6ooJBhGIZR31IzoZODGalE2qm6f7l6jSkrLTP4vM+2bGFG+rzCODs4Mhs+Wa/X3o7t25mh7oN0/xcnT5zQfb6hoUH3fSxburTTD7l++OEHg8/dm5nJODs4MimbkvWONzQ0MM4OjsxQ90HtzgmdHMw4Ozga/P9bVlrG/PzzzwadI5VIdb8PD/4Ol5WWMQsXLNDVuWplEtPQ0GBQ+13tjXnzjW4jdHIwC5XwC2tLaEolEuzeuUt3xzM1bJruLkOhUGDT+vVobm7BgOefR0BgoFmM6wWAqPAI1qfsdpWkhESMGx/A6vTurpCRno7YuLguvWZNdfVD1/aQyWSwsrJq9/ZfpVLh8uXLvJ/9qFQqUV1VBSsrK7xgZ4d9e7MRN3cuZ91bKpUKa1evbjfMsLysHBdqatp9fZ8+lg/9XZgSHIKC4s73O/MRa90UXt7eeMHODhPGBWBi0ES9t3sikQi//fZfiMeKER4Ryet+zu5Co9bgl1+umVUQa9QaZO/NQm5OLmurz3XUXy2y9FdhKxQKeR/EAGBjY4OSgwcxRjwW169fx4njxxE0eRIcHBw4qeer4mKMGj263XE/fz+zuUEzFVZHU9jY2MDV1RWSM9/oHc/LycXUsDDExsVREHeR/fv2ISEpiesyDCKwFCA2Lg6vxbyG9WvXcV1Ot3H61GlYCCwwfsIE/H73LmdBDAAHvzqIutpHP+h/HJlMBq1GA4VCwVJV/MD60DYPT0/8dOUKZDIZgLYgBsxjunF3kZGeDi8fb7OdcvyymxuszLR2vpHL5bC1tcW4gABIJRJ4eHK7tvHa9Z8Y/ZDRxcUFh8tKzWLkiCFYnw49ZGjbSvmlR46i6ttvDd4apbiwCGr1rQ59rZePj8nfyl5taYFGrTGbO/riwiLI5XIIBAJUnDnD6TYyhqiprkZzcwv8/P1woaYG8958g+uSuoUTx4/r+lxPnzoNoZUVtFpthxfGyc7K6vC1xvr7P3ZR/+4WoGxiPYzv9Qdl7dmDyOhoxC80LAiuXbuGmx0cInf79m2D6zOEXC5HQtJK1NRUm03fq01/GwSH/Pni94Idu+u+mko/GxscKz+G67/+2qE/atIxY8Ri3Q3LSN+RGCASGbRCWfMDa2s8Smtrq8H1kT+ZZEPSqPAInK+sxInTp+iPihBCOoD1O+P739Z89+23GD9hAtuX6JCIV2dCq9Vycm1CzEVnV+EzhFKpNNvpy51dTa8zWL0zzsvJhUKhwOw5sfAcOgzRMTFYtvxDg9rwGzUaP1250qGvTVq9+i/XcK2proZGozHo2oT0NAKB4JF7J77k6NThtnbv+eIvu/Pu7UZiblxcXbvsQThrYXxvJbR7U3dDgiahV+/e3W5gNiGEmAIrQ9vuXwntnrH+/qirre2WS90RQgjbjA7jXRk7ce7s2XZjB73/WLxk395sYy9BCCHdXqcf4BUXFuHA/v04X1mJgQMHQiqR6PqLSo8eRX5uHgBga1oaVDdvImRK6CP7pgghpCczydA2QgghhulxO30QQggfURgTQggPUBgTQggPUBgTQggPUBiTLvHrtV+gUf85I1Iul+PXa79wWBHpDu6fx6DValFTXc1hNcahMCYmlZqcgpCgSXjF2xt3f78LlUqFqPAIBI2fgNmzZnFdHjFDKpUKqckpeMnRCRHTZwBoW3A+wM8fM6aFISkhkeMKO4fCmJhU/MJ34OHpiWHDhwMA3l/0HpZ8sBSRUVHQ0tohpBOEQqFukpnYzw9yuRz/ztiJ3IJ8AEBTUxOX5XUahTExudOnTsH5pZewJXUzNiYnw83dHXV1dfgnh9v/EPN2rzvCfdAgHD50COs2bsCdO3cAAKNGj+KytE6jMCYmJZPJ0NjYiB9/bMTb8QsgsBRAqVTifGUlZ8urEvN3VioFAFRXVeH1WbMBAIcPHQIATAoO5qwuY1AYE5OSVrQtnejl5aVbilB3zMc8dk8h/CP9I4ynTg/TbYkmlUoxRiw2270fKYyJSRUXFeE5a2tEREXpjh05fBjDhg+nXWBIp12oqkZ0TAxcXFwAABq1BufOnkPgxIkcV9Z5FMbEZJRKJepqa/HOwoV6+65JKiowZepUlJeVc1gdMVdSiQS/Xr+Ot+MX6I6VHDqEXk88AbHfWLNdyJ7CmJjMhZoLAPS7I6QSCTQaDWplMtjZm8dmqYRfSo8cxbDhw/W6I85KJLC2tsbOHRlwcXXlsLrO671ixYoVXBdBuqerLVfh4+MDD09P3bEnn3wSra13EBEdpdu1mBBDtN65g1GjR2OAaIDuWK/evWFpaYl58+ejT58+HFbXebSEJiGE8AB1UxBCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA/8P9QKpGcLKd6nAAAAAElFTkSuQmCC" />
     * @param arr   target
     * @param fun   function to get target number
     * @param <IN>  element type
     * @param <OUT> result type
     * @return result number
     */
    public static <IN, OUT extends Number> OUT average(IN[] arr, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.average(arr, fun);
    }

    /**
     * <p>
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     * </p>
     * <img  src="data:image/png;base64,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" />
     * @param iterator target
     * @param <IN>     element number type
     * @return result number
     */
    public static <IN extends Number> IN average(Iterator<IN> iterator) {
        return IteratorForMath.average(iterator);
    }

    /**
     * <p>
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     * </p>
     * <img  src="data:image/png;base64,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" />
     * @param col  target
     * @param <IN> element number type
     * @return result number
     */
    public static <IN extends Number> IN average(Iterable<IN> col) {
        return IteratorForMath.average(col.iterator());
    }

    /**
     * <p>
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     * </p>
     * <img  src="data:image/png;base64,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" />
     * @param arr  target
     * @param <IN> element number type
     * @return result number
     */
    public static <IN extends Number> IN average(IN[] arr) {
        return IteratorForMath.average(arr);
    }
    //endregion

    //region mean

    /**
     * <p>
     * A mean is a mathematical term, that describes the average of a sample.<br/>
     * In Statistics, the definition of the mean is similar to the average.<br/>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     * </p>
     * <p>
     * In Statistics, the total amplitude At of a set of values
     * is the difference between the highest and lowest value of the sample, as shown below:
     * </p>
     * <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHMAAAAwCAYAAAAij0UkAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAE1ElEQVR4nO2bL3DiWBzHv725GXCpo+7qwAWXutSlLnUPl3WpAxdkXeqoSx114FJzQ8XNsA5ccoqcShxxYVU49T0B2+W6ty2lsL158z4zzJDkPd6P93l/fgnDEUlCIQW/fHQAiv2hZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZErE7jKTW5xVj3Dame4xnG8skyG6F6c4OjrBRfcByRLIHq5wdtzARecO0/wgzW7PYor7zjlOjo5wenmDx2wVc6txjGari2G8+PkxcScKhqLOWq1CmAHnu33I66QBjYpGMSrXhzbt3uxQre1ARK8O/taekCTLyKclBofrj1fYTebMp2H4DH2dqDkc7zemDVIGRoWaGLH44I76EZFXJ35rc1KM2bZcjoqPi2UHmQVDodMZlSxHDjUY7KWvVCnH9CyTpvn9y/InL1ZNA4MVTafxwR31QyKPddSoGxa9SfmhobxdZuTTMHtMSTLt0cC3ZfAgpD0aqNDs/9/m5FdWS60mQn6sSvLXt+2wCwyvh2heT3EKAKdNNGtfMI0z4KKx/w0dQPb4Gbn2NxaPUyw/XaJ6kFaA5K6FzjDH8vmFahOd+1tcnvx3vWX8iOlCw5fPj4hxibMDxbcVb1IfedRrOi3bpm3btG2LugZW7MHLo3LHZbaMfFrOgFFgsqIJhm8a+nOGrqA3OuCMLsZsW22OJj511Oi+vGNsVuTIE2yH6V7DeYPMOQe2zvazfWHi1oi6x2h9nPY9BukeIttMKOYBzYpG8Rab5ZzpQffYOUPHoheVJGf0dbDmjresW3B+gNi2lllO2jTEgM9jKPomUbHYL0jOQ4q6QTcYrfbUnVl31NPAmTMwK9TscN1+yr5dpyFcCkunIXz2PIembrOfkmTEoO2z5wmK3ozlbEBRN+hHKUPXYRC9vyef3ybN/DpRc9eZfclZz2TddOgKk7rp0vfbFIZOJyxYpgMK3eX4aznLZdsxqdv9d/XbVjKLSUCh16g7A842J8d8zMDRWYFGwx1wVo7omD7fcydYRgN6QqdWt+mPUpJkOurRMTSiolP4q4ESeSbdMckypLBW97ppz6IISbJgUZAsB7StPguSRShoCo+99y67xYR9z2Zd0+kEE85JlrOQvqiv+sEJOJ6THLs0/RnJOfuWzUFJMvJotSckZ/TNtfixS2tdLrDeupX8m60SoOOzKwzjq+8vnJzj6j7G1f36OL9DftJYJUc7Um22cDNs4Wbj3OlFB/cXHdxvnIufKlRRxRLL1dsVywyPwylQjZGvU5Ljyw6a3S7Q+EEmsy3HZ/h084BPmwE2LtEdXqI73DiXPAWIp6ytegx8n2I9lau+cHUb9vtsNomBZuNgGee2LIbXGFZbaLXOnwZW/hDj/OYMD90HfMCDtp/CfmUen2L5eIv7gz+XzJFkObI4QRbHyPIYcZYhjnPkSYJqo4HF3RW6d5+R51P88fstrqcNnF+0cJZ00Rlm75oBr7NElmTI4xhZFiPOckzjDEkcI08S/PXnFHGeYBrn63LJ6jvkGbJk98iOSPVfE1lQP4FJhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkT8A7YI56A7YlkCAAAAAElFTkSuQmCC"/>
     * @param iterator target
     * @param fun      function to get target number
     * @param <IN>     collection element type
     * @param <OUT>    result number type
     * @return result number
     */
    public static <IN, OUT extends Number> OUT mean(Iterator<IN> iterator, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.mean(iterator, fun);
    }

    /**
     * <p>
     * A mean is a mathematical term, that describes the average of a sample.<br/>
     * In Statistics, the definition of the mean is similar to the average.<br/>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     * </p>
     * <p>
     * In Statistics, the total amplitude At of a set of values
     * is the difference between the highest and lowest value of the sample, as shown below:
     * </p>
     * <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHMAAAAwCAYAAAAij0UkAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAE1ElEQVR4nO2bL3DiWBzHv725GXCpo+7qwAWXutSlLnUPl3WpAxdkXeqoSx114FJzQ8XNsA5ccoqcShxxYVU49T0B2+W6ty2lsL158z4zzJDkPd6P93l/fgnDEUlCIQW/fHQAiv2hZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZErE7jKTW5xVj3Dame4xnG8skyG6F6c4OjrBRfcByRLIHq5wdtzARecO0/wgzW7PYor7zjlOjo5wenmDx2wVc6txjGari2G8+PkxcScKhqLOWq1CmAHnu33I66QBjYpGMSrXhzbt3uxQre1ARK8O/taekCTLyKclBofrj1fYTebMp2H4DH2dqDkc7zemDVIGRoWaGLH44I76EZFXJ35rc1KM2bZcjoqPi2UHmQVDodMZlSxHDjUY7KWvVCnH9CyTpvn9y/InL1ZNA4MVTafxwR31QyKPddSoGxa9SfmhobxdZuTTMHtMSTLt0cC3ZfAgpD0aqNDs/9/m5FdWS60mQn6sSvLXt+2wCwyvh2heT3EKAKdNNGtfMI0z4KKx/w0dQPb4Gbn2NxaPUyw/XaJ6kFaA5K6FzjDH8vmFahOd+1tcnvx3vWX8iOlCw5fPj4hxibMDxbcVb1IfedRrOi3bpm3btG2LugZW7MHLo3LHZbaMfFrOgFFgsqIJhm8a+nOGrqA3OuCMLsZsW22OJj511Oi+vGNsVuTIE2yH6V7DeYPMOQe2zvazfWHi1oi6x2h9nPY9BukeIttMKOYBzYpG8Rab5ZzpQffYOUPHoheVJGf0dbDmjresW3B+gNi2lllO2jTEgM9jKPomUbHYL0jOQ4q6QTcYrfbUnVl31NPAmTMwK9TscN1+yr5dpyFcCkunIXz2PIembrOfkmTEoO2z5wmK3ozlbEBRN+hHKUPXYRC9vyef3ybN/DpRc9eZfclZz2TddOgKk7rp0vfbFIZOJyxYpgMK3eX4aznLZdsxqdv9d/XbVjKLSUCh16g7A842J8d8zMDRWYFGwx1wVo7omD7fcydYRgN6QqdWt+mPUpJkOurRMTSiolP4q4ESeSbdMckypLBW97ppz6IISbJgUZAsB7StPguSRShoCo+99y67xYR9z2Zd0+kEE85JlrOQvqiv+sEJOJ6THLs0/RnJOfuWzUFJMvJotSckZ/TNtfixS2tdLrDeupX8m60SoOOzKwzjq+8vnJzj6j7G1f36OL9DftJYJUc7Um22cDNs4Wbj3OlFB/cXHdxvnIufKlRRxRLL1dsVywyPwylQjZGvU5Ljyw6a3S7Q+EEmsy3HZ/h084BPmwE2LtEdXqI73DiXPAWIp6ytegx8n2I9lau+cHUb9vtsNomBZuNgGee2LIbXGFZbaLXOnwZW/hDj/OYMD90HfMCDtp/CfmUen2L5eIv7gz+XzJFkObI4QRbHyPIYcZYhjnPkSYJqo4HF3RW6d5+R51P88fstrqcNnF+0cJZ00Rlm75oBr7NElmTI4xhZFiPOckzjDEkcI08S/PXnFHGeYBrn63LJ6jvkGbJk98iOSPVfE1lQP4FJhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkT8A7YI56A7YlkCAAAAAElFTkSuQmCC"/>
     * @param col   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return result number
     */
    public static <IN, OUT extends Number> OUT mean(Iterable<IN> col, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.mean(col.iterator(), fun);
    }

    /**
     * <p>
     * A mean is a mathematical term, that describes the average of a sample.<br/>
     * In Statistics, the definition of the mean is similar to the average.<br/>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     * </p>
     * <p>
     * In Statistics, the total amplitude At of a set of values
     * is the difference between the highest and lowest value of the sample, as shown below:
     * </p>
     * <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHMAAAAwCAYAAAAij0UkAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAE1ElEQVR4nO2bL3DiWBzHv725GXCpo+7qwAWXutSlLnUPl3WpAxdkXeqoSx114FJzQ8XNsA5ccoqcShxxYVU49T0B2+W6ty2lsL158z4zzJDkPd6P93l/fgnDEUlCIQW/fHQAiv2hZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZErE7jKTW5xVj3Dame4xnG8skyG6F6c4OjrBRfcByRLIHq5wdtzARecO0/wgzW7PYor7zjlOjo5wenmDx2wVc6txjGari2G8+PkxcScKhqLOWq1CmAHnu33I66QBjYpGMSrXhzbt3uxQre1ARK8O/taekCTLyKclBofrj1fYTebMp2H4DH2dqDkc7zemDVIGRoWaGLH44I76EZFXJ35rc1KM2bZcjoqPi2UHmQVDodMZlSxHDjUY7KWvVCnH9CyTpvn9y/InL1ZNA4MVTafxwR31QyKPddSoGxa9SfmhobxdZuTTMHtMSTLt0cC3ZfAgpD0aqNDs/9/m5FdWS60mQn6sSvLXt+2wCwyvh2heT3EKAKdNNGtfMI0z4KKx/w0dQPb4Gbn2NxaPUyw/XaJ6kFaA5K6FzjDH8vmFahOd+1tcnvx3vWX8iOlCw5fPj4hxibMDxbcVb1IfedRrOi3bpm3btG2LugZW7MHLo3LHZbaMfFrOgFFgsqIJhm8a+nOGrqA3OuCMLsZsW22OJj511Oi+vGNsVuTIE2yH6V7DeYPMOQe2zvazfWHi1oi6x2h9nPY9BukeIttMKOYBzYpG8Rab5ZzpQffYOUPHoheVJGf0dbDmjresW3B+gNi2lllO2jTEgM9jKPomUbHYL0jOQ4q6QTcYrfbUnVl31NPAmTMwK9TscN1+yr5dpyFcCkunIXz2PIembrOfkmTEoO2z5wmK3ozlbEBRN+hHKUPXYRC9vyef3ybN/DpRc9eZfclZz2TddOgKk7rp0vfbFIZOJyxYpgMK3eX4aznLZdsxqdv9d/XbVjKLSUCh16g7A842J8d8zMDRWYFGwx1wVo7omD7fcydYRgN6QqdWt+mPUpJkOurRMTSiolP4q4ESeSbdMckypLBW97ppz6IISbJgUZAsB7StPguSRShoCo+99y67xYR9z2Zd0+kEE85JlrOQvqiv+sEJOJ6THLs0/RnJOfuWzUFJMvJotSckZ/TNtfixS2tdLrDeupX8m60SoOOzKwzjq+8vnJzj6j7G1f36OL9DftJYJUc7Um22cDNs4Wbj3OlFB/cXHdxvnIufKlRRxRLL1dsVywyPwylQjZGvU5Ljyw6a3S7Q+EEmsy3HZ/h084BPmwE2LtEdXqI73DiXPAWIp6ytegx8n2I9lau+cHUb9vtsNomBZuNgGee2LIbXGFZbaLXOnwZW/hDj/OYMD90HfMCDtp/CfmUen2L5eIv7gz+XzJFkObI4QRbHyPIYcZYhjnPkSYJqo4HF3RW6d5+R51P88fstrqcNnF+0cJZ00Rlm75oBr7NElmTI4xhZFiPOckzjDEkcI08S/PXnFHGeYBrn63LJ6jvkGbJk98iOSPVfE1lQP4FJhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkT8A7YI56A7YlkCAAAAAElFTkSuQmCC"/>
     * @param arr   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return result number
     */
    public static <IN, OUT extends Number> OUT mean(IN[] arr, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.mean(arr, fun);
    }

    /**
     * <p>
     * A mean is a mathematical term, that describes the average of a sample.<br/>
     * In Statistics, the definition of the mean is similar to the average.<br/>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     * </p>
     * <p>
     * In Statistics, the total amplitude At of a set of values
     * is the difference between the highest and lowest value of the sample, as shown below:
     * </p>
     * <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHMAAAAwCAYAAAAij0UkAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAE1ElEQVR4nO2bL3DiWBzHv725GXCpo+7qwAWXutSlLnUPl3WpAxdkXeqoSx114FJzQ8XNsA5ccoqcShxxYVU49T0B2+W6ty2lsL158z4zzJDkPd6P93l/fgnDEUlCIQW/fHQAiv2hZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZErE7jKTW5xVj3Dame4xnG8skyG6F6c4OjrBRfcByRLIHq5wdtzARecO0/wgzW7PYor7zjlOjo5wenmDx2wVc6txjGari2G8+PkxcScKhqLOWq1CmAHnu33I66QBjYpGMSrXhzbt3uxQre1ARK8O/taekCTLyKclBofrj1fYTebMp2H4DH2dqDkc7zemDVIGRoWaGLH44I76EZFXJ35rc1KM2bZcjoqPi2UHmQVDodMZlSxHDjUY7KWvVCnH9CyTpvn9y/InL1ZNA4MVTafxwR31QyKPddSoGxa9SfmhobxdZuTTMHtMSTLt0cC3ZfAgpD0aqNDs/9/m5FdWS60mQn6sSvLXt+2wCwyvh2heT3EKAKdNNGtfMI0z4KKx/w0dQPb4Gbn2NxaPUyw/XaJ6kFaA5K6FzjDH8vmFahOd+1tcnvx3vWX8iOlCw5fPj4hxibMDxbcVb1IfedRrOi3bpm3btG2LugZW7MHLo3LHZbaMfFrOgFFgsqIJhm8a+nOGrqA3OuCMLsZsW22OJj511Oi+vGNsVuTIE2yH6V7DeYPMOQe2zvazfWHi1oi6x2h9nPY9BukeIttMKOYBzYpG8Rab5ZzpQffYOUPHoheVJGf0dbDmjresW3B+gNi2lllO2jTEgM9jKPomUbHYL0jOQ4q6QTcYrfbUnVl31NPAmTMwK9TscN1+yr5dpyFcCkunIXz2PIembrOfkmTEoO2z5wmK3ozlbEBRN+hHKUPXYRC9vyef3ybN/DpRc9eZfclZz2TddOgKk7rp0vfbFIZOJyxYpgMK3eX4aznLZdsxqdv9d/XbVjKLSUCh16g7A842J8d8zMDRWYFGwx1wVo7omD7fcydYRgN6QqdWt+mPUpJkOurRMTSiolP4q4ESeSbdMckypLBW97ppz6IISbJgUZAsB7StPguSRShoCo+99y67xYR9z2Zd0+kEE85JlrOQvqiv+sEJOJ6THLs0/RnJOfuWzUFJMvJotSckZ/TNtfixS2tdLrDeupX8m60SoOOzKwzjq+8vnJzj6j7G1f36OL9DftJYJUc7Um22cDNs4Wbj3OlFB/cXHdxvnIufKlRRxRLL1dsVywyPwylQjZGvU5Ljyw6a3S7Q+EEmsy3HZ/h084BPmwE2LtEdXqI73DiXPAWIp6ytegx8n2I9lau+cHUb9vtsNomBZuNgGee2LIbXGFZbaLXOnwZW/hDj/OYMD90HfMCDtp/CfmUen2L5eIv7gz+XzJFkObI4QRbHyPIYcZYhjnPkSYJqo4HF3RW6d5+R51P88fstrqcNnF+0cJZ00Rlm75oBr7NElmTI4xhZFiPOckzjDEkcI08S/PXnFHGeYBrn63LJ6jvkGbJk98iOSPVfE1lQP4FJhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkT8A7YI56A7YlkCAAAAAElFTkSuQmCC"/>
     * @param iterator target
     * @param <IN>     collection element number type
     * @return result number
     */
    public static <IN extends Number> IN mean(Iterator<IN> iterator) {
        return IteratorForMath.mean(iterator);
    }

    /**
     * <p>
     * A mean is a mathematical term, that describes the average of a sample.<br/>
     * In Statistics, the definition of the mean is similar to the average.<br/>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     * </p>
     * <p>
     * In Statistics, the total amplitude At of a set of values
     * is the difference between the highest and lowest value of the sample, as shown below:
     * </p>
     * <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHMAAAAwCAYAAAAij0UkAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAE1ElEQVR4nO2bL3DiWBzHv725GXCpo+7qwAWXutSlLnUPl3WpAxdkXeqoSx114FJzQ8XNsA5ccoqcShxxYVU49T0B2+W6ty2lsL158z4zzJDkPd6P93l/fgnDEUlCIQW/fHQAiv2hZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZErE7jKTW5xVj3Dame4xnG8skyG6F6c4OjrBRfcByRLIHq5wdtzARecO0/wgzW7PYor7zjlOjo5wenmDx2wVc6txjGari2G8+PkxcScKhqLOWq1CmAHnu33I66QBjYpGMSrXhzbt3uxQre1ARK8O/taekCTLyKclBofrj1fYTebMp2H4DH2dqDkc7zemDVIGRoWaGLH44I76EZFXJ35rc1KM2bZcjoqPi2UHmQVDodMZlSxHDjUY7KWvVCnH9CyTpvn9y/InL1ZNA4MVTafxwR31QyKPddSoGxa9SfmhobxdZuTTMHtMSTLt0cC3ZfAgpD0aqNDs/9/m5FdWS60mQn6sSvLXt+2wCwyvh2heT3EKAKdNNGtfMI0z4KKx/w0dQPb4Gbn2NxaPUyw/XaJ6kFaA5K6FzjDH8vmFahOd+1tcnvx3vWX8iOlCw5fPj4hxibMDxbcVb1IfedRrOi3bpm3btG2LugZW7MHLo3LHZbaMfFrOgFFgsqIJhm8a+nOGrqA3OuCMLsZsW22OJj511Oi+vGNsVuTIE2yH6V7DeYPMOQe2zvazfWHi1oi6x2h9nPY9BukeIttMKOYBzYpG8Rab5ZzpQffYOUPHoheVJGf0dbDmjresW3B+gNi2lllO2jTEgM9jKPomUbHYL0jOQ4q6QTcYrfbUnVl31NPAmTMwK9TscN1+yr5dpyFcCkunIXz2PIembrOfkmTEoO2z5wmK3ozlbEBRN+hHKUPXYRC9vyef3ybN/DpRc9eZfclZz2TddOgKk7rp0vfbFIZOJyxYpgMK3eX4aznLZdsxqdv9d/XbVjKLSUCh16g7A842J8d8zMDRWYFGwx1wVo7omD7fcydYRgN6QqdWt+mPUpJkOurRMTSiolP4q4ESeSbdMckypLBW97ppz6IISbJgUZAsB7StPguSRShoCo+99y67xYR9z2Zd0+kEE85JlrOQvqiv+sEJOJ6THLs0/RnJOfuWzUFJMvJotSckZ/TNtfixS2tdLrDeupX8m60SoOOzKwzjq+8vnJzj6j7G1f36OL9DftJYJUc7Um22cDNs4Wbj3OlFB/cXHdxvnIufKlRRxRLL1dsVywyPwylQjZGvU5Ljyw6a3S7Q+EEmsy3HZ/h084BPmwE2LtEdXqI73DiXPAWIp6ytegx8n2I9lau+cHUb9vtsNomBZuNgGee2LIbXGFZbaLXOnwZW/hDj/OYMD90HfMCDtp/CfmUen2L5eIv7gz+XzJFkObI4QRbHyPIYcZYhjnPkSYJqo4HF3RW6d5+R51P88fstrqcNnF+0cJZ00Rlm75oBr7NElmTI4xhZFiPOckzjDEkcI08S/PXnFHGeYBrn63LJ6jvkGbJk98iOSPVfE1lQP4FJhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkT8A7YI56A7YlkCAAAAAElFTkSuQmCC"/>
     * @param col  function to get target number
     * @param <IN> collection element number type
     * @return result number
     */
    public static <IN extends Number> IN mean(Iterable<IN> col) {
        return IteratorForMath.mean(col.iterator());
    }

    /**
     * <p>
     * A mean is a mathematical term, that describes the average of a sample.<br/>
     * In Statistics, the definition of the mean is similar to the average.<br/>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     * </p>
     * <p>
     * In Statistics, the total amplitude At of a set of values
     * is the difference between the highest and lowest value of the sample, as shown below:
     * </p>
     * <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHMAAAAwCAYAAAAij0UkAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAE1ElEQVR4nO2bL3DiWBzHv725GXCpo+7qwAWXutSlLnUPl3WpAxdkXeqoSx114FJzQ8XNsA5ccoqcShxxYVU49T0B2+W6ty2lsL158z4zzJDkPd6P93l/fgnDEUlCIQW/fHQAiv2hZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZErE7jKTW5xVj3Dame4xnG8skyG6F6c4OjrBRfcByRLIHq5wdtzARecO0/wgzW7PYor7zjlOjo5wenmDx2wVc6txjGari2G8+PkxcScKhqLOWq1CmAHnu33I66QBjYpGMSrXhzbt3uxQre1ARK8O/taekCTLyKclBofrj1fYTebMp2H4DH2dqDkc7zemDVIGRoWaGLH44I76EZFXJ35rc1KM2bZcjoqPi2UHmQVDodMZlSxHDjUY7KWvVCnH9CyTpvn9y/InL1ZNA4MVTafxwR31QyKPddSoGxa9SfmhobxdZuTTMHtMSTLt0cC3ZfAgpD0aqNDs/9/m5FdWS60mQn6sSvLXt+2wCwyvh2heT3EKAKdNNGtfMI0z4KKx/w0dQPb4Gbn2NxaPUyw/XaJ6kFaA5K6FzjDH8vmFahOd+1tcnvx3vWX8iOlCw5fPj4hxibMDxbcVb1IfedRrOi3bpm3btG2LugZW7MHLo3LHZbaMfFrOgFFgsqIJhm8a+nOGrqA3OuCMLsZsW22OJj511Oi+vGNsVuTIE2yH6V7DeYPMOQe2zvazfWHi1oi6x2h9nPY9BukeIttMKOYBzYpG8Rab5ZzpQffYOUPHoheVJGf0dbDmjresW3B+gNi2lllO2jTEgM9jKPomUbHYL0jOQ4q6QTcYrfbUnVl31NPAmTMwK9TscN1+yr5dpyFcCkunIXz2PIembrOfkmTEoO2z5wmK3ozlbEBRN+hHKUPXYRC9vyef3ybN/DpRc9eZfclZz2TddOgKk7rp0vfbFIZOJyxYpgMK3eX4aznLZdsxqdv9d/XbVjKLSUCh16g7A842J8d8zMDRWYFGwx1wVo7omD7fcydYRgN6QqdWt+mPUpJkOurRMTSiolP4q4ESeSbdMckypLBW97ppz6IISbJgUZAsB7StPguSRShoCo+99y67xYR9z2Zd0+kEE85JlrOQvqiv+sEJOJ6THLs0/RnJOfuWzUFJMvJotSckZ/TNtfixS2tdLrDeupX8m60SoOOzKwzjq+8vnJzj6j7G1f36OL9DftJYJUc7Um22cDNs4Wbj3OlFB/cXHdxvnIufKlRRxRLL1dsVywyPwylQjZGvU5Ljyw6a3S7Q+EEmsy3HZ/h084BPmwE2LtEdXqI73DiXPAWIp6ytegx8n2I9lau+cHUb9vtsNomBZuNgGee2LIbXGFZbaLXOnwZW/hDj/OYMD90HfMCDtp/CfmUen2L5eIv7gz+XzJFkObI4QRbHyPIYcZYhjnPkSYJqo4HF3RW6d5+R51P88fstrqcNnF+0cJZ00Rlm75oBr7NElmTI4xhZFiPOckzjDEkcI08S/PXnFHGeYBrn63LJ6jvkGbJk98iOSPVfE1lQP4FJhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkT8A7YI56A7YlkCAAAAAElFTkSuQmCC"/>
     * @param arr  target
     * @param <IN> collection element number type
     * @return result number
     */
    public static <IN extends Number> IN mean(IN[] arr) {
        return IteratorForMath.mean(arr);
    }
    //endregion

    //region min

    /**
     * Recover minimum value of collection
     *
     * @param iterator target
     * @param fun      function to get Comparable element target
     * @param <IN>     collection element
     * @param <OUT>    result comparable element
     * @return minimal value
     */
    public static <IN, OUT extends Comparable<OUT>> OUT min(Iterator<IN> iterator, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.min(iterator, fun);
    }

    /**
     * Recover minimum value of collection
     *
     * @param <OUT> result comparable element
     * @return minimal value
     */
    public static <IN, OUT extends Comparable<OUT>> OUT min(Iterable<IN> col, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.min(col.iterator(), fun);
    }

    /**
     * Recover minimum value of collection
     *
     * @param arr   target
     * @param fun   function to get Comparable element target
     * @param <IN>  collection element
     * @param <OUT> result comparable element
     * @return minimum value
     */
    public static <IN, OUT extends Comparable<OUT>> OUT min(IN[] arr, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.min(arr, fun);
    }

    /**
     * Recover minimum value of collection
     *
     * @param iterator target
     * @param <E>      collection comparable element
     * @return minimum value
     */
    public static <E extends Comparable<E>> E min(Iterator<E> iterator) {
        return IteratorForMath.min(iterator);
    }

    /**
     * Recover minimum value of collection
     *
     * @param col target
     * @param <E> collection comparable element
     * @return minimum value
     */
    public static <E extends Comparable<E>> E min(Iterable<E> col) {
        return IteratorForMath.min(col.iterator());
    }

    /**
     * Recover minimum value of collection
     *
     * @param arr target
     * @param <E> collection comparable element
     * @return minimum value
     */
    public static <E extends Comparable<E>> E min(E[] arr) {
        return IteratorForMath.min(arr);
    }
    //endregion

    //region max

    /**
     * Recover maximum value of collection
     *
     * @param iterator target
     * @param fun      function to get Comparable element target
     * @param <IN>     collection element
     * @param <OUT>    result comparable element
     * @return maximum value
     */
    public static <IN, OUT extends Comparable<OUT>> OUT max(Iterator<IN> iterator, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.max(iterator, fun);
    }

    /**
     * Recover maximum value of collection
     *
     * @param col   target
     * @param fun   function to get Comparable element target
     * @param <IN>  collection element
     * @param <OUT> result comparable element
     * @return maximum value
     */
    public static <IN, OUT extends Comparable<OUT>> OUT max(Iterable<IN> col, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.max(col.iterator(), fun);
    }

    /**
     * Recover maximum value of collection
     *
     * @param arr   target
     * @param fun   function to get Comparable element target
     * @param <IN>  collection element
     * @param <OUT> result comparable element
     * @return maximum value
     */
    public static <IN, OUT extends Comparable<OUT>> OUT max(IN[] arr, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.max(arr, fun);
    }

    /**
     * Recover maximum value of collection
     *
     * @param iterator target
     * @param <E>      collection comparable element
     * @return maximum value
     */
    public static <E extends Comparable<E>> E max(Iterator<E> iterator) {
        return IteratorForMath.max(iterator);
    }

    /**
     * Recover maximum value of collection
     *
     * @param col target
     * @param <E> collection comparable element
     * @return maximum value
     */
    public static <E extends Comparable<E>> E max(Iterable<E> col) {
        return IteratorForMath.max(col.iterator());
    }

    /**
     * Recover maximum value of collection
     *
     * @param arr target
     * @param <E> collection comparable element
     * @return maximum value
     */
    public static <E extends Comparable<E>> E max(E[] arr) {
        return IteratorForMath.max(arr);
    }
    //endregion
    //endregion

    //region distinct

    /**
     * Recover distinct (non duplicated) element of collection
     *
     * @param iterator target
     * @param mount    function to mount output distinct value
     * @param <OUT>    output type
     * @return new iterable result with distinct elements.
     */
    public static <IN, OUT> IterableResult<OUT> distinct(Iterator<IN> iterator, FunctionMount<IN, OUT> mount) {
        return IterableResultFactory.getInstanceForDistinct(() -> iterator, mount);
    }

    /**
     * Recover distinct (non duplicated) element of collection
     *
     * @param col   target
     * @param mount function to mount output distinct value
     * @param <OUT> output type
     * @return new iterable result with distinct elements.
     */
    public static <IN, OUT> IterableResult<OUT> distinct(Iterable<IN> col, FunctionMount<IN, OUT> mount) {
        return IterableResultFactory.getInstanceForDistinct(col::iterator, mount);
    }

    /**
     * Recover distinct (non duplicated) element of collection
     *
     * @param arr   target
     * @param mount function to mount output distinct value
     * @param <OUT> output type
     * @return new iterable result with distinct elements.
     */
    public static <IN, OUT> IterableResult<OUT> distinct(IN[] arr, FunctionMount<IN, OUT> mount) {
        return IterableResultFactory.getInstanceForDistinctArray(arr, mount);
    }

    /**
     * Recover distinct (non duplicated) element of collection
     *
     * @param iterator target
     * @return new iterable result with distinct elements.
     */
    public static <E> IterableResult<E> distinct(Iterator<E> iterator) {
        return distinct(iterator, i -> i);
    }

    /**
     * Recover distinct (non duplicated) element of collection
     *
     * @param col target
     * @return new iterable result with distinct elements.
     */
    public static <E> IterableResult<E> distinct(Iterable<E> col) {
        return distinct(col, i -> i);
    }

    /**
     * Recover distinct (non duplicated) element of collection
     *
     * @param arr target
     * @return new iterable result with distinct elements.
     */
    public static <E> IterableResult<E> distinct(E[] arr) {
        return distinct(arr, i -> i);
    }
    //endregion

    //region foreach

    /**
     * A simple foreach action.
     *
     * @param list   target
     * @param action action to recover each element on collection
     * @param <I>    collection element type
     */
    public static <I> void foreach(Iterable<I> list, ForEachEntryConsumer<I> action) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(action);
        for (I e : list) action.accept(e);
    }

    /**
     * A simple foreach action.
     *
     * @param list   target
     * @param action action with entry index to recover each element on collection
     * @param <I>    collection element type
     */
    public static <I> void foreach(Iterable<I> list, ForEachIterableEntryConsumer<I> action) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(action);
        int i = 0;
        for (I e : list) action.accept(e, i++);
    }

    /**
     * A simple foreach action.
     *
     * @param arr    target
     * @param action action to recover each element on collection
     * @param <I>    collection element type
     */
    public static <I> void foreach(I[] arr, ForEachEntryConsumer<I> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (I e : arr) action.accept(e);
    }

    /**
     * A simple foreach action.
     *
     * @param arr    target
     * @param action action with entry index to recover each element on collection
     * @param <I>    collection element type
     */
    public static <I> void foreach(I[] arr, ForEachIterableEntryConsumer<I> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (int i = 0, l = arr.length; i < l; i++) action.accept(arr[i], i);
    }
    //endregion

    //region all

    /**
     * Check if all elements on iterable pass on test action.
     *
     * @param list   target
     * @param action check pass action
     * @return return true when all elements on collection pass on test action.
     */
    public static <I> boolean all(Iterable<I> list, CompareEntryValid<I> action) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(action);
        for (I e : list) {
            if (!action.isValid(e)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if all elements on iterable pass on test action.
     *
     * @param arr    target
     * @param action check pass action
     * @return return true when all elements on collection pass on test action.
     */
    public static <I> boolean all(I[] arr, CompareEntryValid<I> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (I e : arr) {
            if (!action.isValid(e)) {
                return false;
            }
        }
        return true;
    }
    //endregion

    //region any

    /**
     * Check if at least one element on iterable pass on test action.
     *
     * @param list   target
     * @param action check pass action
     * @return return true when at least one element on collection pass on test action.
     */
    public static <I> boolean any(Iterable<I> list, CompareEntryValid<I> action) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(action);
        for (I e : list) {
            if (action.isValid(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if at least one element on iterable pass on test action.
     *
     * @param arr    target
     * @param action check pass action
     * @return return true when at least one element on collection pass on test action.
     */
    public static <I> boolean any(I[] arr, CompareEntryValid<I> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (I e : arr) {
            if (action.isValid(e)) {
                return true;
            }
        }
        return false;
    }
    //endregion

    //region jump

    /**
     * How like literally named, "jump" elements on collection
     * returning all others elements after offset count it.
     *
     * @param set   target
     * @param count count of elements will be discarted
     * @param <I>   element type
     * @return new iterable result with non elements after offset count.
     */
    public static <I> IterableResult<I> jump(Iterable<I> set, int count) {
        return IterableResultFactory.getInstanceForJump(set::iterator, count);
    }

    /**
     * How like literally named, "jump" elements on collection
     * returning all others elements after offset count it.
     *
     * @param iterator target
     * @param count    count of elements will be discarted
     * @param <I>      element type
     * @return new iterable result with non elements after offset count.
     */
    public static <I> IterableResult<I> jump(final Iterator<I> iterator, int count) {
        return IterableResultFactory.getInstanceForJump(() -> iterator, count);
    }
    //endregion

    //region take

    /**
     * Take only amount of elements set on count.
     *
     * @param set   target
     * @param count count of elements
     * @param <I>   element type
     * @return new iterable result with taked elements.
     */
    public static <I> IterableResult<I> take(Iterable<I> set, int count) {
        return IterableResultFactory.getInstanceForTake(set::iterator, count);
    }

    /**
     * Take only amount of elements set on count.
     *
     * @param iterator target
     * @param count    count of elements
     * @param <I>      element type
     * @return new iterable result with taked elements.
     */

    public static <I> IterableResult<I> take(final Iterator<I> iterator, int count) {
        return IterableResultFactory.getInstanceForTake(() -> iterator, count);
    }
    //endregion

    //region count

    /**
     * Count of elements on iterable.
     *
     * @param set target
     * @return total elements on iterable.
     */
    public static int count(Iterable<?> set) {
        if (set instanceof Collection) {
            return ((Collection<?>) set).size();
        } else if (set instanceof Map) {
            return ((Map<?, ?>) set).size();
        } else {
            int c = 0;
            for (Object ignored : set) c++;
            return c;
        }
    }

    /**
     * Count element by condition.
     * @param set target
     * @param where condition to accept count element
     * @param <E> element type
     * @return count of element into condition.
     */
    public static <E> int count(Iterable<E> set, CompareEntryValid<E> where) {
        Objects.requireNonNull(set);
        Objects.requireNonNull(where);
        int count = 0;
        for(E e : set) {
            if(where.isValid(e)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count element by condition.
     * @param arr target
     * @param where condition to accept count element
     * @param <E> element type
     * @return count of element into condition.
     */
    public static <E> int count(E[] arr, CompareEntryValid<E> where) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(where);
        int count = 0;
        for(E e : arr) {
            if(where.isValid(e)) {
                count++;
            }
        }
        return count;
    }
    //endregion

    //region join

    /**
     * Join all values how unique string, wheter value is a collection, set or array bring up theses datas
     * to same level of current datas and join it, otherwise, set simple objects toString and join it too.
     * @param prefix prefix of string result
     * @param suffix sufix of string result
     * @param separator data separator
     * @param args target arguments
     * @return string result
     */
    public static String join(String prefix, String suffix, String separator, Object... args) {
        return IteratorForJoin.join(prefix, suffix, separator, args);
    }

    /**
     * Join all values how unique string, wheter value is a collection, set or array bring up theses datas
     * to same level of current datas and join it, otherwise, set simple objects toString and join it too.
     * @param separator data separator
     * @param args target arguments
     * @return string result
     */
    public static String join(String separator, Object... args) {
        return IteratorForJoin.join(null, null, separator, args);
    }

    /**
     * Join all values how unique string, wheter value is a collection, set or array bring up theses datas
     * to same level of current datas and join it, otherwise, set simple objects toString and join it too.
     * @param args target arguments
     * @return string result
     */
    public static String join(Object... args) {
        return IteratorForJoin.join(null, null, ", ", args);
    }
    //endregion

    //region for primitives
    //region foreach
    public static void foreach(char[] arr, ForEachEntryConsumer<Character> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (char e : arr) action.accept(e);
    }

    public static void foreach(char[] arr, ForEachIterableEntryConsumer<Character> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (int i = 0, l = arr.length; i < l; i++) {
            action.accept(arr[i], i);
        }
    }

    public static void foreach(int[] arr, ForEachEntryConsumer<Integer> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (int e : arr) action.accept(e);
    }

    public static void foreach(int[] arr, ForEachIterableEntryConsumer<Integer> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (int i = 0, l = arr.length; i < l; i++) {
            action.accept(arr[i], i);
        }
    }

    public static void foreach(long[] arr, ForEachEntryConsumer<Long> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (long e : arr) action.accept(e);
    }

    public static void foreach(long[] arr, ForEachIterableEntryConsumer<Long> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (int i = 0, l = arr.length; i < l; i++) {
            action.accept(arr[i], i);
        }
    }

    public static void foreach(double[] arr, ForEachEntryConsumer<Double> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (double e : arr) action.accept(e);
    }

    public static void foreach(double[] arr, ForEachIterableEntryConsumer<Double> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (int i = 0, l = arr.length; i < l; i++) {
            action.accept(arr[i], i);
        }
    }

    public static void foreach(boolean[] arr, ForEachEntryConsumer<Boolean> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (boolean e : arr) action.accept(e);
    }

    public static void foreach(boolean[] arr, ForEachIterableEntryConsumer<Boolean> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (int i = 0, l = arr.length; i < l; i++) {
            action.accept(arr[i], i);
        }
    }

    public static void foreach(byte[] arr, ForEachEntryConsumer<Byte> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (byte e : arr) action.accept(e);
    }

    public static void foreach(byte[] arr, ForEachIterableEntryConsumer<Byte> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (int i = 0, l = arr.length; i < l; i++) {
            action.accept(arr[i], i);
        }
    }
    //endregion

    //region all
    public static boolean all(char[] arr, CompareEntryValid<Character> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (char e : arr) {
            if (!action.isValid(e)) {
                return false;
            }
        }
        return true;
    }

    public static boolean all(int[] arr, CompareEntryValid<Integer> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (int e : arr) {
            if (!action.isValid(e)) {
                return false;
            }
        }
        return true;
    }

    public static boolean all(long[] arr, CompareEntryValid<Long> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (long e : arr) {
            if (!action.isValid(e)) {
                return false;
            }
        }
        return true;
    }

    public static boolean all(double[] arr, CompareEntryValid<Double> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (double e : arr) {
            if (!action.isValid(e)) {
                return false;
            }
        }
        return true;
    }

    public static boolean all(boolean[] arr, CompareEntryValid<Boolean> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (boolean e : arr) {
            if (!action.isValid(e)) {
                return false;
            }
        }
        return true;
    }

    public static boolean all(byte[] arr, CompareEntryValid<Byte> action) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (byte e : arr) {
            if (!action.isValid(e)) {
                return false;
            }
        }
        return true;
    }
    //endregion

    //region any
    private static <N> boolean any(Object arr, int len, CompareEntryValid<N> action, FunctionMount<Object, N> mountFun) {
        Objects.requireNonNull(arr);
        Objects.requireNonNull(action);
        for (int i = 0; i < len; i++) {
            Object obj = Array.get(arr, i);
            if (action.isValid(mountFun.mount(obj))) {
                return true;
            }
        }
        return false;
    }

    public static boolean any(char[] arr, CompareEntryValid<Character> action) {
        return any(arr, arr.length, action, e -> (char) e);
    }

    public static boolean any(int[] arr, CompareEntryValid<Integer> action) {
        return any(arr, arr.length, action, e -> (int) e);
    }

    public static boolean any(long[] arr, CompareEntryValid<Long> action) {
        return any(arr, arr.length, action, e -> (long) e);
    }

    public static boolean any(double[] arr, CompareEntryValid<Double> action) {
        return any(arr, arr.length, action, e -> (double) e);
    }

    public static boolean any(boolean[] arr, CompareEntryValid<Boolean> action) {
        return any(arr, arr.length, action, e -> (boolean) e);
    }

    public static boolean any(byte[] arr, CompareEntryValid<Byte> action) {
        return any(arr, arr.length, action, e -> (byte) e);
    }
    //endregion
    //endregion
}
