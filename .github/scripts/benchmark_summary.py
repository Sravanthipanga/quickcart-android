"""Prints Markdown tables of Macrobenchmark results found under a build directory."""
import glob
import json
import os
import sys

root = sys.argv[1] if len(sys.argv) > 1 else "."
files = glob.glob(os.path.join(root, "**", "*benchmarkData.json"), recursive=True)
if not files:
    print("No benchmark results found.")
    sys.exit(0)

single, sampled = [], []
for path in sorted(files):
    with open(path) as f:
        data = json.load(f)
    for bench in data.get("benchmarks", []):
        name = f'{bench["className"].split(".")[-1]}.{bench["name"]}'
        for metric, v in bench.get("metrics", {}).items():
            single.append(f'| {name} | {metric} | {v["median"]:.1f} | {v["minimum"]:.1f} | {v["maximum"]:.1f} |')
        for metric, v in bench.get("sampledMetrics", {}).items():
            sampled.append(f'| {name} | {metric} | {v["P50"]:.1f} | {v["P90"]:.1f} | {v["P99"]:.1f} |')

if single:
    print("| Benchmark | Metric | Median | Min | Max |")
    print("|---|---|---|---|---|")
    print("\n".join(single))
    print()
if sampled:
    print("| Benchmark | Metric | P50 | P90 | P99 |")
    print("|---|---|---|---|---|")
    print("\n".join(sampled))
