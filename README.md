# Quran 16 Line

A simple, fast, offline Android app for reading the 16-line IndoPak Mushaf with colour-coded tajweed (548 pages), the print most people in Pakistan and India learn from.

Open the app, tap **Resume** (or turn on auto-resume), and keep reading from where you stopped.

<p>
  <img src="docs/screenshots/home-light.jpg" width="200" alt="Home screen">
  <img src="docs/screenshots/reader-night.jpg" width="200" alt="Reading in Night style">
  <img src="docs/screenshots/settings-dark.jpg" width="200" alt="Settings">
</p>

## Features

- **Resume:** remembers the last page you read. The Resume button sits at the bottom for one-handed use, or you can turn on **Open last page on start**.
- **Surah, Para (Juz) and Manzil** lists that jump straight to the right page, with verse and page counts. Swipe between the lists.
- **Surah search** that forgives spelling: type a number, the English or Arabic name, a common spelling (Yaseen, Rehman, Kahaf, Mudassir…) or another name (Insan, Fussilat, Ghafir…). It always shows the closest matches.
- **Bookmarks** with a default name (`Para – Surah – Pg`) or your own. Rename or delete them from Home.
- **Clean reading screen:** only the page and a faint ⋮ menu in the corner (bookmark, index, page style, Fill screen, settings). Swipe right for the next page, like a printed Mushaf. Pinch or double-tap to zoom.
- **Aligned pages:** every page is trimmed to its frame and sized identically, so nothing jumps while you swipe.
- **Fill screen** option to stretch pages over the whole screen. Pages always stay clear of a punch-hole or notch.
- **Light / Dark / System theme** for the app, plus a separate **page style**: Auto (follows the app theme), Original, Night, Invert or Warm.
- **Extras:** Dua Khatm-ul-Quran, stop signs (Rumooz-e-Auqaf), and tajweed rules in Urdu, English, Persian, Pashto and Sindhi.
- Fully offline. No ads, no tracking, no internet permission. Android 7.0+. About 105 MB.

## Install

Download the latest `Quran16Line-vX.Y.apk` from the [Releases](../../releases) page and open it on your phone. Allow "install unknown apps" when asked. Android may warn that the app is from an unknown developer; that's because it isn't on the Play Store.

Updates install over the old version and keep your bookmarks and last page.

## Build it yourself

The page images aren't in this repo (too big for git). Rebuild them from the source PDFs:

1. Download the 30 Para PDFs from <https://myonlinestudent.wordpress.com/read-quran/> into `source-pdfs/tajweed coloured (myonlinestudent)/Para 01.pdf` … `Para 30.pdf`.
2. Convert and check the pages (Python 3, see [tools/README.md](tools/README.md)):
   ```bash
   pip install -r tools/requirements.txt
   python tools/convert_pages.py
   python tools/verify_pages.py
   ```
3. Build with Android Studio, or:
   ```bash
   ./gradlew :app:assembleRelease
   ```
   The release is signed with `release.keystore` if `keystore.properties` exists (both kept out of git). Otherwise it falls back to the debug key.

Tests: `python -m pytest tools` and `./gradlew :app:testDebugUnitTest`.

## Credits and sources

- "Quran 16 Line" by Qamar Apps (the PDF states CC BY-SA 4.0). The printed edition is credited to Pak Company (registered), March 2011, proofread by the Punjab Auqaf department. Downloaded from myonlinestudent.wordpress.com.
- **Changes made:** pages were trimmed to their frame, aligned and resized for phone screens. The text is unchanged.

The original publisher of these scans couldn't be fully confirmed. If you hold rights to this material and want it removed, please [open an issue](../../issues) and it will be taken down.
