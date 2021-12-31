package com.egon12.developerhelper.rest

import java.lang.Exception
import java.util.*

object Template {
    fun fill(raw: String, env: Map<String, String>): String {
        val stack = Stack<Replacement>()
        var start = 0
        while(start < raw.length) {
            start = raw.indexOf("{{", start)
            if (start == -1) {
                break
            }

            val end = raw.indexOf("}}", start)
            if (end == -1) {
                break
            }

            val key = raw.substring((start+2) .. end)
            val value = env[key] ?: throw Exception("Cannot find env VAR with name $key")

            stack.push(Replacement(start, end, value))
        }

        var result = raw
        while(!stack.empty()) {
            val r: Replacement = stack.pop()
            result = result.replaceRange(r.start, r.end, r.value)
        }

        return result
    }

    fun mapHeader(header: List<Collection.Header>, env: Map<String, String>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for (h: Collection.Header in header) {
            result[h.key] = fill(h.value, env)
        }
        return result
    }

    class Replacement(
        val start: Int,
        val end: Int,
        val value: String,
    )

}