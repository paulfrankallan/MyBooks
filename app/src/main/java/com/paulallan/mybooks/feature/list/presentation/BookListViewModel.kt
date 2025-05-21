package com.paulallan.mybooks.feature.list.presentation

import androidx.lifecycle.ViewModel
import com.paulallan.mybooks.app.di.IoScheduler
import com.paulallan.mybooks.app.di.MainScheduler
import com.paulallan.mybooks.data.api.ApiConstants
import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.domain.model.BookListType
import com.paulallan.mybooks.domain.usecase.GetAlreadyReadBooksUseCase
import com.paulallan.mybooks.domain.usecase.GetCurrentlyReadingBooksUseCase
import com.paulallan.mybooks.domain.usecase.GetWantToReadBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val getWantToReadBooksUseCase: GetWantToReadBooksUseCase,
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
    private val getAlreadyReadBooksUseCase: GetAlreadyReadBooksUseCase,
    @IoScheduler private val subscribeScheduler: Scheduler,
    @MainScheduler private val observeScheduler: Scheduler
) : ViewModel() {

    private val _state = MutableStateFlow(BookListState())
    val state: StateFlow<BookListState> = _state.asStateFlow()

    private val disposables = CompositeDisposable()

    /**
     * Triggers the initial loading of books if it has not already occurred.
     *
     * This function ensures that books are only loaded once during the initial ViewModel lifecycle,
     * preventing duplicate loads on configuration changes. It sets the hasLoaded flag and initiates loading.
     */
    fun triggerInitialLoadIfNeeded() {
        // This is to address an issue with recalling the loadBooks function when there is a configuration change.
        // This could also be fixed by triggering loadBooks in ViewModel by using the onStart and stateIn flow operators.
        // However, each have their pros & cons, this is a simpler solution for now.
        if (!_state.value.hasLoaded) {
            _state.update { it.copy(hasLoaded = true) }
            loadBooks()
        }
    }

    /**
     * Loads the next page of books for pagination.
     *
     * This function checks if a load operation is already in progress or if there is no more data to load.
     * If not, it updates the state to indicate loading more data and fetches the next page of books.
     */
    fun loadMoreBooks() {
        val currentState = _state.value

        if (currentState.isLoading || currentState.isLoadingMore || currentState.hasNoMoreData) {
            return
        }

        _state.update { it.copy(isLoadingMore = true) }

        val nextPage = currentState.currentPage + 1
        loadBooksForCurrentType(nextPage, isLoadingMore = true)
    }

    /**
     * Switches between different book list types (Want to Read, Currently Reading, Already Read).
     * Resets the list state and loads books for the selected type.
     *
     * @param type The book list type to switch to
     */
    fun changeBookListType(type: BookListType) {
        if (_state.value.bookListType == type) {
            return
        }

        disposables.clear()

        _state.update {
            it.copy(
                bookListType = type,
                books = emptyList(),
                isLoading = true,
                error = null,
                currentPage = 1,
                hasMoreData = true,
                totalCount = 0,
                hasLoaded = true
            )
        }

        loadBooksForCurrentType(1)
    }

    /**
     * Sets the currently selected book in the state.
     *
     * @param book The book to be selected
     */
    fun selectBook(book: Book) {
        _state.update { it.copy(selectedBook = book) }
    }

    /**
     * Clears the currently selected book from the state.
     */
    fun clearSelectedBook() {
        _state.update { it.copy(selectedBook = null) }
    }

    /**
     * Resets the book list state and loads the first page of books for the current list type.
     * Used for initial loading and initial loading after switching BookListType.
     */
    fun loadBooks() {
        disposables.clear()

        _state.update {
            it.copy(
                isLoading = true,
                error = null,
                books = emptyList(),
                currentPage = 1,
                hasMoreData = true
            )
        }

        loadBooksForCurrentType(1)
    }

    /**
     * Fetches books for the currently selected list type from the appropriate use case.
     * Handles subscription management and processes the results.
     *
     * @param page The page number to load (defaults to 1)
     * @param isLoadingMore Whether this is a pagination request (defaults to false)
     */
    fun loadBooksForCurrentType(page: Int = 1, isLoadingMore: Boolean = false) {
        val disposable = when (_state.value.bookListType) {
            BookListType.WANT_TO_READ -> getWantToReadBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, page)
            BookListType.CURRENTLY_READING -> getCurrentlyReadingBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, page)
            BookListType.ALREADY_READ -> getAlreadyReadBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, page)
        }
            .subscribeOn(subscribeScheduler)
            .observeOn(observeScheduler)
            .subscribe(
                { result -> onBooksLoaded(result.books, result.totalCount, page, isLoadingMore) },
                { error: Throwable -> onError(error) }
            )

        disposables.add(disposable)
    }

    /**
     * Processes the loaded books data and updates the state accordingly.
     * Handles both initial loading and pagination by appending new books to existing ones when needed.
     *
     * @param books The list of books loaded from the repository
     * @param totalCount The total number of books available in the repository
     * @param page The current page that was loaded
     * @param isLoadingMore Whether this was a pagination request
     */
    private fun onBooksLoaded(books: List<Book>, totalCount: Int, page: Int, isLoadingMore: Boolean) {
        _state.update { currentState ->
            val updatedBooks = if (isLoadingMore) {
                currentState.books + books
            } else {
                books
            }

            // Calculate if there's more data based on the current page and total count.
            // This is more accurate than using the size of the updated books list.
            val hasMoreData = page * ApiConstants.DEFAULT_PAGE_SIZE < totalCount

            currentState.copy(
                books = updatedBooks,
                isLoading = false,
                isLoadingMore = false,
                currentPage = page,
                hasMoreData = hasMoreData,
                totalCount = totalCount
            )
        }
    }

    /**
     * Handles errors that occur during book loading operations.
     * Updates the state with the error message and resets loading indicators.
     *
     * @param error The error that occurred during the operation
     */
    private fun onError(error: Throwable) {
        _state.update {
            it.copy(
                error = error.message,
                isLoading = false,
                isLoadingMore = false
            )
        }
    }

    /**
     * Called when the ViewModel is being destroyed.
     * Ensures all resources are properly released.
     */
    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
