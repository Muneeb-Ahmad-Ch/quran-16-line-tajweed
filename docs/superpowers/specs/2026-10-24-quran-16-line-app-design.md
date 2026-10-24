# Quran 16 Line — Android App Design

Date: 2026-10-24
Status: draft for review

## Intent

A simple, fast, offline Android app for reading the 16-line IndoPak Mushaf (the Taj-style 548-page print common in Pakistan and India). The everyday use is: open the app, tap Resume, keep reading from where you stopped. Everything else (Surah / Para / Manzil navigation, bookmarks, settings) supports that.

It is for personal use first, and may later be published as a public GitHub repo with credit to the sources. No Play Store.

## Goals

- Show the real printed Mushaf pages, in two switchable versions: **Plain** and **Tajweed coloured**.
- One tap from opening the app to the last page read.
- Jump to any **Surah (114)**, **Para / Juz (30)** or **Manzil (7)**.
- **Bookmarks** with a default or custom name.
- **Settings**: Mushaf version, app theme (Light / Dark / System), dark mode on pages yes/no, page style.
- A sleek, simple UI in both light and dark.
- Pages that line up perfectly: no sideways jump between pages, no stretching, no uneven margins.
- Fully offline.

## Non-goals

Audio or recitation, translation, tafsir, text search, tapping individual ayahs, accounts, cloud sync, ads, analytics, Play Store release, iOS or any other platform.

## Anti-goals (would count as failure even if it "works")

- Anything on the reading screen except the page and the single bookmark button.
- Pages that shift, jump, wobble or look stretched when swiping.
- A page number that means different text in the two versions.
- A slow cold start or visible loading when turning pages.
- Asking for internet permission.

## Source material (verified)

Two sets of 30 Para PDFs, already copied into `source-pdfs/`:

| Version | Folder | Downloaded from | Page images |
|---|---|---|---|
| Plain | `source-pdfs/plain (tanzilone)/Para NN.pdf` | tanzilone.wordpress.com | 2441×3508, lossless |
| Tajweed coloured | `source-pdfs/tajweed coloured (myonlinestudent)/Para NN.pdf` | myonlinestudent.wordpress.com/read-quran | 1301×1684 JPEG; the first page of each Para is 1700×2200 |

Checked across all 60 files:

- **Both sets are complete and use identical page numbering, pages 2–549 (548 pages).** Line breaks match on the pages compared.
- Para page ranges are the same in both: Para 1 = 2–20; Paras 2–27 = 18 pages each (Para *n* = 21+18(n−2) … 38+18(n−2)); Para 28 = 489–508; Para 29 = 509–528; Para 30 = 529–549.
- Plain PDFs contain only Quran pages. PDF page *i* of Para *n* is Mushaf page (start of Para *n*) + i.
- Coloured Para 1 has 3 extra pages at the front (cover, blank, license), which should be dropped. Coloured Para 30 (34 PDF pages) has 21 Quran pages (529–549), then **11 extra pages** (PDF pages 22–32): Dua Khatm-ul-Quran (550), Rumooz-e-Auqaf (551), tajweed mouth chart (552), Surah index (553), Surah index continued + Istiaza / Sanad (554), then six letter-numbered pages: tajweed rules in Urdu, English, Persian, Pashto and Sindhi, and Zaroori Hidayat. The last **two purple back-cover pages** (PDF pages 33–34) are dropped.
- The page images have no text layer.

**Licensing.** The coloured set's front matter says "Quran 16 Line by Qamar Apps — CC BY-SA 4.0", but Qamar Apps' App Store listing says all rights reserved. The plain set gives no license. The original publishers of either scan couldn't be traced. Decision: personal use, plus possibly a public GitHub repo with clear credit and a promise to remove content on request.

## How it gets made (decided)

- **Android only. Native Kotlin + Jetpack Compose.** *(my call; the user confirmed Android only)*
- **Page-image approach.** Every Mushaf page is a pre-made image bundled in the APK. The app is a fast image pager. All navigation, bookmarks and resume are keyed by **Mushaf page number (2–549)**, which is valid in both versions.
- **A build-time conversion step** (a script in the repo, e.g. Python + PyMuPDF + Pillow) turns the PDFs into app-ready images. The PDFs and generated images are not committed to git.

## Page preparation (build-time) — required behaviour

