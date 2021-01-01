package com.egon12.developerhelper.rest.persistent

import androidx.room.TypeConverter
import org.json.JSONObject
import org.json.JSONTokener

class MapTypeConverter {
    @TypeConverter
    fun toString(m: Map<String, String>): String = JSONObject(m).toString()

    @TypeConverter
    fun toMap(s: String): Map<String, String> {
        val j = JSONTokener(s).nextValue() as JSONObject
        val m = HashMap<String, String>()

        val i = j.keys()
        while (i.hasNext()) {
            val key = i.next()
            m[key] = j.getString(key)
        }
        return m
    }
}