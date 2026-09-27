package daa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// tests for closest pair: compare with brute force for n <= 2000
class ClosestPairSolverTest {

    private static final double EPS = 1e-9;

    @Test
    @DisplayName("Matches brute force on random inputs (n <= 2000)")
    void matchesBruteForce() {
        Random r = new Random(11);
        for (int t = 0; t < 100; t++) {
            int n = 2 + r.nextInt(1999);
            Point[] p = new Point[n];
            for (int i = 0; i < n; i++) p[i] = new Point(r.nextDouble() * 1000, r.nextDouble() * 1000);
            double fast = new ClosestPairSolver().solve(p).distance;
            double slow = ClosestPairSolver.bruteForce(p).distance;
            assertEquals(slow, fast, EPS, "trial " + t + " n=" + n);
        }
    }

    @Test
    @DisplayName("Matches brute force on duplicate-heavy integer grids")
    void duplicates() {
        for (int n : new int[]{2, 3, 4, 10, 100, 1000, 2000}) {
            Point[] p = Experiment.generatePoints("duplicates", n, n);
            assertEquals(ClosestPairSolver.bruteForce(p).distance,
                    new ClosestPairSolver().solve(p).distance, EPS, "n=" + n);
        }
    }

    @Test
    @DisplayName("Points on a vertical line and a horizontal line")
    void collinear() {
        Point[] vertical = new Point[500];
        Point[] horizontal = new Point[500];
        for (int i = 0; i < 500; i++) {
            vertical[i] = new Point(3.0, i * i * 0.5);
            horizontal[i] = new Point(i * 1.5 + (i % 7) * 0.01, -2.0);
        }
        assertEquals(ClosestPairSolver.bruteForce(vertical).distance,
                new ClosestPairSolver().solve(vertical).distance, EPS);
        assertEquals(ClosestPairSolver.bruteForce(horizontal).distance,
                new ClosestPairSolver().solve(horizontal).distance, EPS);
    }

    @Test
    @DisplayName("Two points / identical points")
    void tiny() {
        assertEquals(5.0, new ClosestPairSolver().solve(new Point[]{new Point(0, 0), new Point(3, 4)}).distance, EPS);
        assertEquals(0.0, new ClosestPairSolver().solve(
                new Point[]{new Point(1, 1), new Point(5, 5), new Point(1, 1)}).distance, EPS);
    }

    @Test
    @DisplayName("Input array is not modified")
    void inputUntouched() {
        Point[] p = Experiment.generatePoints("random", 1000, 5);
        Point[] copy = p.clone();
        new ClosestPairSolver().solve(p);
        for (int i = 0; i < p.length; i++) assertTrue(p[i] == copy[i]);
    }

    @Test
    @DisplayName("Large input (n = 200000) runs fast and returns a real pair")
    void largeInput() {
        Point[] p = Experiment.generatePoints("random", 200000, 77);
        ClosestPairSolver s = new ClosestPairSolver();
        ClosestPairSolver.Result res = s.solve(p);
        assertEquals(res.p.distanceTo(res.q), res.distance, EPS);
        assertTrue(res.distance >= 0);
        assertTrue(s.getMetrics().getMaxDepth() <= 20);
    }

    @Test
    @DisplayName("Fewer than two points throws")
    void invalid() {
        assertThrows(IllegalArgumentException.class, () -> new ClosestPairSolver().solve(new Point[]{new Point(0, 0)}));
        assertThrows(IllegalArgumentException.class, () -> new ClosestPairSolver().solve(null));
    }
}