1. Extract every Quran page from both sets and map it to its Mushaf page number using the Para ranges above.
2. **Trim each page to its decorative frame.** The coloured scans have about 16% white on the right and about 4% on the left, and the frame drifts 2–3% sideways between consecutive pages. Plain scans have even margins of about 7% on the sides and 5% at the top and bottom. A few heavily decorated pages (e.g. plain pages 2 and 3, and the first page of each Para) fill the whole sheet, so a simple white-margin detector won't find the frame there. Those need a robust or fixed crop that matches neighbouring pages.
3. **Normalise each version to one identical image size**, keeping that version's own aspect ratio (plain ≈ 0.66 w/h, coloured ≈ 0.63 w/h). Never stretch. After this, the frame sits in exactly the same place on every page of a version.
4. Deskew if any page turns out to be tilted.
5. Compress (e.g. WebP) at a resolution that stays sharp on a 1080p+ phone and allows some zoom. Target total APK size ≲ 200 MB for both versions together. The exact resolution and quality are the builder's call.
6. Extras: export the 11 coloured extra pages (without back covers) as a separate small set. They're shown from the Extras entry in either Mushaf version and don't count for Resume or bookmarks *(my call)*.

## Screens

### Home *(my call: option A from the mockups; the user hadn't picked one)*
- Top: app name and a settings icon.
- A **"Continue reading" card**: thumbnail of the last page, Surah, Para and page number, and a big **Resume** button.
- Tabs below: **Surah | Para | Manzil | Bookmarks**. Each row shows the name and starting page, and tapping a row opens that page.
- An **Extras** entry (my call) for the coloured set's extra pages.
- On first launch (nothing read yet), Resume opens page 2.

