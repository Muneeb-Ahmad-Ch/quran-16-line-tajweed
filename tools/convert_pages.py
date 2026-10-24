"""Convert the 16-line tajweed-coloured Mushaf Para PDFs into frame-aligned, uniform WebP pages.

Usage: python tools/convert_pages.py --src source-pdfs --out app/src/main/assets
"""
import argparse
import json
import os
import statistics
from concurrent.futures import ProcessPoolExecutor

import pymupdf
from PIL import Image

SOURCE_DIR = "tajweed coloured (myonlinestudent)"
FIRST_PAGE, LAST_PAGE = 2, 549
TARGET_WIDTH = 1200
WEBP_QUALITY = 72
FRAME_INK = 200        # luma below this counts as frame ink
CONTENT_INK = 235      # luma below this counts as any printed content
FRAME_COVERAGE = 0.55  # a frame line covers at least this share of the page
# Frame edge further than this (share of page size) from the median -> use the median frame.
FRAME_TOLERANCE = 0.03
MARGIN_CAP = 0.20
ANALYSIS_SCALE = 4
RENDER_WIDTH = 1700
EXTRAS = range(21, 32)  # coloured Para 30 PDF indices after page 549, before back covers


def para_start(n):
    if n == 1:
        return 2
    if n <= 28:
        return 21 + 18 * (n - 2)
    return {29: 509, 30: 529}[n]


def quran_pages(para, pdf_page_count):
    """(pdf_index, mushaf_page) pairs for the Quran pages in one Para PDF."""
    first_index = 3 if para == 1 else 0  # Para 1 opens with cover, blank and license pages
    end_page = para_start(para + 1) - 1 if para < 30 else LAST_PAGE
    pairs = []
    for i in range(first_index, pdf_page_count):
        page = para_start(para) + (i - first_index)
        if page > end_page:
            break
        pairs.append((i, page))
    return pairs


def _coverage(mask, horizontal):
    """Share of ink per row (horizontal=True) or per column."""
    w, h = mask.size
    squeezed = mask.resize((1, h) if horizontal else (w, 1), Image.BOX)
    return [v / 255 for v in squeezed.tobytes()]


def detect_frame(img):
    """Outermost rows/columns that look like frame lines: (left, top, right, bottom), right/bottom exclusive."""
    gray = img.convert("L")
    w, h = gray.size
    mask = gray.point(lambda v: 255 if v < FRAME_INK else 0)
    rows = _coverage(mask, True)
    cols = _coverage(mask, False)
    ys = [y for y, c in enumerate(rows) if c >= FRAME_COVERAGE]
    xs = [x for x, c in enumerate(cols) if c >= FRAME_COVERAGE]
    if not ys or not xs:
        return (0, 0, w, h)
    return (xs[0], ys[0], xs[-1] + 1, ys[-1] + 1)


def stable_crop(frame, median_frame_norm, page_size, tolerance=FRAME_TOLERANCE):
    w, h = page_size
    dims = (w, h, w, h)
    median = tuple(round(f * d) for f, d in zip(median_frame_norm, dims))
    if any(abs(a - b) > tolerance * d for a, b, d in zip(frame, median, dims)):
        return median
    return tuple(frame)


def symmetric_sides(margins):
    """Equal left/right margins (the larger of the two) so the frame sits centred on screen."""
    l, t, r, b = margins
    side = max(l, r)
    return (side, t, side, b)


def expand(frame, margins):
    l, t, r, b = frame
    fw, fh = r - l, b - t
    ml, mt, mr, mb = margins
    return (round(l - ml * fw), round(t - mt * fh), round(r + mr * fw), round(b + mb * fh))


# ---------------------------------------------------------------- rendering

def page_path(out, page):
    return os.path.join(out, "pages", f"{page:04d}.webp")


def _render(pdf_path, index):
    page = pymupdf.open(pdf_path)[index]
    zoom = RENDER_WIDTH / page.rect.width  # first page of each Para is a larger scan; unify the scale
    pix = page.get_pixmap(matrix=pymupdf.Matrix(zoom, zoom), alpha=False)
    return Image.frombytes("RGB", (pix.width, pix.height), pix.samples)


def _crop_white(img, box):
    """Crop that pads with white instead of black when the box leaves the image."""
    l, t, r, b = box
    canvas = Image.new("RGB", (r - l, b - t), (255, 255, 255))
    src = img.crop((max(l, 0), max(t, 0), min(r, img.width), min(b, img.height)))
    canvas.paste(src, (max(-l, 0), max(-t, 0)))
    return canvas


