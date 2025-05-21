package com.paulallan.mybooks.domain.extentsion

import com.paulallan.mybooks.feature.shared.presentation.CoverSize

/**
 * Provides a fallback string when the receiver is null.
 * 
 * @param hash A hash value to create a unique identifier for the unknown key
 * @return The original string if not null, or a generated key using the provided hash
 */
fun String?.orUnknownKey(hash: Int): String =
    this ?: "unknown_key_$hash"

/**
 * Converts a cover ID to a full URL for retrieving book cover images.
 * 
 * @param size The desired size of the cover image (default: LARGE)
 * @return A URL string pointing to the book cover image on OpenLibrary
 */
fun Long?.toCoverUrl(size: CoverSize = CoverSize.LARGE): String = "https://covers.openlibrary.org/b/id/$this-${size.code}.jpg"
