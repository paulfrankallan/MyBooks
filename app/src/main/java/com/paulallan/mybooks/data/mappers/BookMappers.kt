package com.paulallan.mybooks.data.mappers

import com.paulallan.mybooks.data.model.BookListResponse
import com.paulallan.mybooks.domain.extentsion.orUnknownKey
import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.domain.model.BookListResult

/**
 * Maps a BookListResponse from the API to a domain model BookListResult.
 * 
 * This function transforms the data layer response into a domain model by:
 * - Extracting and mapping reading log entries to Book objects
 * - Filtering out entries with null work information
 * - Creating a BookListResult with the mapped books and total count
 * 
 * @return A BookListResult containing the list of mapped Book objects and total count
 */
fun BookListResponse.toBookListResult(): BookListResult {
    val books = readingLogEntries.mapNotNull { entry ->
        val work = entry.work ?: return@mapNotNull null
        Book(
            id = work.key.orUnknownKey(work.hashCode()),
            title = work.title ?: "",
            authors = work.authorNames,
            firstPublishedYear = work.firstPublishedYear?.toString() ?: "",
            coverId = work.coverId,
        )
    }
    return BookListResult(
        books = books,
        totalCount = numFound ?: 0
    )
}
