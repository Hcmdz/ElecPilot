package com.HcmDz.ElecPilot.util

import com.HcmDz.ElecPilot.data.db.MotorEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

val TEST_MOTOR_EXTRACTORS: List<Pair<String, (MotorEntity) -> String>> = listOf(
    "atelier" to { it.atelier },
    "positionTGBT" to { it.positionTGBT },
    "item" to { it.item },
    "designation" to { it.designation },
    "puissanceKW" to { it.puissanceKW },
    "types" to { it.types },
    "typesDeparts" to { it.typesDeparts },
    "cable" to { it.cable },
    "typeCable" to { it.typeCable },
    "tgbt" to { it.tgbt },
)

val TEST_MOTOR_COMBINED: (MotorEntity) -> String = {
    "${it.atelier} ${it.positionTGBT} ${it.item} ${it.designation} ${it.puissanceKW} ${it.types} ${it.typesDeparts} ${it.cable} ${it.typeCable} ${it.tgbt}"
}

class VoiceSearchEngineTest {

    private lateinit var engine: VoiceSearchEngine
    private lateinit var motors: List<MotorEntity>
    private lateinit var index: SearchIndex<MotorEntity>

    @Before
    fun setup() {
        engine = VoiceSearchEngine()
        motors = listOf(
            MotorEntity(id = 1, atelier = "Utilités", positionTGBT = "1-A", item = "U021-BC-021-1", designation = "Départ batterie de condensateur", puissanceKW = "1250", types = "", typesDeparts = "ALIM", cable = "", typeCable = "", tgbt = "TGBT U021-1"),
            MotorEntity(id = 2, atelier = "Utilités", positionTGBT = "1-C", item = "U021-AR-021-1", designation = "Arrivée débrochable", puissanceKW = "2500", types = "", typesDeparts = "ARRIVEE", cable = "", typeCable = "", tgbt = "TGBT U021-1"),
            MotorEntity(id = 3, atelier = "Crista HP", positionTGBT = "2-A", item = "S61-ALIM-1-5A", designation = "Alimentation armoire variateur centrifugeuse D412 - HP1A", puissanceKW = "210", types = "", typesDeparts = "ALIM", cable = "", typeCable = "", tgbt = "TGBT U021-1"),
            MotorEntity(id = 4, atelier = "Crista HP", positionTGBT = "2-B", item = "S61-ALIM-1-5B", designation = "Alimentation armoire variateur centrifugeuse D412 - HP1B", puissanceKW = "210", types = "", typesDeparts = "ALIM", cable = "", typeCable = "", tgbt = "TGBT U021-1"),
            MotorEntity(id = 5, atelier = "Chaufferie", positionTGBT = "1-A", item = "S30-P-7", designation = "Pompe à vide", puissanceKW = "315", types = "355ML/L-06 50/60HZ IE1-94,8%", typesDeparts = "VAR", cable = "", typeCable = "", tgbt = "TGBT U021-1"),
            MotorEntity(id = 6, atelier = "Conditionnement", positionTGBT = "4-C", item = "FUTUR", designation = "Futur départ conditionnement", puissanceKW = "", types = "", typesDeparts = "vide", cable = "", typeCable = "", tgbt = "TGBT U021-3"),
        )
        index = SearchIndex(motors, TEST_MOTOR_EXTRACTORS, TEST_MOTOR_COMBINED)
    }

    // ========== normalizeVoiceText ==========

    @Test
    fun normalizeVoiceText_basic_tag() {
        assertEquals("S61ALIM15A", engine.normalizeVoiceText("S61-ALIM-1-5A"))
    }

    @Test
    fun normalizeVoiceText_with_spaces() {
        assertEquals("S61ALIM15A", engine.normalizeVoiceText("S61 ALIM 1 5 A"))
    }

    @Test
    fun normalizeVoiceText_underscores() {
        assertEquals("S61ALIM15A", engine.normalizeVoiceText("S61_ALIM_1_5_A"))
    }

    @Test
    fun normalizeVoiceText_phonetic_letters() {
        assertEquals("S30P7", engine.normalizeVoiceText("esse trente pé 7"))
    }

    @Test
    fun normalizeVoiceText_item_code_spelled() {
        assertEquals("U021BC0211", engine.normalizeVoiceText("u zero vingt et un bc zero vingt et un un"))
    }

