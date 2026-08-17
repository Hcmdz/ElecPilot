package com.HcmDz.ElecPilot.util

import kotlin.math.min
import kotlin.math.max
import java.util.Locale
import androidx.compose.runtime.Immutable

@Immutable
data class VoiceSearchResult(
    val originalQuery: String,
    val normalizedQuery: String,
    val reconstructedTag: String,
    val candidates: List<SearchCandidate>
)

@Immutable
data class SearchCandidate(
    val tag: String,
    val score: Float,
    val matchType: MatchType
)

@Immutable
data class SmartSearchResult<T>(
    val item: T,
    val score: Float,
    val matchType: MatchType,
    val matchedOn: String
)

enum class MatchType { EXACT, NORMALIZED, VOICE_NORMALIZED, STRIPPED, SUBSTRING, FUZZY }

class SearchIndex<T>(
    val items: List<T>,
    fieldExtractors: List<Pair<String, (T) -> String>>,
    val combinedExtractor: (T) -> String = { "" }
) {
    val searchableFields: List<SearchableField>
    val ngramIndex: Map<String, List<Int>>
    val prefixIndex: Map<String, List<Int>>

    data class SearchableField(
        val itemIndex: Int,
        val rawValue: String,
        val normalizedValue: String,
        val strippedValue: String,
        val fieldName: String
    )

    init {
        val fields = mutableListOf<SearchableField>()
        items.forEachIndexed { idx, item ->
            val pairs = fieldExtractors.map { (name, extractor) -> name to extractor(item) }
            for ((name, value) in pairs) {
                if (value.isNotBlank()) {
                    fields.add(
                        SearchableField(
                            idx, value,
                            normalizeForIndex(value),
                            stripFormatting(value),
                            name
                        )
                    )
                }
            }
        }
        searchableFields = fields

        val index = mutableMapOf<String, MutableList<Int>>()
        fields.forEachIndexed { fieldIdx, field ->
            val values = listOf(field.rawValue.uppercase(Locale.ROOT), field.strippedValue.uppercase(Locale.ROOT))
            for (v in values) {
                val ngrams = VoiceSearchEngine.generateNgramsStatic(v, 2) +
                             VoiceSearchEngine.generateNgramsStatic(v, 3)
                for (ngram in ngrams) {
                    index.getOrPut(ngram) { mutableListOf() }.add(fieldIdx)
                }
            }
        }
        ngramIndex = index

        val pIndex = mutableMapOf<String, MutableList<Int>>()
        fields.forEachIndexed { fieldIdx, field ->
            val raw = field.rawValue.uppercase(Locale.ROOT)
            val stripped = field.strippedValue.uppercase(Locale.ROOT)
            for (i in 2..raw.length) {
                pIndex.getOrPut(raw.substring(0, i)) { mutableListOf() }.add(fieldIdx)
            }
            if (stripped != raw) {
                for (i in 1..stripped.length) {
                    pIndex.getOrPut(stripped.substring(0, i)) { mutableListOf() }.add(fieldIdx)
                }
            }
        }
        prefixIndex = pIndex
    }

    companion object {
        private val RE_SEPARATORS = Regex("[-_.,/\\\\()\\[\\]{}]")
        private val RE_A = Regex("[àâä]")
        private val RE_E = Regex("[éèêë]")
        private val RE_I = Regex("[îï]")
        private val RE_O = Regex("[ôö]")
        private val RE_U = Regex("[ùûü]")
        private val RE_NON_ALNUM = Regex("[^A-Z0-9]")

        fun normalizeForIndex(s: String): String {
            return s.lowercase(Locale.ROOT)
                .replace(RE_SEPARATORS, "")
                .replace(RE_A, "a")
                .replace(RE_E, "e")
                .replace(RE_I, "i")
                .replace(RE_O, "o")
                .replace(RE_U, "u")
                .replace("œ", "oe")
                .trim()
        }

        fun stripFormatting(s: String): String {
            return s.uppercase(Locale.ROOT)
                .replace(RE_NON_ALNUM, "")
        }
    }
}

