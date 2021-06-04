package com.atomatus.linq;

import java.io.Closeable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

interface IteratorFor2DMatrix extends Closeable {
    Object[][] toMatrix();
}

final class IteratorFor2DMatrixImplIterableMapEntry<K, V> implements IteratorFor2DMatrix {

    private Map<K, V> it;

    public IteratorFor2DMatrixImplIterableMapEntry(Map<K, V> it) {
        this.it = it;
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public Object[][] toMatrix() {
        try {
            Object[] cols = it.keySet().toArray();
            Object[][] table = new Object[2][cols.length];
            table[0] = cols;
            Object[] row = table[1] = new Object[cols.length];

            for(int i=0,c=row.length; i < c; i++) {
                row[i] = it.get(cols[i]);
            }

            return table;
        } finally {
            this.close();
        }
    }

    @Override
    public void close() {
        this.it = null;
    }
}

final class IteratorFor2DMatrixImplIterableMapEntryItResult<K, V extends IterableResult<?>> implements IteratorFor2DMatrix {

    private static final int COLUMNS_BUFFER = 300;

    private Set<Map.Entry<K, V>> it;

    public IteratorFor2DMatrixImplIterableMapEntryItResult(Set<Map.Entry<K, V>> it) {
        this.it = it;
    }

    private Object[] resize(Object[] arr, int compareLength, int offset) {
        Object[] newArr = arr;
        if(newArr.length == compareLength) {
            Object[] aux = newArr;
            newArr = new Object[aux.length + offset];
            System.arraycopy(aux, 0, newArr, 0, aux.length);
        }
        return newArr;
    }

    private Object[] trim(Object[] arr, int len) {
        if(arr.length > len) {
            Object[] columns = new Object[len];
            System.arraycopy(arr, 0, columns, 0, columns.length);
            return columns;
        }
        return arr;
    }

    @Override
    public Object[][] toMatrix() {
        int columnsOffset  = 0;

        Iterator<Map.Entry<K, V>> it = this.it.iterator();

        Object[][] table = new Object[][] { new Object[COLUMNS_BUFFER] } ;
        int maxRowCount  = 0;
        int colOffset = 0;

        while(it.hasNext()) {
            Map.Entry<K, V> entry = it.next();

            //region table columns
            Object[] columns = table[0] = resize(table[0], columnsOffset, COLUMNS_BUFFER);
            columns[columnsOffset++] = entry.getKey();
            //endregion

            //region check add new rows to table
            V values = entry.getValue();
            int rowCount = values.count();
            if(rowCount > maxRowCount) {
                maxRowCount = rowCount;
                Object[][] aux = table;
                table = new Object[maxRowCount + 1 /*columns name row does not match here*/][];
                System.arraycopy(aux, 0, table, 0, aux.length);
                for(int i=aux.length, l=table.length; i < l; i++) {
                    table[i] = new Object[0];
                }
            }
            //endregion

            //region add columns values to each row
            int rowOffset = 1; //first row is for columns name
            for (Object cell : values) {
                Object[] row = table[rowOffset];
                table[rowOffset++] = row = resize(row, row.length, 1);
                row[colOffset] = cell;
            }
            colOffset++;
            //endregion
        }

        //region trim columns offset
        table[0] = this.trim(table[0], columnsOffset);
        //endregion

        return table[0].length == 0 ? new Object[0][] : table;
    }

    @Override
    public void close() {
        this.it = null;
    }
}

final class IteratorFor2DMatrixFactory {

    public static <K, V> IteratorFor2DMatrix getInstanceForMap(Map<K, V> map) {
        return new IteratorFor2DMatrixImplIterableMapEntry<>(map);
    }

    public static <K, V extends IterableResult<?>> IteratorFor2DMatrix getInstanceForEntrySet(Set<Map.Entry<K, V>> it) {
        return new IteratorFor2DMatrixImplIterableMapEntryItResult<>(it);
    }

}
