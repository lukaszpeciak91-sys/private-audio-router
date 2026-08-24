package app.privateaudio

import java.io.File

internal data class KotlinCallSite(val file: File, val offset: Int)

/** Finds member-call expressions without confusing string literals or comments with executable calls. */
internal fun Iterable<File>.kotlinMemberCallSites(function: String): List<KotlinCallSite> {
    val callExpression = Regex("\\b[A-Za-z_][A-Za-z0-9_]*\\s*\\.\\s*${Regex.escape(function)}\\s*\\(")
    return flatMap { file ->
        callExpression.findAll(file.readText().withoutKotlinCommentsAndStrings())
            .map { match -> KotlinCallSite(file, match.range.first) }
            .toList()
    }
}

private fun String.withoutKotlinCommentsAndStrings(): String {
    val result = StringBuilder(this)
    var index = 0
    var state = ScanState.CODE
    while (index < length) {
        val next = getOrNull(index + 1)
        when (state) {
            ScanState.CODE -> when {
                this[index] == '/' && next == '/' -> state = ScanState.LINE_COMMENT
                this[index] == '/' && next == '*' -> state = ScanState.BLOCK_COMMENT
                this[index] == '"' && substring(index).startsWith("\"\"\"") -> state = ScanState.RAW_STRING
                this[index] == '"' -> state = ScanState.STRING
                this[index] == '\'' -> state = ScanState.CHAR
                else -> index++
            }
            ScanState.LINE_COMMENT -> if (this[index] == '\n') state = ScanState.CODE else result.setCharAt(index++, ' ')
            ScanState.BLOCK_COMMENT -> {
                result.setCharAt(index, ' ')
                if (this[index] == '*' && next == '/') {
                    result.setCharAt(index + 1, ' ')
                    index += 2
                    state = ScanState.CODE
                } else index++
            }
            ScanState.STRING, ScanState.CHAR -> {
                val terminator = if (state == ScanState.STRING) '"' else '\''
                result.setCharAt(index, ' ')
                if (this[index] == '\\') {
                    if (next != null) result.setCharAt(index + 1, ' ')
                    index += 2
                } else if (this[index++] == terminator) state = ScanState.CODE
            }
            ScanState.RAW_STRING -> {
                result.setCharAt(index, ' ')
                if (substring(index).startsWith("\"\"\"")) {
                    repeat(2) { result.setCharAt(++index, ' ') }
                    index++
                    state = ScanState.CODE
                } else index++
            }
        }
    }
    return result.toString()
}

private enum class ScanState { CODE, LINE_COMMENT, BLOCK_COMMENT, STRING, RAW_STRING, CHAR }
