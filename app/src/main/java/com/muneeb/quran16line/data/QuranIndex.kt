package com.muneeb.quran16line.data

data class Surah(val number: Int, val nameEn: String, val nameAr: String, val startPage: Int)

data class Para(val number: Int, val nameEn: String, val nameAr: String, val startPage: Int, val endPage: Int)

data class Manzil(val number: Int, val startSurah: Int, val startPage: Int)

/** Static index of the 16-line (548 page) Mushaf. Page numbers are the printed ones, 2..549. */
object QuranIndex {
    const val FIRST_PAGE = 2
    const val LAST_PAGE = 549
    const val PAGE_COUNT = LAST_PAGE - FIRST_PAGE + 1

    // Start pages transcribed from the Surah index printed on pages 553–554 of the coloured edition.
    private val surahData = listOf(
        "Al-Fatihah" to "الفاتحة" to 2, "Al-Baqarah" to "البقرة" to 3, "Aal-e-Imran" to "آل عمران" to 46,
        "An-Nisa" to "النساء" to 70, "Al-Ma'idah" to "المائدة" to 97, "Al-An'am" to "الأنعام" to 116,
        "Al-A'raf" to "الأعراف" to 137, "Al-Anfal" to "الأنفال" to 160, "At-Tawbah" to "التوبة" to 169,
        "Yunus" to "يونس" to 188, "Hud" to "هود" to 200, "Yusuf" to "يوسف" to 213,
        "Ar-Ra'd" to "الرعد" to 225, "Ibrahim" to "إبراهيم" to 231, "Al-Hijr" to "الحجر" to 236,
        "An-Nahl" to "النحل" to 241, "Bani Isra'il" to "بني إسرائيل" to 255, "Al-Kahf" to "الكهف" to 265,
        "Maryam" to "مريم" to 276, "Ta-Ha" to "طه" to 282, "Al-Anbiya" to "الأنبياء" to 291,
        "Al-Hajj" to "الحج" to 300, "Al-Mu'minun" to "المؤمنون" to 309, "An-Nur" to "النور" to 316,
        "Al-Furqan" to "الفرقان" to 325, "Ash-Shu'ara" to "الشعراء" to 331, "An-Naml" to "النمل" to 340,
        "Al-Qasas" to "القصص" to 348, "Al-Ankabut" to "العنكبوت" to 358, "Ar-Rum" to "الروم" to 365,
        "Luqman" to "لقمان" to 371, "As-Sajdah" to "السجدة" to 374, "Al-Ahzab" to "الأحزاب" to 377,
        "Saba" to "سبأ" to 386, "Fatir" to "فاطر" to 392, "Ya-Sin" to "يس" to 397,
        "As-Saffat" to "الصافات" to 402, "Sad" to "ص" to 409, "Az-Zumar" to "الزمر" to 413,
        "Al-Mu'min" to "المؤمن" to 421, "Ha-Mim As-Sajdah" to "حم السجدة" to 430, "Ash-Shura" to "الشورى" to 435,
        "Az-Zukhruf" to "الزخرف" to 441, "Ad-Dukhan" to "الدخان" to 447, "Al-Jathiyah" to "الجاثية" to 449,
        "Al-Ahqaf" to "الأحقاف" to 453, "Muhammad" to "محمد" to 457, "Al-Fath" to "الفتح" to 461,
        "Al-Hujurat" to "الحجرات" to 464, "Qaf" to "ق" to 467, "Adh-Dhariyat" to "الذاريات" to 469,
        "At-Tur" to "الطور" to 472, "An-Najm" to "النجم" to 474, "Al-Qamar" to "القمر" to 476,
        "Ar-Rahman" to "الرحمن" to 479, "Al-Waqi'ah" to "الواقعة" to 482, "Al-Hadid" to "الحديد" to 485,
        "Al-Mujadilah" to "المجادلة" to 489, "Al-Hashr" to "الحشر" to 492, "Al-Mumtahanah" to "الممتحنة" to 496,
        "As-Saff" to "الصف" to 498, "Al-Jumu'ah" to "الجمعة" to 500, "Al-Munafiqun" to "المنافقون" to 501,
        "At-Taghabun" to "التغابن" to 503, "At-Talaq" to "الطلاق" to 505, "At-Tahrim" to "التحريم" to 507,
        "Al-Mulk" to "الملك" to 509, "Al-Qalam" to "القلم" to 511, "Al-Haqqah" to "الحاقة" to 513,
        "Al-Ma'arij" to "المعارج" to 515, "Nuh" to "نوح" to 517, "Al-Jinn" to "الجن" to 519,
        "Al-Muzzammil" to "المزمل" to 521, "Al-Muddaththir" to "المدثر" to 522, "Al-Qiyamah" to "القيامة" to 524,
        "Ad-Dahr" to "الدهر" to 525, "Al-Mursalat" to "المرسلات" to 527, "An-Naba" to "النبأ" to 529,
        "An-Nazi'at" to "النازعات" to 530, "Abasa" to "عبس" to 531, "At-Takwir" to "التكوير" to 533,
        "Al-Infitar" to "الانفطار" to 533, "Al-Mutaffifin" to "المطففين" to 534, "Al-Inshiqaq" to "الانشقاق" to 535,
        "Al-Buruj" to "البروج" to 536, "At-Tariq" to "الطارق" to 537, "Al-A'la" to "الأعلى" to 538,
        "Al-Ghashiyah" to "الغاشية" to 538, "Al-Fajr" to "الفجر" to 539, "Al-Balad" to "البلد" to 540,
        "Ash-Shams" to "الشمس" to 541, "Al-Layl" to "الليل" to 541, "Ad-Duha" to "الضحى" to 542,
        "Al-Inshirah" to "الانشراح" to 542, "At-Tin" to "التين" to 543, "Al-Alaq" to "العلق" to 543,
        "Al-Qadr" to "القدر" to 544, "Al-Bayyinah" to "البينة" to 544, "Az-Zalzalah" to "الزلزلة" to 545,
        "Al-Adiyat" to "العاديات" to 545, "Al-Qari'ah" to "القارعة" to 545, "At-Takathur" to "التكاثر" to 546,
        "Al-Asr" to "العصر" to 546, "Al-Humazah" to "الهمزة" to 546, "Al-Fil" to "الفيل" to 547,
        "Quraysh" to "قريش" to 547, "Al-Ma'un" to "الماعون" to 547, "Al-Kawthar" to "الكوثر" to 547,
        "Al-Kafirun" to "الكافرون" to 548, "An-Nasr" to "النصر" to 548, "Al-Lahab" to "اللهب" to 548,
        "Al-Ikhlas" to "الإخلاص" to 548, "Al-Falaq" to "الفلق" to 549, "An-Nas" to "الناس" to 549,
    )

