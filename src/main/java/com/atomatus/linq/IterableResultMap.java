package com.atomatus.linq;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Generated result map from collection, set or array iteration or filter using {@link CollectionHelper}
 * or another {@link IterableResult} or {@link IterableResultGroup}.
 *
 * @param <K> iterable element key type
 * @param <V> iterable element value type
 * @author Carlos Matos
 */
public abstract class IterableResultMap<K, V> implements Iterable<Map.Entry<K, V>> {

    interface IteratorMap<K, V> extends Iterator<Map.Entry<K, V>> {

        IterableResult<K> keySet();

        IterableResult<V> values();

        int count();

        boolean isEmpty();

        Map<K, V> toMap();

        Set<Map.Entry<K, V>> toSet();

        V get(K key);

        Map.Entry<K, V> minEntry();

        Map.Entry<K, V> maxEntry();

        void foreach(CollectionHelper.ForEachEntryConsumer<Map.Entry<K, V>> action);
    }

    private IteratorMap<K, V> iterator;

    protected abstract IteratorMap<K, V> initIterator();

    protected final synchronized IteratorMap<K, V> getIterator() {
        if (iterator == null) {
            iterator = this.initIterator();
        }
        return iterator;
    }

    @Override
    public final Iterator<Map.Entry<K, V>> iterator() {
        return getIterator();
    }

    @Override
    public String toString() {
        return this.toMap().toString();
    }

    /**
     * All keys of iterable result map.
     *
     * @return iterable result with all keys.
     */
    public final IterableResult<K> keySet() {
        return getIterator().keySet();
    }

    /**
     * All values of iterable result map.
     *
     * @return iterable result with all values.
     */
    public final IterableResult<V> values() {
        return getIterator().values();
    }

    /**
     * Count of element.
     *
     * @return count of elements.
     */
    public int count() {
        return getIterator().count();
    }

    /**
     * Check if is empty.
     *
     * @return when true, iterable result is empty.
     */
    public boolean isEmpty() {
        return getIterator().isEmpty();
    }

    /**
     * Check if result contains data.
     *
     * @return when true, iterable result contains some data.
     */
    public boolean any() {
        return !getIterator().isEmpty();
    }

    /**
     * Generate a map.
     *
     * @return instance of map within data of iterable result map.
     */
    public Map<K, V> toMap() {
        return getIterator().toMap();
    }

    /**
     * Generate a set of result.
     *
     * @return instance of set within all entry data of iterable result map.
     */
    public Set<Map.Entry<K, V>> toSet() {
        return getIterator().toSet();
    }

    /**
     * Recover value by key.
     * @param key access key.
     * @return value indexed to key.
     */
    public V get(K key) {
        return getIterator().get(key);
    }

    /**
     * Recovery min entry value (compare only values) on result map.
     * @return min entry on result map.
     */
    public Map.Entry<K, V> minEntry() {
        return getIterator().minEntry();
    }

    /**
     * Recovery max entry value (compare only values) on result map.
     * @return max entry on result map.
     */
    public Map.Entry<K, V> maxEntry() {
        return getIterator().maxEntry();
    }

    /**
     * A simple foreach action.
     *
     * @param action action to recover each element on collection
     */
    public void foreach(CollectionHelper.ForEachEntryConsumer<Map.Entry<K, V>> action) {
        getIterator().foreach(action);
    }
}
