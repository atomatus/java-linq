package com.atomatus.linq;

import junit.framework.TestCase;

import java.util.Arrays;

public class CollectionHelperTest extends TestCase {

    private final Integer[] arr0, arr1;

    {
        arr0 = new Integer[]{0, 2, 4, 6, 8};
        arr1 = new Integer[]{2, 3, 5, 7, 9};
    }

    public void testMerge() {
        IterableResult<Integer> ir = CollectionHelper.merge(arr0, arr1);
        assertEquals(arr0.length + arr1.length, ir.count());
        System.out.println("Sample array (arr0):");
        System.out.println(Arrays.toString(arr0));
        System.out.println("Sample array (arr1):");
        System.out.println(Arrays.toString(arr1));
        System.out.println("Merged:");
        System.out.println(ir);
    }

    public void testDistinct() {
        IterableResult<Integer> ir = CollectionHelper.merge(arr0, arr1).distinct();
        assertEquals(9, ir.count());
        System.out.println("Distinct:");
        System.out.println(ir);
    }

    public void testIntersection() {
        IterableResult<Integer> ir = CollectionHelper.intersection(arr0, arr1);
        assertEquals(ir.count(), 1);
        int i = ir.iterator().next();
        assertEquals(2, i);
        System.out.println("Intersection: " + i);
    }

    public void testGroupBy() {
        IterableResultGroup<Integer, Integer> group =
                CollectionHelper.merge(arr0, arr1).groupBy(e -> e % 2);
        assertEquals(2, group.count());
        System.out.println("Group");
        System.out.println("Group by (grouping values pair and odd):");
        group.foreach(System.out::println);
        System.out.println("Group size (count of values in each group):");
        group.size().foreach(System.out::println);
    }

    public void testSum() {
        Integer i = CollectionHelper.sum(arr0);
        System.out.printf("\nSum (all values in arr0): %d\n", i);
        assertEquals(20, i.intValue());

        i = CollectionHelper.sum(arr1);
        System.out.printf("\nSum (all values in arr1): %d\n", i);
        assertEquals(26, i.intValue());
    }

    public void testMin() {
        Integer i = CollectionHelper.min(arr0);
        System.out.printf("Min (all values in arr0): %d\n", i);
        assertEquals(0, i.intValue());

        i = CollectionHelper.min(arr1);
        System.out.printf("Min (all values in arr1): %d\n", i);
        assertEquals(2, i.intValue());
    }

    public void testMax() {
        Integer i = CollectionHelper.max(arr0);
        System.out.printf("Max (all values in arr0): %d\n", i);
        assertEquals(8, i.intValue());

        i = CollectionHelper.max(arr1);
        System.out.printf("Max (all values in arr1): %d\n", i);
        assertEquals(9, i.intValue());
    }

    public void testMean() {
        Integer i = CollectionHelper.mean(arr0);
        System.out.printf("\nMean (all values in arr0): %d\n", i);
        assertEquals(4, i.intValue());

        i = CollectionHelper.mean(arr1);
        System.out.printf("\nMean (all values in arr1): %d\n", i);
        assertEquals(11/2, i.intValue());
    }

    public void testAverage() {
        Integer i = CollectionHelper.average(arr0);
        System.out.printf("\nAverage (all values in arr0): %d\n", i);
        assertEquals(20/5, i.intValue());

        i = CollectionHelper.average(arr1);
        System.out.printf("\nAverage (all values in arr1): %d\n", i);
        assertEquals(26/5, i.intValue());
    }

    public void testMedian(){
        Integer i = CollectionHelper.median(arr0);
        System.out.printf("\nMedian (all values in arr0): %d\n", i);
        assertEquals(4, i.intValue());

        i = CollectionHelper.median(arr1);
        System.out.printf("\nMedian (all values in arr1): %d\n", i);
        assertEquals(5, i.intValue());
    }

    public void testMode(){
        Integer i = CollectionHelper.merge(arr0, arr1).mode();
        System.out.printf("\nMode (from merged arr0 and arr1): %d\n", i);
        assertEquals(2, i.intValue());
    }

    public void testAmplitude(){
        Integer i = CollectionHelper.amplitude(arr0);
        System.out.printf("\nAmplitude (all values in arr0): %d\n", i);
        assertEquals(8, i.intValue());

        i = CollectionHelper.amplitude(arr1);
        System.out.printf("\nAmplitude (all values in arr1): %d\n", i);
        assertEquals(7, i.intValue());
    }

    public void testPopulationVariance() {
        Float pv = CollectionHelper.variance(new Float[]{3.2f, 3.3f, 3.4f, 3.4f, 3.6f, 3.5f, 3.4f});
        assertEquals(0.014285708f, pv);
    }

}