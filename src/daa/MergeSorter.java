package daa;

// merge sort with one buffer and insertion sort for small parts
public class MergeSorter {
    public static final int CUTOFF = 16;

    private Metrics metrics;

    public MergeSorter() {
        this.metrics = new Metrics();
    }

    public MergeSorter(Metrics metrics) {
        this.metrics = metrics;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void sort(int[] a) {
        if (a == null || a.length < 2) {
            return;
        }
        // create buffer only one time
        int[] buffer = new int[a.length];
        metrics.addAllocations(1);
        mergeSort(a, buffer, 0, a.length - 1);
    }

    private void mergeSort(int[] a, int[] buffer, int left, int right) {
        metrics.enter();

        // small array -> insertion sort
        if (right - left + 1 <= CUTOFF) {
            insertionSort(a, left, right);
            metrics.exit();
            return;
        }

        int mid = (left + right) / 2;
        mergeSort(a, buffer, left, mid);
        mergeSort(a, buffer, mid + 1, right);

        // if already sorted we dont need merge
        metrics.incComparisons();
        if (a[mid] > a[mid + 1]) {
            merge(a, buffer, left, mid, right);
        }

        metrics.exit();
    }

    private void merge(int[] a, int[] buffer, int left, int mid, int right) {
        for (int i = left; i <= right; i++) {
            buffer[i] = a[i];
        }
        int i = left;
        int j = mid + 1;
        for (int k = left; k <= right; k++) {
            if (i > mid) {
                a[k] = buffer[j];
                j++;
            } else if (j > right) {
                a[k] = buffer[i];
                i++;
            } else {
                metrics.incComparisons();
                if (buffer[j] < buffer[i]) {
                    a[k] = buffer[j];
                    j++;
                } else {
                    a[k] = buffer[i];
                    i++;
                }
            }
            metrics.incSwaps();
        }
    }

    private void insertionSort(int[] a, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= left) {
                metrics.incComparisons();
                if (a[j] <= key) {
                    break;
                }
                a[j + 1] = a[j];
                metrics.incSwaps();
                j--;
            }
            a[j + 1] = key;
        }
    }
}