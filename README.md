# Java Collection Linq and Analyzer Project
<p>
A library to help developer manipulate iterables collections or arrays, 
how like, linq principle to select, find, filter, group and manipulate elements 
on these set. You can generate map groups to help data analyses, 
checking elements by conditions on set, filtering data or applying simple math 
operations.
</p>

## Iterable Result
All request generates a type of iterable result, each iterable result is not result of execution operation in time, 
but a scheduled of execution that's will be applied only when iterator is request, forced by method iterator(), or 
iteration in a loop.

### IterableResult
Simple iterable result contained methods to schedule new manipulate action on future elements result.

### IterableResultGroup
Iterable result for elements grouped, each group contains the proposed key and set of elements how values of each key. 

### IterableResultMap
Iterable result map that is base class of iterableResultGroup, contains methods
to filter and manipulate like a dictionary.

### CollectionHelper
<p>
Set of static methods to help for query actions (from any iterable, set or array) to
manipulate, filter, find, group elements on set or array fastestway for reducing code.
</p>
<p>
Every query action result generate an IterableResult, IterableResultGroup or IterableResultMap,
all thus are tree schedules Iterator actions that will be mounted and executed only when do a directed request it.
</p>

```
//Samples
Integer[] arr0 = new Integer[]{0, 2, 4, 6, 8};
Integer[] arr1 = new Integer[]{2, 3, 5, 7, 9};
```

```
System.out.println("\nMerge:");
IterableResult<Integer> result = CollectionHelper.merge(arr0, arr1);
result.foreach(System.out::println);
```
``
Merge: 
0
2
4
6
8
2
3
5
7
9
``

```
System.out.println("\nDistinct:");
result = result.distinct();
result.foreach(System.out::println);
```
``
Distinct:
0
2
4
6
8
3
5
7
9
``

```
System.out.println("\nIntersection:");
CollectionHelper
    .intersection(arr0, arr1)
    .foreach(System.out::println);
```
``
Intersection:
2
``

```
IterableResultGroup<Integer, Integer> group = result.groupBy(e -> e % 2);
System.out.println("\nGroup by (grouping values pair and odd):");
group.foreach(System.out::println);
```

``Group by (grouping values pair and odd):``

``0=[0, 2, 4, 6, 8]``

``1=[3, 5, 7, 9]``

```
System.out.println("\nGroup size (count of values in each group):");
group.size().foreach(System.out::println);
```

``Group size (count of values in each group):``

``0=5``

``1=4``


```
System.out.println("\nGroup sum (sum of all values in each group - needs explicit set of Number class type or Function to sum operation):");
group.sum(Integer.class).foreach(System.out::println);
```

``Group sum (sum of all values in each group - needs explicit set of Number class type or Function to sum operation):``

``0=20``

``1=24``


```
Integer i = CollectionHelper.sum(arr0);
System.out.printf("\nSum (all values in arr0):\n%d\n", i);

i = CollectionHelper.sum(arr1);
System.out.printf("\nSum (all values in arr1):\n%d\n", i);
```

``Sum (all values in arr0): 20``

``Sum (all values in arr1): 26``

```
i = CollectionHelper.min(arr0);
System.out.printf("\nMin (all values in arr0):\n%d\n", i);

i = CollectionHelper.min(arr1);
System.out.printf("\nMin (all values in arr1):\n%d\n", i);
```

``Min (all values in arr0): 0`` 
 
``Min (all values in arr1): 2``

```
i = CollectionHelper.max(arr0);
System.out.printf("\Max (all values in arr0):\n%d\n", i);

i = CollectionHelper.max(arr1);
System.out.printf("\Max (all values in arr1):\n%d\n", i);
```

``Max (all values in arr0): 8``  

``Max (all values in arr1): 9``

```
i = CollectionHelper.mean(arr0);
System.out.printf("\Mean (all values in arr0):\n%d\n", i);

i = CollectionHelper.mean(arr1);
System.out.printf("\Mean (all values in arr1):\n%d\n", i);
```

``Mean (all values in arr0): 4``  

``Mean (all values in arr1): 5``

```
System.out.println("\nAny (value equals 2):");
System.out.println(CollectionHelper.any(arr0, e -> e == 2));

System.out.println("\nAll (values pair in arr0):");
System.out.println(CollectionHelper.all(arr0, e -> e % 2 == 0));

System.out.println("\nAll (values pair in arr1):");
System.out.println(CollectionHelper.all(arr1, e -> e % 2 == 0));
```

``Any (value equals 2): true``

``All (values pair in arr0): true``

``All (values pair in arr1): false``

## Analyzer
<p>
Last but not least, Analyzer. This class can be used to load spreadsheets files (from local system disk or using https://, http:// or file:// protocol), how like, CSV to 
analize and manipulate data. Generating IterableResult, IterableResultGroup, IterableResultMap.
</p>

```
//Example, analyzing how many patients have obesity.
//obs.: real data of patients with respiratory conditions between years 2018 and 2019 in Brazil.
try(Analyzer a = Analyzer.load("https://raw.githubusercontent.com/chcmatos/nanodegree_py_analyze_srag/main/doc/influd18_limpo-final.csv")) {
    System.out.printf("Were found %d patients having obesity.", a.get("OBESIDADE").asInteger().count(e -> e == 1));
}
```

``Were found 1466 patients having obesity.``
