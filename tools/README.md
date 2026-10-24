# Page tools

## `convert_pages.py`

Turns the 60 Para PDFs in `source-pdfs/` into the app's page images:

- `app/src/main/assets/pages/plain/0002.webp` … `0549.webp`
- `app/src/main/assets/pages/color/0002.webp` … `0549.webp`
- `app/src/main/assets/extras/01.webp` … `11.webp` (the coloured edition's pages after 549)
- `app/src/main/assets/pages/manifest.json` (page size and background colour per edition)

How it works:

1. Every PDF page is mapped to its Mushaf page number from the fixed Para ranges (Para 1 = 2–20, Paras 2–27 = 18 pages each, 28 = 489–508, 29 = 509–528, 30 = 529–549). The coloured edition's front pages and back covers are skipped.
2. The page frame is found as the outermost rows and columns that are mostly ink.
   - **Coloured:** the scans drift sideways, so each page uses its own detected frame. A page whose frame is more than 3% off the median uses the median.
   - **Plain:** the scans are already aligned, so every page uses the median frame.
3. The crop is the frame plus fixed margins (95th percentile of content outside the frame). This keeps ruku marks, the Manzil marker and the tajweed legend.
4. Every page is resized to the same size per edition (1200 px wide, keeping the edition's own aspect ratio) and saved as WebP at quality 72.

Runs in about a minute on 8 cores.

## `verify_pages.py`

Checks that all 548 pages of both editions exist, have identical sizes, and have frames within 1% of each other. Pages listed in `ALLOW` are excluded from the frame check: their measurement is thrown off by a Surah header box or a margin note, and they were checked by eye. It also checks the total asset size and writes contact sheets to `tools/out/` for a visual check.

## Tests

```bash
python -m pytest tools
```
