package daa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// tests for select: compare with Arrays.sort(a)[k]
class DeterministicSelectorTest {

    private static int reference(int[] a, int k) {
        int[] s = a.clone();
        Arrays.sort(s);
        return s[k];
    }

    @Test
    @DisplayName("100+ random tests vs Arrays.sort(a)[k]")
    void randomTrials() {
        Random r = new Random(2024);
        for (int t = 0; t < 300; t++) {
            int n = 1 + r.nextInt(5000);
            int[] a = new int[n];
            int bound = 1 + r.nextInt(t % 3 == 0 ? 20 : 1000000);   // every 3rd test has many duplicates
            for (int i = 0; i < n; i++) a[i] = r.nextInt(bound) - bound / 2;
            int k = r.nextInt(n);
            int expected = reference(a, k);
            assertEquals(expected, new DeterministicSelector().select(a.clone(), k),
                    "trial " + t + " n=" + n + " k=" + k);
        }
    }

    @Test
    @DisplayName("Every k for a small array")
    void everyK() {
        int[] a = {9, 1, 8, 2, 7, 3, 6, 4, 5, 5, 0, -3, 12};
        for (int k = 0; k < a.length; k++) {
            assertEquals(reference(a, k), new DeterministicSelector().select(a.clone(), k));
        }
    }

    @Test
    @DisplayName("Sorted, reverse and duplicate inputs")
    void structuredInputs() {
        for (String type : new String[]{"random", "sorted", "reverse", "duplicates"}) {
            for (int n : new int[]{1, 2, 5, 6, 11, 100, 10000, 100000}) {
                int[] a = Experiment.generateArray(type, n, n);
                for (int k : new int[]{0, n / 4, n / 2, n - 1}) {
                    assertEquals(reference(a, k), new DeterministicSelector().select(a.clone(), k),
                            type + " n=" + n + " k=" + k);
                }
            }
        }
    }

    @Test
    @DisplayName("All-equal array")
    void allEqual() {
        int[] a = new int[10001];
        Arrays.fill(a, -4);
        assertEquals(-4, new DeterministicSelector().select(a, 5000));
    }

    @Test
    @DisplayName("Invalid arguments throw")
    void invalid() {
        assertThrows(IllegalArgumentException.class, () -> new DeterministicSelector().select(new int[0], 0));
        assertThrows(IllegalArgumentException.class, () -> new DeterministicSelector().select(new int[]{1, 2}, 2));
        assertThrows(IllegalArgumentException.class, () -> new DeterministicSelector().select(new int[]{1, 2}, -1));
    }
}
