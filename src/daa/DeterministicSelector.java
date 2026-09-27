package daa;

// deterministic select (median of medians)
// finds k-th smallest element (k from 0)
public class DeterministicSelector {
    private Metrics metrics;

    public DeterministicSelector() {
        this.metrics = new Metrics();
    }

    public DeterministicSelector(Metrics metrics) {
        this.metrics = metrics;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    // note: this changes the order of array
    public int select(int[] a, int k) {
        if (a == null || a.length == 0) {
            throw new IllegalArgumentException("array is empty");
        }
        if (k < 0 || k >= a.length) {
            throw new IllegalArgumentException("wrong k: " + k);
        }
        return select(a, 0, a.length - 1, k);
    }

    private int select(int[] a, int left, int right, int k) {
        metrics.enter();

        // small part - just sort it
        if (right - left + 1 <= 5) {
            insertionSort(a, left, right);
            metrics.exit();
            return a[k];
        }

        int pivot = medianOfMedians(a, left, right);

        // 3 way partition around pivot
        int lt = left;
        int gt = right;
        int i = left;
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

        // go only to the side where k is
        int result;
        if (k < lt) {
            result = select(a, left, lt - 1, k);
        } else if (k > gt) {
            result = select(a, gt + 1, right, k);
        } else {
            result = pivot;
        }

        metrics.exit();
        return result;
    }

    // groups of 5, take median of every group, move it to the front
    // then find median of these medians with recursion
    private int medianOfMedians(int[] a, int left, int right) {
        int count = 0;
        for (int start = left; start <= right; start += 5) {
            int end = Math.min(start + 4, right);
            insertionSort(a, start, end);
            int median = start + (end - start) / 2;
            swap(a, left + count, median);
            count++;
        }
        int mid = left + (count - 1) / 2;
        return select(a, left, left + count - 1, mid);
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
