package com.paulallan.mybooks.domain.extentsion

import com.paulallan.mybooks.feature.shared.presentation.CoverSize

fun String?.orUnknownKey(hash: Int): String =
    this ?: "unknown_key_$hash"

fun Long?.toCoverUrl(size: CoverSize = CoverSize.LARGE): String = "https://covers.openlibrary.org/b/id/$this-${size.code}.jpg"