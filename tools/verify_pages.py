"""Check generated page assets: completeness, identical sizes, frame alignment; write contact sheets.

Usage: python tools/verify_pages.py --assets app/src/main/assets --out tools/out
Exit code 1 when a check fails.
"""
import argparse
import json
import os
import statistics
import sys
from concurrent.futures import ProcessPoolExecutor

from PIL import Image

from convert_pages import FIRST_PAGE, LAST_PAGE, detect_frame

TOLERANCE = 0.01
SIZE_LIMIT_MB = 280  # 1200px pages; no Play Store limit applies (GitHub release)
# Decorated full-sheet pages whose frame differs by design (checked visually on the contact sheets).
ALLOW = {"plain": {2, 3, 526, 545, 546, 547, 548}, "color": {2, 3}}
SHEET_PAGES = [2, 3, 20, 21, 22, 181, 182, 547, 548, 549]


def _frame(path):
    img = Image.open(path)
    small = img.resize((img.width // 4, img.height // 4), Image.BOX)
    return img.size, tuple(v * 4 for v in detect_frame(small))


def check_version(kind, assets, pool, report):
    paths = {p: os.path.join(assets, "pages", kind, f"{p:04d}.webp") for p in range(FIRST_PAGE, LAST_PAGE + 1)}
    missing = [p for p, path in paths.items() if not os.path.exists(path)]
    if missing:
        report.append(f"{kind}: MISSING pages {missing[:10]}")
        return False
    results = dict(zip(paths, pool.map(_frame, paths.values(), chunksize=8)))
    sizes = {s for s, _ in results.values()}
    ok = len(sizes) == 1
    report.append(f"{kind}: {len(paths)} pages, sizes {sizes}")
    (w, h), = sizes if ok else (next(iter(sizes)),)
    dims = (w, h, w, h)
    for edge, name in enumerate(("left", "top", "right", "bottom")):
        vals = {p: f[edge] for p, (_, f) in results.items()}
        med = statistics.median(vals.values())
        off = sorted(p for p, v in vals.items() if abs(v - med) > TOLERANCE * dims[edge])
        bad = [p for p in off if p not in ALLOW[kind]]
        core = [v for p, v in vals.items() if p not in ALLOW[kind]]
        report.append(f"  {name:6s} median {med:.0f} min {min(core)} max {max(core)} "
                      f"std {statistics.pstdev(core):.2f}px  off>{TOLERANCE:.0%}: {off}")
        ok &= not bad
    size_mb = sum(os.path.getsize(p) for p in paths.values()) / 1e6
    report.append(f"  total {size_mb:.1f} MB")
    return ok


def contact_sheet(assets, out):
    for kind in ("plain", "color"):
        ims = [Image.open(os.path.join(assets, "pages", kind, f"{p:04d}.webp")) for p in SHEET_PAGES]
        tw = 288
        th = round(ims[0].height * tw / ims[0].width)
        sheet = Image.new("RGB", ((tw + 8) * 5, (th + 8) * 2), (90, 90, 90))
        for n, im in enumerate(ims):
            sheet.paste(im.resize((tw, th)), ((n % 5) * (tw + 8) + 4, (n // 5) * (th + 8) + 4))
        sheet.save(os.path.join(out, f"contact_{kind}.png"))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--assets", default="app/src/main/assets")
    ap.add_argument("--out", default="tools/out")
    args = ap.parse_args()
    os.makedirs(args.out, exist_ok=True)
    report = []
    with ProcessPoolExecutor() as pool:
        ok = all([check_version(k, args.assets, pool, report) for k in ("plain", "color")])
    with open(os.path.join(args.assets, "pages", "manifest.json")) as f:
        report.append("manifest " + json.dumps(json.load(f)))
    extras = sorted(os.listdir(os.path.join(args.assets, "extras")))
    report.append(f"extras: {len(extras)} files")
    ok &= len(extras) == 11
    total = sum(os.path.getsize(os.path.join(r, n)) for r, _, fs in os.walk(args.assets) for n in fs) / 1e6
    report.append(f"assets total {total:.1f} MB (limit {SIZE_LIMIT_MB})")
    ok &= total <= SIZE_LIMIT_MB
    contact_sheet(args.assets, args.out)
    report.append("RESULT " + ("OK" if ok else "FAIL"))
    text = "\n".join(report)
    with open(os.path.join(args.out, "alignment_report.txt"), "w") as f:
        f.write(text + "\n")
    print(text)
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
