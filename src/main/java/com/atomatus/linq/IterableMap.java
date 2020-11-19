package com.atomatus.linq;

import java.util.Iterator;
import java.util.Map;

interface IterableMap<K, V> extends Map<K, V>, Iterable<Map.Entry<K, V>> {

    Iterator<K> iteratorKeys();

    Iterator<V> iteratorValues();

    Entry<K, V> minEntry();

    Entry<K, V> maxEntry();

    void foreach(CollectionHelper.ForEachEntryConsumer<Entry<K, V>> action);
}
