import random

from PIL import Image, ImageDraw

from convert_pages import FRAME_TOLERANCE, detect_frame, expand, para_start, quran_pages, stable_crop, symmetric_sides


def test_para_start():
    assert para_start(1) == 2
    assert para_start(2) == 21
    assert para_start(27) == 471
    assert para_start(28) == 489
    assert para_start(29) == 509
    assert para_start(30) == 529


def test_quran_pages_plain_p1():
    pages = quran_pages("plain", 1, 19)
    assert pages[0] == (0, 2)
    assert pages[-1] == (18, 20)
    assert len(pages) == 19


def test_quran_pages_color_p1_skips_front():
    pages = quran_pages("color", 1, 22)
    assert pages[0] == (3, 2)
    assert len(pages) == 19


def test_quran_pages_color_p30_stops_at_549():
    pages = quran_pages("color", 30, 34)
    assert len(pages) == 21
    assert pages[-1] == (20, 549)


def test_quran_pages_plain_p28_range():
    pages = quran_pages("plain", 28, 20)
    assert pages[0] == (0, 489)
    assert pages[-1] == (19, 508)


def _framed_page():
    img = Image.new("L", (400, 600), 255)
    d = ImageDraw.Draw(img)
    d.rectangle((40, 50, 360, 560), outline=0, width=2)
    rnd = random.Random(1)
    for _ in range(300):  # text-like noise inside the frame
        x, y = rnd.randint(60, 330), rnd.randint(70, 540)
        d.rectangle((x, y, x + 6, y + 3), fill=0)
    d.ellipse((5, 280, 25, 300), fill=0)  # marginal mark outside the frame
    return img


def test_detect_frame_synthetic():
    l, t, r, b = detect_frame(_framed_page())
    for got, want in zip((l, t, r, b), (40, 50, 361, 561)):
        assert abs(got - want) <= 2, (l, t, r, b)


def test_outlier_frame_falls_back_to_median():
    median = (0.10, 0.08, 0.90, 0.93)
    size = (1000, 1500)
    # within tolerance: keep detected frame
    near = (105, 125, 898, 1392)
    assert stable_crop(near, median, size) == near
    # far off (decorated full-sheet page): use the median frame
    far = (2, 3, 997, 1496)
    assert stable_crop(far, median, size) == (100, 120, 900, 1395)


def test_zero_tolerance_always_uses_median():
    # plain scans are digitally aligned already: every page gets the same crop
    median = (0.10, 0.08, 0.90, 0.93)
    near = (105, 125, 898, 1392)
    assert stable_crop(near, median, (1000, 1500), tolerance=0) == (100, 120, 900, 1395)


def test_versions_frame_tolerance():
    assert FRAME_TOLERANCE["plain"] == 0
    assert FRAME_TOLERANCE["color"] == 0.03


def test_expand_uses_fractions_of_frame():
    frame = (100, 200, 900, 1200)  # 800 x 1000
    assert expand(frame, (0.05, 0.02, 0.0, 0.1)) == (60, 180, 900, 1300)


def test_symmetric_side_margins():
    # side margins become equal (the larger one) so the frame sits centred
    assert symmetric_sides((0.008, 0.033, 0.033, 0.013)) == (0.033, 0.033, 0.033, 0.013)
    assert symmetric_sides((0.04, 0.01, 0.02, 0.05)) == (0.04, 0.01, 0.04, 0.05)
