package com.paulallan.mybooks.data.model

import com.squareup.moshi.Json

data class BookListResponse(
    val page: Int?,
    val numFound: Int?,
    @Json(name = "reading_log_entries")
    val readingLogEntries: List<ReadingLogEntry> = emptyList()
)

data class ReadingLogEntry(
    val work: BookWork?,
    @Json(name = "logged_edition")
    val loggedEdition: String?,
    @Json(name = "logged_date")
    val loggedDate: String?
)

data class BookWork(
    val key: String?,
    val title: String?,
    @Json(name = "cover_id")
    val coverId: Long?,
    val subjects: List<String> = emptyList(),
    @Json(name = "author_keys")
    val authorKeys: List<String> = emptyList(),
    @Json(name = "author_names")
    val authorNames: List<String> = emptyList(),
    @Json(name = "first_publish_year")
    val firstPublishedYear: Int?,
)
