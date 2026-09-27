package daa;

import java.util.Arrays;

// closest pair of points, divide and conquer
public class ClosestPairSolver {

    public static class Result {
        public Point p;
        public Point q;
        public double distance;

        public Result(Point p, Point q, double distance) {
            this.p = p;
            this.q = q;
            this.distance = distance;
        }

        @Override
        public String toString() {
            return p + " - " + q + "  d=" + String.format("%.6f", distance);
        }
    }

    private Metrics metrics;

    private Point bestP;
    private Point bestQ;
    private double best;

    private Point[] temp;   // for merge by y
    private Point[] strip;

    public ClosestPairSolver() {
        this.metrics = new Metrics();
    }

    public ClosestPairSolver(Metrics metrics) {
        this.metrics = metrics;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public Result solve(Point[] points) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("need at least 2 points");
        }
        int n = points.length;
        // copy so we dont change input
        Point[] pts = Arrays.copyOf(points, n);
        temp = new Point[n];
        strip = new Point[n];
        metrics.addAllocations(3);

        // sort by x
        Arrays.sort(pts, (a, b) -> {
            if (a.x != b.x) return Double.compare(a.x, b.x);
            return Double.compare(a.y, b.y);
        });

        best = Double.MAX_VALUE;
        bestP = null;
        bestQ = null;
        closest(pts, 0, n - 1);
        return new Result(bestP, bestQ, best);
    }

    // at the end pts[left..right] will be sorted by y
    private double closest(Point[] pts, int left, int right) {
        metrics.enter();
        int n = right - left + 1;

        // 3 or less points - brute force
        if (n <= 3) {
            double d = Double.MAX_VALUE;
            for (int i = left; i <= right; i++) {
                for (int j = i + 1; j <= right; j++) {
                    d = Math.min(d, check(pts[i], pts[j]));
                }
            }
            sortByY(pts, left, right);
            metrics.exit();
            return d;
        }

        int mid = (left + right) / 2;
        double midX = pts[mid].x;   // save before recursion changes order

        double dLeft = closest(pts, left, mid);
        double dRight = closest(pts, mid + 1, right);
        double d = Math.min(dLeft, dRight);

        // merge two halves by y (like in merge sort)
        mergeByY(pts, left, mid, right);

        // strip = points near the middle line
        int m = 0;
        for (int i = left; i <= right; i++) {
            if (Math.abs(pts[i].x - midX) < d) {
                strip[m] = pts[i];
                m++;
            }
        }

        // check points in strip (they are sorted by y)
        for (int i = 0; i < m; i++) {
            for (int j = i + 1; j < m; j++) {
                metrics.incComparisons();
                if (strip[j].y - strip[i].y >= d) {
                    break;
                }
                d = Math.min(d, check(strip[i], strip[j]));
            }
        }

        metrics.exit();
        return d;
    }

    // swaps counter is used here as number of distance calculations
    private double check(Point a, Point b) {
        metrics.incSwaps();
        double d = a.distanceTo(b);
        if (d < best) {
            best = d;
            bestP = a;
            bestQ = b;
        }
        return d;
    }

    private void mergeByY(Point[] pts, int left, int mid, int right) {
        for (int i = left; i <= right; i++) {
            temp[i] = pts[i];
        }
        int i = left;
        int j = mid + 1;
        for (int k = left; k <= right; k++) {
            if (i > mid) {
                pts[k] = temp[j++];
            } else if (j > right) {
                pts[k] = temp[i++];
            } else if (temp[j].y < temp[i].y) {
                pts[k] = temp[j++];
            } else {
                pts[k] = temp[i++];
            }
        }
    }

    private void sortByY(Point[] pts, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            Point key = pts[i];
            int j = i - 1;
            while (j >= left && pts[j].y > key.y) {
                pts[j + 1] = pts[j];
                j--;
            }
            pts[j + 1] = key;
        }
    }

    // O(n^2) version for testing
    public static Result bruteForce(Point[] points) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("need at least 2 points");
        }
        double best = Double.MAX_VALUE;
        Point bp = null;
        Point bq = null;
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                double d = points[i].distanceTo(points[j]);
                if (d < best) {
                    best = d;
                    bp = points[i];
                    bq = points[j];
                }
            }
        }
        return new Result(bp, bq, best);
    }
}
