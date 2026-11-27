"""Prints a Markdown table of Macrobenchmark results found under a build directory."""
import glob
import json
import os
import sys

root = sys.argv[1] if len(sys.argv) > 1 else "."
files = glob.glob(os.path.join(root, "**", "*benchmarkData.json"), recursive=True)
if not files:
    print("No benchmark results found.")
    sys.exit(0)

print("| Benchmark | Metric | Median | Min | Max |")
print("|---|---|---|---|---|")
for path in sorted(files):
    with open(path) as f:
        data = json.load(f)
    for bench in data.get("benchmarks", []):
        name = f'{bench["className"].split(".")[-1]}.{bench["name"]}'
        for metric, values in bench.get("metrics", {}).items():
            print(f'| {name} | {metric} | {values["median"]:.1f} | {values["minimum"]:.1f} | {values["maximum"]:.1f} |')
        for metric, values in bench.get("sampledMetrics", {}).items():
            p50 = values.get("P50")
            p90 = values.get("P90")
            p99 = values.get("P99")
            print(f"| {name} | {metric} (P50 / P90 / P99) | {p50:.1f} | {p90:.1f} | {p99:.1f} |")
