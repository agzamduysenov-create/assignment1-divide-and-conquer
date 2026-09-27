package daa;

import java.util.Random;

// quicksort with random pivot
// recursion goes to smaller part, bigger part is done in while loop
public class QuickSorter {
    private Metrics metrics;
    private Random random;

    public QuickSorter() {
        this.metrics = new Metrics();
        this.random = new Random();
    }

    public QuickSorter(Metrics metrics, Random random) {
        this.metrics = metrics;
        this.random = random;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void sort(int[] a) {
        if (a == null || a.length < 2) {
            return;
        }
        quickSort(a, 0, a.length - 1);
    }

    private void quickSort(int[] a, int low, int high) {
        metrics.enter();

        while (low < high) {
            // random pivot
            int p = low + random.nextInt(high - low + 1);
            swap(a, low, p);
            int pivot = a[low];

            // 3 way partition (because of duplicates)
            // [low..lt-1] < pivot, [lt..gt] == pivot, [gt+1..high] > pivot
            int lt = low;
            int gt = high;
            int i = low + 1;
            while (i <= gt) {
                metrics.incComparisons();
                if (a[i] < pivot) {
                    swap(a, lt, i);
                    lt++;
                    i++;
                } else {
                    metrics.incComparisons();
                    if (a[i] > pivot) {
                        swap(a, i, gt);
                        gt--;
                    } else {
                        i++;
                    }
                }
            }

            // go into smaller part with recursion
            if (lt - low < high - gt) {
                quickSort(a, low, lt - 1);
                low = gt + 1;
            } else {
                quickSort(a, gt + 1, high);
                high = lt - 1;
            }
        }

        metrics.exit();
    }

    private void swap(int[] a, int i, int j) {
        if (i == j) {
            return;
        }
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
        metrics.incSwaps();
    }
}