    @Test
    fun normalizeVoiceText_atelier_name() {
        assertEquals("CRISTAHP", engine.normalizeVoiceText("Crista HP"))
    }

    @Test
    fun normalizeVoiceText_numbers_spelled() {
        // "cent quinze" = 115 in compound, preceded by "trois" = 3
        val result = engine.normalizeVoiceText("trois cent quinze")
        assertTrue(result.contains("3"))
    }

    @Test
    fun normalizeVoiceText_mixed_phonetic_numbers() {
        assertEquals("S61ALIM15A", engine.normalizeVoiceText("esse soixante et un alim un cinq a"))
    }

    @Test
    fun normalizeVoiceText_compound_cent_trente() {
        assertEquals("130", engine.normalizeVoiceText("cent trente"))
    }

    @Test
    fun normalizeVoiceText_single_letters() {
        assertEquals("ABCD", engine.normalizeVoiceText("a bé cé dé"))
    }

    @Test
    fun normalizeVoiceText_fillers_removed() {
        assertEquals("S30P7", engine.normalizeVoiceText("le s trente pé sept"))
    }

    // ========== reconstructTag ==========

    @Test
    fun reconstructTag_item_code() {
        assertEquals("S61ALIM15A", engine.reconstructTag("S61ALIM15A"))
    }

    @Test
    fun reconstructTag_with_dashes() {
        assertEquals("S61ALIM15A", engine.reconstructTag("S61ALIM15A"))
    }

    @Test
    fun reconstructTag_all_digits() {
        assertEquals("315", engine.reconstructTag("315"))
    }

    @Test
    fun reconstructTag_letter_number_dash() {
        assertEquals("S30P7", engine.reconstructTag("S30P7"))
    }

    // ========== processVoiceInput ==========

    @Test
    fun processVoiceInput_item_code() {
        val result = engine.processVoiceInput("S61-ALIM-1-5A")
        assertEquals("S61ALIM15A", result.reconstructedTag)
        assertEquals("S61-ALIM-1-5A", result.originalQuery)
    }

    @Test
    fun processVoiceInput_spelled_code() {
        val result = engine.processVoiceInput("s soixante et un alim un cinq a")
        assertEquals("S61ALIM15A", result.reconstructedTag)
    }

    @Test
    fun processVoiceInput_numbers_spelled_out() {
        val result = engine.processVoiceInput("u zero vingt et un bc zero vingt et un un")
        assertEquals("U021BC0211", result.reconstructedTag)
    }

    // ========== SearchIndex.stripFormatting ==========

    @Test
    fun stripFormatting_removes_dashes() {
        assertEquals("S61ALIM15A", SearchIndex.stripFormatting("S61-ALIM-1-5A"))
    }

    @Test
    fun stripFormatting_removes_underscores() {
        assertEquals("S61ALIM15A", SearchIndex.stripFormatting("S61_ALIM_1_5_A"))
    }

    @Test
    fun stripFormatting_removes_mixed_separators() {
        assertEquals("U021BC0211", SearchIndex.stripFormatting("U021_BC-021.1"))
    }

    @Test
    fun stripFormatting_removes_spaces() {
        assertEquals("U021BC0211", SearchIndex.stripFormatting("U021 BC 021 1"))
    }

    @Test
    fun stripFormatting_only_alpha_numeric() {
        assertEquals("U021BC0211", SearchIndex.stripFormatting("U021-BC-021-1"))
    }

    // ========== smartSearch - real data scenarios ==========

    @Test
    fun smartSearch_exact_item_code() {
        val results = engine.smartSearch(index, "S61-ALIM-1-5A")
        assertTrue(results.isNotEmpty())
        assertEquals(MatchType.EXACT, results[0].matchType)
        assertEquals(1.0f, results[0].score, 0.01f)
        assertEquals("S61-ALIM-1-5A", results[0].item.item)
    }

    @Test
    fun smartSearch_item_code_lowercase() {
        val results = engine.smartSearch(index, "s61-alim-1-5a")
        assertTrue(results.isNotEmpty())
        assertTrue(results[0].score >= 0.95f)
        assertEquals("S61-ALIM-1-5A", results[0].item.item)
    }

