# makes plots from results/results.csv
# run: python3 docs/plots/plots.py
import csv
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

rows = list(csv.DictReader(open("results/results.csv")))

def get(algo, typ, col):
    xs, ys = [], []
    for r in rows:
        if r["algorithm"] == algo and r["input_type"] == typ and r[col] != "":
            xs.append(int(r["n"]))
            ys.append(float(r[col]))
    return xs, ys

# time vs n
plt.figure(figsize=(8, 5))
for algo in ["MergeSort", "QuickSort", "DeterministicSelect", "ClosestPair", "ArraysSort"]:
    x, y = get(algo, "random", "time_ms")
    plt.plot(x, y, marker="o", label=algo)
plt.xscale("log")
plt.yscale("log")
plt.xlabel("n")
plt.ylabel("time (ms)")
plt.title("Time vs n (random input)")
plt.legend()
plt.grid(True)
plt.savefig("docs/plots/time_vs_n.png")

# depth vs n
plt.figure(figsize=(8, 5))
for algo in ["MergeSort", "QuickSort", "DeterministicSelect", "ClosestPair"]:
    x, y = get(algo, "random", "max_depth")
    plt.plot(x, y, marker="o", label=algo)
plt.xscale("log")
plt.xlabel("n")
plt.ylabel("max recursion depth")
plt.title("Recursion depth vs n (random input)")
plt.legend()
plt.grid(True)
plt.savefig("docs/plots/depth_vs_n.png")

# closest pair vs brute force
plt.figure(figsize=(8, 5))
for algo in ["ClosestPair", "ClosestPairBrute"]:
    x, y = get(algo, "random", "time_ms")
    plt.plot(x, y, marker="o", label=algo)
plt.xscale("log")
plt.yscale("log")
plt.xlabel("n")
plt.ylabel("time (ms)")
plt.title("Closest pair: divide and conquer vs brute force")
plt.legend()
plt.grid(True)
plt.savefig("docs/plots/closest_vs_brute.png")
print("done")
