import random

from PIL import Image, ImageDraw

from convert_pages import FRAME_TOLERANCE, detect_frame, expand, page_path, para_start, quran_pages, stable_crop, symmetric_sides


def test_para_start():
    assert para_start(1) == 2
    assert para_start(2) == 21
    assert para_start(27) == 471
    assert para_start(28) == 489
    assert para_start(29) == 509
    assert para_start(30) == 529




def test_quran_pages_color_p1_skips_front():
    pages = quran_pages(1, 22)
    assert pages[0] == (3, 2)
    assert len(pages) == 19


def test_quran_pages_color_p30_stops_at_549():
    pages = quran_pages(30, 34)
    assert len(pages) == 21
    assert pages[-1] == (20, 549)




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






def test_expand_uses_fractions_of_frame():
    frame = (100, 200, 900, 1200)  # 800 x 1000
    assert expand(frame, (0.05, 0.02, 0.0, 0.1)) == (60, 180, 900, 1300)


def test_symmetric_side_margins():
    # side margins become equal (the larger one) so the frame sits centred
    assert symmetric_sides((0.008, 0.033, 0.033, 0.013)) == (0.033, 0.033, 0.033, 0.013)
    assert symmetric_sides((0.04, 0.01, 0.02, 0.05)) == (0.04, 0.01, 0.04, 0.05)



def test_quran_pages_middle_para_has_no_front_matter():
    pages = quran_pages(28, 20)
    assert pages[0] == (0, 489)
    assert pages[-1] == (19, 508)


def test_frame_tolerance_is_a_number():
    assert FRAME_TOLERANCE == 0.03


def test_page_path_has_no_edition_folder():
    assert page_path("out", 24).replace("\\", "/") == "out/pages/0024.webp"
