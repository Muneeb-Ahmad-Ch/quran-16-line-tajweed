# Quran 16 Line App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** An offline, fast Android reader for the 16-line IndoPak Mushaf. It has two switchable page-image versions, Resume, Surah/Para/Manzil index, bookmarks, and theme settings.

**Architecture:** A Python build step turns the 60 source PDFs into frame-aligned, uniform-size WebP pages in `app/src/main/assets/`. The Kotlin/Compose app is a single-activity image pager keyed by Mushaf page number (2–549). Static index data lives in Kotlin. Settings, resume position and bookmarks live in one Preferences DataStore. Dark page styles are `ColorMatrix` filters applied at draw time.

**Tech Stack:** Python 3 + PyMuPDF + Pillow (build tool, pytest). Gradle 9.1.0, AGP 8.13.1, Kotlin 2.2.20, Compose BOM 2025.10.01, Material3, DataStore Preferences 1.1.7, kotlinx-serialization-json 1.9.0, telephoto `zoomable` 0.17.0, JUnit 4.13.2. compileSdk/targetSdk 36, minSdk 24.

**Spec:** `docs/superpowers/specs/2026-10-24-quran-16-line-app-design.md`

## Global Constraints

- Android only. Native Kotlin + Jetpack Compose. minSdk 24 (Android 7.0). Portrait only.
- No `INTERNET` permission in the manifest.
- Page numbers 2–549 are the only key for resume, bookmarks and navigation. They are identical in both versions.
- Para ranges: P1 = 2–20; Pn (2..27) = 21+18(n−2) … 38+18(n−2); P28 = 489–508; P29 = 509–528; P30 = 529–549.
- Reading screen shows only the page, plus one semi-transparent round bookmark button (top-left). Immersive mode. About 3 dp gap left and right.
- Swipe right = next page (RTL book order).
- Default bookmark name: `<Para name> – <Surah name> – Pg <n>` (en dash, e.g. `Sayaqool – Al-Baqarah – Pg 24`).
- Settings: Mushaf version Plain/Tajweed coloured (default Tajweed coloured); App theme Light/Dark/System (default System); Dark mode on Quran pages too (default on); Page style Original/Night/Invert/Warm (default Night, used only when dark is active and the switch is on); Keep screen on (default on).
- Night = invert then hue-rotate 180°. Invert = plain inversion. Warm = brightness ×0.62, then sepia 0.25.
- PDFs and generated images are never committed to git.
- No decoding or disk I/O on the main thread. Neighbouring pages (±2) are decoded ahead of time.

## Review Focus

1. **First launch with no saved page.** Resume opens page 2 and the Resume card shows Al-Fatihah. Test: `ReaderStateTest.firstLaunchResumesAtPage2` (Task 5).
2. **Switching version while reading.** The page number stays the same, and the image swaps without the pager jumping to a different index. Test: `SettingsRepositoryTest.versionSwitchKeepsLastPage` (Task 5).
3. **Bookmark name containing the en dash, quotes, emoji or newlines** must round-trip through storage. Test: `BookmarkCodecTest.roundTripsAwkwardNames` (Task 5).
4. **Decorated full-sheet pages** (plain pages 2–3, the first page of each Para) must not get a wrong crop that makes them a different size or offset. Test: `test_outlier_frame_falls_back_to_median` (Task 1), plus the alignment report (Task 2).
5. **Page boundaries.** The pager can't go below 2 or above 549, and pages with several Surahs get a sensible Surah in the name. Tests: `QuranIndexTest.surahForPageMultiSurahPage` and `QuranIndexTest.pageBounds` (Task 3).

---

### Task 1: Page converter (Python build tool)

**Files:**
- Create: `tools/convert_pages.py`, `tools/test_convert_pages.py`, `tools/requirements.txt`, `.gitignore`

