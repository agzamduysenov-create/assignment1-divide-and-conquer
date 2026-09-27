package daa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// tests for merge sort and quick sort: compare with Arrays.sort()
class SortingTest {

    private static final int[] SIZES = {0, 1, 2, 3, 15, 16, 17, 100, 1000, 10000, 100000};
    private static final String[] TYPES = {"random", "sorted", "reverse", "duplicates"};

    private static void checkBoth(int[] input) {
        int[] expected = input.clone();
        Arrays.sort(expected);

        int[] a = input.clone();
        new MergeSorter().sort(a);
        assertArrayEquals(expected, a, "MergeSort failed for n=" + input.length);

        int[] b = input.clone();
        new QuickSorter().sort(b);
        assertArrayEquals(expected, b, "QuickSort failed for n=" + input.length);
    }

    @Test
    @DisplayName("All input types and sizes match Arrays.sort")
    void allTypesAndSizes() {
        long seed = 1;
        for (String type : TYPES) {
            for (int n : SIZES) {
                checkBoth(Experiment.generateArray(type, n, seed++));
            }
        }
    }

    @Test
    @DisplayName("Empty array")
    void emptyArray() {
        checkBoth(new int[0]);
    }

    @Test
    @DisplayName("Single element")
    void singleElement() {
        checkBoth(new int[]{42});
    }

    @Test
    @DisplayName("Null input is ignored")
    void nullInput() {
        new MergeSorter().sort(null);
        new QuickSorter().sort(null);
    }

    @Test
    @DisplayName("All elements equal")
    void allEqual() {
        int[] a = new int[50000];
        Arrays.fill(a, 7);
        checkBoth(a);
    }

    @Test
    @DisplayName("Negative numbers and extreme values")
    void extremes() {
        checkBoth(new int[]{Integer.MAX_VALUE, -1, 0, Integer.MIN_VALUE, 5, -5, Integer.MAX_VALUE, Integer.MIN_VALUE});
    }

    @Test
    @DisplayName("200 random arrays of random length")
    void manyRandom() {
        Random r = new Random(123);
        for (int t = 0; t < 200; t++) {
            int n = r.nextInt(2000);
            int[] a = new int[n];
            int bound = 1 + r.nextInt(1000);
            for (int i = 0; i < n; i++) a[i] = r.nextInt(bound) - bound / 2;
            checkBoth(a);
        }
    }

    @Test
    @DisplayName("QuickSort recursion depth stays O(log n) (smaller-first)")
    void quickSortDepthIsLogarithmic() {
        for (String type : TYPES) {
            int n = 200000;
            int[] a = Experiment.generateArray(type, n, 99);
            QuickSorter qs = new QuickSorter(new Metrics(), new Random(5));
            qs.sort(a);
            // smaller part is at most half, so depth <= log2(n) + 2
            int bound = (int) Math.floor(Math.log(n) / Math.log(2)) + 2;
            assertTrue(qs.getMetrics().getMaxDepth() <= bound,
                    "depth " + qs.getMetrics().getMaxDepth() + " > " + bound + " for " + type);
        }
    }

    @Test
    @DisplayName("MergeSort recursion depth is about log2(n/cutoff)")
    void mergeSortDepth() {
        int n = 100000;
        int[] a = Experiment.generateArray("random", n, 3);
        MergeSorter ms = new MergeSorter();
        ms.sort(a);
        int expectedMax = (int) Math.ceil(Math.log((double) n / MergeSorter.CUTOFF) / Math.log(2)) + 2;
        assertTrue(ms.getMetrics().getMaxDepth() <= expectedMax,
                "depth " + ms.getMetrics().getMaxDepth() + " > " + expectedMax);
    }
}
