package com.paulallan.mybooks.feature.list.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
) : ViewModel(), DefaultLifecycleObserver {
    private val _state = MutableStateFlow(BookListState())
    val state: StateFlow<BookListState> = _state.asStateFlow()

    private val disposables = CompositeDisposable()

    fun loadMoreBooks() {
        val currentState = _state.value

        if (currentState.isLoading || currentState.isLoadingMore || currentState.hasNoMoreData) {
            return
        }

        _state.update { it.copy(isLoadingMore = true) }

        val nextPage = currentState.currentPage + 1
        loadBooksForCurrentType(nextPage, isLoadingMore = true)
    }

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
                totalCount = 0
            )
        }

        loadBooksForCurrentType(1)
    }

    fun selectBook(book: Book) {
        _state.update { it.copy(selectedBook = book) }
    }

    fun clearSelectedBook() {
        _state.update { it.copy(selectedBook = null) }
    }

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

    private fun onError(error: Throwable) {
        _state.update {
            it.copy(
                error = error.message,
                isLoading = false,
                isLoadingMore = false,
            )
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (_state.value.books.isEmpty()) {
            loadBooks()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        disposables.clear()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
