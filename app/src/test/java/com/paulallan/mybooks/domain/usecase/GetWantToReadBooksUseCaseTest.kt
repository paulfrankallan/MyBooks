package com.paulallan.mybooks.domain.usecase

import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.domain.model.BookListResult
import com.paulallan.mybooks.domain.repository.BookRepository
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test

class GetWantToReadBooksUseCaseTest {

    private lateinit var bookRepository: BookRepository
    private lateinit var getWantToReadBooksUseCase: GetWantToReadBooksUseCase

    @Before
    fun setup() {
        bookRepository = mockk()
        getWantToReadBooksUseCase = GetWantToReadBooksUseCase(bookRepository)
    }

    @Test
    fun `invoke returns books from repository`() {
        // Arrange
        val limit = 10
        val page = 1
        val books = listOf(
            Book(
                id = "1",
                title = "Book 1",
                authors = listOf("Author 1"),
                firstPublishedYear = "2021",
                coverId = 12345L
            ),
            Book(
                id = "2",
                title = "Book 2",
                authors = listOf("Author 2"),
                firstPublishedYear = "2022",
                coverId = 67890L
            )
        )
        val totalCount = 2
        val bookListResult = BookListResult(books, totalCount)

        every { bookRepository.getWantToReadBooks(limit, page) } returns Single.just(bookListResult)

        // Act
        val testObserver = getWantToReadBooksUseCase(limit, page).test()

        // Assert
        testObserver.assertValue(bookListResult)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    fun `invoke with default parameters calls repository with default values`() {
        // Arrange
        val defaultLimit = 10
        val defaultPage = 1
        val books = listOf(
            Book(
                id = "1",
                title = "Book 1",
                authors = listOf("Author 1"),
                firstPublishedYear = "2021",
                coverId = 12345L
            )
        )
        val totalCount = 1
        val bookListResult = BookListResult(books, totalCount)

        every { bookRepository.getWantToReadBooks(defaultLimit, defaultPage) } returns Single.just(bookListResult)

        // Act
        val testObserver = getWantToReadBooksUseCase().test()

        // Assert
        testObserver.assertValue(bookListResult)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }
}
