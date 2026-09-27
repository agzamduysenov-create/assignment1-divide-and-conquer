package daa;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException {
        String mode = "all";
        if (args.length > 0) {
            mode = args[0];
        }

        if (mode.equals("demo")) {
            demo();
        } else if (mode.equals("experiment")) {
            experiment();
        } else {
            demo();
            experiment();
        }
    }

    private static void demo() {
        System.out.println("=== Demo ===");
        Random r = new Random(7);
        int[] arr = new int[20];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = r.nextInt(100);
        }
        System.out.println("Input array      : " + Arrays.toString(arr));

        int[] a = arr.clone();
        MergeSorter ms = new MergeSorter();
        ms.sort(a);
        System.out.println("MergeSort        : " + Arrays.toString(a) + "  [" + ms.getMetrics() + "]");

        int[] b = arr.clone();
        QuickSorter qs = new QuickSorter(new Metrics(), new Random(1));
        qs.sort(b);
        System.out.println("QuickSort        : " + Arrays.toString(b) + "  [" + qs.getMetrics() + "]");

        int[] c = arr.clone();
        int k = c.length / 2;
        DeterministicSelector sel = new DeterministicSelector();
        int result = sel.select(c, k);
        int[] sorted = arr.clone();
        Arrays.sort(sorted);
        System.out.println("Select k=" + k + "      : " + result + " (Arrays.sort gives " + sorted[k] + ")  ["
                + sel.getMetrics() + "]");

        Point[] pts = Experiment.generatePoints("random", 1000, 3);
        ClosestPairSolver cp = new ClosestPairSolver();
        ClosestPairSolver.Result fast = cp.solve(pts);
        ClosestPairSolver.Result slow = ClosestPairSolver.bruteForce(pts);
        System.out.println("ClosestPair n=1000: " + fast + "  [" + cp.getMetrics() + "]");
        System.out.println("Brute force n=1000: d=" + String.format("%.6f", slow.distance)
                + (Math.abs(fast.distance - slow.distance) < 1e-9 ? "  -> same" : "  -> WRONG"));

        // empty and one element
        new MergeSorter().sort(new int[0]);
        new QuickSorter().sort(new int[0]);
        new MergeSorter().sort(new int[]{5});
        new QuickSorter().sort(new int[]{5});
        System.out.println("Empty / one element arrays: OK");
        System.out.println();
    }

    private static void experiment() throws IOException {
        System.out.println("=== Experiments ===");
        long start = System.nanoTime();
        List<String> rows = new Experiment().runAll(true);
        Experiment.writeCsv(rows, "results/results.csv");
        System.out.println();
        System.out.println("Saved " + rows.size() + " rows to results/results.csv ("
                + String.format("%.1f", (System.nanoTime() - start) / 1e9) + " s)");
    }
}