**Interfaces:**
- Produces CLI: `python tools/convert_pages.py --src source-pdfs --out app/src/main/assets`. It writes `pages/plain/NNNN.webp`, `pages/color/NNNN.webp` (NNNN = 0002…0549), `extras/NN.webp` (01…11) and `pages/manifest.json`, which looks like `{"plain":{"width":W,"height":H,"bg":"#RRGGBB"},"color":{...},"extras":{"count":11}}`.
- Functions:
  - `para_start(n:int)->int`
  - `quran_pages(kind:str, para:int, pdf_page_count:int)->list[tuple[int,int]]`, which returns (pdf_index, mushaf_page) pairs and skips coloured Para 1's first 3 pages and everything after coloured Para 30's index 20
  - `detect_frame(img: PIL.Image)->tuple[int,int,int,int]`, the outermost rows/columns whose ink coverage (luma < 200) is ≥ 55% of width/height
  - `stable_crop(frame, median_frame_norm, page_size)->box`
  - `expand(frame, margins)->box`

- [ ] **Step 1:** `git init`. Add `.gitignore` with `source-pdfs/`, `.superpowers/`, `app/src/main/assets/pages/`, `app/src/main/assets/extras/`, `build/`, `.gradle/`, `local.properties`, `*.keystore`, `__pycache__/`, `tools/out/`.
- [ ] **Step 2: Write failing tests** in `tools/test_convert_pages.py`:
  - `test_para_start`: `para_start(1)==2, para_start(2)==21, para_start(27)==471, para_start(28)==489, para_start(29)==509, para_start(30)==529`
  - `test_quran_pages_plain_p1`: plain, para 1, 19 pages → first `(0,2)`, last `(18,20)`
  - `test_quran_pages_color_p1_skips_front`: color, para 1, 22 pages → first `(3,2)`, length 19
  - `test_quran_pages_color_p30_stops_at_549`: color, para 30, 34 pages → length 21, last `(20,549)`
  - `test_detect_frame_synthetic`: a 400×600 white image with a 2px black rectangle at (40,50)-(360,560), some text-like noise inside, and a black blob outside at x=10 → returns `(40,50,361,561)` ±2
  - `test_outlier_frame_falls_back_to_median`: a detected frame that differs from the median (normalised) by more than 3% of page size on any edge → `stable_crop` returns the median frame scaled to the page size
- [ ] **Step 3:** Run `python -m pytest tools -q` and expect FAIL (import error).
- [ ] **Step 4: Implement the converter.** Pipeline per version:
  1. Render each Quran page at a fixed pixel width (plain: draw the embedded image at native 2441 px; coloured: render so the width is 1700 px, so the two sizes of source image are unified).
  2. Detect the frame on a 25%-scale grayscale copy and scale it back up.
  3. Compute `median_frame_norm` across all pages of that version, then apply `stable_crop`.
  4. Compute the margins as the 95th percentile, across pages, of the non-white bbox (luma < 235) extending beyond the frame on each side. This keeps the coloured version's ruku marks, Manzil marker and tajweed legend, and the plain version's ornament. Cap each margin at 20% of the frame size.
  5. Crop with `expand(frame, margins)`, then resize with LANCZOS to the version target: width 1440 px, height = round(1440 × median crop aspect).
  6. Save as WebP at quality 82, method 6.
  7. Record `bg` as the median RGB of the outer 4 px of all crops.

  Extras: coloured Para 30 PDF indices 21–31 → `extras/01..11.webp`, scaled to width 1440, with no frame logic. Use a multiprocessing pool to keep it fast.
- [ ] **Step 5:** Run `python -m pytest tools -q` and expect PASS.
- [ ] **Step 6:** Commit with `feat(tools): page converter with frame alignment`.

### Task 2: Generate assets and verify alignment

**Files:**
- Create: `tools/verify_pages.py`
- Generated (git-ignored): `app/src/main/assets/pages/**`, `app/src/main/assets/extras/**`, `tools/out/alignment_report.txt`, `tools/out/contact_*.png`

**Interfaces:**
- Consumes the Task 1 CLI.
- Produces assets used from Task 6 onwards.

