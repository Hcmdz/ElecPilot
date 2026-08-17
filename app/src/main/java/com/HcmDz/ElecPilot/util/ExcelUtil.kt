package com.HcmDz.ElecPilot.util

import android.content.Context
import android.net.Uri
import com.HcmDz.ElecPilot.data.db.MotorEntity
import com.HcmDz.ElecPilot.data.db.PlcEntity
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.io.OutputStream

object ExcelUtil {

    init {
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl")
        System.setProperty("org.apache.poi.ss.ignoreMissingFontSystem", "true")
    }

    private val HEADERS = listOf(
        "Atelier", "Position TGBT", "Item", "Désignation",
        "Puissance (kW)", "Types", "Types Départs", "Câble",
        "Type Câble", "TGBT"
    )

    fun importFromUri(context: Context, uri: Uri): List<MotorEntity> {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open file")
        return inputStream.use { stream ->
            val workbook = WorkbookFactory.create(stream)
            try {
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(0)
                if (headerRow != null) {
                    val headers = (0..headerRow.lastCellNum - 1).mapNotNull { idx ->
                        normalizeHeader(getCellValueAsString(headerRow.getCell(idx)))
                    }
                    val matched = MOTOR_HEADERS.count { expected -> headers.any { it == expected } }
                    if (matched < 5) {
                        throw Exception("Excel header mismatch: unexpected column format")
                    }
                }
                val motors = mutableListOf<MotorEntity>()
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    val atelier = getCellValueAsString(row.getCell(0))
                    val positionTGBT = getCellValueAsString(row.getCell(1))
                    val item = getCellValueAsString(row.getCell(2))
                    val designation = getCellValueAsString(row.getCell(3))
                    val puissanceKW = getCellValueAsString(row.getCell(4))
                    val types = getCellValueAsString(row.getCell(5))
                    val typesDeparts = getCellValueAsString(row.getCell(6))
                    val cable = getCellValueAsString(row.getCell(7))
                    val typeCable = getCellValueAsString(row.getCell(8))
                    val tgbt = getCellValueAsString(row.getCell(9))
                    val allBlank = listOf(atelier, positionTGBT, item, designation, puissanceKW, types, typesDeparts, cable, typeCable, tgbt).all { it.isBlank() }
                    if (allBlank) continue
                    motors.add(
                        MotorEntity(
                            atelier = atelier, positionTGBT = positionTGBT, item = item,
                            designation = designation, puissanceKW = puissanceKW, types = types,
                            typesDeparts = typesDeparts, cable = cable, typeCable = typeCable, tgbt = tgbt
                        )
                    )
                }
                motors
            } finally {
                workbook.close()
            }
        }
    }

    fun importCsvFromUri(context: Context, uri: Uri): List<MotorEntity> {
        val charset = detectCsvCharset(context, uri)
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open file")
        val motors = mutableListOf<MotorEntity>()
        inputStream.use { stream ->
            stream.bufferedReader(charset).use { reader ->
                var line = reader.readLine()
                if (line != null) line = line.removePrefix("\uFEFF")
                val delimiter = if (line != null) detectCsvDelimiter(line) else ';'
                var skipHeader = true
                while (line != null) {
                    val trimmed = line.trimEnd('\r')
                    if (trimmed.isNotBlank()) {
                        if (skipHeader) {
                            val headers = parseCsvLine(trimmed, delimiter).map { normalizeHeader(it) }
                            val matched = MOTOR_HEADERS.count { expected -> headers.any { it == expected } }
                            if (matched < 5) {
                                throw Exception("CSV header mismatch: unexpected column format")
                            }
                            skipHeader = false
                        } else {
                            val values = parseCsvLine(trimmed, delimiter)
                            motors.add(
                                MotorEntity(
                                    atelier = values.getOrElse(0) { "" },
                                    positionTGBT = values.getOrElse(1) { "" },
                                    item = values.getOrElse(2) { "" },
                                    designation = values.getOrElse(3) { "" },
                                    puissanceKW = values.getOrElse(4) { "" },
                                    types = values.getOrElse(5) { "" },
                                    typesDeparts = values.getOrElse(6) { "" },
                                    cable = values.getOrElse(7) { "" },
                                    typeCable = values.getOrElse(8) { "" },
                                    tgbt = values.getOrElse(9) { "" }
                                )
                            )
                        }
                    }
                    line = reader.readLine()
                }
            }
        }
        return motors
    }

    private fun detectCsvDelimiter(firstLine: String): Char {
        val semicolons = firstLine.count { it == ';' }
        val commas = firstLine.count { it == ',' }
        val tabs = firstLine.count { it == '\t' }
        return when {
            semicolons >= commas && semicolons >= tabs -> ';'
            commas >= tabs -> ','
            tabs > 0 -> '\t'
            else -> ';'
        }
    }

    private fun parseCsvLine(line: String, delimiter: Char = ';'): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                c == delimiter && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        if (inQuotes) {
            android.util.Log.w("ExcelUtil", "Unterminated quoted field in CSV line")
        }
        return result
    }

    fun exportToCsvStream(outputStream: OutputStream, motors: List<MotorEntity>) {
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write("\uFEFF")
            writer.write(HEADERS.joinToString(";") { csvEscape(it) })
            writer.newLine()
            motors.forEach { motor ->
                val values = listOf(
                    motor.atelier, motor.positionTGBT, motor.item, motor.designation,
                    motor.puissanceKW, motor.types, motor.typesDeparts, motor.cable,
                    motor.typeCable, motor.tgbt
                )
                writer.write(values.joinToString(";") { csvEscape(it) })
                writer.newLine()
            }
        }
    }

    fun exportPlcToCsvStream(outputStream: OutputStream, plcList: List<PlcEntity>) {
        val headers = listOf("Atelier", "DP", "Carte", "Position", "Item", "Désignation")
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write("\uFEFF")
            writer.write(headers.joinToString(";") { csvEscape(it) })
            writer.newLine()
            plcList.forEach { plc ->
                val values = listOf(plc.atelier, plc.dp, plc.carte, plc.position, plc.item, plc.designation)
                writer.write(values.joinToString(";") { csvEscape(it) })
                writer.newLine()
            }
        }
    }

    fun exportToExcelStream(outputStream: OutputStream, motors: List<MotorEntity>) {
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("Departs")
            val headerFont = workbook.createFont().apply { bold = true }
            val headerStyle = workbook.createCellStyle().apply { setFont(headerFont) }

            val headerRow = sheet.createRow(0)
            HEADERS.forEachIndexed { i, h ->
                headerRow.createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
            }
            motors.forEachIndexed { rowIdx, m ->
                val row = sheet.createRow(rowIdx + 1)
                row.createCell(0).setCellValue(m.atelier)
                row.createCell(1).setCellValue(m.positionTGBT)
                row.createCell(2).setCellValue(m.item)
                row.createCell(3).setCellValue(m.designation)
                row.createCell(4).setCellValue(m.puissanceKW)
                row.createCell(5).setCellValue(m.types)
                row.createCell(6).setCellValue(m.typesDeparts)
                row.createCell(7).setCellValue(m.cable)
                row.createCell(8).setCellValue(m.typeCable)
                row.createCell(9).setCellValue(m.tgbt)
            }
            sheet.setColumnWidth(0, 20 * 256)  // Atelier
            sheet.setColumnWidth(1, 18 * 256)  // Position TGBT
            sheet.setColumnWidth(2, 22 * 256)  // Item
            sheet.setColumnWidth(3, 35 * 256)  // Désignation
            sheet.setColumnWidth(4, 16 * 256)  // Puissance (kW)
            sheet.setColumnWidth(5, 16 * 256)  // Types
            sheet.setColumnWidth(6, 16 * 256)  // Types Départs
            sheet.setColumnWidth(7, 20 * 256)  // Câble
            sheet.setColumnWidth(8, 14 * 256)  // Type Câble
            sheet.setColumnWidth(9, 14 * 256)  // TGBT
            workbook.write(outputStream)
        }
    }

    fun exportPlcToExcelStream(outputStream: OutputStream, plcList: List<PlcEntity>) {
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("PLC IO")
            val headerFont = workbook.createFont().apply { bold = true }
            val headerStyle = workbook.createCellStyle().apply { setFont(headerFont) }
            val headers = listOf("Atelier", "DP", "Carte", "Position", "Item", "Désignation")

            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { i, h ->
                headerRow.createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
            }
            plcList.forEachIndexed { rowIdx, p ->
                val row = sheet.createRow(rowIdx + 1)
                row.createCell(0).setCellValue(p.atelier)
                row.createCell(1).setCellValue(p.dp)
                row.createCell(2).setCellValue(p.carte)
                row.createCell(3).setCellValue(p.position)
                row.createCell(4).setCellValue(p.item)
                row.createCell(5).setCellValue(p.designation)
            }
            sheet.setColumnWidth(0, 20 * 256)  // Atelier
            sheet.setColumnWidth(1, 14 * 256)  // DP
            sheet.setColumnWidth(2, 18 * 256)  // Carte
            sheet.setColumnWidth(3, 14 * 256)  // Position
            sheet.setColumnWidth(4, 22 * 256)  // Item
            sheet.setColumnWidth(5, 35 * 256)  // Désignation
            workbook.write(outputStream)
        }
    }

    fun importMotorsFromUri(context: Context, uri: Uri, isExcel: Boolean): List<MotorEntity> {
        return if (isExcel) importFromUri(context, uri) else importCsvFromUri(context, uri)
    }

    fun importPlcsFromUri(context: Context, uri: Uri, isExcel: Boolean): List<PlcEntity> {
        return if (isExcel) importPlcFromUri(context, uri) else importPlcCsvFromUri(context, uri)
    }

    private fun isExcelFile(context: Context, uri: Uri): Boolean {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            val magic = ByteArray(4)
            val n = stream.read(magic)
            n >= 2 && ((magic[0] == 0x50.toByte() && magic[1] == 0x4B.toByte()) ||
            (magic[0] == 0xD0.toByte() && magic[1] == 0xCF.toByte()))
        } ?: false
    }

    private fun detectCsvCharset(context: Context, uri: Uri): java.nio.charset.Charset {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            val bom = ByteArray(2)
            val n = stream.read(bom)
            when {
                n >= 2 && bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte() -> java.nio.charset.Charset.forName("UTF-16LE")
                n >= 2 && bom[0] == 0xFE.toByte() && bom[1] == 0xFF.toByte() -> java.nio.charset.Charset.forName("UTF-16BE")
                else -> Charsets.UTF_8
            }
        } ?: Charsets.UTF_8
    }

    private fun csvEscape(value: String): String {
        return if (value.contains(';') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
    }
    fun importPlcFromUri(context: Context, uri: Uri): List<PlcEntity> {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open file")
        return inputStream.use { stream ->
            val workbook = WorkbookFactory.create(stream)
            try {
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(0)
                if (headerRow != null) {
                    val headers = (0..headerRow.lastCellNum - 1).mapNotNull { idx ->
                        normalizeHeader(getCellValueAsString(headerRow.getCell(idx)))
                    }
                    val matched = PLC_HEADERS.count { expected -> headers.any { it == expected } }
                    if (matched < 3) {
                        throw Exception("Excel header mismatch: unexpected column format")
                    }
                }
                val plcList = mutableListOf<PlcEntity>()
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    val atelier = getCellValueAsString(row.getCell(0))
                    val dp = getCellValueAsString(row.getCell(1))
                    val carte = getCellValueAsString(row.getCell(2))
                    val position = getCellValueAsString(row.getCell(3))
                    val item = getCellValueAsString(row.getCell(4))
                    val designation = getCellValueAsString(row.getCell(5))
                    val allBlank = listOf(atelier, dp, carte, position, item, designation).all { it.isBlank() }
                    if (allBlank) continue
                    plcList.add(
                        PlcEntity(atelier = atelier, dp = dp, carte = carte, position = position, item = item, designation = designation)
                    )
                }
                plcList
            } finally {
                workbook.close()
            }
        }
    }

    fun importPlcCsvFromUri(context: Context, uri: Uri): List<PlcEntity> {
        val charset = detectCsvCharset(context, uri)
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open file")
        val plcList = mutableListOf<PlcEntity>()
        inputStream.use { stream ->
            stream.bufferedReader(charset).use { reader ->
                var line = reader.readLine()
                if (line != null) line = line.removePrefix("\uFEFF")
                val delimiter = if (line != null) detectCsvDelimiter(line) else ';'
                var skipHeader = true
                while (line != null) {
                    val trimmed = line.trimEnd('\r')
                    if (trimmed.isNotBlank()) {
                        if (skipHeader) {
                            val headers = parseCsvLine(trimmed, delimiter).map { normalizeHeader(it) }
                            val matched = PLC_HEADERS.count { expected -> headers.any { it == expected } }
                            if (matched < 3) {
                                throw Exception("CSV header mismatch: unexpected column format")
                            }
                            skipHeader = false
                        } else {
                            val values = parseCsvLine(trimmed, delimiter)
                            plcList.add(
                                PlcEntity(
                                    atelier = values.getOrElse(0) { "" },
                                    dp = values.getOrElse(1) { "" },
                                    carte = values.getOrElse(2) { "" },
                                    position = values.getOrElse(3) { "" },
                                    item = values.getOrElse(4) { "" },
                                    designation = values.getOrElse(5) { "" }
                                )
                            )
                        }
                    }
                    line = reader.readLine()
                }
            }
        }
        return plcList
    }

    private fun normalizeHeader(h: String): String {
        val sb = StringBuilder()
        for (c in h.lowercase().trim()) {
            when (c) {
                'é', 'è', 'ê', 'ë' -> sb.append('e')
                'à', 'â', 'ä' -> sb.append('a')
                'ù', 'û', 'ü' -> sb.append('u')
                'ô', 'ö' -> sb.append('o')
                'î', 'ï' -> sb.append('i')
                'ç' -> sb.append('c')
                '\n', '\r', '\t', '(', ')', ' ', '.', '-', '\'' -> {}
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private val MOTOR_HEADERS = HEADERS.map { normalizeHeader(it) }.toSet()
    private val PLC_HEADERS = listOf("Atelier", "DP", "Carte", "Position", "Item", "Désignation").map { normalizeHeader(it) }.toSet()

    enum class FileType { DEPARTS, PLC, UNKNOWN }

    data class FileTypeResult(val type: FileType, val isExcel: Boolean)

    fun detectFileTypeWithFormat(context: Context, uri: Uri): FileTypeResult {
        val isExcel = isExcelFile(context, uri)
        val fileType = detectFileTypeFromFormat(context, uri, isExcel)
        return FileTypeResult(fileType, isExcel)
    }

    private fun detectFileTypeFromFormat(context: Context, uri: Uri, isExcel: Boolean): FileType {
        val firstRowHeaders: List<String>
        if (isExcel) {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return FileType.UNKNOWN
            val workbook = inputStream.use { WorkbookFactory.create(it) }
            try {
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(0) ?: return FileType.UNKNOWN
                firstRowHeaders = (0..headerRow.lastCellNum - 1).mapNotNull { idx ->
                    getCellValueAsString(headerRow.getCell(idx))
                }
            } finally {
                workbook.close()
            }
        } else {
            val charset = detectCsvCharset(context, uri)
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return FileType.UNKNOWN
            val firstLine = inputStream.use { stream ->
                stream.bufferedReader(charset).use { reader ->
                    var line = reader.readLine()
                    if (line != null) line = line.removePrefix("\uFEFF").trimEnd('\r')
                    line
                }
            }
            if (firstLine.isNullOrBlank()) return FileType.UNKNOWN
            val csvDelimiter = detectCsvDelimiter(firstLine)
            firstRowHeaders = parseCsvLine(firstLine, csvDelimiter)
        }

        val normalizedHeaders = firstRowHeaders.map { normalizeHeader(it) }
        val motorScore = MOTOR_HEADERS.count { expected -> normalizedHeaders.any { it.contains(expected) } }
        val plcScore = PLC_HEADERS.count { expected -> normalizedHeaders.any { it.contains(expected) } }
        return when {
            motorScore >= plcScore && motorScore >= 5 -> FileType.DEPARTS
            plcScore > motorScore && plcScore >= 3 -> FileType.PLC
            motorScore >= 5 -> FileType.DEPARTS
            plcScore >= 3 -> FileType.PLC
            else -> FileType.UNKNOWN
        }
    }

    fun detectFileType(context: Context, uri: Uri): FileType {
        return detectFileTypeWithFormat(context, uri).type
    }

    private fun getCellValueAsString(cell: org.apache.poi.ss.usermodel.Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue
            org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
                val num = cell.numericCellValue
                if (num == num.toLong().toDouble()) num.toLong().toString()
                else num.toString()
            }
            org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
            org.apache.poi.ss.usermodel.CellType.FORMULA -> {
                try {
                    val num = cell.numericCellValue
                    if (num == num.toLong().toDouble()) num.toLong().toString()
                    else num.toString()
                } catch (_: Exception) { try { cell.stringCellValue } catch (_: Exception) { "" } }
            }
            org.apache.poi.ss.usermodel.CellType.ERROR -> ""
            else -> ""
        }
    }

    fun detectFileTypeFromBytes(bytes: ByteArray): FileTypeResult {
        val isExcel = bytes.size >= 2 &&
            ((bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()) ||
            (bytes[0] == 0xD0.toByte() && bytes[1] == 0xCF.toByte()))
        val fileType = detectFileTypeFromBytesImpl(bytes, isExcel)
        return FileTypeResult(fileType, isExcel)
    }

    private fun detectFileTypeFromBytesImpl(bytes: ByteArray, isExcel: Boolean): FileType {
        val firstRowHeaders: List<String>
        if (isExcel) {
            val inputStream = java.io.ByteArrayInputStream(bytes)
            val workbook = inputStream.use { WorkbookFactory.create(it) }
            try {
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(0) ?: return FileType.UNKNOWN
                firstRowHeaders = (0..headerRow.lastCellNum - 1).mapNotNull { idx ->
                    getCellValueAsString(headerRow.getCell(idx))
                }
            } finally {
                workbook.close()
            }
        } else {
            val charset = detectCsvCharsetFromBytes(bytes)
            val firstLine = java.io.ByteArrayInputStream(bytes).use { stream ->
                stream.bufferedReader(charset).use { reader ->
                    var line = reader.readLine()
                    if (line != null) line = line.removePrefix("\uFEFF").trimEnd('\r')
                    line
                }
            }
            if (firstLine.isNullOrBlank()) return FileType.UNKNOWN
            val csvDelimiter = detectCsvDelimiter(firstLine)
            firstRowHeaders = parseCsvLine(firstLine, csvDelimiter)
        }
        val normalizedHeaders = firstRowHeaders.map { normalizeHeader(it) }
        val motorScore = MOTOR_HEADERS.count { expected -> normalizedHeaders.any { it.contains(expected) } }
        val plcScore = PLC_HEADERS.count { expected -> normalizedHeaders.any { it.contains(expected) } }
        return when {
            motorScore >= plcScore && motorScore >= 5 -> FileType.DEPARTS
            plcScore > motorScore && plcScore >= 3 -> FileType.PLC
            motorScore >= 5 -> FileType.DEPARTS
            plcScore >= 3 -> FileType.PLC
            else -> FileType.UNKNOWN
        }
    }

    private fun detectCsvCharsetFromBytes(bytes: ByteArray): java.nio.charset.Charset {
        if (bytes.size >= 2) {
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) return java.nio.charset.Charset.forName("UTF-16LE")
            if (bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) return java.nio.charset.Charset.forName("UTF-16BE")
        }
        return Charsets.UTF_8
    }

    fun importMotorsFromBytes(bytes: ByteArray, isExcel: Boolean): List<MotorEntity> {
        return if (isExcel) importFromBytes(bytes) else importCsvFromBytes(bytes)
    }

    fun importPlcsFromBytes(bytes: ByteArray, isExcel: Boolean): List<PlcEntity> {
        return if (isExcel) importPlcFromBytes(bytes) else importPlcCsvFromBytes(bytes)
    }

    private fun importFromBytes(bytes: ByteArray): List<MotorEntity> {
        val inputStream = java.io.ByteArrayInputStream(bytes)
        return inputStream.use { stream ->
            val workbook = WorkbookFactory.create(stream)
            try {
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(0)
                if (headerRow != null) {
                    val headers = (0..headerRow.lastCellNum - 1).mapNotNull { idx ->
                        normalizeHeader(getCellValueAsString(headerRow.getCell(idx)))
                    }
                    val matched = MOTOR_HEADERS.count { expected -> headers.any { it == expected } }
                    if (matched < 5) {
                        throw Exception("Excel header mismatch: unexpected column format")
                    }
                }
                val motors = mutableListOf<MotorEntity>()
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    val atelier = getCellValueAsString(row.getCell(0))
                    val positionTGBT = getCellValueAsString(row.getCell(1))
                    val item = getCellValueAsString(row.getCell(2))
                    val designation = getCellValueAsString(row.getCell(3))
                    val puissanceKW = getCellValueAsString(row.getCell(4))
                    val types = getCellValueAsString(row.getCell(5))
                    val typesDeparts = getCellValueAsString(row.getCell(6))
                    val cable = getCellValueAsString(row.getCell(7))
                    val typeCable = getCellValueAsString(row.getCell(8))
                    val tgbt = getCellValueAsString(row.getCell(9))
                    val allBlank = listOf(atelier, positionTGBT, item, designation, puissanceKW, types, typesDeparts, cable, typeCable, tgbt).all { it.isBlank() }
                    if (allBlank) continue
                    motors.add(
                        MotorEntity(
                            atelier = atelier, positionTGBT = positionTGBT, item = item,
                            designation = designation, puissanceKW = puissanceKW, types = types,
                            typesDeparts = typesDeparts, cable = cable, typeCable = typeCable, tgbt = tgbt
                        )
                    )
                }
                motors
            } finally {
                workbook.close()
            }
        }
    }

    private fun importCsvFromBytes(bytes: ByteArray): List<MotorEntity> {
        val charset = detectCsvCharsetFromBytes(bytes)
        val motors = mutableListOf<MotorEntity>()
        java.io.ByteArrayInputStream(bytes).use { stream ->
            stream.bufferedReader(charset).use { reader ->
                var line = reader.readLine()
                if (line != null) line = line.removePrefix("\uFEFF")
                val delimiter = if (line != null) detectCsvDelimiter(line) else ';'
                var skipHeader = true
                while (line != null) {
                    val trimmed = line.trimEnd('\r')
                    if (trimmed.isNotBlank()) {
                        if (skipHeader) {
                            val headers = parseCsvLine(trimmed, delimiter).map { normalizeHeader(it) }
                            val matched = MOTOR_HEADERS.count { expected -> headers.any { it == expected } }
                            if (matched < 5) {
                                throw Exception("CSV header mismatch: unexpected column format")
                            }
                            skipHeader = false
                        } else {
                            val values = parseCsvLine(trimmed, delimiter)
                            motors.add(
                                MotorEntity(
                                    atelier = values.getOrElse(0) { "" },
                                    positionTGBT = values.getOrElse(1) { "" },
                                    item = values.getOrElse(2) { "" },
                                    designation = values.getOrElse(3) { "" },
                                    puissanceKW = values.getOrElse(4) { "" },
                                    types = values.getOrElse(5) { "" },
                                    typesDeparts = values.getOrElse(6) { "" },
                                    cable = values.getOrElse(7) { "" },
                                    typeCable = values.getOrElse(8) { "" },
                                    tgbt = values.getOrElse(9) { "" }
                                )
                            )
                        }
                    }
                    line = reader.readLine()
                }
            }
        }
        return motors
    }

    private fun importPlcFromBytes(bytes: ByteArray): List<PlcEntity> {
        val inputStream = java.io.ByteArrayInputStream(bytes)
        return inputStream.use { stream ->
            val workbook = WorkbookFactory.create(stream)
            try {
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(0)
                if (headerRow != null) {
                    val headers = (0..headerRow.lastCellNum - 1).mapNotNull { idx ->
                        normalizeHeader(getCellValueAsString(headerRow.getCell(idx)))
                    }
                    val matched = PLC_HEADERS.count { expected -> headers.any { it == expected } }
                    if (matched < 3) {
                        throw Exception("Excel header mismatch: unexpected column format")
                    }
                }
                val plcList = mutableListOf<PlcEntity>()
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    val atelier = getCellValueAsString(row.getCell(0))
                    val dp = getCellValueAsString(row.getCell(1))
                    val carte = getCellValueAsString(row.getCell(2))
                    val position = getCellValueAsString(row.getCell(3))
                    val item = getCellValueAsString(row.getCell(4))
                    val designation = getCellValueAsString(row.getCell(5))
                    val allBlank = listOf(atelier, dp, carte, position, item, designation).all { it.isBlank() }
                    if (allBlank) continue
                    plcList.add(
                        PlcEntity(atelier = atelier, dp = dp, carte = carte, position = position, item = item, designation = designation)
                    )
                }
                plcList
            } finally {
                workbook.close()
            }
        }
    }

    private fun importPlcCsvFromBytes(bytes: ByteArray): List<PlcEntity> {
        val charset = detectCsvCharsetFromBytes(bytes)
        val plcList = mutableListOf<PlcEntity>()
        java.io.ByteArrayInputStream(bytes).use { stream ->
            stream.bufferedReader(charset).use { reader ->
                var line = reader.readLine()
                if (line != null) line = line.removePrefix("\uFEFF")
                val delimiter = if (line != null) detectCsvDelimiter(line) else ';'
                var skipHeader = true
                while (line != null) {
                    val trimmed = line.trimEnd('\r')
                    if (trimmed.isNotBlank()) {
                        if (skipHeader) {
                            val headers = parseCsvLine(trimmed, delimiter).map { normalizeHeader(it) }
                            val matched = PLC_HEADERS.count { expected -> headers.any { it == expected } }
                            if (matched < 3) {
                                throw Exception("CSV header mismatch: unexpected column format")
                            }
                            skipHeader = false
                        } else {
                            val values = parseCsvLine(trimmed, delimiter)
                            plcList.add(
                                PlcEntity(
                                    atelier = values.getOrElse(0) { "" },
                                    dp = values.getOrElse(1) { "" },
                                    carte = values.getOrElse(2) { "" },
                                    position = values.getOrElse(3) { "" },
                                    item = values.getOrElse(4) { "" },
                                    designation = values.getOrElse(5) { "" }
                                )
                            )
                        }
                    }
                    line = reader.readLine()
                }
            }
        }
        return plcList
    }
}
