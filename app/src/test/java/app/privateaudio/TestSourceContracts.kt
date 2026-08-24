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

/**
 * Returns a Kotlin declaration and its body while ignoring non-structural braces.
 *
 * [declaration] should identify the meaningful declaration shape (for example,
 * `fun refresh()`) rather than incidental modifiers such as visibility.
 */
internal fun String.kotlinDeclaration(declaration: String): String {
    val sanitized = withoutKotlinCommentsAndStrings()
    val start = sanitized.indexOf(declaration)
    check(start >= 0) { "Missing Kotlin declaration: $declaration" }

    val openingBrace = sanitized.indexOf('{', start)
    check(openingBrace >= 0) { "Missing body for Kotlin declaration: $declaration" }

    var depth = 0
    for (index in openingBrace until sanitized.length) {
        when (sanitized[index]) {
            '{' -> depth++
            '}' -> if (--depth == 0) return substring(start, index + 1)
        }
    }
    error("Unterminated body for Kotlin declaration: $declaration")
}

private fun String.withoutKotlinCommentsAndStrings(): String {
    val result = StringBuilder(this)
    var index = 0
    var state = ScanState.CODE
    while (index < length) {
        val next = getOrNull(index + 1)
        when (state) {
            ScanState.CODE -> when {
                this[index] == '/' && next == '/' -> {
                    result.blank(index, 2)
                    index += 2
                    state = ScanState.LINE_COMMENT
                }
                this[index] == '/' && next == '*' -> {
                    result.blank(index, 2)
                    index += 2
                    state = ScanState.BLOCK_COMMENT
                }
                startsWith("\"\"\"", index) -> {
                    result.blank(index, 3)
                    index += 3
                    state = ScanState.RAW_STRING
                }
                this[index] == '"' -> {
                    result.setCharAt(index++, ' ')
                    state = ScanState.STRING
                }
                this[index] == '\'' -> {
                    result.setCharAt(index++, ' ')
                    state = ScanState.CHAR
                }
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
                if (startsWith("\"\"\"", index)) {
                    result.blank(index, 3)
                    index += 3
                    state = ScanState.CODE
                } else {
                    result.setCharAt(index++, ' ')
                }
            }
        }
    }
    return result.toString()
}

private fun StringBuilder.blank(start: Int, count: Int) {
    repeat(count) { offset -> setCharAt(start + offset, ' ') }
}

private enum class ScanState { CODE, LINE_COMMENT, BLOCK_COMMENT, STRING, RAW_STRING, CHAR }