- [ ] **Step 1:** Run the converter. Expect 548 files in each of `pages/plain` and `pages/color`, 11 in `extras`, and a manifest.
- [ ] **Step 2:** Write `verify_pages.py`. It asserts that every page 2–549 exists in both versions and that all pages in a version have identical dimensions. It re-runs `detect_frame` on each output page and writes the per-version min/max/std of each frame edge to `alignment_report.txt`. It exits non-zero if any frame edge's max−min is more than 1% of the image size, except pages listed in an explicit allow-list of decorated pages, which is printed. It also writes contact sheets (pages 2–3, 20–22, 181–182 and 547–549 for both versions) for visual review.
- [ ] **Step 3:** Run it and expect exit 0. Open the contact sheets and confirm visually: no stretched text, the ruku and Manzil marks are kept, and the decorated pages look complete.
- [ ] **Step 4:** Record total asset size in the report. It must be ≤ 200 MB, otherwise lower the WebP quality in Task 1 and rerun.
- [ ] **Step 5:** Commit `verify_pages.py` with `feat(tools): alignment verification`.

### Task 3: Quran index data

**Files:**
- Create: `app/src/main/java/com/muneeb/quran16line/data/QuranIndex.kt`
- Test: `app/src/test/java/com/muneeb/quran16line/data/QuranIndexTest.kt`

(The test is written now and run after Task 4 creates the Gradle project. Task 4 Step 4 runs it.)

**Interfaces:**
- Produces:
  - `data class Surah(val number:Int, val nameEn:String, val nameAr:String, val startPage:Int)`
  - `data class Para(val number:Int, val nameEn:String, val nameAr:String, val startPage:Int, val endPage:Int)`
  - `data class Manzil(val number:Int, val startSurah:Int, val startPage:Int)`
  - `object QuranIndex { const val FIRST_PAGE=2; const val LAST_PAGE=549; val surahs:List<Surah>; val paras:List<Para>; val manzils:List<Manzil>; fun paraForPage(p:Int):Para; fun surahForPage(p:Int):Surah; fun manzilForPage(p:Int):Manzil; fun defaultBookmarkName(p:Int):String }`

- [ ] **Step 1: Transcribe Surah start pages** from coloured pages 553–554, the Surah index (column صفحہ). Spot-check at least 15 of them, including 1, 2, 9, 18, 36, 67, 78, 103–114, by viewing that page's header or Surah banner in the generated assets. Fix any mismatches.
- [ ] **Step 2: Write tests:**
  - `surahCountAndOrder`: 114 entries, numbers 1..114, startPage non-decreasing, first = 2, Al-Baqarah = 3
  - `paraRanges`: 30 entries matching the Global Constraints formula; para 2 nameEn = "Sayaqool"
  - `manzils`: 7 entries with startSurah = 1, 5, 10, 17, 26, 37, 50
  - `pageBounds`: `paraForPage(2).number==1`, `paraForPage(549).number==30`; `paraForPage(1)` throws `IllegalArgumentException`
  - `surahForPageMultiSurahPage`: the Surah used is the first Surah that starts on the page if one does, otherwise the one continuing; `surahForPage(549).number==113`
  - `defaultName`: `defaultBookmarkName(24)=="Sayaqool – Al-Baqarah – Pg 24"`
- [ ] **Step 3: Implement `QuranIndex.kt`.** Para names in English: Alif Lam Meem, Sayaqool, Tilkar Rusul, Lan Tanaloo, Wal Mohsanat, La Yuhibbullah, Wa Iza Samiu, Wa Lau Annana, Qalal Malao, Wa A'lamu, Yatazeroon, Wa Ma Min Da'abbah, Wa Ma Ubarrio, Rubama, Subhanallazi, Qal Alam, Iqtaraba, Qad Aflaha, Wa Qalallazina, Amman Khalaq, Utlu Ma Oohi, Wa Manyaqnut, Wa Mali, Faman Azlam, Ilaihi Yuraddu, Ha Meem, Qala Fama Khatbukum, Qad Sami Allah, Tabarakallazi, Amma.
- [ ] **Step 4:** Commit with `feat: Quran index data`.

