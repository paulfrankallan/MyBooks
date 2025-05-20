package com.paulallan.mybooks.domain.extentsion

fun String?.orUnknownKey(hash: Int): String =
    this ?: "unknown_key_$hash"

fun Long?.toCoverUrl(): String = "https://covers.openlibrary.org/b/id/$this-L.jpg"