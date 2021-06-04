package com.atomatus.linq;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Base for dataframe files within separator character.
 */
abstract class AnalyzerForSepChar extends Analyzer {

    private final class IteratorGroupForSepChar implements IteratorGroup<String, String> {

        private class AnalyzerEntry implements Map.Entry<String, IterableResult<String>> {

            private class IteratorForColValues implements Iterator<String> {

                final Iterator<IterableResult<String>> rows;

                {
                    rows = values.iterator();
                }

                @Override
                public boolean hasNext() {
                    return rows.hasNext();
                }
                @Override
                public String next() {
                    IterableResult<String> row = rows.next();
                    return (keyIndex == 0 ? row.take(1) : row.jump(keyIndex).take(1)).iterator().next();
                }
            }

            private class IterableResultForColValues extends IterableResult<String> {
                @Override
                public Iterator<String> iterator() {
                    return new IteratorForColValues();
                }
            }

            private final int keyIndex;
            private final String key;
            private final Iterable<IterableResult<String>> values;
            private transient IterableResult<String> colValues;

            public AnalyzerEntry(int keyIndex, String key, Iterable<IterableResult<String>> values) {
                this.keyIndex   = keyIndex;
                this.key        = Objects.requireNonNull(key);
                this.values     = Objects.requireNonNull(values);
            }

            public AnalyzerEntry(int keyIndex, IterableResult<String> keys, Iterable<IterableResult<String>> values) {
                this(keyIndex, (keyIndex == 0 ? keys.take(1) : keys.jump(keyIndex).take(1)).iterator().next(), values);
            }

            @Override
            public String getKey() {
                return key;
            }

            @Override
            public IterableResult<String> getValue() {
                return colValues == null ? (colValues = new IterableResultForColValues()) : colValues;
            }

            @Override
            public IterableResult<String> setValue(IterableResult<String> value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String toString() {
                return key + "=" + IteratorForJoin.toString(values);
            }
        }

        private class IterableResultMerged extends IterableResult<IterableResult<String>> {

            final List<IterableResult<String>> merged;
            int count;

            public IterableResultMerged(){
                this.merged = new ArrayList<>();
            }

            public IterableResultMerged push(IterableResult<String> other) {
                synchronized (merged) {
                    merged.add(other);
                    count++;
                    return this;
                }
            }

            @Override
            public Iterator<IterableResult<String>> iterator() {
                synchronized (merged) {
                    return new Iterator<IterableResult<String>>() {
                        int index;

                        @Override
                        public boolean hasNext() {
                            synchronized (merged) {
                                return index < count || nextLine(false, true);
                            }
                        }

                        @Override
                        public IterableResult<String> next() {
                            synchronized (merged) {
                                if (index < count) {
                                    return merged.get(index++);
                                } else {
                                    throw new NoSuchElementException();
                                }
                            }
                        }
                    };
                }
            }
        }

        private class OneShotIteratorBase extends IterableResult<Map.Entry<String, IterableResult<String>>>  implements Iterator<Map.Entry<String, IterableResult<String>>> {

            private boolean done, busy;
            private final CollectionHelper.FunctionGet<Boolean> hasNextFun;
            private final CollectionHelper.FunctionGet<Map.Entry<String, IterableResult<String>>> nextFun;

            OneShotIteratorBase(CollectionHelper.FunctionGet<Boolean> hasNextFun,
                                CollectionHelper.FunctionGet<Map.Entry<String, IterableResult<String>>> nextFun) {
                this.hasNextFun = hasNextFun;
                this.nextFun    = nextFun;
            }

            @Override
            public boolean hasNext() {
                boolean hasNext = !done && hasNextFun.get();
                if(!done && !hasNext) {
                    IteratorGroupForSepChar.this.reset();
                    done = true;
                }
                return hasNext;
            }

            @Override
            public Map.Entry<String, IterableResult<String>> next() {
                return nextFun.get();
            }

