package com.egon12.developerhelper.graphql

interface Matcher {
    val goingLeft: Boolean
    val goingRight: Boolean
    fun match(char: Char): Boolean
}

fun findLeftUntil(content: String, index: Int, m: Matcher): Int {
    if (!m.goingLeft) throw Exception("matcher not compatible")
    var i = index
    while (i > 0) {
        if (m.match(
                content[i]
            )
        ) {
            return i
        }
    }
    return -1
}

fun findRightUntil(content: String, index: Int, m: Matcher): Int {
    if (!m.goingLeft) throw Exception("matcher not compatible")
    var i = index
    while (i < content.length) {
        if (m.match(content[i])) {
            return i
        }
    }
    return -1
}

abstract class BothMatcher : Matcher {
    override val goingLeft = true
    override val goingRight = true
}

class SimpleCharMatcher(val c: Char) : BothMatcher() {
    override fun match(char: Char) = c == char
}

class WhitespaceMatcher() : BothMatcher() {
    override fun match(c: Char) = c == ' ' || c == '\n' || c == '\t'
}

class GroupOpenerMatcher : Matcher {
    override val goingLeft = true
    override val goingRight = false

    private var pCloser = 0
    private var bCloser = 0

    override fun match(c: Char): Boolean {
        return when (c) {
            '}' -> {
                bCloser += 1; false
            }
            ')' -> {
                pCloser += 1; false
            }
            '{' -> {
                bCloser -= 1; bCloser < 0 && pCloser == 0
            }
            '(' -> {
                pCloser -= 1; pCloser < 0 && bCloser == 0
            }
            else -> false
        }
    }
}

class FieldNameMatcher : Matcher {
    override val goingLeft = true
    override val goingRight = false

    private var wordIsFound = false
    private var whiteSpaceMatcher = WhitespaceMatcher()

    override fun match(char: Char): Boolean {
        if (wordIsFound && whiteSpaceMatcher.match(char)) {
            return true
        }
        return false
    }
}


class ContentType {}

data class ContentRange(val type: ContentType, val from: Int, val to: Int)
