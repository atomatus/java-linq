package com.atomatus.linq;

import com.atomatus.util.ArrayHelper;

import java.lang.reflect.Array;
import java.util.*;

/**
 * <p>
 * Helper for query actions (from any iterable, set or array) to
 * manipulate, filter, find, group elements on set or array fastest way for reducing code.
 * </p>
 * <p>
 * Every query action result generate an {@link IterableResult}, {@link IteratorForGroup} or {@link IteratorForMap},
 * all thus are tree schedules Iterator actions that will be mounted and executed only when do a directed request it.
 * </p>
 *
 * @author Carlos Matos
 */
@SuppressWarnings("SpellCheckingInspection")
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
     * Create a new object array from current object (current object referencing a array).
     *
     * @param arr target object.
     * @return object array.
     */
    public static Object[] toArray(Object arr) {
        return ArrayHelper.toArray(arr);
    }

    /**
     * Create a new object array from current object (current object referencing a array).
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
     * Generate an array with result of mountFun execution for each index.
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
     * Generate an array with result of mountFun execution for each index.
     * @param count array size
     * @param mountFun function to generate element for each index.
     * @param <E> element type
     * @return array generated.
     */
    public static <E> E[] repeat(int count, FunctionGet<E> mountFun) {
        return loop(count, null, null, mountFun);
    }

    /**
     * Generate an array with result of mountFun execution for each index.
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
     * Build an iterable from any collection, filtering values and accepting only who is valid for classType and
     * recovering from a iterable mode.
     *
     * @param classType                   desired class type.
     * @param getCollection               target collection.
     * @param checkEntryValidForClassType check if current entry is valid for classType.
     * @param mountElementFun             mount a element from current entry.
     * @param <I>                         current entry type, same type (or heritage type) for collection S.
     * @param <E>                         mounted element from entry type.
     * @param <C>                         desired class type.
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
     * Build an iterable from any collection, filtering values and accepting only who is valid for classType and
     * recovering from a iterable mode.
     *
     * @param classType                   desired class type.
     * @param set                         target collection.
     * @param checkEntryValidForClassType check if current entry is valid for classType.
     * @param mountElementFun             mount a element from current entry.
     * @param <I>                         current entry type, same type (or heritage type) for collection S.
     * @param <E>                         mounted element from entry type.
     * @param <C>                         desired class type.
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
     * Build an iterable from any collection, filtering values and accepting only who is valid for function validate entry and
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
     * Build an iterable from any collection, filtering values and accepting only who is valid for function validate entry and
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
     * @param <I>       element type (heritage from super class type)
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
     * @param <I>       element type (heritage from extend class type)
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
     * @param <I>       element type (heritage from super class type)
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
     * @param <I>       element type (heritage from extend class type)
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
     * @param <I>       element type (heritage from super class type)
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
     * @param <I>       element type (heritage from extend class type)
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
     * Generate an iterable result grouping elements by groupFun key result.
     *
     * @param iterator target.
     * @param groupFun group function to get grouping key.
     * @param <K>      key type
     * @param <V>      value type
     * @return an instance of toMap within set values grouped by key.
     */
    public static <K, V> IterableResultGroup<K, V> groupBy(Iterator<V> iterator, FunctionMount<V, K> groupFun) {
        return IterableResultFactory.getInstanceForGroup(() -> iterator, groupFun);
    }

    /**
     * Generate an iterable result grouping elements by groupFun key result.
     *
     * @param col      target.
     * @param groupFun group function to get grouping key.
     * @param <K>      key type
     * @param <V>      value type
     * @return an instance of toMap within set values grouped by key.
     */
    public static <K, V> IterableResultGroup<K, V> groupBy(Iterable<V> col, FunctionMount<V, K> groupFun) {
        return IterableResultFactory.getInstanceForGroup(col::iterator, groupFun);
    }

    /**
     * Generate an iterable result grouping elements by groupFun key result.
     *
     * @param arr      target.
     * @param groupFun group function to get grouping key.
     * @param <K>      key type
     * @param <V>      value type
     * @return an instance of toMap within array values grouped by key.
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
     * @return new iterable within set of values from mount function.
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
     * @return new iterable within set of values from mount function.
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
     * @return new iterable within set of values from mount function.
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
     * @param args one or more iterable within compatible elements with current iterable base.
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
     * @param args one or more iterable within compatible elements with current array base.
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
     * @param args     one or more arrays within compatible elements with current iterable base.
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
     * Apply summation operation (Σx) in a sequence of any kind of number.
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
     * Apply summation operation (Σx) in a sequence of any kind of number.
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
     * Apply summation operation (Σx) in a sequence of any kind of number.
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
     * Apply summation operation (Σx) in a sequence of any kind of number.
     *
     * @param iterator target
     * @return summation result
     */
    public static <IN extends Number> IN sum(Iterator<IN> iterator) {
        return IteratorForMath.sum(iterator);
    }

    /**
     * Apply summation operation (Σx) in a sequence of any kind of number.
     *
     * @param col target
     * @return summation result
     */
    public static <IN extends Number> IN sum(Iterable<IN> col) {
        return IteratorForMath.sum(col.iterator());
    }

    /**
     * Apply summation operation (Σx) in a sequence of any kind of number.
     *
     * @param arr target
     * @return summation result
     */
    public static <IN extends Number> IN sum(IN[] arr) {
        return IteratorForMath.sum(arr);
    }
    //endregion

    //region amplitude

    /**
     * <p>
     * In relation to a time series, the amplitude of a fluctuation is the value of the ordinate at its peak or
     * trough taken from some mean value or trend line. Sometimes the difference between values at peak and
     * trough is referred to as an "amplitude".
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
    public static <IN, OUT extends Number> OUT amplitude(Iterator<IN> iterator, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.amplitude(iterator, fun);
    }

    /**
     * <p>
     * In relation to a time series, the amplitude of a fluctuation is the value of the ordinate at its peak or
     * trough taken from some mean value or trend line. Sometimes the difference between values at peak and
     * trough is referred to as an "amplitude".
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
    public static <IN, OUT extends Number> OUT amplitude(Iterable<IN> col, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.amplitude(col.iterator(), fun);
    }

    /**
     * <p>
     * In relation to a time series, the amplitude of a fluctuation is the value of the ordinate at its peak or
     * trough taken from some mean value or trend line. Sometimes the difference between values at peak and
     * trough is referred to as an "amplitude".
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
    public static <IN, OUT extends Number> OUT amplitude(IN[] arr, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.amplitude(arr, fun);
    }

    /**
     * <p>
     * In relation to a time series, the amplitude of a fluctuation is the value of the ordinate at its peak or
     * trough taken from some mean value or trend line. Sometimes the difference between values at peak and
     * trough is referred to as an "amplitude".
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
    public static <IN extends Number> IN amplitude(Iterator<IN> iterator) {
        return IteratorForMath.amplitude(iterator);
    }

    /**
     * <p>
     * In relation to a time series, the amplitude of a fluctuation is the value of the ordinate at its peak or
     * trough taken from some mean value or trend line. Sometimes the difference between values at peak and
     * trough is referred to as an "amplitude".
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
    public static <IN extends Number> IN amplitude(Iterable<IN> col) {
        return IteratorForMath.amplitude(col.iterator());
    }

    /**
     * <p>
     * In relation to a time series, the amplitude of a fluctuation is the value of the ordinate at its peak or
     * trough taken from some mean value or trend line. Sometimes the difference between values at peak and
     * trough is referred to as an "amplitude".
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
    public static <IN extends Number> IN amplitude(IN[] arr) {
        return IteratorForMath.amplitude(arr);
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
     * <img  src="data:image/png;base64,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" />
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
     * @param arr  target
     * @param <IN> collection element number type
     * @return result number
     */
    public static <IN extends Number> IN mean(IN[] arr) {
        return IteratorForMath.mean(arr);
    }
    //endregion

    //region median

    /**
     * Arrange the data points from smallest to largest.<br/>
     * <ul>
     * <li>If the number of data points is odd, the median is the middle data point in the list;</li>
     * <li>If the number of data points is even, the median is the average of the two middle data points in the list.</li>
     * </ul>
     * @param iterator target
     * @param fun      function to get target number
     * @param <IN>     collection element type
     * @param <OUT>    result number type
     * @return result number
     */
    public static <IN, OUT extends Number> OUT median(Iterator<IN> iterator, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.median(iterator, fun);
    }

    /**
     * Arrange the data points from smallest to largest.<br/>
     * <ul>
     * <li>If the number of data points is odd, the median is the middle data point in the list;</li>
     * <li>If the number of data points is even, the median is the average of the two middle data points in the list.</li>
     * </ul>
     * @param col   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return result number
     */
    public static <IN, OUT extends Number> OUT median(Iterable<IN> col, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.median(col.iterator(), fun);
    }

    /**
     * Arrange the data points from smallest to largest.<br/>
     * <ul>
     * <li>If the number of data points is odd, the median is the middle data point in the list;</li>
     * <li>If the number of data points is even, the median is the average of the two middle data points in the list.</li>
     * </ul>
     * @param arr   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return result number
     */
    public static <IN, OUT extends Number> OUT median(IN[] arr, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.median(arr, fun);
    }

    /**
     * Arrange the data points from smallest to largest.<br/>
     * <ul>
     * <li>If the number of data points is odd, the median is the middle data point in the list;</li>
     * <li>If the number of data points is even, the median is the average of the two middle data points in the list.</li>
     * </ul>
     * @param iterator target
     * @param <IN>     collection element number type
     * @return result number
     */
    public static <IN extends Number> IN median(Iterator<IN> iterator) {
        return IteratorForMath.median(iterator);
    }

    /**
     * Arrange the data points from smallest to largest.<br/>
     * <ul>
     * <li>If the number of data points is odd, the median is the middle data point in the list;</li>
     * <li>If the number of data points is even, the median is the average of the two middle data points in the list.</li>
     * </ul>
     * @param col  function to get target number
     * @param <IN> collection element number type
     * @return result number
     */
    public static <IN extends Number> IN median(Iterable<IN> col) {
        return IteratorForMath.median(col.iterator());
    }

    /**
     * Arrange the data points from smallest to largest.<br/>
     * <ul>
     * <li>If the number of data points is odd, the median is the middle data point in the list;</li>
     * <li>If the number of data points is even, the median is the average of the two middle data points in the list.</li>
     * </ul>
     * @param arr  target
     * @param <IN> collection element number type
     * @return result number
     */
    public static <IN extends Number> IN median(IN[] arr) {
        return IteratorForMath.median(arr);
    }
    //endregion

    //region mode

    /**
     * In statistics, the mode is the most commonly observed value in a set of data.
     * @param iterator target
     * @param fun      function to get target number
     * @param <IN>     collection element type
     * @param <OUT>    result number type
     * @return result number
     */
    public static <IN, OUT extends Number> OUT mode(Iterator<IN> iterator, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.mode(iterator, fun);
    }

    /**
     * In statistics, the mode is the most commonly observed value in a set of data.
     * @param col   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return result number
     */
    public static <IN, OUT extends Number> OUT mode(Iterable<IN> col, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.mode(col.iterator(), fun);
    }

    /**
     * In statistics, the mode is the most commonly observed value in a set of data.
     * @param arr   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return result number
     */
    public static <IN, OUT extends Number> OUT mode(IN[] arr, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.mode(arr, fun);
    }

    /**
     * In statistics, the mode is the most commonly observed value in a set of data.
     * @param iterator target
     * @param <IN>     collection element number type
     * @return result number
     */
    public static <IN extends Number> IN mode(Iterator<IN> iterator) {
        return IteratorForMath.mode(iterator);
    }

    /**
     * In statistics, the mode is the most commonly observed value in a set of data.
     * @param col  function to get target number
     * @param <IN> collection element number type
     * @return result number
     */
    public static <IN extends Number> IN mode(Iterable<IN> col) {
        return IteratorForMath.mode(col.iterator());
    }

    /**
     * In statistics, the mode is the most commonly observed value in a set of data.
     * @param arr  target
     * @param <IN> collection element number type
     * @return result number
     */
    public static <IN extends Number> IN mode(IN[] arr) {
        return IteratorForMath.mode(arr);
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

    //region variance

    //region population variance
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
     * Population Variance is calculated using the following formula:<br/>
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
    public static <IN, OUT extends Number> OUT variance(Iterator<IN> iterator, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.variance(iterator, fun);
    }

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
     * Population Variance is calculated using the following formula:<br/>
     * <img src="data:image/png;base64,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">
     * </p><br/>
     * <p>
     * <ul>
     * <li>A large variance indicates that numbers in the set are far from the mean and far from each other.</li>
     * <li>A small variance, on the other hand, indicates the opposite.</li>
     * <li>A variance value of zero, though, indicates that all values within a set of numbers are identical.</li>
     * </ul>
     * </p>
     * @param col   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return population variance result
     */
    public static <IN, OUT extends Number> OUT variance(Iterable<IN> col, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.variance(col.iterator(), fun);
    }

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
     * Population Variance is calculated using the following formula:<br/>
     * <img src="data:image/png;base64,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">
     * </p><br/>
     * <p>
     * <ul>
     * <li>A large variance indicates that numbers in the set are far from the mean and far from each other.</li>
     * <li>A small variance, on the other hand, indicates the opposite.</li>
     * <li>A variance value of zero, though, indicates that all values within a set of numbers are identical.</li>
     * </ul>
     * </p>
     * @param arr   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return population variance result
     */
    public static <IN, OUT extends Number> OUT variance(IN[] arr, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.variance(arr, fun);
    }

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
     * Population Variance is calculated using the following formula:<br/>
     * <img src="data:image/png;base64,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">
     * </p><br/>
     * <p>
     * <ul>
     * <li>A large variance indicates that numbers in the set are far from the mean and far from each other.</li>
     * <li>A small variance, on the other hand, indicates the opposite.</li>
     * <li>A variance value of zero, though, indicates that all values within a set of numbers are identical.</li>
     * </ul>
     * </p>
     * @param iterator target
     * @param <IN>     collection element number type
     * @return population variance result
     */
    public static <IN extends Number> IN variance(Iterator<IN> iterator) {
        return IteratorForMath.variance(iterator);
    }

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
     * Population Variance is calculated using the following formula:<br/>
     * <img src="data:image/png;base64,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">
     * </p><br/>
     * <p>
     * <ul>
     * <li>A large variance indicates that numbers in the set are far from the mean and far from each other.</li>
     * <li>A small variance, on the other hand, indicates the opposite.</li>
     * <li>A variance value of zero, though, indicates that all values within a set of numbers are identical.</li>
     * </ul>
     * </p>
     * @param col  function to get target number
     * @param <IN> collection element number type
     * @return population variance result
     */
    public static <IN extends Number> IN variance(Iterable<IN> col) {
        return IteratorForMath.variance(col.iterator());
    }

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
     * Population Variance is calculated using the following formula:<br/>
     * <img src="data:image/png;base64,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">
     * </p><br/>
     * <p>
     * <ul>
     * <li>A large variance indicates that numbers in the set are far from the mean and far from each other.</li>
     * <li>A small variance, on the other hand, indicates the opposite.</li>
     * <li>A variance value of zero, though, indicates that all values within a set of numbers are identical.</li>
     * </ul>
     * </p>
     * @param arr  target
     * @param <IN> collection element number type
     * @return population variance result
     */
    public static <IN extends Number> IN variance(IN[] arr) {
        return IteratorForMath.variance(arr);
    }
    //endregion

    //region sample variance
    /**
     * <h2>What Is Sample Variance?</h2>
     * <p>
     * The sample variance, s2, is used to calculate how varied a sample is.
     * A sample is a select number of items taken from a population. <br/>
     * For example, if you are measuring American people’s weights, it wouldn’t be
     * feasible (from either a time or a monetary standpoint) for you to measure the
     * weights of every person in the population. The solution is to take a sample of
     * the population, say 1000 people, and use that sample size to estimate the actual
     * weights of the whole population.<br/>
     * The variance helps you to figure out how spread out your weights are.
     * </p><br/>
     * <h2>Understanding Population Variance</h2>
     * <p>
     * In statistics, variance measures variability from the average or mean. <br/>
     * It is calculated by taking the differences between each number in the data
     * set and the mean, then squaring the differences to make them positive,
     * and finally dividing the sum of the squares by the number of values in the data set.
     * </p><br/>
     * <p>
     * Sample Variance is calculated using the following formula:<br/>
     * <img src="data:image/jpeg;base64,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">
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
    public static <IN, OUT extends Number> OUT varianceSample(Iterator<IN> iterator, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.varianceSample(iterator, fun);
    }

    /**
     * <h2>What Is Sample Variance?</h2>
     * <p>
     * The sample variance, s2, is used to calculate how varied a sample is.
     * A sample is a select number of items taken from a population. <br/>
     * For example, if you are measuring American people’s weights, it wouldn’t be
     * feasible (from either a time or a monetary standpoint) for you to measure the
     * weights of every person in the population. The solution is to take a sample of
     * the population, say 1000 people, and use that sample size to estimate the actual
     * weights of the whole population.<br/>
     * The variance helps you to figure out how spread out your weights are.
     * </p><br/>
     * <h2>Understanding Population Variance</h2>
     * <p>
     * In statistics, variance measures variability from the average or mean. <br/>
     * It is calculated by taking the differences between each number in the data
     * set and the mean, then squaring the differences to make them positive,
     * and finally dividing the sum of the squares by the number of values in the data set.
     * </p><br/>
     * <p>
     * Sample Variance is calculated using the following formula:<br/>
     * <img src="data:image/jpeg;base64,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">
     * </p><br/>
     * <p>
     * <ul>
     * <li>A large variance indicates that numbers in the set are far from the mean and far from each other.</li>
     * <li>A small variance, on the other hand, indicates the opposite.</li>
     * <li>A variance value of zero, though, indicates that all values within a set of numbers are identical.</li>
     * </ul>
     * </p>
     *
     * @param col   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return population variance result
     */
    public static <IN, OUT extends Number> OUT varianceSample(Iterable<IN> col, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.varianceSample(col.iterator(), fun);
    }

    /**
     * <h2>What Is Sample Variance?</h2>
     * <p>
     * The sample variance, s2, is used to calculate how varied a sample is.
     * A sample is a select number of items taken from a population. <br/>
     * For example, if you are measuring American people’s weights, it wouldn’t be
     * feasible (from either a time or a monetary standpoint) for you to measure the
     * weights of every person in the population. The solution is to take a sample of
     * the population, say 1000 people, and use that sample size to estimate the actual
     * weights of the whole population.<br/>
     * The variance helps you to figure out how spread out your weights are.
     * </p><br/>
     * <h2>Understanding Population Variance</h2>
     * <p>
     * In statistics, variance measures variability from the average or mean. <br/>
     * It is calculated by taking the differences between each number in the data
     * set and the mean, then squaring the differences to make them positive,
     * and finally dividing the sum of the squares by the number of values in the data set.
     * </p><br/>
     * <p>
     * Sample Variance is calculated using the following formula:<br/>
     * <img src="data:image/jpeg;base64,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">
     * </p><br/>
     * <p>
     * <ul>
     * <li>A large variance indicates that numbers in the set are far from the mean and far from each other.</li>
     * <li>A small variance, on the other hand, indicates the opposite.</li>
     * <li>A variance value of zero, though, indicates that all values within a set of numbers are identical.</li>
     * </ul>
     * </p>
     *
     * @param arr   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return population variance result
     */
    public static <IN, OUT extends Number> OUT varianceSample(IN[] arr, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.varianceSample(arr, fun);
    }

    /**
     * <h2>What Is Sample Variance?</h2>
     * <p>
     * The sample variance, s2, is used to calculate how varied a sample is.
     * A sample is a select number of items taken from a population. <br/>
     * For example, if you are measuring American people’s weights, it wouldn’t be
     * feasible (from either a time or a monetary standpoint) for you to measure the
     * weights of every person in the population. The solution is to take a sample of
     * the population, say 1000 people, and use that sample size to estimate the actual
     * weights of the whole population.<br/>
     * The variance helps you to figure out how spread out your weights are.
     * </p><br/>
     * <h2>Understanding Population Variance</h2>
     * <p>
     * In statistics, variance measures variability from the average or mean. <br/>
     * It is calculated by taking the differences between each number in the data
     * set and the mean, then squaring the differences to make them positive,
     * and finally dividing the sum of the squares by the number of values in the data set.
     * </p><br/>
     * <p>
     * Sample Variance is calculated using the following formula:<br/>
     * <img src="data:image/jpeg;base64,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">
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
     * @param <IN>     collection element number type
     * @return population variance result
     */
    public static <IN extends Number> IN varianceSample(Iterator<IN> iterator) {
        return IteratorForMath.varianceSample(iterator);
    }

    /**
     * <h2>What Is Sample Variance?</h2>
     * <p>
     * The sample variance, s2, is used to calculate how varied a sample is.
     * A sample is a select number of items taken from a population. <br/>
     * For example, if you are measuring American people’s weights, it wouldn’t be
     * feasible (from either a time or a monetary standpoint) for you to measure the
     * weights of every person in the population. The solution is to take a sample of
     * the population, say 1000 people, and use that sample size to estimate the actual
     * weights of the whole population.<br/>
     * The variance helps you to figure out how spread out your weights are.
     * </p><br/>
     * <h2>Understanding Population Variance</h2>
     * <p>
     * In statistics, variance measures variability from the average or mean. <br/>
     * It is calculated by taking the differences between each number in the data
     * set and the mean, then squaring the differences to make them positive,
     * and finally dividing the sum of the squares by the number of values in the data set.
     * </p><br/>
     * <p>
     * Sample Variance is calculated using the following formula:<br/>
     * <img src="data:image/jpeg;base64,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">
     * </p><br/>
     * <p>
     * <ul>
     * <li>A large variance indicates that numbers in the set are far from the mean and far from each other.</li>
     * <li>A small variance, on the other hand, indicates the opposite.</li>
     * <li>A variance value of zero, though, indicates that all values within a set of numbers are identical.</li>
     * </ul>
     * </p>
     *
     * @param col  function to get target number
     * @param <IN> collection element number type
     * @return population variance result
     */
    public static <IN extends Number> IN varianceSample(Iterable<IN> col) {
        return IteratorForMath.varianceSample(col.iterator());
    }

    /**
     * <h2>What Is Sample Variance?</h2>
     * <p>
     * The sample variance, s2, is used to calculate how varied a sample is.
     * A sample is a select number of items taken from a population. <br/>
     * For example, if you are measuring American people’s weights, it wouldn’t be
     * feasible (from either a time or a monetary standpoint) for you to measure the
     * weights of every person in the population. The solution is to take a sample of
     * the population, say 1000 people, and use that sample size to estimate the actual
     * weights of the whole population.<br/>
     * The variance helps you to figure out how spread out your weights are.
     * </p><br/>
     * <h2>Understanding Population Variance</h2>
     * <p>
     * In statistics, variance measures variability from the average or mean. <br/>
     * It is calculated by taking the differences between each number in the data
     * set and the mean, then squaring the differences to make them positive,
     * and finally dividing the sum of the squares by the number of values in the data set.
     * </p><br/>
     * <p>
     * Sample Variance is calculated using the following formula:<br/>
     * <img src="data:image/jpeg;base64,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">
     * </p><br/>
     * <p>
     * <ul>
     * <li>A large variance indicates that numbers in the set are far from the mean and far from each other.</li>
     * <li>A small variance, on the other hand, indicates the opposite.</li>
     * <li>A variance value of zero, though, indicates that all values within a set of numbers are identical.</li>
     * </ul>
     * </p>
     *
     * @param arr  target
     * @param <IN> collection element number type
     * @return population variance result
     */
    public static <IN extends Number> IN varianceSample(IN[] arr) {
        return IteratorForMath.varianceSample(arr);
    }
    //endregion
    //endregion

    //region standard deviation

    //region population standard deviation
    /**
     * <h2>Population Standard Deviation</h2>
     * <p>
     *     In statistics, the population standard deviation is a measure of the amount of variation or
     *     dispersion of a population of values.
     * </p>
     * <p>
     *   <ul>
     *     <li>A low standard deviation indicates that the values tend to be close to the mean (also called the expected value) of the set;</li>
     *     <li>A high standard deviation indicates that the values are spread out over a wider range.</li>
     *   </ul>
     * </p>
     * <img src="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/4TIiaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjMtYzAxMSA2Ni4xNDU2NjEsIDIwMTIvMDIvMDYtMTQ6NTY6MjcgICAgICAgICI+CiAgIDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyI+CiAgICAgICAgIDx4bXA6Q3JlYXRvclRvb2w+QWRvYmUgRmlyZXdvcmtzIENTNiAoV2luZG93cyk8L3htcDpDcmVhdG9yVG9vbD4KICAgICAgICAgPHhtcDpDcmVhdGVEYXRlPjIwMjAtMTEtMjNUMTQ6MTg6MDRaPC94bXA6Q3JlYXRlRGF0ZT4KICAgICAgICAgPHhtcDpNb2RpZnlEYXRlPjIwMjAtMTEtMjNUMTQ6MTk6MjNaPC94bXA6TW9kaWZ5RGF0ZT4KICAgICAgPC9yZGY6RGVzY3JpcHRpb24+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyI+CiAgICAgICAgIDxkYzpmb3JtYXQ+aW1hZ2UvanBlZzwvZGM6Zm9ybWF0PgogICAgICA8L3JkZjpEZXNjcmlwdGlvbj4KICAgPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAKPD94cGFja2V0IGVuZD0idyI/Pv/bAEMABwQEBAUEBwUFBwoHBQcKDAkHBwkMDQsLDAsLDRENDQ0NDQ0RDQ8QERAPDRQUFhYUFB4dHR0eIiIiIiIiIiIiIv/bAEMBCAcHDQwNGBAQGBoVERUaICAgICAgICAgICAgICEgICAgICAhISEgICAhISEhISEhISIiIiIiIiIiIiIiIiIiIv/AABEIANABiAMBEQACEQEDEQH/xAAbAAEAAgMBAQAAAAAAAAAAAAAABAUBAwYCB//EAFAQAAEDAwICBAgKBgkCBAcAAAIAAQMEBREGEhMhFCIxQQcWMlFVYdHSFRcjQlRxgZKTojNSYnKRlCQ1NnN0obGys1OCNEOD00RWZaTB4fD/xAAaAQEBAQEBAQEAAAAAAAAAAAAAAQIDBAUG/8QAOhEBAAEDAQUHAgQFBAEFAAAAAAECAxEEEhQhMVIFExVBUWGRMoEicaGxFkJicvAjU8HRM4KS4fHy/9oADAMBAAIRAxEAPwD7wvC7CAgICAgICAgICAgICAgICAgIC00IooCAgICAgICAgICAgICAgICAgICAgICILQICAgICwyICAgICAgICAgICAgICAgICAgqrtq/TFoquiXOvipqlxY+Ge7O0ux+TP24Xe3p664zTGXC9rrNqcVVYlE+MnQfpen/N7q67ld6XLxbT9R8ZOg/S9P8Am91Nyu9J4tp+o+MnQfpen/N7qbld6TxbT9R8ZOg/S9P+b3U3K70ni2n6j4ydB+l6f83upuV3pPFtP1Hxk6D9L0/5vdTcrvSeLafqPjJ0H6Xp/wA3upuV3pPFtP1Hxk6D9L0/5vdTcrvSeLafqPjJ0H6Xp/ze6m5Xek8W0/UfGToP0vT/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mmqn+qSQtn5GZcdT9WPSHo0f0Z9ZmVmuD0iAgICAgICoKDjdXX7VcGuLVYrHLHGNxpJXLjCxCBCXObHlO4i3Ic4d1l9PSaezOnruXP5ao/8ApqpblqzT2t7dZLvcmu9tvIycCd4hhliljbdjqcnF0dJt2b+nquUU7FVHvnMO3dV8hjCAgygICAgKgiioICAgIgtAgICAgLDIgICAgICAgICoKtCiigICAgIOR1jVPatU0N0blxrfXUufOYi00bfxZe7SRt25j0mJfH7Qq7u9TV60zDprbS9Dt1NSf9CII3+sQZn/ANF5blWZl9OzTs0RHskLm6CAgICAgICAg5K9W24y+FWw14QGVBDRVQTTs3UFyzhnLud+5TzfRs3aY0ldOfxTVBq613Co17pSsggOSlpZKh6iYRyMe4G2ub92UXRXaadPdpmeMxwdaq+aKAgICAgICoIoqCAgICILQICAgICwyICAgICAgICAqCrQoogICAgIKDXFj+FgtLM2eBcoDL+7fLGvXpLuxtf2vndpabvdj2rhfu+XyvG+hIgICAgICAgICAiCKICAgICAgICAiioICAgIgtAgICAgLDIgICAgICAgICoKtCiiAgICAgKoKYBMAmATAJgEwCYBMAmATAJgEwCYBMAmATAJgEwCYBMAiiAgICAiC0CAgICAsMiAgICAgICAgKgq0KKICAgICAqCIICAgICAgICAgICAgICAgIMEQgLmbsIN2uT4b+LokyCQmLEDs4P2OL5b+LIrKAoogICAgIgtAgICAgLDIgICAgICAgICoKtCiiAgICAgICAgICAgICAgICAgICAgICCo1dqq3aXsklzrOu+dlPTj5UspeSA/6u/cy62bNVyrEPNq9VTZo2pfCNQ368alqyq73O8z5+Tpmd2gib9UA7Ptfm6/R6fSUW44PxWr7Ru3qs54PWm9RXnTNYNVZZnAWf5WjJ3enlHzEHc/mJubLN/R03I5cWtH2ncs1c8w+76U1PbtS2SK60OREurNCXlxSj5YH628/e3Nfnb1qbdWzL9ppdTTeo2oWq5vQICAgICILQICAgICwyICAzZdmRYcJZPCDrTUVDJJYbLTlLTySRVElTMQQuQk+0IvnETjh3d8M2VMvsX+z7FmqIuVziYjlHH7r7ROrh1PaCq3gekraeUqatpCfdw5Q7WZ+WWfPJWHi1+j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"/>
     * @see CollectionHelper#variance(Object[], CollectionHelper.FunctionMount)
     * @param iterator target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return population variance result
     */
    public static <IN, OUT extends Number> OUT standardDeviation(Iterator<IN> iterator, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.standardDeviation(iterator, fun);
    }

    /**
     * <h2>Population Standard Deviation</h2>
     * <p>
     *     In statistics, the population standard deviation is a measure of the amount of variation or
     *     dispersion of a population of values.
     * </p>
     * <p>
     *   <ul>
     *     <li>A low standard deviation indicates that the values tend to be close to the mean (also called the expected value) of the set;</li>
     *     <li>A high standard deviation indicates that the values are spread out over a wider range.</li>
     *   </ul>
     * </p>
     * <img src="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/4TIiaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjMtYzAxMSA2Ni4xNDU2NjEsIDIwMTIvMDIvMDYtMTQ6NTY6MjcgICAgICAgICI+CiAgIDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyI+CiAgICAgICAgIDx4bXA6Q3JlYXRvclRvb2w+QWRvYmUgRmlyZXdvcmtzIENTNiAoV2luZG93cyk8L3htcDpDcmVhdG9yVG9vbD4KICAgICAgICAgPHhtcDpDcmVhdGVEYXRlPjIwMjAtMTEtMjNUMTQ6MTg6MDRaPC94bXA6Q3JlYXRlRGF0ZT4KICAgICAgICAgPHhtcDpNb2RpZnlEYXRlPjIwMjAtMTEtMjNUMTQ6MTk6MjNaPC94bXA6TW9kaWZ5RGF0ZT4KICAgICAgPC9yZGY6RGVzY3JpcHRpb24+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyI+CiAgICAgICAgIDxkYzpmb3JtYXQ+aW1hZ2UvanBlZzwvZGM6Zm9ybWF0PgogICAgICA8L3JkZjpEZXNjcmlwdGlvbj4KICAgPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAKPD94cGFja2V0IGVuZD0idyI/Pv/bAEMABwQEBAUEBwUFBwoHBQcKDAkHBwkMDQsLDAsLDRENDQ0NDQ0RDQ8QERAPDRQUFhYUFB4dHR0eIiIiIiIiIiIiIv/bAEMBCAcHDQwNGBAQGBoVERUaICAgICAgICAgICAgICEgICAgICAhISEgICAhISEhISEhISIiIiIiIiIiIiIiIiIiIv/AABEIANABiAMBEQACEQEDEQH/xAAbAAEAAgMBAQAAAAAAAAAAAAAABAUBAwYCB//EAFAQAAEDAwICBAgKBgkCBAcAAAIAAQMEBREGEhMhFCIxQQcWMlFVYdHSFRcjQlRxgZKTojNSYnKRlCQ1NnN0obGys1OCNEOD00RWZaTB4fD/xAAaAQEBAQEBAQEAAAAAAAAAAAAAAQIDBAUG/8QAOhEBAAEDAQUHAgQFBAEFAAAAAAECAxEEEhQhMVIFExVBUWGRMoEicaGxFkJicvAjU8HRM4KS4fHy/9oADAMBAAIRAxEAPwD7wvC7CAgICAgICAgICAgICAgICAgIC00IooCAgICAgICAgICAgICAgICAgICAgICILQICAgICwyICAgICAgICAgICAgICAgICAgqrtq/TFoquiXOvipqlxY+Ge7O0ux+TP24Xe3p664zTGXC9rrNqcVVYlE+MnQfpen/N7q67ld6XLxbT9R8ZOg/S9P8Am91Nyu9J4tp+o+MnQfpen/N7qbld6TxbT9R8ZOg/S9P+b3U3K70ni2n6j4ydB+l6f83upuV3pPFtP1Hxk6D9L0/5vdTcrvSeLafqPjJ0H6Xp/wA3upuV3pPFtP1Hxk6D9L0/5vdTcrvSeLafqPjJ0H6Xp/ze6m5Xek8W0/UfGToP0vT/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mmqn+qSQtn5GZcdT9WPSHo0f0Z9ZmVmuD0iAgICAgICoKDjdXX7VcGuLVYrHLHGNxpJXLjCxCBCXObHlO4i3Ic4d1l9PSaezOnruXP5ao/8ApqpblqzT2t7dZLvcmu9tvIycCd4hhliljbdjqcnF0dJt2b+nquUU7FVHvnMO3dV8hjCAgygICAgKgiioICAgIgtAgICAgLDIgICAgICAgICoKtCiigICAgIOR1jVPatU0N0blxrfXUufOYi00bfxZe7SRt25j0mJfH7Qq7u9TV60zDprbS9Dt1NSf9CII3+sQZn/ANF5blWZl9OzTs0RHskLm6CAgICAgICAg5K9W24y+FWw14QGVBDRVQTTs3UFyzhnLud+5TzfRs3aY0ldOfxTVBq613Co17pSsggOSlpZKh6iYRyMe4G2ub92UXRXaadPdpmeMxwdaq+aKAgICAgICoIoqCAgICILQICAgICwyICAgICAgICAqCrQoogICAgIKDXFj+FgtLM2eBcoDL+7fLGvXpLuxtf2vndpabvdj2rhfu+XyvG+hIgICAgICAgICAiCKICAgICAgICAiioICAgIgtAgICAgLDIgICAgICAgICoKtCiiAgICAgKoKYBMAmATAJgEwCYBMAmATAJgEwCYBMAmATAJgEwCYBMAiiAgICAiC0CAgICAsMiAgICAgICAgKgq0KKICAgICAqCIICAgICAgICAgICAgICAgIMEQgLmbsIN2uT4b+LokyCQmLEDs4P2OL5b+LIrKAoogICAgIgtAgICAgLDIgICAgICAgICoKtCiiAgICAgICAgICAgICAgICAgICAgICCo1dqq3aXsklzrOu+dlPTj5UspeSA/6u/cy62bNVyrEPNq9VTZo2pfCNQ368alqyq73O8z5+Tpmd2gib9UA7Ptfm6/R6fSUW44PxWr7Ru3qs54PWm9RXnTNYNVZZnAWf5WjJ3enlHzEHc/mJubLN/R03I5cWtH2ncs1c8w+76U1PbtS2SK60OREurNCXlxSj5YH628/e3Nfnb1qbdWzL9ppdTTeo2oWq5vQICAgICILQICAgICwyICAzZdmRYcJZPCDrTUVDJJYbLTlLTySRVElTMQQuQk+0IvnETjh3d8M2VMvsX+z7FmqIuVziYjlHH7r7ROrh1PaCq3gekraeUqatpCfdw5Q7WZ+WWfPJWHi1+j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"/>
     * @see CollectionHelper#variance(Object[], CollectionHelper.FunctionMount)
     * @param col   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return population variance result
     */
    public static <IN, OUT extends Number> OUT standardDeviation(Iterable<IN> col, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.standardDeviation(col.iterator(), fun);
    }

    /**
     * <h2>Population Standard Deviation</h2>
     * <p>
     *     In statistics, the population standard deviation is a measure of the amount of variation or
     *     dispersion of a population of values.
     * </p>
     * <p>
     *   <ul>
     *     <li>A low standard deviation indicates that the values tend to be close to the mean (also called the expected value) of the set;</li>
     *     <li>A high standard deviation indicates that the values are spread out over a wider range.</li>
     *   </ul>
     * </p>
     * <img src="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/4TIiaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjMtYzAxMSA2Ni4xNDU2NjEsIDIwMTIvMDIvMDYtMTQ6NTY6MjcgICAgICAgICI+CiAgIDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyI+CiAgICAgICAgIDx4bXA6Q3JlYXRvclRvb2w+QWRvYmUgRmlyZXdvcmtzIENTNiAoV2luZG93cyk8L3htcDpDcmVhdG9yVG9vbD4KICAgICAgICAgPHhtcDpDcmVhdGVEYXRlPjIwMjAtMTEtMjNUMTQ6MTg6MDRaPC94bXA6Q3JlYXRlRGF0ZT4KICAgICAgICAgPHhtcDpNb2RpZnlEYXRlPjIwMjAtMTEtMjNUMTQ6MTk6MjNaPC94bXA6TW9kaWZ5RGF0ZT4KICAgICAgPC9yZGY6RGVzY3JpcHRpb24+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyI+CiAgICAgICAgIDxkYzpmb3JtYXQ+aW1hZ2UvanBlZzwvZGM6Zm9ybWF0PgogICAgICA8L3JkZjpEZXNjcmlwdGlvbj4KICAgPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAKPD94cGFja2V0IGVuZD0idyI/Pv/bAEMABwQEBAUEBwUFBwoHBQcKDAkHBwkMDQsLDAsLDRENDQ0NDQ0RDQ8QERAPDRQUFhYUFB4dHR0eIiIiIiIiIiIiIv/bAEMBCAcHDQwNGBAQGBoVERUaICAgICAgICAgICAgICEgICAgICAhISEgICAhISEhISEhISIiIiIiIiIiIiIiIiIiIv/AABEIANABiAMBEQACEQEDEQH/xAAbAAEAAgMBAQAAAAAAAAAAAAAABAUBAwYCB//EAFAQAAEDAwICBAgKBgkCBAcAAAIAAQMEBREGEhMhFCIxQQcWMlFVYdHSFRcjQlRxgZKTojNSYnKRlCQ1NnN0obGys1OCNEOD00RWZaTB4fD/xAAaAQEBAQEBAQEAAAAAAAAAAAAAAQIDBAUG/8QAOhEBAAEDAQUHAgQFBAEFAAAAAAECAxEEEhQhMVIFExVBUWGRMoEicaGxFkJicvAjU8HRM4KS4fHy/9oADAMBAAIRAxEAPwD7wvC7CAgICAgICAgICAgICAgICAgIC00IooCAgICAgICAgICAgICAgICAgICAgICILQICAgICwyICAgICAgICAgICAgICAgICAgqrtq/TFoquiXOvipqlxY+Ge7O0ux+TP24Xe3p664zTGXC9rrNqcVVYlE+MnQfpen/N7q67ld6XLxbT9R8ZOg/S9P8Am91Nyu9J4tp+o+MnQfpen/N7qbld6TxbT9R8ZOg/S9P+b3U3K70ni2n6j4ydB+l6f83upuV3pPFtP1Hxk6D9L0/5vdTcrvSeLafqPjJ0H6Xp/wA3upuV3pPFtP1Hxk6D9L0/5vdTcrvSeLafqPjJ0H6Xp/ze6m5Xek8W0/UfGToP0vT/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mmqn+qSQtn5GZcdT9WPSHo0f0Z9ZmVmuD0iAgICAgICoKDjdXX7VcGuLVYrHLHGNxpJXLjCxCBCXObHlO4i3Ic4d1l9PSaezOnruXP5ao/8ApqpblqzT2t7dZLvcmu9tvIycCd4hhliljbdjqcnF0dJt2b+nquUU7FVHvnMO3dV8hjCAgygICAgKgiioICAgIgtAgICAgLDIgICAgICAgICoKtCiigICAgIOR1jVPatU0N0blxrfXUufOYi00bfxZe7SRt25j0mJfH7Qq7u9TV60zDprbS9Dt1NSf9CII3+sQZn/ANF5blWZl9OzTs0RHskLm6CAgICAgICAg5K9W24y+FWw14QGVBDRVQTTs3UFyzhnLud+5TzfRs3aY0ldOfxTVBq613Co17pSsggOSlpZKh6iYRyMe4G2ub92UXRXaadPdpmeMxwdaq+aKAgICAgICoIoqCAgICILQICAgICwyICAgICAgICAqCrQoogICAgIKDXFj+FgtLM2eBcoDL+7fLGvXpLuxtf2vndpabvdj2rhfu+XyvG+hIgICAgICAgICAiCKICAgICAgICAiioICAgIgtAgICAgLDIgICAgICAgICoKtCiiAgICAgKoKYBMAmATAJgEwCYBMAmATAJgEwCYBMAmATAJgEwCYBMAiiAgICAiC0CAgICAsMiAgICAgICAgKgq0KKICAgICAqCIICAgICAgICAgICAgICAgIMEQgLmbsIN2uT4b+LokyCQmLEDs4P2OL5b+LIrKAoogICAgIgtAgICAgLDIgICAgICAgICoKtCiiAgICAgICAgICAgICAgICAgICAgICCo1dqq3aXsklzrOu+dlPTj5UspeSA/6u/cy62bNVyrEPNq9VTZo2pfCNQ368alqyq73O8z5+Tpmd2gib9UA7Ptfm6/R6fSUW44PxWr7Ru3qs54PWm9RXnTNYNVZZnAWf5WjJ3enlHzEHc/mJubLN/R03I5cWtH2ncs1c8w+76U1PbtS2SK60OREurNCXlxSj5YH628/e3Nfnb1qbdWzL9ppdTTeo2oWq5vQICAgICILQICAgICwyICAzZdmRYcJZPCDrTUVDJJYbLTlLTySRVElTMQQuQk+0IvnETjh3d8M2VMvsX+z7FmqIuVziYjlHH7r7ROrh1PaCq3gekraeUqatpCfdw5Q7WZ+WWfPJWHi1+j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"/>
     * @see CollectionHelper#variance(Object[], CollectionHelper.FunctionMount)
     * @param arr   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return population variance result
     */
    public static <IN, OUT extends Number> OUT standardDeviation(IN[] arr, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.standardDeviation(arr, fun);
    }

    /**
     * <h2>Population Standard Deviation</h2>
     * <p>
     *     In statistics, the population standard deviation is a measure of the amount of variation or
     *     dispersion of a population of values.
     * </p>
     * <p>
     *   <ul>
     *     <li>A low standard deviation indicates that the values tend to be close to the mean (also called the expected value) of the set;</li>
     *     <li>A high standard deviation indicates that the values are spread out over a wider range.</li>
     *   </ul>
     * </p>
     * <img src="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/4TIiaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjMtYzAxMSA2Ni4xNDU2NjEsIDIwMTIvMDIvMDYtMTQ6NTY6MjcgICAgICAgICI+CiAgIDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyI+CiAgICAgICAgIDx4bXA6Q3JlYXRvclRvb2w+QWRvYmUgRmlyZXdvcmtzIENTNiAoV2luZG93cyk8L3htcDpDcmVhdG9yVG9vbD4KICAgICAgICAgPHhtcDpDcmVhdGVEYXRlPjIwMjAtMTEtMjNUMTQ6MTg6MDRaPC94bXA6Q3JlYXRlRGF0ZT4KICAgICAgICAgPHhtcDpNb2RpZnlEYXRlPjIwMjAtMTEtMjNUMTQ6MTk6MjNaPC94bXA6TW9kaWZ5RGF0ZT4KICAgICAgPC9yZGY6RGVzY3JpcHRpb24+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyI+CiAgICAgICAgIDxkYzpmb3JtYXQ+aW1hZ2UvanBlZzwvZGM6Zm9ybWF0PgogICAgICA8L3JkZjpEZXNjcmlwdGlvbj4KICAgPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAKPD94cGFja2V0IGVuZD0idyI/Pv/bAEMABwQEBAUEBwUFBwoHBQcKDAkHBwkMDQsLDAsLDRENDQ0NDQ0RDQ8QERAPDRQUFhYUFB4dHR0eIiIiIiIiIiIiIv/bAEMBCAcHDQwNGBAQGBoVERUaICAgICAgICAgICAgICEgICAgICAhISEgICAhISEhISEhISIiIiIiIiIiIiIiIiIiIv/AABEIANABiAMBEQACEQEDEQH/xAAbAAEAAgMBAQAAAAAAAAAAAAAABAUBAwYCB//EAFAQAAEDAwICBAgKBgkCBAcAAAIAAQMEBREGEhMhFCIxQQcWMlFVYdHSFRcjQlRxgZKTojNSYnKRlCQ1NnN0obGys1OCNEOD00RWZaTB4fD/xAAaAQEBAQEBAQEAAAAAAAAAAAAAAQIDBAUG/8QAOhEBAAEDAQUHAgQFBAEFAAAAAAECAxEEEhQhMVIFExVBUWGRMoEicaGxFkJicvAjU8HRM4KS4fHy/9oADAMBAAIRAxEAPwD7wvC7CAgICAgICAgICAgICAgICAgIC00IooCAgICAgICAgICAgICAgICAgICAgICILQICAgICwyICAgICAgICAgICAgICAgICAgqrtq/TFoquiXOvipqlxY+Ge7O0ux+TP24Xe3p664zTGXC9rrNqcVVYlE+MnQfpen/N7q67ld6XLxbT9R8ZOg/S9P8Am91Nyu9J4tp+o+MnQfpen/N7qbld6TxbT9R8ZOg/S9P+b3U3K70ni2n6j4ydB+l6f83upuV3pPFtP1Hxk6D9L0/5vdTcrvSeLafqPjJ0H6Xp/wA3upuV3pPFtP1Hxk6D9L0/5vdTcrvSeLafqPjJ0H6Xp/ze6m5Xek8W0/UfGToP0vT/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mmqn+qSQtn5GZcdT9WPSHo0f0Z9ZmVmuD0iAgICAgICoKDjdXX7VcGuLVYrHLHGNxpJXLjCxCBCXObHlO4i3Ic4d1l9PSaezOnruXP5ao/8ApqpblqzT2t7dZLvcmu9tvIycCd4hhliljbdjqcnF0dJt2b+nquUU7FVHvnMO3dV8hjCAgygICAgKgiioICAgIgtAgICAgLDIgICAgICAgICoKtCiigICAgIOR1jVPatU0N0blxrfXUufOYi00bfxZe7SRt25j0mJfH7Qq7u9TV60zDprbS9Dt1NSf9CII3+sQZn/ANF5blWZl9OzTs0RHskLm6CAgICAgICAg5K9W24y+FWw14QGVBDRVQTTs3UFyzhnLud+5TzfRs3aY0ldOfxTVBq613Co17pSsggOSlpZKh6iYRyMe4G2ub92UXRXaadPdpmeMxwdaq+aKAgICAgICoIoqCAgICILQICAgICwyICAgICAgICAqCrQoogICAgIKDXFj+FgtLM2eBcoDL+7fLGvXpLuxtf2vndpabvdj2rhfu+XyvG+hIgICAgICAgICAiCKICAgICAgICAiioICAgIgtAgICAgLDIgICAgICAgICoKtCiiAgICAgKoKYBMAmATAJgEwCYBMAmATAJgEwCYBMAmATAJgEwCYBMAiiAgICAiC0CAgICAsMiAgICAgICAgKgq0KKICAgICAqCIICAgICAgICAgICAgICAgIMEQgLmbsIN2uT4b+LokyCQmLEDs4P2OL5b+LIrKAoogICAgIgtAgICAgLDIgICAgICAgICoKtCiiAgICAgICAgICAgICAgICAgICAgICCo1dqq3aXsklzrOu+dlPTj5UspeSA/6u/cy62bNVyrEPNq9VTZo2pfCNQ368alqyq73O8z5+Tpmd2gib9UA7Ptfm6/R6fSUW44PxWr7Ru3qs54PWm9RXnTNYNVZZnAWf5WjJ3enlHzEHc/mJubLN/R03I5cWtH2ncs1c8w+76U1PbtS2SK60OREurNCXlxSj5YH628/e3Nfnb1qbdWzL9ppdTTeo2oWq5vQICAgICILQICAgICwyICAzZdmRYcJZPCDrTUVDJJYbLTlLTySRVElTMQQuQk+0IvnETjh3d8M2VMvsX+z7FmqIuVziYjlHH7r7ROrh1PaCq3gekraeUqatpCfdw5Q7WZ+WWfPJWHi1+j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"/>
     * @see CollectionHelper#variance(Object[], CollectionHelper.FunctionMount)
     * @param iterator target
     * @param <IN>     collection element number type
     * @return population variance result
     */
    public static <IN extends Number> IN standardDeviation(Iterator<IN> iterator) {
        return IteratorForMath.standardDeviation(iterator);
    }

    /**
     * <h2>Population Standard Deviation</h2>
     * <p>
     *     In statistics, the population standard deviation is a measure of the amount of variation or
     *     dispersion of a population of values.
     * </p>
     * <p>
     *   <ul>
     *     <li>A low standard deviation indicates that the values tend to be close to the mean (also called the expected value) of the set;</li>
     *     <li>A high standard deviation indicates that the values are spread out over a wider range.</li>
     *   </ul>
     * </p>
     * <img src="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/4TIiaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjMtYzAxMSA2Ni4xNDU2NjEsIDIwMTIvMDIvMDYtMTQ6NTY6MjcgICAgICAgICI+CiAgIDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyI+CiAgICAgICAgIDx4bXA6Q3JlYXRvclRvb2w+QWRvYmUgRmlyZXdvcmtzIENTNiAoV2luZG93cyk8L3htcDpDcmVhdG9yVG9vbD4KICAgICAgICAgPHhtcDpDcmVhdGVEYXRlPjIwMjAtMTEtMjNUMTQ6MTg6MDRaPC94bXA6Q3JlYXRlRGF0ZT4KICAgICAgICAgPHhtcDpNb2RpZnlEYXRlPjIwMjAtMTEtMjNUMTQ6MTk6MjNaPC94bXA6TW9kaWZ5RGF0ZT4KICAgICAgPC9yZGY6RGVzY3JpcHRpb24+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyI+CiAgICAgICAgIDxkYzpmb3JtYXQ+aW1hZ2UvanBlZzwvZGM6Zm9ybWF0PgogICAgICA8L3JkZjpEZXNjcmlwdGlvbj4KICAgPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAKPD94cGFja2V0IGVuZD0idyI/Pv/bAEMABwQEBAUEBwUFBwoHBQcKDAkHBwkMDQsLDAsLDRENDQ0NDQ0RDQ8QERAPDRQUFhYUFB4dHR0eIiIiIiIiIiIiIv/bAEMBCAcHDQwNGBAQGBoVERUaICAgICAgICAgICAgICEgICAgICAhISEgICAhISEhISEhISIiIiIiIiIiIiIiIiIiIv/AABEIANABiAMBEQACEQEDEQH/xAAbAAEAAgMBAQAAAAAAAAAAAAAABAUBAwYCB//EAFAQAAEDAwICBAgKBgkCBAcAAAIAAQMEBREGEhMhFCIxQQcWMlFVYdHSFRcjQlRxgZKTojNSYnKRlCQ1NnN0obGys1OCNEOD00RWZaTB4fD/xAAaAQEBAQEBAQEAAAAAAAAAAAAAAQIDBAUG/8QAOhEBAAEDAQUHAgQFBAEFAAAAAAECAxEEEhQhMVIFExVBUWGRMoEicaGxFkJicvAjU8HRM4KS4fHy/9oADAMBAAIRAxEAPwD7wvC7CAgICAgICAgICAgICAgICAgIC00IooCAgICAgICAgICAgICAgICAgICAgICILQICAgICwyICAgICAgICAgICAgICAgICAgqrtq/TFoquiXOvipqlxY+Ge7O0ux+TP24Xe3p664zTGXC9rrNqcVVYlE+MnQfpen/N7q67ld6XLxbT9R8ZOg/S9P8Am91Nyu9J4tp+o+MnQfpen/N7qbld6TxbT9R8ZOg/S9P+b3U3K70ni2n6j4ydB+l6f83upuV3pPFtP1Hxk6D9L0/5vdTcrvSeLafqPjJ0H6Xp/wA3upuV3pPFtP1Hxk6D9L0/5vdTcrvSeLafqPjJ0H6Xp/ze6m5Xek8W0/UfGToP0vT/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mmqn+qSQtn5GZcdT9WPSHo0f0Z9ZmVmuD0iAgICAgICoKDjdXX7VcGuLVYrHLHGNxpJXLjCxCBCXObHlO4i3Ic4d1l9PSaezOnruXP5ao/8ApqpblqzT2t7dZLvcmu9tvIycCd4hhliljbdjqcnF0dJt2b+nquUU7FVHvnMO3dV8hjCAgygICAgKgiioICAgIgtAgICAgLDIgICAgICAgICoKtCiigICAgIOR1jVPatU0N0blxrfXUufOYi00bfxZe7SRt25j0mJfH7Qq7u9TV60zDprbS9Dt1NSf9CII3+sQZn/ANF5blWZl9OzTs0RHskLm6CAgICAgICAg5K9W24y+FWw14QGVBDRVQTTs3UFyzhnLud+5TzfRs3aY0ldOfxTVBq613Co17pSsggOSlpZKh6iYRyMe4G2ub92UXRXaadPdpmeMxwdaq+aKAgICAgICoIoqCAgICILQICAgICwyICAgICAgICAqCrQoogICAgIKDXFj+FgtLM2eBcoDL+7fLGvXpLuxtf2vndpabvdj2rhfu+XyvG+hIgICAgICAgICAiCKICAgICAgICAiioICAgIgtAgICAgLDIgICAgICAgICoKtCiiAgICAgKoKYBMAmATAJgEwCYBMAmATAJgEwCYBMAmATAJgEwCYBMAiiAgICAiC0CAgICAsMiAgICAgICAgKgq0KKICAgICAqCIICAgICAgICAgICAgICAgIMEQgLmbsIN2uT4b+LokyCQmLEDs4P2OL5b+LIrKAoogICAgIgtAgICAgLDIgICAgICAgICoKtCiiAgICAgICAgICAgICAgICAgICAgICCo1dqq3aXsklzrOu+dlPTj5UspeSA/6u/cy62bNVyrEPNq9VTZo2pfCNQ368alqyq73O8z5+Tpmd2gib9UA7Ptfm6/R6fSUW44PxWr7Ru3qs54PWm9RXnTNYNVZZnAWf5WjJ3enlHzEHc/mJubLN/R03I5cWtH2ncs1c8w+76U1PbtS2SK60OREurNCXlxSj5YH628/e3Nfnb1qbdWzL9ppdTTeo2oWq5vQICAgICILQICAgICwyICAzZdmRYcJZPCDrTUVDJJYbLTlLTySRVElTMQQuQk+0IvnETjh3d8M2VMvsX+z7FmqIuVziYjlHH7r7ROrh1PaCq3gekraeUqatpCfdw5Q7WZ+WWfPJWHi1+j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"/>
     * @see CollectionHelper#variance(Object[], CollectionHelper.FunctionMount)
     * @param col  function to get target number
     * @param <IN> collection element number type
     * @return population variance result
     */
    public static <IN extends Number> IN standardDeviation(Iterable<IN> col) {
        return IteratorForMath.standardDeviation(col.iterator());
    }

    /**
     * <h2>Population Standard Deviation</h2>
     * <p>
     *     In statistics, the population standard deviation is a measure of the amount of variation or
     *     dispersion of a population of values.
     * </p>
     * <p>
     *   <ul>
     *     <li>A low standard deviation indicates that the values tend to be close to the mean (also called the expected value) of the set;</li>
     *     <li>A high standard deviation indicates that the values are spread out over a wider range.</li>
     *   </ul>
     * </p>
     * <img src="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/4TIiaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjMtYzAxMSA2Ni4xNDU2NjEsIDIwMTIvMDIvMDYtMTQ6NTY6MjcgICAgICAgICI+CiAgIDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyI+CiAgICAgICAgIDx4bXA6Q3JlYXRvclRvb2w+QWRvYmUgRmlyZXdvcmtzIENTNiAoV2luZG93cyk8L3htcDpDcmVhdG9yVG9vbD4KICAgICAgICAgPHhtcDpDcmVhdGVEYXRlPjIwMjAtMTEtMjNUMTQ6MTg6MDRaPC94bXA6Q3JlYXRlRGF0ZT4KICAgICAgICAgPHhtcDpNb2RpZnlEYXRlPjIwMjAtMTEtMjNUMTQ6MTk6MjNaPC94bXA6TW9kaWZ5RGF0ZT4KICAgICAgPC9yZGY6RGVzY3JpcHRpb24+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyI+CiAgICAgICAgIDxkYzpmb3JtYXQ+aW1hZ2UvanBlZzwvZGM6Zm9ybWF0PgogICAgICA8L3JkZjpEZXNjcmlwdGlvbj4KICAgPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAKPD94cGFja2V0IGVuZD0idyI/Pv/bAEMABwQEBAUEBwUFBwoHBQcKDAkHBwkMDQsLDAsLDRENDQ0NDQ0RDQ8QERAPDRQUFhYUFB4dHR0eIiIiIiIiIiIiIv/bAEMBCAcHDQwNGBAQGBoVERUaICAgICAgICAgICAgICEgICAgICAhISEgICAhISEhISEhISIiIiIiIiIiIiIiIiIiIv/AABEIANABiAMBEQACEQEDEQH/xAAbAAEAAgMBAQAAAAAAAAAAAAAABAUBAwYCB//EAFAQAAEDAwICBAgKBgkCBAcAAAIAAQMEBREGEhMhFCIxQQcWMlFVYdHSFRcjQlRxgZKTojNSYnKRlCQ1NnN0obGys1OCNEOD00RWZaTB4fD/xAAaAQEBAQEBAQEAAAAAAAAAAAAAAQIDBAUG/8QAOhEBAAEDAQUHAgQFBAEFAAAAAAECAxEEEhQhMVIFExVBUWGRMoEicaGxFkJicvAjU8HRM4KS4fHy/9oADAMBAAIRAxEAPwD7wvC7CAgICAgICAgICAgICAgICAgIC00IooCAgICAgICAgICAgICAgICAgICAgICILQICAgICwyICAgICAgICAgICAgICAgICAgqrtq/TFoquiXOvipqlxY+Ge7O0ux+TP24Xe3p664zTGXC9rrNqcVVYlE+MnQfpen/N7q67ld6XLxbT9R8ZOg/S9P8Am91Nyu9J4tp+o+MnQfpen/N7qbld6TxbT9R8ZOg/S9P+b3U3K70ni2n6j4ydB+l6f83upuV3pPFtP1Hxk6D9L0/5vdTcrvSeLafqPjJ0H6Xp/wA3upuV3pPFtP1Hxk6D9L0/5vdTcrvSeLafqPjJ0H6Xp/ze6m5Xek8W0/UfGToP0vT/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mmqn+qSQtn5GZcdT9WPSHo0f0Z9ZmVmuD0iAgICAgICoKDjdXX7VcGuLVYrHLHGNxpJXLjCxCBCXObHlO4i3Ic4d1l9PSaezOnruXP5ao/8ApqpblqzT2t7dZLvcmu9tvIycCd4hhliljbdjqcnF0dJt2b+nquUU7FVHvnMO3dV8hjCAgygICAgKgiioICAgIgtAgICAgLDIgICAgICAgICoKtCiigICAgIOR1jVPatU0N0blxrfXUufOYi00bfxZe7SRt25j0mJfH7Qq7u9TV60zDprbS9Dt1NSf9CII3+sQZn/ANF5blWZl9OzTs0RHskLm6CAgICAgICAg5K9W24y+FWw14QGVBDRVQTTs3UFyzhnLud+5TzfRs3aY0ldOfxTVBq613Co17pSsggOSlpZKh6iYRyMe4G2ub92UXRXaadPdpmeMxwdaq+aKAgICAgICoIoqCAgICILQICAgICwyICAgICAgICAqCrQoogICAgIKDXFj+FgtLM2eBcoDL+7fLGvXpLuxtf2vndpabvdj2rhfu+XyvG+hIgICAgICAgICAiCKICAgICAgICAiioICAgIgtAgICAgLDIgICAgICAgICoKtCiiAgICAgKoKYBMAmATAJgEwCYBMAmATAJgEwCYBMAmATAJgEwCYBMAiiAgICAiC0CAgICAsMiAgICAgICAgKgq0KKICAgICAqCIICAgICAgICAgICAgICAgIMEQgLmbsIN2uT4b+LokyCQmLEDs4P2OL5b+LIrKAoogICAgIgtAgICAgLDIgICAgICAgICoKtCiiAgICAgICAgICAgICAgICAgICAgICCo1dqq3aXsklzrOu+dlPTj5UspeSA/6u/cy62bNVyrEPNq9VTZo2pfCNQ368alqyq73O8z5+Tpmd2gib9UA7Ptfm6/R6fSUW44PxWr7Ru3qs54PWm9RXnTNYNVZZnAWf5WjJ3enlHzEHc/mJubLN/R03I5cWtH2ncs1c8w+76U1PbtS2SK60OREurNCXlxSj5YH628/e3Nfnb1qbdWzL9ppdTTeo2oWq5vQICAgICILQICAgICwyICAzZdmRYcJZPCDrTUVDJJYbLTlLTySRVElTMQQuQk+0IvnETjh3d8M2VMvsX+z7FmqIuVziYjlHH7r7ROrh1PaCq3gekraeUqatpCfdw5Q7WZ+WWfPJWHi1+j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"/>
     * @see CollectionHelper#variance(Object[], CollectionHelper.FunctionMount)
     * @param arr  target
     * @param <IN> collection element number type
     * @return population variance result
     */
    public static <IN extends Number> IN standardDeviation(IN[] arr) {
        return IteratorForMath.standardDeviation(arr);
    }
    //endregion

    //region sample standard deviation
    /**
     * <h2>Sample Standard Deviation</h2>
     * <p>
     *     In statistics, the sample standard deviation is a measure of the amount of variation or
     *     dispersion of a set of values (from a population).
     * </p>
     * <p>
     *   <ul>
     *     <li>A low standard deviation indicates that the values tend to be close to the mean (also called the expected value) of the set;</li>
     *     <li>A high standard deviation indicates that the values are spread out over a wider range.</li>
     *   </ul>
     * </p>
     * <img src="data:image/jpeg;base64,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"/>
     * @see CollectionHelper#standardDeviationSample(Object[], CollectionHelper.FunctionMount)
     * @param iterator target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return population variance result
     */
    public static <IN, OUT extends Number> OUT standardDeviationSample(Iterator<IN> iterator, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.standardDeviationSample(iterator, fun);
    }

    /**
     * <h2>Sample Standard Deviation</h2>
     * <p>
     *     In statistics, the sample standard deviation is a measure of the amount of variation or
     *     dispersion of a set of values (from a population).
     * </p>
     * <p>
     *   <ul>
     *     <li>A low standard deviation indicates that the values tend to be close to the mean (also called the expected value) of the set;</li>
     *     <li>A high standard deviation indicates that the values are spread out over a wider range.</li>
     *   </ul>
     * </p>
     * <img src="data:image/jpeg;base64,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"/>
     * @see CollectionHelper#standardDeviationSample(Object[], CollectionHelper.FunctionMount)
     * @param col   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return population variance result
     */
    public static <IN, OUT extends Number> OUT standardDeviationSample(Iterable<IN> col, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.standardDeviationSample(col.iterator(), fun);
    }

    /**
     * <h2>Sample Standard Deviation</h2>
     * <p>
     *     In statistics, the sample standard deviation is a measure of the amount of variation or
     *     dispersion of a set of values (from a population).
     * </p>
     * <p>
     *   <ul>
     *     <li>A low standard deviation indicates that the values tend to be close to the mean (also called the expected value) of the set;</li>
     *     <li>A high standard deviation indicates that the values are spread out over a wider range.</li>
     *   </ul>
     * </p>
     * <img src="data:image/jpeg;base64,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"/>
     * @see CollectionHelper#standardDeviationSample(Object[], CollectionHelper.FunctionMount)
     * @param arr   target
     * @param fun   function to get target number
     * @param <IN>  collection element type
     * @param <OUT> result number type
     * @return population variance result
     */
    public static <IN, OUT extends Number> OUT standardDeviationSample(IN[] arr, FunctionMount<IN, OUT> fun) {
        return IteratorForMath.standardDeviationSample(arr, fun);
    }

    /**
     * <h2>Sample Standard Deviation</h2>
     * <p>
     *     In statistics, the sample standard deviation is a measure of the amount of variation or
     *     dispersion of a set of values (from a population).
     * </p>
     * <p>
     *   <ul>
     *     <li>A low standard deviation indicates that the values tend to be close to the mean (also called the expected value) of the set;</li>
     *     <li>A high standard deviation indicates that the values are spread out over a wider range.</li>
     *   </ul>
     * </p>
     * <img src="data:image/jpeg;base64,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"/>
     * @see CollectionHelper#standardDeviationSample(Object[], CollectionHelper.FunctionMount)
     * @param iterator target
     * @param <IN>     collection element number type
     * @return population variance result
     */
    public static <IN extends Number> IN standardDeviationSample(Iterator<IN> iterator) {
        return IteratorForMath.standardDeviationSample(iterator);
    }

    /**
     * <h2>Sample Standard Deviation</h2>
     * <p>
     *     In statistics, the sample standard deviation is a measure of the amount of variation or
     *     dispersion of a set of values (from a population).
     * </p>
     * <p>
     *   <ul>
     *     <li>A low standard deviation indicates that the values tend to be close to the mean (also called the expected value) of the set;</li>
     *     <li>A high standard deviation indicates that the values are spread out over a wider range.</li>
     *   </ul>
     * </p>
     * <img src="data:image/jpeg;base64,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"/>
     * @see CollectionHelper#standardDeviationSample(Object[], CollectionHelper.FunctionMount)
     * @param col  function to get target number
     * @param <IN> collection element number type
     * @return population variance result
     */
    public static <IN extends Number> IN standardDeviationSample(Iterable<IN> col) {
        return IteratorForMath.standardDeviationSample(col.iterator());
    }

    /**
     * <h2>Sample Standard Deviation</h2>
     * <p>
     *     In statistics, the sample standard deviation is a measure of the amount of variation or
     *     dispersion of a set of values (from a population).
     * </p>
     * <p>
     *   <ul>
     *     <li>A low standard deviation indicates that the values tend to be close to the mean (also called the expected value) of the set;</li>
     *     <li>A high standard deviation indicates that the values are spread out over a wider range.</li>
     *   </ul>
     * </p>
     * <img src="data:image/jpeg;base64,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"/>
     * @see CollectionHelper#standardDeviationSample(Object[], CollectionHelper.FunctionMount)
     * @param arr  target
     * @param <IN> collection element number type
     * @return population variance result
     */
    public static <IN extends Number> IN standardDeviationSample(IN[] arr) {
        return IteratorForMath.standardDeviationSample(arr);
    }
    //endregion
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

    //region sort

    /**
     * Sort all elements in iterable as ascendant mode (from smallest to biggest).
     * @param set target iterable.
     * @param <I> element type.
     * @return new iterable result sorted.
     */
    public static <I> IterableResult<I> asc(Iterable<I> set) {
        return IterableResultFactory.getInstanceForSortAsc(set::iterator);
    }

    /**
     * Sort all elements in iterator as ascendant mode (from smallest to biggest).
     * @param iterator target iterator.
     * @param <I> element type.
     * @return new iterable result sorted.
     */
    public static <I> IterableResult<I> asc(Iterator<I> iterator) {
        return IterableResultFactory.getInstanceForSortAsc(iterator);
    }

    /**
     * Sort all elements in iterable as descendant mode (from biggest to smallest).
     * @param set target iterable.
     * @param <I> element type.
     * @return new iterable result sorted.
     */
    public static <I> IterableResult<I> desc(Iterable<I> set) {
        return IterableResultFactory.getInstanceForSortDesc(set::iterator);
    }

    /**
     * Sort all elements in iterator as descendant mode (from biggest to smallest).
     * @param iterator target iterator.
     * @param <I> element type.
     * @return new iterable result sorted.
     */
    public static <I> IterableResult<I> desc(Iterator<I> iterator) {
        return IterableResultFactory.getInstanceForSortDesc(iterator);
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
