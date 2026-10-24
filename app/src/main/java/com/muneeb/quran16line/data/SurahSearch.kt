package com.muneeb.quran16line.data

/**
 * Forgiving Surah search: number, English name, Arabic name, common alternative spellings and
 * other names. Spelling variants are folded together (yaseen = yasin, kausar = kawthar,
 * mudassir = muddaththir) and typos are scored by edit distance, so a query that isn't exact
 * still returns the closest Surahs.
 */
object SurahSearch {

    // Alternative names and spellings people commonly type (folded like the names, so case and
    // spelling variants don't matter here).
    private val aliases: Map<Int, List<String>> = mapOf(
        2 to listOf("baqra"), 3 to listOf("imran", "ale imran"), 9 to listOf("taubah", "baraat"),
        10 to listOf("younus"), 11 to listOf("hood"), 12 to listOf("yousuf"), 13 to listOf("raad"),
        17 to listOf("isra", "bani israel"), 19 to listOf("mariam"), 21 to listOf("ambiya"),
        24 to listOf("noor"), 29 to listOf("ankaboot"), 30 to listOf("room"), 32 to listOf("sajda"),
        36 to listOf("yaseen"), 38 to listOf("saad"), 40 to listOf("ghafir", "momin"),
        41 to listOf("fussilat", "hamim sajda"), 45 to listOf("jasiya"), 47 to listOf("qital"),
        48 to listOf("fatah"), 51 to listOf("zariyat"), 52 to listOf("toor"), 55 to listOf("rehman"),
        57 to listOf("hadeed"), 60 to listOf("mumtahina"), 62 to listOf("juma", "jumma"),
        66 to listOf("tahreem"), 68 to listOf("nun", "noon"), 71 to listOf("nooh"),
        74 to listOf("mudassir"), 76 to listOf("insan", "dahar"), 81 to listOf("takweer"),
        83 to listOf("mutaffifeen", "tatfif"), 85 to listOf("burooj"), 87 to listOf("aala"),
        93 to listOf("zuha"), 94 to listOf("sharh", "alam nashrah"), 95 to listOf("teen"),
        97 to listOf("qadar"), 99 to listOf("zilzal"), 102 to listOf("takasur"), 105 to listOf("feel"),
        106 to listOf("quraish"), 107 to listOf("maoon"), 108 to listOf("kausar"), 111 to listOf("masad"),
        18 to listOf("kahaf"),
    )

    private val articles = Regex("^(al|an|ash|at|ad|adh|ar|az|as|aal)[- ]+")
    private val surahWord = Regex("\\b(surah|surat|sura|soorah|surah-)\\b")
    private val foldRules = listOf(
        "ee" to "i", "ii" to "i", "oo" to "u", "uu" to "u", "ou" to "u", "aa" to "a",
        "aw" to "au", "q" to "k", "dh" to "z", "th" to "s",
    )

    /** Latin-script key: lowercase, no article, no punctuation, spelling variants folded. */
    internal fun fold(text: String): String {
        var s = text.lowercase().replace(Regex("['’`]"), "")
        s = s.replace(surahWord, " ").trim().replace(articles, "")
        s = s.filter { it in 'a'..'z' }
        for ((from, to) in foldRules) s = s.replace(from, to)
        if (s.length > 2 && s.endsWith("h") && s[s.length - 2] in "aeiou") s = s.dropLast(1)
        return s.fold(StringBuilder()) { b, c -> if (b.isNotEmpty() && b.last() == c) b else b.append(c) }.toString()
    }

    /** Arabic key: no diacritics or tatweel, unified alef / ya / ta marbuta, no "ال", no spaces. */
    internal fun foldArabic(text: String): String {
        val s = text
            .filterNot { it in 'ً'..'ٟ' || it == 'ٰ' || it == 'ـ' || it.isWhitespace() }
            .map { c ->
                when (c) {
                    'أ', 'إ', 'آ', 'ٱ' -> 'ا'
                    'ى' -> 'ي'
                    'ة' -> 'ه'
                    else -> c
                }
            }.joinToString("")
        return if (s.length > 3 && s.startsWith("ال")) s.drop(2) else s
    }

    private val latinKeys: Map<Int, List<String>> = QuranIndex.surahs.associate { s ->
        s.number to (listOf(s.nameEn) + aliases[s.number].orEmpty()).map(::fold).filter { it.isNotEmpty() }
    }
    private val arabicKeys: Map<Int, String> = QuranIndex.surahs.associate { it.number to foldArabic(it.nameAr) }

    fun search(query: String): List<Surah> {
        val q = query.trim()
        if (q.isEmpty()) return QuranIndex.surahs

        if (q.all { it.isDigit() }) {
            val byNumber = QuranIndex.surahs.filter { it.number.toString().startsWith(q) }
                .sortedBy { if (it.number.toString() == q) 0 else 1 }
            if (byNumber.isNotEmpty()) return byNumber
        }

        val arabic = q.any { it in '؀'..'ۿ' }
        val key = if (arabic) foldArabic(q) else fold(q)
        if (key.isEmpty()) return QuranIndex.surahs

        val scored = QuranIndex.surahs
            .map { s ->
                val keys = if (arabic) listOf(arabicKeys.getValue(s.number)) else latinKeys.getValue(s.number)
                s to keys.maxOf { score(key, it) }
            }
            .sortedWith(compareByDescending<Pair<Surah, Double>> { it.second }.thenBy { it.first.number })
        val good = scored.filter { it.second >= THRESHOLD }
        // Never leave the user with nothing: fall back to the closest few.
        return (good.ifEmpty { scored.take(FALLBACK_COUNT) }).map { it.first }
    }

    private const val THRESHOLD = 45.0
    private const val FALLBACK_COUNT = 3

    private fun score(q: String, k: String): Double = when {
        q == k -> 100.0
        k.startsWith(q) -> 90.0 - (k.length - q.length) * 0.1
        q.length >= 3 && k.contains(q) -> 80.0
        else -> {
            val whole = 1.0 - distance(q, k).toDouble() / maxOf(q.length, k.length)
            val prefix = if (k.length > q.length) 1.0 - distance(q, k.take(q.length)).toDouble() / q.length else 0.0
            70.0 * maxOf(whole, prefix * 0.95)
        }
    }

    /** Optimal string alignment distance (Levenshtein plus adjacent swaps). */
    private fun distance(a: String, b: String): Int {
        val d = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) d[i][0] = i
        for (j in 0..b.length) d[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                d[i][j] = minOf(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost)
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                    d[i][j] = minOf(d[i][j], d[i - 2][j - 2] + 1)
                }
            }
        }
        return d[a.length][b.length]
    }
}