class VoiceSearchEngine {

    private val numberWords: Map<String, Int>
    private val letterSounds: Map<String, String>
    private val fillers: Set<String>

    init {
        numberWords = buildMap {
            put("zéro", 0); put("zero", 0); put("zèro", 0)
            put("un", 1); put("une", 1)
            put("deux", 2)
            put("trois", 3)
            put("quatre", 4)
            put("cinq", 5)
            put("six", 6)
            put("sept", 7)
            put("huit", 8)
            put("neuf", 9)
            put("dix", 10)
            put("onze", 11)
            put("douze", 12)
            put("treize", 13)
            put("quatorze", 14)
            put("quinze", 15)
            put("seize", 16)
            put("vingt", 20); put("vins", 20); put("vint", 20)
            put("trente", 30)
            put("quarante", 40); put("carante", 40)
            put("cinquante", 50)
            put("soixante", 60)
            put("cent", 100); put("cents", 100); put("san", 100); put("sang", 100)
            put("mille", 1000)
        }

        letterSounds = buildMap {
            put("a", "A"); put("à", "A"); put("â", "A")
            put("bé", "B"); put("b", "B"); put("be", "B")
            put("cé", "C"); put("c", "C"); put("ce", "C")
            put("dé", "D"); put("d", "D"); put("de", "D")
            put("e", "E"); put("é", "E"); put("è", "E"); put("ê", "E"); put("euh", "E")
            put("effe", "F"); put("f", "F"); put("fe", "F"); put("eff", "F")
            put("gé", "G"); put("g", "G"); put("ge", "G")
            put("ache", "H"); put("h", "H"); put("hache", "H")
            put("i", "I"); put("î", "I")
            put("ji", "J"); put("j", "J"); put("gie", "J")
            put("ka", "K"); put("k", "K"); put("ca", "K"); put("ke", "K"); put("que", "K")
            put("elle", "L"); put("l", "L"); put("èle", "L"); put("aile", "L")
            put("ème", "M"); put("emme", "M"); put("m", "M"); put("me", "M"); put("aime", "M")
            put("ène", "N"); put("enne", "N"); put("n", "N"); put("ne", "N"); put("aine", "N")
            put("o", "O"); put("ô", "O"); put("au", "O")
            put("pé", "P"); put("p", "P"); put("pe", "P")
            put("ku", "Q"); put("q", "Q"); put("keu", "Q"); put("qu", "Q")
            put("erre", "R"); put("r", "R"); put("re", "R"); put("ère", "R"); put("air", "R")
            put("esse", "S"); put("s", "S"); put("se", "S"); put("ss", "S"); put("es", "S")
            put("té", "T"); put("t", "T"); put("te", "T"); put("thé", "T")
            put("u", "U"); put("ù", "U"); put("û", "U")
            put("vé", "V"); put("v", "V"); put("ve", "V")
            put("double vé", "W"); put("double v", "W")
            put("doubleve", "W"); put("w", "W"); put("we", "W")
            put("ixe", "X"); put("iks", "X"); put("x", "X"); put("icse", "X")
            put("igrec", "Y"); put("y", "Y"); put("i grec", "Y")
            put("zède", "Z"); put("z", "Z"); put("ze", "Z"); put("zede", "Z")
        }

        fillers = setOf(
            "le", "la", "les", "de", "du", "des", "au", "aux", "en", "par", "est",
            "un", "une", "et", "ou", "pour", "dans", "sur", "avec", "sans", "sous",
            "trait", "tiret", "slash", "barre", "point", "points", "espace", "virgule",
            "moins", "plus", "fois", "tiré",
            "moteur", "moteurs", "tag", "référence", "reference", "réf", "ref"
        )
    }