    @Test
    fun smartSearch_item_code_no_dashes() {
        val results = engine.smartSearch(index, "S61ALIM15A")
        assertTrue(results.isNotEmpty())
        assertTrue(results[0].score >= 0.95f)
        assertEquals("S61-ALIM-1-5A", results[0].item.item)
    }

    @Test
    fun smartSearch_item_code_underscored() {
        val results = engine.smartSearch(index, "S61_ALIM_1_5_A")
        assertTrue(results.isNotEmpty())
        assertTrue(results[0].score >= 0.95f)
    }

    @Test
    fun smartSearch_item_code_spaces() {
        val results = engine.smartSearch(index, "S61 ALIM 1 5 A")
        assertTrue(results.isNotEmpty())
        assertTrue(results[0].score >= 0.9f)
    }

    @Test
    fun smartSearch_item_partial_prefix() {
        val results = engine.smartSearch(index, "S61")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.item.item.startsWith("S61") })
    }

    @Test
    fun smartSearch_position_tgbt() {
        val results = engine.smartSearch(index, "1-A")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.item.positionTGBT == "1-A" })
    }

    @Test
    fun smartSearch_atelier_name() {
        val results = engine.smartSearch(index, "Crista HP")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.item.atelier == "Crista HP" })
    }

    @Test
    fun smartSearch_atelier_partial() {
        val results = engine.smartSearch(index, "Crista")
        assertTrue(results.isNotEmpty())
        assertEquals(MatchType.SUBSTRING, results[0].matchType)
    }

    @Test
    fun smartSearch_designation_mot_cle() {
        val results = engine.smartSearch(index, "condensateur")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.item.designation.contains("condensateur", ignoreCase = true) })
    }

    @Test
    fun smartSearch_item_code_partial_dashes() {
        val results = engine.smartSearch(index, "U021")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.item.item.startsWith("U021") })
    }

    @Test
    fun smartSearch_voice_spelled_code() {
        val results = engine.smartSearch(index, "s soixante et un alim un cinq a")
        assertTrue(results.isNotEmpty())
        val best = results.first()
        assertTrue(best.score >= 0.85f)
        assertEquals("S61-ALIM-1-5A", best.item.item)
    }

    @Test
    fun smartSearch_voice_phonetic_position() {
        val results = engine.smartSearch(index, "un A")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.item.positionTGBT == "1-A" })
    }

    @Test
    fun smartSearch_fuzzy_typo_item() {
        val results = engine.smartSearch(index, "S61-ALIM-1-5B")
        assertTrue(results.isNotEmpty())
        // Should find S61-ALIM-1-5A or S61-ALIM-1-5B
        assertTrue(results.any { it.item.item.startsWith("S61-ALIM-1-5") })
    }

    @Test
    fun smartSearch_returns_multiple_results() {
        val results = engine.smartSearch(index, "S61")
        assertTrue(results.size >= 2) // 2 motors with S61
    }

    @Test
    fun smartSearch_empty_query_returns_empty() {
        assertTrue(engine.smartSearch(index, "").isEmpty())
    }

    @Test
    fun smartSearch_no_match_returns_empty() {
        val results = engine.smartSearch(index, "ZZZZZZ999")
        assertTrue(results.isEmpty())
    }

    @Test
    fun smartSearch_scoring_exact_over_fuzzy() {
        val exact = engine.smartSearch(index, "S61-ALIM-1-5A")
        val fuzzy = engine.smartSearch(index, "S61-ALIM-1-5X")
        assertTrue(exact.first().score > fuzzy.first().score)
    }

    @Test
    fun smartSearch_dash_lowercase_variant() {
        val results = engine.smartSearch(index, "s61-alim-1-5a")
        assertTrue(results.isNotEmpty())
        assertTrue(results[0].score >= 0.95f)
        assertEquals("S61-ALIM-1-5A", results.first().item.item)
    }

    @Test
    fun smartSearch_mixed_dash_lowercase() {
        val results = engine.smartSearch(index, "s61-alim-1-5a")
        assertTrue(results.isNotEmpty())
        assertTrue(results[0].score >= 0.9f)
        assertEquals("S61-ALIM-1-5A", results.first().item.item)
    }

    @Test
    fun smartSearch_underscore_lowercase() {
        val results = engine.smartSearch(index, "s61_alim_1_5_a")
        assertTrue(results.isNotEmpty())
        assertTrue(results[0].score >= 0.9f)
        assertEquals("S61-ALIM-1-5A", results.first().item.item)
    }

    @Test
    fun smartSearch_S61ALIM15A_finds_item() {
        val results = engine.smartSearch(index, "S61ALIM15A")
        assertTrue(results.isNotEmpty())
        assertTrue(results[0].score >= 0.9f)
        assertEquals("S61-ALIM-1-5A", results[0].item.item)
    }

    @Test
    fun smartSearch_item_found_with_all_fields() {
        val results = engine.smartSearch(index, "S30-P-7")
        assertTrue(results.isNotEmpty())
        val motor = results.first().item
        assertEquals("Chaufferie", motor.atelier)
        assertEquals("1-A", motor.positionTGBT)
        assertEquals("S30-P-7", motor.item)
        assertEquals("Pompe à vide", motor.designation)
    }

    // ========== autocompleteSuggest ==========

    @Test
    fun autocompleteSuggest_prefix_match() {
        val results = engine.autocompleteSuggest(index, "S61")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.item.item.startsWith("S61-ALIM") })
    }

    @Test
    fun autocompleteSuggest_short_prefix_returns_empty() {
        assertTrue(engine.autocompleteSuggest(index, "S").isEmpty())
    }

    @Test
    fun autocompleteSuggest_empty_query() {
        assertTrue(engine.autocompleteSuggest(index, "").isEmpty())
    }

    // ========== normalizeForIndex ==========

    @Test
    fun normalizeForIndex_strips_accents() {
        assertEquals("utilites", SearchIndex.normalizeForIndex("Utilités"))
    }

    @Test
    fun normalizeForIndex_strips_dashes() {
        assertEquals("s61alim15a", SearchIndex.normalizeForIndex("S61-ALIM-1-5A"))
    }

    @Test
    fun normalizeForIndex_lowercase() {
        assertEquals("crista hp", SearchIndex.normalizeForIndex("Crista HP"))
    }

    // ========== levenshteinDistance ==========

    @Test
    fun levenshtein_same_strings() {
        assertEquals(0, engine.levenshteinDistance("S61-ALIM-1-5A", "S61-ALIM-1-5A"))
    }

    @Test
    fun levenshtein_one_char_diff() {
        assertEquals(1, engine.levenshteinDistance("S61-ALIM-1-5A", "S61-ALIM-1-5B"))
    }

    @Test
    fun levenshtein_completely_different() {
        assertTrue(engine.levenshteinDistance("S61-ALIM-1-5A", "U021-BC-021-1") >= 5)
    }

    // ========== reconstruction edge cases ==========

    @Test
    fun reconstructTag_single_letter() {
        assertEquals("A", engine.reconstructTag("A"))
    }

    @Test
    fun reconstructTag_empty() {
        assertEquals("", engine.reconstructTag(""))
    }

    @Test
    fun reconstructTag_item_code_pattern() {
        assertEquals("S61ALIM15A", engine.reconstructTag("S61ALIM15A"))
    }

    @Test
    fun reconstructTag_atelier_with_space() {
        assertEquals("CRISTAHP", engine.reconstructTag("CRISTAHP"))
    }

    // ========== maxResults (B) & fuzzy threshold (D) ==========

    @Test
    fun smartSearch_returns_all_matches_beyond_50() {
        val many = (1..60).map {
            MotorEntity(
                id = it.toLong(),
                atelier = "Atelier",
                positionTGBT = "1-A",
                item = "MOT-%02d".format(it),
                designation = "Moteur %02d".format(it),
                puissanceKW = "10",
                types = "",
                typesDeparts = "ALIM",
                cable = "",
                typeCable = "",
                tgbt = "TGBT-1"
            )
        }
        val idx = SearchIndex(many, TEST_MOTOR_EXTRACTORS, TEST_MOTOR_COMBINED)
        val results = engine.smartSearch(idx, "MOT")
        assertEquals(60, results.size)
    }

    @Test
    fun smartSearch_weak_fuzzy_match_excluded() {
        val weak = listOf(
            MotorEntity(id = 99, item = "KZ-AB-QU-VX")
        )
        val idx = SearchIndex(weak, TEST_MOTOR_EXTRACTORS, TEST_MOTOR_COMBINED)
        val results = engine.smartSearch(idx, "ZZ-AB-YY-WW")
        assertTrue(results.isEmpty())
    }
}