def _analyse(job):
    pdf_path, index, page = job
    img = _render(pdf_path, index)
    small = img.resize((img.width // ANALYSIS_SCALE, img.height // ANALYSIS_SCALE), Image.BOX)
    frame = tuple(v * ANALYSIS_SCALE for v in detect_frame(small))
    content = small.convert("L").point(lambda v: 255 if v < CONTENT_INK else 0).getbbox()
    content = tuple(v * ANALYSIS_SCALE for v in content) if content else frame
    return page, img.size, frame, content


def _write(job):
    pdf_path, index, box, size, out_path = job
    img = _render(pdf_path, index)
    out = _crop_white(img, box).resize(size, Image.LANCZOS)
    out.save(out_path, "WEBP", quality=WEBP_QUALITY, method=6)
    edge = [out.getpixel((x, y)) for x in range(0, out.width, 16) for y in (1, out.height - 2)]
    return tuple(statistics.median(c[i] for c in edge) for i in range(3))


def _write_extra(job):
    pdf_path, index, out_path = job
    img = _render(pdf_path, index)
    bbox = img.convert("L").point(lambda v: 255 if v < CONTENT_INK else 0).getbbox()
    img = img.crop(bbox)
    h = round(img.height * TARGET_WIDTH / img.width)
    img.resize((TARGET_WIDTH, h), Image.LANCZOS).save(out_path, "WEBP", quality=WEBP_QUALITY, method=6)


def convert_pages(src, out, pool):
    jobs = []
    for para in range(1, 31):
        pdf_path = os.path.join(src, SOURCE_DIR, f"Para {para:02d}.pdf")
        count = pymupdf.open(pdf_path).page_count
        jobs += [(pdf_path, i, p) for i, p in quran_pages(para, count)]
    pages = [p for *_, p in jobs]
    assert pages == list(range(FIRST_PAGE, LAST_PAGE + 1)), "page mapping is not 2..549"

    analysed = {p: (size, frame, content) for p, size, frame, content in pool.map(_analyse, jobs, chunksize=4)}

    norm = [tuple(v / d for v, d in zip(f, (s[0], s[1], s[0], s[1]))) for s, f, _ in analysed.values()]
    median_norm = tuple(statistics.median(n[i] for n in norm) for i in range(4))
    frames = {p: stable_crop(f, median_norm, s) for p, (s, f, _) in analysed.items()}

    def ext(p):
        l, t, r, b = frames[p]
        cl, ct, cr, cb = analysed[p][2]
        fw, fh = r - l, b - t
        return (max(l - cl, 0) / fw, max(t - ct, 0) / fh, max(cr - r, 0) / fw, max(cb - b, 0) / fh)

    exts = [ext(p) for p in pages]
    margins = symmetric_sides(tuple(min(sorted(e[i] for e in exts)[int(0.95 * (len(exts) - 1))], MARGIN_CAP) for i in range(4)))
    boxes = {p: expand(frames[p], margins) for p in pages}
    aspect = statistics.median((b[3] - b[1]) / (b[2] - b[0]) for b in boxes.values())
    size = (TARGET_WIDTH, round(TARGET_WIDTH * aspect))

    os.makedirs(os.path.join(out, "pages"), exist_ok=True)
    write_jobs = [(path, i, boxes[p], size, page_path(out, p)) for path, i, p in jobs]
    edges = list(pool.map(_write, write_jobs, chunksize=4))
    bg = tuple(round(statistics.median(e[i] for e in edges)) for i in range(3))
    print(f"{len(pages)} pages, size {size}, margins {tuple(round(m, 3) for m in margins)}, "
          f"median frame {tuple(round(v, 3) for v in median_norm)}, "
          f"{sum(frames[p] != analysed[p][1] for p in pages)} pages used the median frame")
    return {"width": size[0], "height": size[1], "bg": "#%02x%02x%02x" % bg}


def convert_extras(src, out, pool):
    out_dir = os.path.join(out, "extras")
    os.makedirs(out_dir, exist_ok=True)
    pdf_path = os.path.join(src, SOURCE_DIR, "Para 30.pdf")
    jobs = [(pdf_path, idx, os.path.join(out_dir, f"{n:02d}.webp")) for n, idx in enumerate(EXTRAS, start=1)]
    list(pool.map(_write_extra, jobs))
    return {"count": len(jobs)}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", default="source-pdfs")
    ap.add_argument("--out", default="app/src/main/assets")
    args = ap.parse_args()
    with ProcessPoolExecutor() as pool:
        manifest = {"page": convert_pages(args.src, args.out, pool), "extras": convert_extras(args.src, args.out, pool)}
    with open(os.path.join(args.out, "pages", "manifest.json"), "w") as f:
        json.dump(manifest, f, indent=2)
    print(json.dumps(manifest, indent=2))


if __name__ == "__main__":
    main()
