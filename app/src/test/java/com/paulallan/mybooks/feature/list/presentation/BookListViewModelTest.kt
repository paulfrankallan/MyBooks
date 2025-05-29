@file:Suppress("ReactiveStreamsUnusedPublisher")

package com.paulallan.mybooks.feature.list.presentation

import androidx.lifecycle.LifecycleOwner
import com.paulallan.mybooks.data.api.ApiConstants
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class BookListViewModelTest {

    private lateinit var getWantToReadBooksUseCase: GetWantToReadBooksUseCase
    private lateinit var getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase
    private lateinit var getAlreadyReadBooksUseCase: GetAlreadyReadBooksUseCase
    private lateinit var testScheduler: TestScheduler
    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var viewModel: BookListViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // Set up the Main dispatcher for tests
        Dispatchers.setMain(testDispatcher)

        getWantToReadBooksUseCase = mockk()
        getCurrentlyReadingBooksUseCase = mockk()
        getAlreadyReadBooksUseCase = mockk()
        testScheduler = TestScheduler()
        lifecycleOwner = mockk(relaxed = true)

        // Mock the initial call to getWantToReadBooksUseCase that happens in the init block
        val emptyBooks = emptyList<Book>()
        val emptyResult = BookListResult(emptyBooks, 0)
        every { getWantToReadBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, 1) } returns Single.just(emptyResult)

        viewModel = BookListViewModel(
            getWantToReadBooksUseCase = getWantToReadBooksUseCase,
            getCurrentlyReadingBooksUseCase = getCurrentlyReadingBooksUseCase,
            getAlreadyReadBooksUseCase = getAlreadyReadBooksUseCase,
            subscribeScheduler = testScheduler,
            observeScheduler = testScheduler
        )

        // Clear the mock to avoid interference with test-specific mocks
        every { getWantToReadBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, 1) } returns Single.just(emptyResult)

        // Advance the test dispatcher to complete the init block
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        // Reset the Main dispatcher after tests
        Dispatchers.resetMain()
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

        // Set up the mocks
        every { getWantToReadBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, 1) } returns Single.just(bookListResult)

        // Act
        viewModel.processIntent(BookListIntent.LoadBooks)

        // Advance both schedulers to ensure all operations complete
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Additional delay to ensure state updates are processed
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        println("[DEBUG_LOG] books: ${viewModel.state.value.books}")
        println("[DEBUG_LOG] isLoading: ${viewModel.state.value.isLoading}")
        println("[DEBUG_LOG] error: ${viewModel.state.value.error}")
        println("[DEBUG_LOG] currentPage: ${viewModel.state.value.currentPage}")
        println("[DEBUG_LOG] hasMoreData: ${viewModel.state.value.hasMoreData}")
        println("[DEBUG_LOG] hasNoMoreData: ${viewModel.state.value.hasNoMoreData}")
        println("[DEBUG_LOG] totalCount: ${viewModel.state.value.totalCount}")

        assert(viewModel.state.value.books == books)
        assertFalse(viewModel.state.value.isLoading)
        assert(viewModel.state.value.error == null)
        assert(viewModel.state.value.currentPage == 1)
        assertFalse(viewModel.state.value.hasMoreData)
        assertTrue(viewModel.state.value.hasNoMoreData)
    }

    @Test
    fun `loadBooks should update state with error when failure`() {
        // Arrange
        val errorMessage = "Error loading books"
        every { getWantToReadBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, 1) } returns Single.error(RuntimeException(errorMessage))

        // Act
        viewModel.processIntent(BookListIntent.LoadBooks)

        // Advance both schedulers to ensure all operations complete
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Additional delay to ensure state updates are processed
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        println("[DEBUG_LOG] error test - books: ${viewModel.state.value.books}")
        println("[DEBUG_LOG] error test - isLoading: ${viewModel.state.value.isLoading}")
        println("[DEBUG_LOG] error test - error: ${viewModel.state.value.error}")

        assert(viewModel.state.value.books.isEmpty())
        assert(!viewModel.state.value.isLoading)
        assert(viewModel.state.value.error == errorMessage)
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

        every { getWantToReadBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, 1) } returns Single.just(firstPageResult)

        // Load first page
        viewModel.processIntent(BookListIntent.LoadBooks)

        // Advance both schedulers to ensure all operations complete
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Additional delay to ensure state updates are processed
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify first page loaded correctly
        println("[DEBUG_LOG] loadMoreBooks - after first load - books: ${viewModel.state.value.books}")
        println("[DEBUG_LOG] loadMoreBooks - after first load - currentPage: ${viewModel.state.value.currentPage}")
        println("[DEBUG_LOG] loadMoreBooks - after first load - hasMoreData: ${viewModel.state.value.hasMoreData}")

        assert(viewModel.state.value.books == firstPageBooks)
        assert(viewModel.state.value.currentPage == 1)
        assert(viewModel.state.value.hasMoreData)

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

        every { getWantToReadBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, 2) } returns Single.just(secondPageResult)

        // Act
        viewModel.processIntent(BookListIntent.LoadMoreBooks)

        // Advance both schedulers to ensure all operations complete
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Additional delay to ensure state updates are processed
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        println("[DEBUG_LOG] loadMoreBooks - after second load - books: ${viewModel.state.value.books}")
        println("[DEBUG_LOG] loadMoreBooks - after second load - currentPage: ${viewModel.state.value.currentPage}")
        println("[DEBUG_LOG] loadMoreBooks - after second load - hasMoreData: ${viewModel.state.value.hasMoreData}")

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

        every { getWantToReadBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, 1) } returns Single.just(bookListResult)

        viewModel.processIntent(BookListIntent.LoadBooks)

        // Act
        viewModel.processIntent(BookListIntent.LoadMoreBooks)

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

        every { getWantToReadBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, 1) } returns Single.just(wantToReadResult)
        every { getCurrentlyReadingBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, 1) } returns Single.just(currentlyReadingResult)

        // Act - First load the default type (WANT_TO_READ)
        viewModel.processIntent(BookListIntent.LoadBooks)

        // Advance both schedulers to ensure all operations complete
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Additional delay to ensure state updates are processed
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert initial state
        println("[DEBUG_LOG] changeBookListType - initial state - bookListType: ${viewModel.state.value.bookListType}")
        println("[DEBUG_LOG] changeBookListType - initial state - books: ${viewModel.state.value.books}")

        assert(viewModel.state.value.bookListType == BookListType.WANT_TO_READ)
        assert(viewModel.state.value.books == wantToReadBooks)

        // Act - Change the book list type
        viewModel.processIntent(BookListIntent.ChangeBookListType(BookListType.CURRENTLY_READING))

        // Advance both schedulers to ensure all operations complete
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Additional delay to ensure state updates are processed
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert updated state
        println("[DEBUG_LOG] changeBookListType - updated state - bookListType: ${viewModel.state.value.bookListType}")
        println("[DEBUG_LOG] changeBookListType - updated state - books: ${viewModel.state.value.books}")

        assert(viewModel.state.value.bookListType == BookListType.CURRENTLY_READING)
        assert(viewModel.state.value.books == currentlyReadingBooks)

        // Verify both use cases were called
        verify { getWantToReadBooksUseCase(any(), 1) }
        verify { getCurrentlyReadingBooksUseCase(any(), 1) }
    }

    @Test
    fun `onStart should load books when books list is empty`() {
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

        // Act - Trigger onStart
        viewModel.onStart(lifecycleOwner)
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // Assert
        assert(viewModel.state.value.books == books)
        verify { getWantToReadBooksUseCase(any(), 1) }
    }

    @Test
    fun `onStop should clear disposables`() {
        // Arrange - Set up a long-running operation
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

        // Use a Single that won't complete immediately
        every { getWantToReadBooksUseCase(any(), 1) } returns Single.just(bookListResult).delay(10, TimeUnit.SECONDS, testScheduler)

        // Start loading
        viewModel.processIntent(BookListIntent.LoadBooks)

        // Act - Call onStop before the operation completes
        viewModel.onStop(lifecycleOwner)

        // Advance time to when the operation would have completed
        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        // Assert - State should still show loading since the operation was disposed
        assert(viewModel.state.value.isLoading)
    }
}
