package daa;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

// runs all experiments and saves results to csv
public class Experiment {

    public static final int WARMUP = 2;   // runs before measuring (for JIT)
    public static final int TRIALS = 5;   // we take median of 5 runs

    public static final int[] SIZES = {1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000};
    public static final int BRUTE_MAX = 20000;  // brute force is too slow after this

    public static final String[] TYPES = {"random", "sorted", "reverse", "duplicates"};
    public static final String[] POINT_TYPES = {"random", "duplicates"};

    public static final String HEADER =
            "algorithm,input_type,n,size_class,time_ms,max_depth,recursive_calls,comparisons,swaps,allocations";

    private Random seeds = new Random(42);
    private List<String> rows = new ArrayList<>();
    private boolean print;

    // so java does not delete "unused" results
    public static long sink = 0;

    public static int[] generateArray(String type, int n, long seed) {
        Random r = new Random(seed);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            if (type.equals("random")) {
                a[i] = r.nextInt();
            } else if (type.equals("sorted")) {
                a[i] = i;
            } else if (type.equals("reverse")) {
                a[i] = n - i;
            } else if (type.equals("duplicates")) {
                a[i] = r.nextInt(10);   // only 10 different numbers
            } else {
                throw new IllegalArgumentException("unknown type " + type);
            }
        }
        return a;
    }

    public static Point[] generatePoints(String type, int n, long seed) {
        Random r = new Random(seed);
        Point[] p = new Point[n];
        int side = Math.max(2, (int) Math.sqrt(n) / 2);  // small grid -> many same points
        for (int i = 0; i < n; i++) {
            if (type.equals("random")) {
                p[i] = new Point(r.nextDouble() * 1000000, r.nextDouble() * 1000000);
            } else if (type.equals("duplicates")) {
                p[i] = new Point(r.nextInt(side), r.nextInt(side));
            } else {
                throw new IllegalArgumentException("unknown type " + type);
            }
        }
        return p;
    }

    public static String sizeClass(int n) {
        if (n <= 5000) return "small";
        if (n <= 100000) return "medium";
        return "large";
    }

    public List<String> runAll(boolean print) {
        this.print = print;
        rows.clear();

        // sorting
        for (String type : TYPES) {
            for (int n : SIZES) {
                runSort("MergeSort", type, n);
                runSort("QuickSort", type, n);
                runSort("ArraysSort", type, n);
            }
        }
        // select
        for (String type : TYPES) {
            for (int n : SIZES) {
                runSelect("DeterministicSelect", type, n);
                runSelect("SortThenIndex", type, n);
            }
        }
        // closest pair
        for (String type : POINT_TYPES) {
            for (int n : SIZES) {
                runClosest("ClosestPair", type, n);
                if (n <= BRUTE_MAX) {
                    runClosest("ClosestPairBrute", type, n);
                }
            }
        }
        return rows;
    }

    private void runSort(String algo, String type, int n) {
        long[] times = new long[TRIALS];
        Metrics m = null;
        for (int t = 0; t < WARMUP + TRIALS; t++) {
            long seed = seeds.nextLong();
            int[] a = generateArray(type, n, seed);
            long start = System.nanoTime();
            if (algo.equals("MergeSort")) {
                MergeSorter s = new MergeSorter();
                s.sort(a);
                m = s.getMetrics();
            } else if (algo.equals("QuickSort")) {
                QuickSorter s = new QuickSorter(new Metrics(), new Random(seed));
                s.sort(a);
                m = s.getMetrics();
            } else {
                Arrays.sort(a);
                m = null;
            }
            long time = System.nanoTime() - start;
            checkSorted(a, algo);
            if (t >= WARMUP) {
                times[t - WARMUP] = time;
            }
        }
        save(algo, type, n, times, m);
    }

    private void runSelect(String algo, String type, int n) {
        long[] times = new long[TRIALS];
        Metrics m = null;
        for (int t = 0; t < WARMUP + TRIALS; t++) {
            int[] a = generateArray(type, n, seeds.nextLong());
            long start = System.nanoTime();
            if (algo.equals("DeterministicSelect")) {
                DeterministicSelector s = new DeterministicSelector();
                sink += s.select(a, n / 2);
                m = s.getMetrics();
            } else {
                Arrays.sort(a);
                sink += a[n / 2];
                m = null;
            }
            long time = System.nanoTime() - start;
            if (t >= WARMUP) {
                times[t - WARMUP] = time;
            }
        }
        save(algo, type, n, times, m);
    }

    private void runClosest(String algo, String type, int n) {
        long[] times = new long[TRIALS];
        Metrics m = null;
        for (int t = 0; t < WARMUP + TRIALS; t++) {
            Point[] p = generatePoints(type, n, seeds.nextLong());
            long start = System.nanoTime();
            if (algo.equals("ClosestPair")) {
                ClosestPairSolver s = new ClosestPairSolver();
                sink += (long) s.solve(p).distance;
                m = s.getMetrics();
            } else {
                sink += (long) ClosestPairSolver.bruteForce(p).distance;
                m = new Metrics();
                m.addComparisons((long) n * (n - 1) / 2);
            }
            long time = System.nanoTime() - start;
            if (t >= WARMUP) {
                times[t - WARMUP] = time;
            }
        }
        save(algo, type, n, times, m);
    }

    private void save(String algo, String type, int n, long[] times, Metrics m) {
        Arrays.sort(times);
        double ms = times[TRIALS / 2] / 1000000.0;   // median
        String row;
        if (m == null) {
            row = String.format(Locale.US, "%s,%s,%d,%s,%.4f,,,,,", algo, type, n, sizeClass(n), ms);
        } else {
            row = String.format(Locale.US, "%s,%s,%d,%s,%.4f,%d,%d,%d,%d,%d", algo, type, n, sizeClass(n), ms,
                    m.getMaxDepth(), m.getRecursiveCalls(), m.getComparisons(), m.getSwaps(), m.getAllocations());
        }
        rows.add(row);
        if (print) {
            System.out.printf(Locale.US, "%-20s %-11s n=%-9d time=%10.3f ms  %s%n",
                    algo, type, n, ms, m == null ? "" : m.toString());
        }
    }

    private static void checkSorted(int[] a, String algo) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) {
                throw new RuntimeException(algo + " is not sorted!");
            }
        }
    }

    public static void writeCsv(List<String> rows, String file) throws IOException {
        PrintWriter w = new PrintWriter(new FileWriter(file));
        w.println(HEADER);
        for (String r : rows) {
            w.println(r);
        }
        w.close();
    }
}
