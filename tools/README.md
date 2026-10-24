# Page tools

## `convert_pages.py`

Turns the 30 tajweed-coloured Para PDFs in `source-pdfs/tajweed coloured (myonlinestudent)/` into the app's page images:

- `app/src/main/assets/pages/0002.webp` … `0549.webp`
- `app/src/main/assets/extras/01.webp` … `11.webp` (the pages after 549: Dua, stop signs, tajweed rules)
- `app/src/main/assets/pages/manifest.json` (page size and background colour)

How it works:

1. Every PDF page is mapped to its Mushaf page number from the fixed Para ranges (Para 1 = 2–20, Paras 2–27 = 18 pages each, 28 = 489–508, 29 = 509–528, 30 = 529–549). Para 1's cover, blank and license pages and Para 30's back covers are skipped.
2. The page frame is found as the outermost rows and columns that are mostly ink. The scans drift sideways, so each page uses its own frame. A page whose frame is more than 3% off the median uses the median.
3. The crop is the frame plus fixed margins: the 95th percentile of content outside the frame, with equal left and right margins so the page is centred. This keeps ruku marks, the Manzil marker and the tajweed legend.
4. Every page is resized to the same size (1200 px wide) and saved as WebP at quality 72.

Runs in about a minute on 8 cores.

## `verify_pages.py`

Checks that all 548 pages exist, have identical sizes, and have frames within 1% of each other. Pages 2–3 are excluded: they're decorated opening pages without a plain frame. It also checks the total asset size and writes a contact sheet to `tools/out/` for a visual check.

## Tests

```bash
python -m pytest tools
```
