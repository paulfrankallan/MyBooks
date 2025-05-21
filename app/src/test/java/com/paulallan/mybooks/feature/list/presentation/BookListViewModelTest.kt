@file:Suppress("ReactiveStreamsUnusedPublisher")

package com.paulallan.mybooks.feature.list.presentation

import androidx.lifecycle.LifecycleOwner
import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.domain.model.BookListResult
import com.paulallan.mybooks.domain.model.BookListType
import com.paulallan.mybooks.domain.usecase.GetAlreadyReadBooksUseCase
import com.paulallan.mybooks.domain.usecase.GetCurrentlyReadingBooksUseCase
import com.paulallan.mybooks.domain.usecase.GetWantToReadBooksUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.TestScheduler
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class BookListViewModelTest {

    private lateinit var getWantToReadBooksUseCase: GetWantToReadBooksUseCase
    private lateinit var getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase
    private lateinit var getAlreadyReadBooksUseCase: GetAlreadyReadBooksUseCase
    private lateinit var testScheduler: TestScheduler
    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var viewModel: BookListViewModel

    @Before
    fun setup() {
        getWantToReadBooksUseCase = mockk()
        getCurrentlyReadingBooksUseCase = mockk()
        getAlreadyReadBooksUseCase = mockk()
        testScheduler = TestScheduler()
        lifecycleOwner = mockk(relaxed = true)

        viewModel = BookListViewModel(
            getWantToReadBooksUseCase = getWantToReadBooksUseCase,
            getCurrentlyReadingBooksUseCase = getCurrentlyReadingBooksUseCase,
            getAlreadyReadBooksUseCase = getAlreadyReadBooksUseCase,
            subscribeScheduler = testScheduler,
            observeScheduler = testScheduler
        )
    }

    @Test
    fun `loadBooks should update state with books when successful`() {
        // Arrange
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

        every { getWantToReadBooksUseCase(any(), 1) } returns Single.just(bookListResult)

        // Initial hasLoaded state should be false
        assertFalse(viewModel.state.value.hasLoaded)

        // Act
        viewModel.loadBooks()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // Assert
        assert(viewModel.state.value.books == books)
        assertFalse(viewModel.state.value.isLoading)
        assert(viewModel.state.value.error == null)
        assert(viewModel.state.value.currentPage == 1)
        assertFalse(viewModel.state.value.hasMoreData)
        assertTrue(viewModel.state.value.hasNoMoreData)

        // hasLoaded should remain unchanged by loadBooks
        assertFalse(viewModel.state.value.hasLoaded)
    }

    @Test
    fun `loadBooks should update state with error when failure`() {
        // Arrange
        val errorMessage = "Error loading books"
        every { getWantToReadBooksUseCase(any(), 1) } returns Single.error(RuntimeException(errorMessage))

        // Initial hasLoaded state should be false
        assertFalse(viewModel.state.value.hasLoaded)

        // Act
        viewModel.loadBooks()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // Assert
        assert(viewModel.state.value.books.isEmpty())
        assert(!viewModel.state.value.isLoading)
        assert(viewModel.state.value.error == errorMessage)

        // hasLoaded should remain unchanged by loadBooks, even on error
        assertFalse(viewModel.state.value.hasLoaded)
    }

    @Test
    fun `loadMoreBooks should load next page when there is more data`() {
        // Arrange - First load
        val firstPageBooks = listOf(
            Book(
                id = "1",
                title = "Book 1",
                authors = listOf("Author 1"),
                firstPublishedYear = "2021",
                coverId = 12345L
            )
        )
        val totalCount = 25 // More than two pages
        val firstPageResult = BookListResult(firstPageBooks, totalCount)

        every { getWantToReadBooksUseCase(any(), 1) } returns Single.just(firstPageResult)

        // Load first page
        viewModel.loadBooks()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // Arrange - Second load
        val secondPageBooks = listOf(
            Book(
                id = "2",
                title = "Book 2",
                authors = listOf("Author 2"),
                firstPublishedYear = "2022",
                coverId = 67890L
            )
        )
        val secondPageResult = BookListResult(secondPageBooks, totalCount)

        every { getWantToReadBooksUseCase(any(), 2) } returns Single.just(secondPageResult)

        // Act
        viewModel.loadMoreBooks()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // Assert
        assert(viewModel.state.value.books.size == 2)
        assert(!viewModel.state.value.isLoadingMore)
        assert(viewModel.state.value.currentPage == 2)
        assert(viewModel.state.value.hasMoreData)
        assertFalse(viewModel.state.value.hasNoMoreData)

        verify { getWantToReadBooksUseCase(any(), 2) }
    }

    @Test
    fun `loadMoreBooks should not load when already loading`() {
        // Arrange
        val books = listOf(
            Book(
                id = "1",
                title = "Book 1",
                authors = listOf("Author 1"),
                firstPublishedYear = "2021",
                coverId = 12345L
            )
        )
        val totalCount = 15
        val bookListResult = BookListResult(books, totalCount)

        every { getWantToReadBooksUseCase(any(), 1) } returns Single.just(bookListResult)

        viewModel.loadBooks()

        // Act
        viewModel.loadMoreBooks()

        // Assert - Verify the use case was only called once (for loadBooks, not for loadMoreBooks)
        verify(exactly = 1) { getWantToReadBooksUseCase(any(), any()) }
    }

    @Test
    fun `changeBookListType should update state and load books for new type`() {
        // Arrange
        val wantToReadBooks = listOf(
            Book(
                id = "1",
                title = "Want to Read Book",
                authors = listOf("Author 1"),
                firstPublishedYear = "2021",
                coverId = 12345L
            )
        )
        val wantToReadResult = BookListResult(wantToReadBooks, 1)

        val currentlyReadingBooks = listOf(
            Book(
                id = "2",
                title = "Currently Reading Book",
                authors = listOf("Author 2"),
                firstPublishedYear = "2022",
                coverId = 67890L
            )
        )
        val currentlyReadingResult = BookListResult(currentlyReadingBooks, 1)

        every { getWantToReadBooksUseCase(any(), 1) } returns Single.just(wantToReadResult)
        every { getCurrentlyReadingBooksUseCase(any(), 1) } returns Single.just(currentlyReadingResult)

        // Initial hasLoaded state should be false
        assertFalse(viewModel.state.value.hasLoaded)

        // Act - First load the default type (WANT_TO_READ)
        viewModel.loadBooks()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // Assert initial state
        assert(viewModel.state.value.bookListType == BookListType.WANT_TO_READ)
        assert(viewModel.state.value.books == wantToReadBooks)

        // hasLoaded should still be false after loadBooks
        assertFalse(viewModel.state.value.hasLoaded)

        // Act - Change the book list type
        viewModel.changeBookListType(BookListType.CURRENTLY_READING)
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // Assert updated state
        assert(viewModel.state.value.bookListType == BookListType.CURRENTLY_READING)
        assert(viewModel.state.value.books == currentlyReadingBooks)

        // hasLoaded should be set to true by changeBookListType
        assertTrue(viewModel.state.value.hasLoaded)

        // Verify both use cases were called
        verify { getWantToReadBooksUseCase(any(), 1) }
        verify { getCurrentlyReadingBooksUseCase(any(), 1) }
    }

    @Test
    fun `selectBook and clearSelectedBook should update state correctly`() {
        // Arrange
        val book = Book(
            id = "1",
            title = "Test Book",
            authors = listOf("Test Author"),
            firstPublishedYear = "2021",
            coverId = 12345L
        )

        // Act - Select book
        viewModel.selectBook(book)

        // Assert the selected book is as expected
        assert(viewModel.state.value.selectedBook == book)

        // Act - Clear selected book
        viewModel.clearSelectedBook()

        // Assert cleared selection
        assert(viewModel.state.value.selectedBook == null)
    }

    @Test
    fun `triggerInitialLoadIfNeeded should load books when hasLoaded is false`() {
        // Arrange
        val books = listOf(
            Book(
                id = "1",
                title = "Book 1",
                authors = listOf("Author 1"),
                firstPublishedYear = "2021",
                coverId = 12345L
            )
        )
        val bookListResult = BookListResult(books, 1)

        every { getWantToReadBooksUseCase(any(), 1) } returns Single.just(bookListResult)

        // Act - Trigger initial load
        viewModel.triggerInitialLoadIfNeeded()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // Assert
        assert(viewModel.state.value.books == books)
        assert(viewModel.state.value.hasLoaded)
        verify { getWantToReadBooksUseCase(any(), 1) }
    }

    @Test
    fun `triggerInitialLoadIfNeeded should not load books when hasLoaded is true`() {
        // Arrange
        val books = listOf(
            Book(
                id = "1",
                title = "Book 1",
                authors = listOf("Author 1"),
                firstPublishedYear = "2021",
                coverId = 12345L
            )
        )
        val bookListResult = BookListResult(books, 1)

        every { getWantToReadBooksUseCase(any(), 1) } returns Single.just(bookListResult)

        // First call to set hasLoaded to true
        viewModel.triggerInitialLoadIfNeeded()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // Reset the mock to verify it's not called again
        verify(exactly = 1) { getWantToReadBooksUseCase(any(), 1) }

        // Act - Call triggerInitialLoadIfNeeded again
        viewModel.triggerInitialLoadIfNeeded()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // Assert - Verify the use case was only called once (not again on second call)
        verify(exactly = 1) { getWantToReadBooksUseCase(any(), 1) }
    }
}
