package com.paulallan.mybooks.data.mappers

import com.paulallan.mybooks.data.model.BookListResponse
import com.paulallan.mybooks.data.model.BookWork
import com.paulallan.mybooks.data.model.ReadingLogEntry
import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.domain.model.BookListResult
import org.junit.Assert.assertEquals
import org.junit.Test

class BookMappersTest {

    @Test
    fun `toBookListResult maps BookListResponse to BookListResult correctly`() {
        // Arrange
        val bookListResponse = BookListResponse(
            page = 1,
            numFound = 2,
            readingLogEntries = listOf(
                ReadingLogEntry(
                    work = BookWork(
                        key = "key1",
                        title = "Title 1",
                        coverId = 12345L,
                        subjects = listOf("Fiction", "Adventure"),
                        authorKeys = listOf("author1", "author2"),
                        authorNames = listOf("Author One", "Author Two"),
                        firstPublishedYear = 2020
                    ),
                    loggedEdition = "edition1",
                    loggedDate = "2023-01-01"
                ),
                ReadingLogEntry(
                    work = BookWork(
                        key = "key2",
                        title = "Title 2",
                        coverId = 67890L,
                        subjects = listOf("Non-fiction", "Biography"),
                        authorKeys = listOf("author3"),
                        authorNames = listOf("Author Three"),
                        firstPublishedYear = 2021
                    ),
                    loggedEdition = "edition2",
                    loggedDate = "2023-02-01"
                )
            )
        )

        // Expected result
        val expectedBooks = listOf(
            Book(
                id = "key1",
                title = "Title 1",
                authors = listOf("Author One", "Author Two"),
                firstPublishedYear = "2020",
                coverId = 12345L
            ),
            Book(
                id = "key2",
                title = "Title 2",
                authors = listOf("Author Three"),
                firstPublishedYear = "2021",
                coverId = 67890L
            )
        )
        val expectedResult = BookListResult(
            books = expectedBooks,
            totalCount = 2
        )

        // Act
        val result = bookListResponse.toBookListResult()

        // Assert
        assertEquals(expectedResult.totalCount, result.totalCount)
        assertEquals(expectedResult.books.size, result.books.size)

        // Verify each book's properties
        for (i in expectedResult.books.indices) {
            assertEquals(expectedResult.books[i].id, result.books[i].id)
            assertEquals(expectedResult.books[i].title, result.books[i].title)
            assertEquals(expectedResult.books[i].authors, result.books[i].authors)
            assertEquals(expectedResult.books[i].firstPublishedYear, result.books[i].firstPublishedYear)
            assertEquals(expectedResult.books[i].coverId, result.books[i].coverId)
        }
    }

    @Test
    fun `toBookListResult handles null values correctly`() {
        // Arrange
        val bookListResponse = BookListResponse(
            page = null,
            numFound = null,
            readingLogEntries = listOf(
                ReadingLogEntry(
                    work = BookWork(
                        key = null,
                        title = null,
                        coverId = null,
                        subjects = emptyList(),
                        authorKeys = emptyList(),
                        authorNames = emptyList(),
                        firstPublishedYear = null
                    ),
                    loggedEdition = null,
                    loggedDate = null
                )
            )
        )

        // Act
        val result = bookListResponse.toBookListResult()

        // Assert
        assertEquals(0, result.totalCount)
        assertEquals(1, result.books.size)

        val book = result.books[0]
        // The key should be "unknown_key_" followed by the hash code
        assert(book.id.startsWith("unknown_key_"))
        assertEquals("", book.title)
        assertEquals(emptyList<String>(), book.authors)
        assertEquals("", book.firstPublishedYear)
        assertEquals(null, book.coverId)
    }

    @Test
    fun `toBookListResult filters out null works`() {
        // Arrange
        val bookListResponse = BookListResponse(
            page = 1,
            numFound = 2,
            readingLogEntries = listOf(
                ReadingLogEntry(
                    work = null,
                    loggedEdition = "edition1",
                    loggedDate = "2023-01-01"
                ),
                ReadingLogEntry(
                    work = BookWork(
                        key = "key2",
                        title = "Title 2",
                        coverId = 67890L,
                        subjects = listOf("Non-fiction"),
                        authorKeys = listOf("author3"),
                        authorNames = listOf("Author Three"),
                        firstPublishedYear = 2021
                    ),
                    loggedEdition = "edition2",
                    loggedDate = "2023-02-01"
                )
            )
        )

        // Act
        val result = bookListResponse.toBookListResult()

        // Assert
        assertEquals(2, result.totalCount) // numFound from response
        assertEquals(1, result.books.size) // Only one valid work
        assertEquals("key2", result.books[0].id)
    }
}
