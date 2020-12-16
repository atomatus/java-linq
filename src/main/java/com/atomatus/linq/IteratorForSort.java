package com.atomatus.linq;

import com.atomatus.util.ArrayHelper;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * <p>Sort collection values using ASC/DESC mode.</p>
 * <br>
 * <p>
 * <h3>{@link SortMode}:</h3>
 * Specifies that the values in the specified collection should be sorted in
 * ascending or descending order.<br>
 * <ul>
 *     <li>{@link SortMode#ASC} sorts from the lowest value to highest value.</li>
 *     <li>{@link SortMode#DESC} sorts from highest value to lowest value.</li>
 * </ul>
 * <i>Null values are treated as the lowest possible values.</i>
 * </p>
 * @param <I>
 */
final class IteratorForSort<I> implements Iterator<I> {

    enum SortMode {
        ASC,
        DESC
    }

    @SuppressWarnings("unchecked")
    static class SortCollection<T> {

        int leftOffset, rightOffset, len;
        Object[] values;

        SortCollection() {
            values = new Object[0];
        }

        int binarySearchIndex(Object[] arr, T value, int low, int high, int candidate) {
            if(low > high) {
                return candidate;
            }

            int mid = (low + high) / 2;

            switch (IteratorForMath.compare(value, (T) arr[mid])) {
                case -1: //value < arr[mid]
                    return binarySearchIndex(arr, value, low, mid - 1, mid);
                case 1: //value > arr[mid]
                    return binarySearchIndex(arr, value, mid + 1, high, mid + 1);
                default: //value == arr[mid]
                    return mid;
            }
        }

        void push(T value) {
            int i = binarySearchIndex(values, value, 0, values.length - 1, 0);
            values = ArrayHelper.insertAt(values, value, i);
            rightOffset = len = values.length;
        }

        boolean hasNext() {
            return (leftOffset < values.length && rightOffset > 0);
        }

        /**
         * Get and remove first element on array.
         */
        T shift() {
            if(leftOffset >= len) throw new NoSuchElementException();
            return (T) values[leftOffset++];
        }

        /**
         * Get and remove last element on array.
         */
        T pop() {
            if(rightOffset <= 0) throw new NoSuchElementException();
            return (T) values[--rightOffset];
        }
    }

    private CollectionHelper.FunctionGet<Iterator<I>> colFun;
    private Iterator<I> iterator;
    private final SortMode mode;
    private SortCollection<I> sort;

    IteratorForSort(CollectionHelper.FunctionGet<Iterator<I>> colFun, SortMode mode) {
        this.colFun = Objects.requireNonNull(colFun);
        this.mode = Objects.requireNonNull(mode);
    }

    IteratorForSort(Iterator<I> iterator, SortMode mode) {
        this.iterator = Objects.requireNonNull(iterator);
        this.mode = Objects.requireNonNull(mode);
    }

    private void checkInit() {
        if(sort == null) {
            try {
                sort = new SortCollection<>();
                Iterator<I> iterator = this.colFun != null ? this.colFun.get() : this.iterator;
                while (iterator.hasNext()) {
                    sort.push(iterator.next());
                }
            } finally {
                iterator = null;
                colFun = null;
            }
        }
    }

    @Override
    public boolean hasNext() {
        checkInit();
        return sort.hasNext();
    }

    @Override
    public I next() {
        checkInit();
        return mode == SortMode.ASC ? sort.shift() : sort.pop();
    }
}