### Reading screen (the user's explicit requirements)
- **Only the page. Nothing else on screen.** Full screen, with the status and navigation bars hidden (immersive mode).
- A **few pixels of gap on the left and right** of the page (≈3 dp). The page fills the width, and the leftover space above and below is filled with the page's own background colour (or black in a dark page style), so it never looks like a gap.
- One **small semi-transparent round bookmark button** in a corner (top-left, my call). It turns solid green when the current page is bookmarked.
- Tapping it opens a small bottom panel with a name field pre-filled as **`<Para name> – <Surah name> – Pg <n>`**, e.g. *"Sayaqool – Al-Baqarah – Pg 24"* (Para's traditional name: my call). Save or Cancel. On an already-bookmarked page, the panel offers Rename or Remove.
- Page turning by swipe, in Mushaf order: **swipe right to go to the next page** (right-to-left book) *(my call)*.
- Pinch or double-tap to zoom *(my call)*.
- The phone's Back button returns to Home.
- **Resume is automatic**: the current page is saved every time it changes.
- When the page changes, nothing appears on screen; no toasts or page counters except the brief "Saved" confirmation after bookmarking.

### Bookmarks (tab on Home)
- A list of saved bookmarks (name and page), newest first *(my call)*. Tapping one opens the page. Long-press or swipe to rename or delete. One bookmark per page, any number of pages *(my call)*.

### Settings
- **Mushaf version**: Plain / Tajweed coloured. Switching keeps you on the same page.
- **App theme**: Light / Dark / System (default System).
- **Dark mode on Quran pages too**: on/off. Off means only the UI goes dark and pages stay as printed.
- **Quran page style**: Original / Night / Invert / Warm, shown as thumbnails of the actual trimmed page in the current version. It applies when dark mode is active and the switch above is on *(my call; the user didn't say whether it should also apply in light mode)*.
  - Night = invert + hue-rotate 180° (black page, white text, colours roughly kept)
  - Invert = plain colour inversion
  - Warm = dimmed + slightly sepia
  - Apply these as a colour filter when drawing; don't ship extra image sets.
  - Default: Night *(my call)*.
- **Keep screen on while reading**: on/off, default on *(my guess, shown to the user as a guess)*.
- **Credits & sources**: an About screen (see below).
- Changes apply immediately, including to the page being read.

## Index data
- **Para**: 30 entries with traditional names (Alif Laam Meem, Sayaqool, Tilkar Rusul, …) and the start pages listed above.
- **Surah**: 114 entries with English transliteration and Arabic name, and start page in this Mushaf. They can be transcribed from the coloured set's Surah index (pages 553–554) and spot-checked against the page headers.
- **Manzil**: 7 entries, starting at Al-Fatihah, Al-Ma'idah, Yunus, Bani Isra'il, Ash-Shu'ara, As-Saffat and Qaf. Pages come from the Surah table.
- For the bookmark default name and the Resume card, each page needs its Para and Surah. When a page holds more than one Surah, use the Surah at the top of the page *(builder's call)*.

## Look and feel
- Sleek and minimal. A calm palette (mockups used a deep green accent on a warm off-white in light mode, and a near-black with mint accent in dark mode). The exact palette and typography are the builder's call.
- Portrait only *(my call)*.

## Distribution and credit
- Public GitHub repo: app source and the conversion script. **No PDFs or page images in git** (GitHub blocks files over 100 MB and big repos get slow). `source-pdfs/` and `.superpowers/` go in `.gitignore`.
- A ready-to-install APK with both versions bundled is attached to **GitHub Releases**.
- README and the in-app About screen credit "Quran 16 Line by Qamar Apps" (named in the coloured edition), tanzilone.wordpress.com and myonlinestudent.wordpress.com. They state that the original publishers couldn't be identified and that content will be removed on request from a rights holder.

## Performance (user requirement: "responsive, smooth and fast, no delay")
- **Cold start:** Home with the Resume card should be usable in well under a second on a mid-range phone. Tapping Resume should show the page at once, with no blank frame or spinner.
- **Page turns:** swipes track the finger at the display's frame rate (60/90/120 Hz) with no stutter. Neighbouring pages (at least ±1, ideally ±2) are decoded ahead of time, so a page is never blank or blurry when it arrives.
- **Zoom:** smooth pinch and double-tap. Higher detail can load in after the gesture, but the view must not freeze.
- **Lists:** the Surah, Para, Manzil and Bookmark lists scroll smoothly. The index data is static and loaded instantly.
- **Settings:** changing the theme, page style or version applies immediately, with no restart.
- **Never on the main thread:** image decoding, disk reads and writes (resume position, bookmarks) all happen off the main thread.
- **Memory:** decoded-bitmap caching stays within normal limits, so a low-RAM phone doesn't kill or stall the app.
- **Low-end phones:** it should feel the same on an older or cheaper Android phone (API 24+, 2–3 GB RAM). Measure there, not just on a flagship.

## Constraints
- Min Android 7.0 (API 24) *(my call)*.
- No internet permission; works fully offline.

## Left to the builder
Project structure, persistence (e.g. DataStore / Room), image library and pager implementation, exact image resolution, compression and quality, crop detection method, palette, fonts and icons, and test approach.

## Changes in v1.1 (user feedback after first install, 2026-10-24)
- **Stretch page to fill screen** (Settings → Reading, default off): when on, pages fill the whole screen (FillBounds, 3 dp gap all round) instead of keeping printed proportions. Also a quick switch in the reader menu.
- **Open last page on start** (Settings → Reading, default off): the app starts on the last page read, with Home underneath so Back lands on Home.
- **Reader control is now a faint ⋮ menu** (replaces the bookmark button): Bookmark this page / Edit bookmark, Surah / Para index, quick switches for Dark mode, Tajweed colours and Fill screen, and Settings. The button sits in the top-left corner on a soft backdrop matching the page (soft green when the page is bookmarked).
- **Home:** the tabs are swipeable, and the Continue reading card with a large Resume button moved to the bottom for one-handed use.

## Changes in v1.2
- Pages (reader and Extras) stay clear of the display cutout (punch-hole / notch) in every mode; stretched pages also keep a 6 dp gap top and bottom.
- Home tab labels shrink to fit when the phone uses a large font size, instead of being cut off ("Bookma…").

## Changes in v1.3
- **Plain edition removed completely** at the user's request: the converter, assets, Settings, reader menu and credits now cover the tajweed-coloured edition only. App size is about 105 MB. Pages live at `assets/pages/NNNN.webp`; the manifest is `{"page": {...}, "extras": {...}}`.
- **Surah search** at the top of the Surah tab: number, English or Arabic name, aliases and common spellings, typo-tolerant ranking. It always returns the closest matches.
- Home, Settings and About keep clear of the larger of the status bar and the display cutout. Leaving the reader also re-shows the system bars explicitly, because some OEMs keep them hidden.