    fun normalizeVoiceText(rawText: String): String {
        val step1 = rawText.lowercase(Locale.ROOT)
            .replace(Regex("[-_.,/\\\\]"), " ")
            .trim()

        val words = step1.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val result = StringBuilder()
        var i = 0

        val simple = mutableListOf<String>()
        for (w in words) {
            simple.add(w.replace(Regex("[àâä]"),"a")
                .replace(Regex("[éèêë]"),"e")
                .replace(Regex("[îï]"),"i")
                .replace(Regex("[ôö]"),"o")
                .replace(Regex("[ùûü]"),"u"))
        }

        while (i < words.size) {
            val word = words[i]
            val clean = simple[i]

            val compoundParsed = tryParseCompoundNumber(simple, i)
            if (compoundParsed != null) {
                result.append(compoundParsed.first)
                i += compoundParsed.second
                continue
            }

            if (clean in numberWords) {
                result.append(numberWords[clean])
                i++
                continue
            }

            if (word in letterSounds) {
                result.append(letterSounds[word])
                i++
                continue
            }

            if (clean in fillers) {
                i++
                continue
            }

            if (word.all { it.isLetterOrDigit() }) {
                result.append(word.uppercase(Locale.ROOT))
                i++
                continue
            }

            i++
        }

        return result.toString()
    }

    private fun tryParseCompoundNumber(words: List<String>, start: Int): Pair<String, Int>? {
        val remaining = words.size - start
        if (remaining < 2) return null

        val w0 = words[start]
        val w1 = words.getOrElse(start + 1) { "" }

        if (w1 == "et" && remaining > 2) {
            val w2 = words[start + 2]
            val n0 = numberWords[w0]
            val n2 = numberWords[w2]
            if (n0 != null && n2 != null && n0 in listOf(20, 30, 40, 50, 60) && n2 in 1..9) {
                return Pair((n0 + n2).toString(), 3)
            }
        }

        if (w0 == "quatre" && w1 == "vingt") {
            if (start + 2 < words.size && words[start + 2] in listOf(
                    "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize",
                    "un", "une", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf"
                )) {
                val extra = numberWords[words[start + 2]] ?: 0
                return Pair((80 + extra).toString(), 3)
            }
            return Pair("80", 2)
        }

        if (w0 == "soixante" && w1 in listOf(
                "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize"
            )) {
            return Pair((60 + (numberWords[w1] ?: 0)).toString(), 2)
        }

        if (w0 == "cent" || w0 == "cents") {
            var total = 100
            var consumed = 1
            if (start + consumed < words.size) {
                val nw = words[start + consumed]
                val tensVal = numberWords[nw]
                if (tensVal != null && tensVal in listOf(10, 20, 30, 40, 50, 60, 100)) {
                    total += tensVal
                    consumed++
                    if (tensVal < 100 && start + consumed < words.size) {
                        val uw = words[start + consumed]
                        val unitVal = numberWords[uw]
                        if (unitVal != null && unitVal in 1..9) {
                            total += unitVal
                            consumed++
                        }
                    }
                }
            }
            return Pair(total.toString(), consumed)
        }

        if (w0 in numberWords && w1 in numberWords) {
            val n0 = numberWords[w0]!!
            val n1 = numberWords[w1]!!
            if (n0 in listOf(20, 30, 40, 50, 60) && n1 in 1..9) {
                return Pair((n0 + n1).toString(), 2)
            }
        }

        return null
    }

    fun reconstructTag(normalized: String): String {
        if (normalized.isEmpty()) return ""

        val groups = mutableListOf<Pair<Boolean, String>>()
        var currentLetters = StringBuilder()
        var currentDigits = StringBuilder()

        for (ch in normalized) {
            if (ch.isDigit()) {
                if (currentLetters.isNotEmpty()) {
                    groups.add(true to currentLetters.toString())
                    currentLetters = StringBuilder()
                }
                currentDigits.append(ch)
            } else if (ch.isLetter()) {
                if (currentDigits.isNotEmpty()) {
                    groups.add(false to currentDigits.toString())
                    currentDigits = StringBuilder()
                }
                currentLetters.append(ch)
            }
        }
        if (currentLetters.isNotEmpty()) groups.add(true to currentLetters.toString())
        if (currentDigits.isNotEmpty()) groups.add(false to currentDigits.toString())

        val merged = mutableListOf<Pair<Boolean, String>>()
        for ((isLetter, value) in groups) {
            if (merged.isNotEmpty() && merged.last().first == isLetter) {
                merged[merged.size - 1] = isLetter to (merged.last().second + value)
            } else {
                merged.add(isLetter to value)
            }
        }

        if (merged.isEmpty()) return ""

        if (merged.size > 2 && merged[0].first && merged[1].first) {
            val mergedLetters = merged[0].second + merged[1].second
            val newMerged = mutableListOf(merged[0].first to mergedLetters)
            newMerged.addAll(merged.drop(2))
            return newMerged.joinToString("") { it.second }
        }

        return merged.joinToString("") { it.second }
    }

