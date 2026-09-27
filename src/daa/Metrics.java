package daa;

// counts depth, calls, comparisons, swaps and allocations
public class Metrics {
    private int depth = 0;
    private int maxDepth = 0;
    private long calls = 0;
    private long comparisons = 0;
    private long swaps = 0;
    private long allocations = 0;

    // call this at start of recursive method
    public void enter() {
        depth++;
        calls++;
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    // call this at end of recursive method
    public void exit() {
        depth--;
    }

    public void incComparisons() { comparisons++; }
    public void addComparisons(long c) { comparisons += c; }
    public void incSwaps() { swaps++; }
    public void addAllocations(long a) { allocations += a; }

    public int getMaxDepth() { return maxDepth; }
    public long getRecursiveCalls() { return calls; }
    public long getComparisons() { return comparisons; }
    public long getSwaps() { return swaps; }
    public long getAllocations() { return allocations; }

    public void reset() {
        depth = 0;
        maxDepth = 0;
        calls = 0;
        comparisons = 0;
        swaps = 0;
        allocations = 0;
    }

    @Override
    public String toString() {
        return "depth=" + maxDepth + " calls=" + calls + " cmp=" + comparisons
                + " swaps=" + swaps + " alloc=" + allocations;
    }
}