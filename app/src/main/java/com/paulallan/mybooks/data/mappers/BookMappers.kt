package com.paulallan.mybooks.data.mappers

import com.paulallan.mybooks.data.model.BookListResponse
import com.paulallan.mybooks.domain.extentsion.orUnknownKey
import com.paulallan.mybooks.domain.extentsion.toCoverUrl
import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.domain.model.BookListResult

fun BookListResponse.toBookListResult(): BookListResult {
    val books = readingLogEntries.mapNotNull { entry ->
        val work = entry.work ?: return@mapNotNull null
        Book(
            id = work.key.orUnknownKey(work.hashCode()),
            title = work.title ?: "",
            authors = work.authorNames,
            firstPublishedYear = work.firstPublishedYear?.toString() ?: "",
            coverUrl = work.coverId?.toCoverUrl(),
        )
    }
    return BookListResult(
        books = books,
        totalCount = numFound ?: 0
    )
}