    fun processVoiceInput(rawText: String): VoiceSearchResult {
        val normalized = normalizeVoiceText(rawText)
        val reconstructed = reconstructTag(normalized)
        return VoiceSearchResult(
            originalQuery = rawText,
            normalizedQuery = normalized,
            reconstructedTag = reconstructed,
            candidates = emptyList()
        )
    }

    fun generateNgrams(s: String, n: Int): Set<String> = generateNgramsStatic(s, n)

    companion object {
        @JvmStatic
        fun generateNgramsStatic(s: String, n: Int): Set<String> {
            val result = mutableSetOf<String>()
            val padded = " ".repeat(n - 1) + s + " ".repeat(n - 1)
            for (i in 0..(padded.length - n)) {
                result.add(padded.substring(i, i + n))
            }
            return result
        }
    }

    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    fun <T> autocompleteSuggest(
        index: SearchIndex<T>,
        query: String,
        maxResults: Int = 8
    ): List<SmartSearchResult<T>> {
        if (query.length < 2) return emptyList()

        val voiceResult = processVoiceInput(query)
        val cleanQuery = SearchIndex.stripFormatting(query)
        val voiceClean = SearchIndex.stripFormatting(voiceResult.reconstructedTag)

        val scoredMap = mutableMapOf<Int, SmartSearchResult<T>>()

        fun record(idx: Int, score: Float, type: MatchType, field: String) {
            val existing = scoredMap[idx]
            if (existing == null || existing.score < score) {
                scoredMap[idx] = SmartSearchResult(index.items[idx], score, type, field)
            }
        }

        val prefixFieldIndices = mutableSetOf<Int>()
        val prefixes = listOfNotNull(cleanQuery.takeIf { it.length >= 2 }, voiceClean.takeIf { it.length >= 2 && it != cleanQuery }).distinct()
        for (p in prefixes) {
            val matches = index.prefixIndex[p] ?: continue
            for (fi in matches) {
                val field = index.searchableFields[fi]
                prefixFieldIndices.add(field.itemIndex)
            }
        }

        for (itemIdx in prefixFieldIndices) {
            val item = index.items[itemIdx]
            val combined = index.combinedExtractor(item)
            val stripped = SearchIndex.stripFormatting(combined)

            val score = when {
                cleanQuery.length >= 3 && stripped.startsWith(cleanQuery) -> 0.95f
                voiceClean.length >= 3 && stripped.startsWith(voiceClean) -> 0.9f
                cleanQuery.length >= 2 && stripped.startsWith(cleanQuery) -> 0.85f
                stripped.contains(cleanQuery) -> 0.75f
                else -> 0.7f
            }
            record(itemIdx, score, MatchType.SUBSTRING, "autocomplete")
        }

        if (prefixFieldIndices.isEmpty()) {
            val strippedQuery = SearchIndex.stripFormatting(query)
            if (strippedQuery.length >= 3) {
                val ngrams = generateNgrams(strippedQuery.uppercase(Locale.ROOT), 2) + generateNgrams(strippedQuery.uppercase(Locale.ROOT), 3)
                val fieldIndices = mutableSetOf<Int>()
                for (ngram in ngrams) {
                    val matches = index.ngramIndex[ngram] ?: continue
                    for (fi in matches) fieldIndices.add(index.searchableFields[fi].itemIndex)
                }
                val sQueryNorm = SearchIndex.normalizeForIndex(query)
                for (itemIdx in fieldIndices) {
                    if (itemIdx in scoredMap) continue
                    val item = index.items[itemIdx]
                    val combined = index.combinedExtractor(item)
                    val norm = SearchIndex.normalizeForIndex(combined)
                    if (norm.contains(sQueryNorm)) {
                        val score = 0.6f + 0.3f * (sQueryNorm.length.toFloat() / norm.length.toFloat())
                        record(itemIdx, score.coerceAtMost(0.85f), MatchType.FUZZY, "autocomplete")
                    }
                }
            }
        }

        return scoredMap.entries
            .sortedByDescending { it.value.score }
            .take(maxResults)
            .map { it.value }
    }