            @Override
            public synchronized Iterator<Map.Entry<String, IterableResult<String>>> iterator() {
                if(busy) throw new IllegalStateException("Iterator is busy! Require how a new function.");
                IteratorGroupForSepChar.this.reset();
                busy = true;
                return this;
            }
        }

        private BufferedReader reader;
        private boolean isOpen, isClosed;
        private int lineIndex, count, keyIndex, keyCount;
        private String nextLine;
        private IterableResult<String> keys;
        private IterableResultMerged values;
        private final IteratorForGroupCalculator<String, String> calculator;
        private final Object lock;

        {
            count = -1;
            lock  = new Object();
            calculator = new IteratorForGroupCalculator<>(this::getOneShotIterableResettable);
        }

        private void checkInit(boolean isThrowsException) {
            if(isClosed) {
                if(isThrowsException) {
                    throw new UnsupportedOperationException("Iterator for Analyser is closed!");
                }
            } else if(reader == null) {
                reader = initReaderFromFilename();
                isOpen = true;
            }
        }

        private BufferedReader initReaderFromFilename() {
            return isLocalFile() ? initReaderForLocal() : initReaderForUrl();
        }

        private BufferedReader initReaderForUrl() {
            try {
                String charset = getCharset().name();
                HttpURLConnection connection = (HttpURLConnection) new URL(getFilename()).openConnection();
                connection.setRequestProperty("Accept-Charset", charset);
                connection.connect();
                InputStream response = connection.getInputStream();
                return new BufferedReader(new InputStreamReader(response, charset));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private BufferedReader initReaderForLocal() {
            try {
                return new BufferedReader(new FileReader(getFilename()));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        private void closeReader(){
            if(!isClosed) {
                try {
                    if(isOpen) {
                        reader.close();
                    }
                } catch (IOException ignored) {
                } finally {
                    isOpen = false;
                    isClosed = true;
                    lineIndex = -1;
                    reader = null;
                }
            }
        }

        private void checkDiscoverySeparatorChar(){
            if(lineIndex == 0 && isRequestSeparatorChar() && hasNotSeparatorChar()) {
                setSeparatorChar(getIdentifySeparatorCharByLine(nextLine));
            }
        }

        private boolean nextLine(boolean isThrowsExceptionOnCheckInit, boolean isMountLine) {
            checkInit(isThrowsExceptionOnCheckInit);
            boolean ready = nextLine != null;
            if(!ready) {
                try {
                    if (isOpen) {
                        nextLine = reader.readLine();
                        ready = nextLine != null && nextLine.length() != 0;
                    }
                } catch (IOException ignored) { } finally {
                    if (!ready) {
                        closeReader();
                    }
                }
            }

            if(ready && isMountLine){
                checkDiscoverySeparatorChar();
                String aux = nextLine;
                nextLine = null;
                lineIndex++;
                IterableResult<String> entries = mountEntries(aux);
                if (keys == null) {
                    keys = entries;
                    keyCount = keys.count();
                    return nextLine(isThrowsExceptionOnCheckInit, true);
                } else {
                    values = values == null ? new IterableResultMerged().push(entries) : values.push(entries);
                }
            }

            return ready;
        }

        private boolean checkNextEntry(boolean isMountLine){
            return keys != null ? keyIndex < keyCount : nextLine(false, isMountLine);
        }

        private void reset(){
            synchronized (lock) {
                keyIndex = 0;
            }
        }

        private OneShotIteratorBase getOneShotIterableNotLockedResettable(){
            return new OneShotIteratorBase(this::hasNextLocal, this::nextLocal);
        }

        private OneShotIteratorBase getOneShotIterableResettable(){
            return new OneShotIteratorBase(this::hasNext, this::next);
        }

        private boolean hasNextLocal(){
            return checkNextEntry(false);
        }

        private Map.Entry<String, IterableResult<String>> nextLocal() {
            if (checkNextEntry(true)) {
                return new AnalyzerEntry(keyIndex++, keys, values);
            } else {
                throw new NoSuchElementException();
            }
        }

        private Map.Entry<String, IterableResult<String>> compareEntry(CollectionHelper.FunctionComparer<Long> comparer) {
            synchronized (lock) {
                return CollectionHelper.reduce(this.getOneShotIterableNotLockedResettable().iterator(), (acc, curr) -> {
                    IterableResult<String> v0 = acc.getValue();
                    IterableResult<String> v1 = curr.getValue();
                    Long c0 = v0 == null ? 0L : v0.sum(e -> e == null ? 0L : e.length());
                    Long c1 = v1 == null ? 0L : v1.sum(e -> e == null ? 0L : e.length());
                    return comparer.compare(c0, c1) ? acc : curr;
                });
            }
        }

        @Override
        public boolean hasNext() {
            synchronized (lock) {
                return hasNextLocal();
            }
        }

        @Override
        public Map.Entry<String, IterableResult<String>> next() {
            synchronized (lock) {
                return nextLocal();
            }
        }

        @Override
        public IterableResult<String> keySet() {
            synchronized (lock) {
                if (keys != null || nextLine(true, true)) {
                    return keys;
                } else {
                    throw new UnsupportedOperationException("File is empty!");
                }
            }
        }

        @Override
        public IterableResult<IterableResult<String>> values() {
            synchronized (lock) {
                if (values != null || nextLine(true, true)) {
                    return values;
                } else {
                    throw new UnsupportedOperationException("File is empty!");
                }
            }
        }

        @Override
        public int count() {
            synchronized (lock){
                if(count != -1) {
                    return count;
                } else if(!nextLine(true, false)) {
                    return count = lineIndex;
                } else {
                    int lines = 0;
                    try (BufferedReader reader = initReaderFromFilename()) {
                        while (reader.readLine() != null) lines++;
                    } catch (IOException ignored) { }
                    return count = lines;
                }
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (lock) {
                return keys == null && values == null && count == -1 && !nextLine(false, false);
            }
        }

        @Override
        public Map<String, IterableResult<String>> toMap() {
            synchronized (lock) {
                Map<String, IterableResult<String>> map = new HashMap<>();
                for(Map.Entry<String, IterableResult<String>> entry : getOneShotIterableNotLockedResettable()) {
                    map.put(entry.getKey(), entry.getValue());
                }
                return map;
            }
        }

        @Override
        public Set<Map.Entry<String, IterableResult<String>>> toSet() {
            synchronized (lock){
                return getOneShotIterableNotLockedResettable().toSet();
            }
        }

        @Override
        public Object[][] toTable() {
            return IteratorFor2DMatrixFactory
                    .getInstanceForEntrySet(this.toSet())
                    .toMatrix();
        }

        @Override
        public IterableResult<String> get(String key){
            synchronized (lock){
                for (Map.Entry<String, IterableResult<String>> entry : getOneShotIterableNotLockedResettable()) {
                    String k = entry.getKey();
                    if ((k == null && key == null) || (k != null && k.equalsIgnoreCase(key))) {
                        return entry.getValue();
                    }
                }
            }
            return null;
        }

        @Override
        public Map.Entry<String, IterableResult<String>> minEntry() {
            return compareEntry((a, b) -> a < b);
        }

        @Override
        public Map.Entry<String, IterableResult<String>> maxEntry() {
            return compareEntry((a, b) -> a > b);
        }

        @Override
        public void foreach(CollectionHelper.ForEachEntryConsumer<Map.Entry<String, IterableResult<String>>> action) {
            synchronized (lock){
                for(Map.Entry<String, IterableResult<String>> entry : getOneShotIterableNotLockedResettable()) {
                    action.accept(entry);
                }
            }
        }

        /**
         * Generate an iterable result with the amount of items in each entry.
         *
         * @return instance of IterableResultMap in lazy mode, will mount map on request.
         */
        @Override
        public IterableResultMap<String, Integer> size() {
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
        public <N extends Number> IterableResultMap<String, N> sum(Class<N> resultClass) {
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
        public <N extends Number> IterableResultMap<String, N> sum(CollectionHelper.FunctionMount<String, N> mountFun) {
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
        public <N extends Number> IterableResultMap<String, N> average(Class<N> resultClass) {
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
        public <N extends Number> IterableResultMap<String, N> average(CollectionHelper.FunctionMount<String, N> mountFun) {
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
        public <N extends Number> IterableResultMap<String, N> mean(Class<N> resultClass) {
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
        public <N extends Number> IterableResultMap<String, N> mean(CollectionHelper.FunctionMount<String, N> mountFun) {
            return calculator.mean(mountFun);
        }

        /**
         * Recover minimum value of collection
         *
         * @return minimum value
         */
        @Override
        public IterableResultMap<String, String> min() {
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
        public <C extends Comparable<C>> IterableResultMap<String, String> min(CollectionHelper.FunctionMount<String, C> mountFun) {
            return calculator.min(mountFun);
        }

        /**
         * Recover maximum value of collection
         *
         * @return maximum value
         */
        @Override
        public IterableResultMap<String, String> max() {
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
        public <C extends Comparable<C>> IterableResultMap<String, String> max(CollectionHelper.FunctionMount<String, C> mountFun) {
            return calculator.max(mountFun);
        }

        @Override
        public IterableResultGroup<String, String> sample(CollectionHelper.CompareEntryValid<String> checkFun) {
            return calculator.sample(checkFun);
        }

        @Override
        public IterableResultGroup<String, String> amount(int count) {
            return calculator.amount(count);
        }

        public void close() {
            closeReader();
            keys = null;
            values = null;
        }
    }

    private IteratorGroupForSepChar iteratorGroupForSepChar;
    private final char[] defaultChars;

    protected AnalyzerForSepChar(String filename, char separatorChar, char[] defaultChars) {
        super(filename, separatorChar, true);
        this.defaultChars = Objects.requireNonNull(defaultChars);
    }

    //region getIdentifySeparatorCharByLine
    protected final char getIdentifySeparatorCharByLine(String line, char... chars) {
        this.requireNonClosed();
        int len;
        if((len = Objects.requireNonNull(line).length()) == 0){
            throw new IllegalArgumentException("String is empty!");
        }

        int[] cArr = new int[chars.length];
        int maxi = 0;
        for(int i=0, l = chars.length; i < len; i++) {
            char c = line.charAt(i);
            for(int j=0; j < l; j++){
                if(c == chars[j]) {
                    int cc = ++cArr[j];
                    if(maxi != j && cArr[maxi] < cc) {
                        maxi = j;
                    }
                }
            }
        }

        return chars[maxi];
    }

    protected final char getIdentifySeparatorCharByLine(String line) {
        return this.getIdentifySeparatorCharByLine(line, defaultChars);
    }
    //endregion

    //region mountEntries
    protected IterableResult<String> mountEntries(String line) {
        if(hasNotSeparatorChar()) {
            throw new UnsupportedOperationException("Can not mount line entries by default method without a separator!");
        }
        return new IterableResultSplit(line, getSeparatorChar());
    }
    //endregion

    //region Analyzer
    @Override
    protected IteratorGroup<String, String> initIterator() {
        if(iteratorGroupForSepChar != null){
            iteratorGroupForSepChar.close();
        }
        return iteratorGroupForSepChar = new IteratorGroupForSepChar();
    }

    @Override
    protected void onClose() {
        super.onClose();
        if(iteratorGroupForSepChar != null) {
            iteratorGroupForSepChar.close();
            iteratorGroupForSepChar = null;
        }
    }
    //endregion
}
