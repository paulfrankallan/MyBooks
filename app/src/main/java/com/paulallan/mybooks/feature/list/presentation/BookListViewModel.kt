package com.paulallan.mybooks.feature.list.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.paulallan.mybooks.core.presentation.BaseViewModel
import com.paulallan.mybooks.data.api.ApiConstants
import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.domain.model.BookListType
import com.paulallan.mybooks.domain.usecase.GetAlreadyReadBooksUseCase
import com.paulallan.mybooks.domain.usecase.GetCurrentlyReadingBooksUseCase
import com.paulallan.mybooks.domain.usecase.GetWantToReadBooksUseCase
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable

class BookListViewModel(
    private val getWantToReadBooksUseCase: GetWantToReadBooksUseCase,
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
    private val getAlreadyReadBooksUseCase: GetAlreadyReadBooksUseCase,
    private val subscribeScheduler: Scheduler,
    private val observeScheduler: Scheduler
) : BaseViewModel<BookListState, BookListIntent, BookListEffect>(BookListState()), DefaultLifecycleObserver {

    private val disposables = CompositeDisposable()

    init {
        processIntent(BookListIntent.LoadBooks)
    }

    override suspend fun reduce(intent: BookListIntent, state: BookListState) {
        when (intent) {
            is BookListIntent.LoadBooks -> loadBooks()
            is BookListIntent.LoadMoreBooks -> loadMoreBooks()
            is BookListIntent.ChangeBookListType -> changeBookListType(intent.type)
        }
        // Wait for the RxJava operations to complete in tests
        kotlinx.coroutines.delay(100)
    }

    private fun loadMoreBooks() {
        val currentState = state.value

        if (currentState.isLoading || currentState.isLoadingMore || currentState.hasNoMoreData) {
            return
        }

        updateState { it.copy(isLoadingMore = true) }

        val nextPage = currentState.currentPage + 1
        loadBooksForCurrentType(nextPage, isLoadingMore = true)
    }

    private fun changeBookListType(type: BookListType) {
        val currentState = state.value
        if (currentState.bookListType == type) {
            return
        }

        disposables.clear()

        updateState {
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

    private fun loadBooks() {
        disposables.clear()

        updateState {
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

    private fun loadBooksForCurrentType(page: Int = 1, isLoadingMore: Boolean = false) {
        val currentState = state.value
        val disposable = when (currentState.bookListType) {
            BookListType.WANT_TO_READ -> getWantToReadBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, page)
            BookListType.CURRENTLY_READING -> getCurrentlyReadingBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, page)
            BookListType.ALREADY_READ -> getAlreadyReadBooksUseCase(ApiConstants.DEFAULT_PAGE_SIZE, page)
        }
            .subscribeOn(subscribeScheduler)
            .observeOn(observeScheduler)
            .subscribe(
                { result -> onBooksLoaded(result.books, result.totalCount, page, isLoadingMore) },
                { error -> onError(error) }
            )

        disposables.add(disposable)
    }

    private fun onBooksLoaded(books: List<Book>, totalCount: Int, page: Int, isLoadingMore: Boolean) {
        println("[DEBUG_LOG] onBooksLoaded - books: $books")
        println("[DEBUG_LOG] onBooksLoaded - totalCount: $totalCount")
        println("[DEBUG_LOG] onBooksLoaded - page: $page")
        println("[DEBUG_LOG] onBooksLoaded - isLoadingMore: $isLoadingMore")

        updateState { currentState ->
            val updatedBooks = if (isLoadingMore) {
                currentState.books + books
            } else {
                books
            }

            // Calculate if there's more data based on the current page and total count.
            val hasMoreData = page * ApiConstants.DEFAULT_PAGE_SIZE < totalCount

            println("[DEBUG_LOG] onBooksLoaded - updatedBooks: $updatedBooks")
            println("[DEBUG_LOG] onBooksLoaded - hasMoreData: $hasMoreData")

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
        updateState {
            it.copy(
                error = error.message,
                isLoading = false,
                isLoadingMore = false,
            )
        }
        emitEffect(BookListEffect.ShowError(error.message ?: "Unknown error"))
    }

    override fun onStart(owner: LifecycleOwner) {
        if (state.value.books.isEmpty()) {
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