    fun <T> smartSearch(index: SearchIndex<T>, query: String, maxResults: Int = 1000): List<SmartSearchResult<T>> {
        if (query.isBlank()) return emptyList()

        val rawQuery = query.trim()
        val normalizedQuery = SearchIndex.normalizeForIndex(rawQuery)
        val strippedQuery = SearchIndex.stripFormatting(rawQuery)

        val needsVoice = rawQuery.any { !it.isLetterOrDigit() || it.isLowerCase() } ||
                          rawQuery.uppercase(Locale.ROOT) != rawQuery
        val voiceNormalized: String
        val voiceReconstructed: String
        val voiceStripped: String
        if (needsVoice) {
            val vn = normalizeVoiceText(rawQuery)
            voiceNormalized = vn
            voiceReconstructed = reconstructTag(vn)
            voiceStripped = SearchIndex.stripFormatting(voiceReconstructed)
        } else {
            voiceNormalized = ""
            voiceReconstructed = ""
            voiceStripped = ""
        }

        val scoredMap = mutableMapOf<Int, SmartSearchResult<T>>()

        fun record(idx: Int, score: Float, type: MatchType, field: String) {
            val existing = scoredMap[idx]
            if (existing == null || existing.score < score) {
                scoredMap[idx] = SmartSearchResult(index.items[idx], score, type, field)
            }
        }

        val scoredItems = mutableSetOf<Int>()

        fun tryMatch(itemIdx: Int, raw: String, norm: String, stripped: String, fname: String): Boolean {
            var matched = false

            if (raw.equals(rawQuery, ignoreCase = true)) { record(itemIdx, 1f, MatchType.EXACT, fname); matched = true }
            if (!matched && stripped.isNotBlank() && stripped == strippedQuery) { record(itemIdx, 0.98f, MatchType.STRIPPED, fname); matched = true }
            if (!matched && norm == normalizedQuery) { record(itemIdx, 0.95f, MatchType.NORMALIZED, fname); matched = true }
            if (!matched && needsVoice && voiceReconstructed.isNotBlank() && raw.equals(voiceReconstructed, ignoreCase = true)) { record(itemIdx, 0.95f, MatchType.VOICE_NORMALIZED, fname); matched = true }
            if (!matched && needsVoice && voiceStripped.isNotBlank() && stripped == voiceStripped) { record(itemIdx, 0.93f, MatchType.VOICE_NORMALIZED, fname); matched = true }
            if (!matched && needsVoice && voiceNormalized.length >= 3 && raw.equals(voiceNormalized, ignoreCase = true)) { record(itemIdx, 0.92f, MatchType.VOICE_NORMALIZED, fname); matched = true }

            if (rawQuery.length >= 2) {
                if (!matched && raw.contains(rawQuery, ignoreCase = true)) {
                    val boost = if (raw.startsWith(rawQuery, ignoreCase = true)) 0.05f else 0f
                    val lenRatio = rawQuery.length.toFloat() / raw.length.toFloat()
                    record(itemIdx, (0.7f + lenRatio * 0.15f + boost).coerceIn(0.65f, 0.9f), MatchType.SUBSTRING, fname)
                    matched = true
                }
                if (!matched && strippedQuery.length >= 2 && stripped.contains(strippedQuery)) {
                    val boost = if (stripped.startsWith(strippedQuery)) 0.05f else 0f
                    record(itemIdx, (0.7f + boost).coerceIn(0.65f, 0.85f), MatchType.STRIPPED, fname)
                    matched = true
                }
            }

            if (!matched && norm.length >= 2 && norm.contains(normalizedQuery)) {
                val boost = if (norm.startsWith(normalizedQuery)) 0.05f else 0f
                record(itemIdx, (0.65f + boost).coerceAtMost(0.85f), MatchType.SUBSTRING, fname)
                matched = true
            }

            if (!matched && needsVoice && voiceReconstructed.isNotBlank() && voiceReconstructed.length >= 2) {
                if (raw.contains(voiceReconstructed, ignoreCase = true)) {
                    record(itemIdx, 0.85f, MatchType.VOICE_NORMALIZED, fname)
                    matched = true
                }
            }

            if (!matched && needsVoice && voiceNormalized.length >= 3) {
                val normVoice = SearchIndex.normalizeForIndex(voiceNormalized)
                if (norm.contains(normVoice)) {
                    record(itemIdx, 0.82f, MatchType.VOICE_NORMALIZED, fname)
                    matched = true
                }
            }

            return matched
        }

        for (field in index.searchableFields) {
            val itemIdx = field.itemIndex
            if (itemIdx in scoredItems) continue
            if (tryMatch(itemIdx, field.rawValue, field.normalizedValue, field.strippedValue, field.fieldName)) {
                scoredItems.add(itemIdx)
            }
        }

        if (scoredMap.size < maxResults) {
            val queryNgrams = buildSet {
                addAll(generateNgrams(rawQuery.uppercase(Locale.ROOT), 2))
                addAll(generateNgrams(rawQuery.uppercase(Locale.ROOT), 3))
                if (needsVoice) {
                    addAll(generateNgrams(voiceNormalized, 2))
                    addAll(generateNgrams(voiceNormalized, 3))
                }
                if (strippedQuery != rawQuery.uppercase(Locale.ROOT)) {
                    addAll(generateNgrams(strippedQuery, 2))
                    addAll(generateNgrams(strippedQuery, 3))
                }
            }

            val queriedFieldIndices = mutableSetOf<Int>()
            for (ngram in queryNgrams) {
                if (ngram.isBlank()) continue
                val matches = index.ngramIndex[ngram] ?: continue
                for (fieldIdx in matches) {
                    val field = index.searchableFields[fieldIdx]
                    if (field.itemIndex in scoredItems) continue
                    queriedFieldIndices.add(fieldIdx)
                }
            }

            val fuzzyTargets = buildList {
                add(normalizedQuery)
                if (strippedQuery != normalizedQuery) add(SearchIndex.normalizeForIndex(strippedQuery))
                if (needsVoice) {
                    val r = SearchIndex.normalizeForIndex(voiceReconstructed)
                    if (r.isNotBlank() && r != normalizedQuery) add(r)
                    val n = SearchIndex.normalizeForIndex(voiceNormalized)
                    if (n.isNotBlank() && n != normalizedQuery) add(n)
                }
            }.distinct()

            for (fieldIdx in queriedFieldIndices) {
                val field = index.searchableFields[fieldIdx]
                val itemIdx = field.itemIndex
                if (itemIdx in scoredItems) continue
                var bestSim = 0f
                for (target in fuzzyTargets) {
                    if (target.length < 2) continue
                    val fv = field.normalizedValue.take(30)
                    val tv = target.take(30)
                    val dist = levenshteinDistance(fv, tv)
                    val maxLen = maxOf(fv.length, tv.length, 1)
                    val sim = 1f - dist.toFloat() / maxLen.toFloat()
                    if (sim > bestSim) bestSim = sim
                    if (bestSim >= 0.6f) break
                }
                if (bestSim >= 0.55f) {
                    record(itemIdx, bestSim, MatchType.FUZZY, "multiple")
                    scoredItems.add(itemIdx)
                }
            }
        }

        return scoredMap.entries
            .sortedByDescending { it.value.score }
            .take(maxResults)
            .map { it.value }
    }
}