    val surahs: List<Surah> = surahData.mapIndexed { i, (names, page) ->
        Surah(i + 1, names.first, names.second, page)
    }

    private val paraNames = listOf(
        "Alif Lam Meem" to "الم", "Sayaqool" to "سيقول", "Tilkar Rusul" to "تلك الرسل",
        "Lan Tanaloo" to "لن تنالوا", "Wal Mohsanat" to "والمحصنت", "La Yuhibbullah" to "لا يحب الله",
        "Wa Iza Samiu" to "وإذا سمعوا", "Wa Lau Annana" to "ولو أننا", "Qalal Malao" to "قال الملأ",
        "Wa A'lamu" to "واعلموا", "Yatazeroon" to "يعتذرون", "Wa Ma Min Da'abbah" to "وما من دابة",
        "Wa Ma Ubarrio" to "وما أبرئ", "Rubama" to "ربما", "Subhanallazi" to "سبحن الذي",
        "Qal Alam" to "قال ألم", "Iqtaraba" to "اقترب", "Qad Aflaha" to "قد أفلح",
        "Wa Qalallazina" to "وقال الذين", "Amman Khalaq" to "أمن خلق", "Utlu Ma Oohi" to "اتل ما أوحي",
        "Wa Manyaqnut" to "ومن يقنت", "Wa Mali" to "وما لي", "Faman Azlam" to "فمن أظلم",
        "Ilaihi Yuraddu" to "إليه يرد", "Ha Meem" to "حم", "Qala Fama Khatbukum" to "قال فما خطبكم",
        "Qad Sami Allah" to "قد سمع الله", "Tabarakallazi" to "تبرك الذي", "Amma" to "عم",
    )

    private fun paraStart(n: Int): Int = when {
        n == 1 -> 2
        n <= 28 -> 21 + 18 * (n - 2)
        n == 29 -> 509
        else -> 529
    }

    val paras: List<Para> = paraNames.mapIndexed { i, (en, ar) ->
        val n = i + 1
        Para(n, en, ar, paraStart(n), if (n == 30) LAST_PAGE else paraStart(n + 1) - 1)
    }

    val manzils: List<Manzil> = listOf(1, 5, 10, 17, 26, 37, 50).mapIndexed { i, s ->
        Manzil(i + 1, s, surahs[s - 1].startPage)
    }

    private fun requirePage(p: Int) =
        require(p in FIRST_PAGE..LAST_PAGE) { "page $p outside $FIRST_PAGE..$LAST_PAGE" }

    fun paraForPage(p: Int): Para {
        requirePage(p)
        return paras.last { it.startPage <= p }
    }

    /** The first Surah that starts on [p] if any, otherwise the Surah continuing onto it. */
    fun surahForPage(p: Int): Surah {
        requirePage(p)
        return surahs.firstOrNull { it.startPage == p } ?: surahs.last { it.startPage <= p }
    }

    fun manzilForPage(p: Int): Manzil {
        requirePage(p)
        return manzils.last { it.startPage <= p }
    }

    fun defaultBookmarkName(p: Int): String =
        "${paraForPage(p).nameEn} – ${surahForPage(p).nameEn} – Pg $p"
}