### Task 4: Android project scaffold

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/*`, `gradlew`, `gradlew.bat`, `local.properties` (sdk.dir, git-ignored), `app/build.gradle.kts`, `app/proguard-rules.pro`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/muneeb/quran16line/MainActivity.kt`, `.../ui/theme/Theme.kt`, `app/src/main/res/values/{strings.xml,themes.xml}`, launcher icon (adaptive, vector)

**Interfaces:**
- Produces `@Composable fun QuranTheme(dark: Boolean, content: @Composable () -> Unit)`. Palette: light background #FBF8F1, accent #1F5F4A. Dark background #121412, accent #6FC3A0.
- `applicationId "com.muneeb.quran16line"`, app name "Quran 16 Line".
- `androidResources { noCompress += "webp" }`.
- Release build: R8 minify + shrinkResources, signed with the debug signing config (personal use).

- [ ] **Step 1:** Write the Gradle files with the versions from the Tech Stack. Use the Gradle wrapper 9.1.0. JAVA_HOME = Android Studio's JBR.
- [ ] **Step 2:** Manifest: a single activity, `screenOrientation="portrait"`, no permissions.
- [ ] **Step 3:** Run `./gradlew :app:assembleDebug` and expect BUILD SUCCESSFUL.
- [ ] **Step 4:** Run `./gradlew :app:testDebugUnitTest` and expect the QuranIndex tests to PASS.
- [ ] **Step 5:** Commit with `feat: Android project scaffold`.

### Task 5: Settings, resume and bookmarks storage

**Files:**
- Create: `.../data/Settings.kt`, `.../data/SettingsRepository.kt`, `.../data/Bookmark.kt`
- Test: `.../data/SettingsRepositoryTest.kt`, `.../data/BookmarkCodecTest.kt`, `.../data/ReaderStateTest.kt`

**Interfaces:**
- `enum class MushafVersion(val assetDir:String) { PLAIN("plain"), COLOR("color") }`
- `enum class AppTheme { LIGHT, DARK, SYSTEM }`
- `enum class PageStyle { ORIGINAL, NIGHT, INVERT, WARM }`
- `data class Settings(val version:MushafVersion=COLOR, val theme:AppTheme=SYSTEM, val darkPages:Boolean=true, val pageStyle:PageStyle=NIGHT, val keepScreenOn:Boolean=true, val lastPage:Int=2)`
- `@Serializable data class Bookmark(val page:Int, val name:String, val createdAt:Long)`
- `object BookmarkCodec { fun encode(list:List<Bookmark>):String; fun decode(s:String?):List<Bookmark> }`. Decoding malformed input returns an empty list.
- `class SettingsRepository(private val store: DataStore<Preferences>)` with:
  - `val settings: Flow<Settings>`, `val bookmarks: Flow<List<Bookmark>>`
  - `suspend fun setLastPage(p:Int)`, which clamps to 2..549
  - `suspend fun update(transform:(Settings)->Settings)`
  - `suspend fun upsertBookmark(page:Int, name:String)`, which keeps one bookmark per page and moves it to newest
  - `suspend fun removeBookmark(page:Int)`
  - `bookmarks` is sorted newest first

- [ ] **Step 1: Write tests.** Use a real `PreferenceDataStoreFactory` on a temp file with `runTest`:
  - `firstLaunchResumesAtPage2`: default settings have `lastPage==2`
  - `versionSwitchKeepsLastPage`: setLastPage(300), then update version → lastPage still 300
  - `setLastPageClamps`: 0 → 2, 999 → 549
  - `upsertReplacesSamePage`: two upserts on page 24 → one bookmark with the second name
  - `bookmarksNewestFirst`
  - `roundTripsAwkwardNames`: a name with `– " ' \n 😀` survives encode and decode
  - `decodeGarbageReturnsEmpty`
- [ ] **Step 2:** Run the tests and expect FAIL.
- [ ] **Step 3:** Implement.
- [ ] **Step 4:** Run the tests and expect PASS.
- [ ] **Step 5:** Commit with `feat: settings, resume and bookmarks storage`.

### Task 6: Page image loading

**Files:**
- Create: `.../pages/PageImages.kt`, `.../pages/PageStyles.kt`
- Test: `.../pages/PageStylesTest.kt`, `.../pages/PageImagesTest.kt`

**Interfaces:**
- `class PageImages(context: Context)` with:
  - `fun assetPath(version:MushafVersion, page:Int):String`, which returns `"pages/${version.assetDir}/%04d.webp"`
  - `fun extraPath(i:Int):String`, which returns `"extras/%02d.webp"`
  - `suspend fun load(path:String, targetWidth:Int): ImageBitmap`, which decodes on `Dispatchers.IO` using `inDensity`/`inTargetDensity` scaling to targetWidth and `ARGB_8888`, cached in an `LruCache<String, Bitmap>` sized to 1/6 of `maxMemory`
  - `fun prefetch(paths:List<String>, targetWidth:Int)`
  - `val manifest: PageManifest`, parsed from `pages/manifest.json` and giving aspect ratio and bg per version
- `fun pageColorFilter(style:PageStyle): ColorFilter?`, which returns null for ORIGINAL
- `fun pageStyleMatrix(style:PageStyle): FloatArray`, a 4×5 matrix (for tests)
- `fun letterboxColor(bg:Color, style:PageStyle): Color`, the bg colour run through the same matrix

- [ ] **Step 1: Write tests:**
  - `assetPathFormat`: `assetPath(COLOR,24)=="pages/color/0024.webp"`
  - `invertWhiteIsBlack`: the INVERT matrix applied to (1,1,1) gives (0,0,0)
  - `nightWhiteIsBlack`: NIGHT on white gives black within 0.02
  - `nightKeepsHueRoughly`: pure blue (0,0,1) under NIGHT stays blue-dominant (b > r and b > g)
  - `warmDimsWhite`: WARM applied to white gives luminance between 0.5 and 0.75
- [ ] **Step 2:** Run the tests and expect FAIL.
- [ ] **Step 3:** Implement. Matrices: INVERT = −I + 1. NIGHT = hueRotate(180°) · INVERT, using the standard luminance-preserving hue-rotation matrix. WARM = sepia(0.25) · brightness(0.62).
- [ ] **Step 4:** Run the tests and expect PASS.
- [ ] **Step 5:** Commit with `feat: page image loading and styles`.

### Task 7: Reader screen

**Files:**
- Create: `.../ui/reader/ReaderScreen.kt`, `.../ui/reader/BookmarkSheet.kt`, `.../ui/AppViewModel.kt`, `.../ui/Navigation.kt`
- Modify: `MainActivity.kt`

**Interfaces:**
- `class AppViewModel(app: Application): AndroidViewModel` exposes:
  - `settings: StateFlow<Settings>`, `bookmarks: StateFlow<List<Bookmark>>`, `images: PageImages`
  - `fun onPageShown(p:Int)`
  - `fun saveBookmark(p:Int, name:String)`, `fun removeBookmark(p:Int)`
  - `fun updateSettings(t:(Settings)->Settings)`
- `sealed interface Screen { data object Home; data class Reader(val page:Int); data object Settings; data object About; data class Extras(val index:Int) }`
- Navigation is a back stack held in the ViewModel. `BackHandler` pops it.
- `@Composable fun ReaderScreen(startPage:Int, vm:AppViewModel, onBack:()->Unit)`

Behaviour:
- `HorizontalPager` over 548 pages inside `CompositionLocalProvider(LocalLayoutDirection provides Rtl)`, so swiping right goes forward. `beyondViewportPageCount = 1`.
- Each page: a `Box` filled with `letterboxColor`, containing an `Image` with `Modifier.padding(horizontal=3.dp).fillMaxWidth().aspectRatio(manifest aspect)`, centred vertically, with `colorFilter = if (darkActive && darkPages) pageColorFilter(style) else null`, and telephoto `Modifier.zoomable(rememberZoomableState())` with double-tap zoom.
- While a page's bitmap is not yet decoded, draw only the letterbox colour, never a spinner.
- `LaunchedEffect(pagerState.settledPage)` calls `vm.onPageShown` and prefetches settled ±2.
- Immersive mode via `WindowInsetsControllerCompat` (hide systemBars, `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`), restored when leaving the reader.
- `FLAG_KEEP_SCREEN_ON` while the reader is shown and `keepScreenOn` is on.
- Bookmark button: 40 dp circle, top-left with 12 dp inset, background black at 28% alpha, or accent at 75% when the page is bookmarked.
- `BookmarkSheet` (ModalBottomSheet) has a text field pre-filled with `defaultBookmarkName(p)` or the existing name, plus Save/Cancel, and Remove when a bookmark already exists. After saving, show a 1.5 s "Saved: <name>" pill at the bottom.

- [ ] **Step 1:** Implement.
- [ ] **Step 2:** Build, install on the emulator, and open page 24. Take an adb screenshot and verify that only the page and the button are visible, the gap is about 3 dp, and swiping right shows page 25.
- [ ] **Step 3:** Commit with `feat: reader screen`.

### Task 8: Home screen

**Files:**
- Create: `.../ui/home/HomeScreen.kt`

**Interfaces:**
- `@Composable fun HomeScreen(vm:AppViewModel, open:(Screen)->Unit)`

Layout:
- Top bar: "Quran 16 Line" and a settings icon.
- "Continue reading" card: a thumbnail of lastPage at 120 px width, Surah, Para and Pg, and a Resume button that opens `Reader(lastPage)`.
- Tabs: Surah | Para | Manzil | Bookmarks, each a `LazyColumn`. Rows show the number, English name, Arabic name and `p. N`.
- The Bookmarks tab shows name and page. Tapping opens the page. Swipe-to-dismiss or a long-press menu offers Rename (reuses `BookmarkSheet`) and Delete. When empty, it shows a hint: "Tap 🔖 while reading to bookmark a page."
- An "Extras" row at the end of the Surah tab opens the Extras screen.

- [ ] **Step 1:** Implement.
- [ ] **Step 2:** Verify on the emulator with screenshots in light and dark (`adb shell cmd uimode night yes/no`). Check: Resume opens the saved page; tapping Surah 36 opens its start page; a bookmark appears in the tab, and rename and delete work.
- [ ] **Step 3:** Commit with `feat: home screen`.

### Task 9: Settings, About and Extras screens

**Files:**
- Create: `.../ui/settings/SettingsScreen.kt`, `.../ui/settings/AboutScreen.kt`, `.../ui/extras/ExtrasScreen.kt`

Behaviour:
- Settings follow the mockup sections: Quran (Mushaf version), Appearance (App theme segmented control; the Dark-pages switch; Page style as four thumbnails of the current lastPage, each with its filter applied and the selected one outlined), Reading (Keep screen on), and About.
- Changes call `vm.updateSettings` and apply live.
- About text credits "Quran 16 Line by Qamar Apps (CC BY-SA 4.0 as stated in the PDF)", the Plain scans hosted at tanzilone.wordpress.com, and the Tajweed scans hosted at myonlinestudent.wordpress.com. It says the original publishers couldn't be identified and content will be removed on request.
- Extras uses the same pager and zoom as the reader over `extras/01..11`, without the bookmark button or resume.

- [ ] **Step 1:** Implement.
- [ ] **Step 2:** Verify on the emulator: switching version on the Settings screen and returning to the reader shows the same page number in the other version; turning the Dark-pages switch off shows the original page in dark theme.
- [ ] **Step 3:** Commit with `feat: settings, about and extras`.

### Task 10: Release build, performance check, README

**Files:**
- Create: `README.md`, `tools/README.md`

- [ ] **Step 1:** Run `./gradlew :app:assembleRelease` and expect success. Record the APK size, which should be ≤ 200 MB.
- [ ] **Step 2:** Performance on the emulator using the release APK:
  - Measure cold start with `adb shell am start -W`, recording TotalTime.
  - Reset `dumpsys gfxinfo`, then do 30 `adb shell input swipe` page turns in the reader and read `dumpsys gfxinfo <pkg>`. Janky frames should be ≤ 5% (emulator numbers are indicative).
  - If either fails, profile and fix: check decode size, cache hits and prefetch.
- [ ] **Step 3:** Write the README: what it is, screenshots, a link to the Releases APK, how to rebuild (download the PDFs into `source-pdfs/`, run the converter, then gradle), and credits and takedown note (same as the About text).
- [ ] **Step 4:** Run the full test suites (`pytest tools`, `gradlew testDebugUnitTest`) and expect them to pass.
- [ ] **Step 5:** Commit with `docs: README; release build`.
