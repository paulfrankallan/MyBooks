package com.paulallan.mybooks.feature.details.presentation

import androidx.lifecycle.viewModelScope
import com.paulallan.mybooks.core.presentation.BaseViewModel
import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.domain.repository.BookRepository
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.util.NoSuchElementException

class BookDetailsViewModel(
    private val bookId: String,
    private val bookRepository: BookRepository,
    private val subscribeScheduler: Scheduler,
    private val observeScheduler: Scheduler
) : BaseViewModel<BookDetailsState, BookDetailsIntent, BookDetailsEffect>(BookDetailsState()) {

    private val disposables = CompositeDisposable()

    init {
        loadBookDetails()
    }

    override suspend fun reduce(intent: BookDetailsIntent, state: BookDetailsState) {
        when (intent) {
            is BookDetailsIntent.LoadBookDetails -> loadBookDetails()
            is BookDetailsIntent.RetryLoadingBookDetails -> loadBookDetails()
        }
    }

    private fun loadBookDetails() {
        updateState { it.copy(isLoading = true, error = null) }

        // Since we don't have a direct getBookById method, we'll get all books and filter
        val disposable = bookRepository.getWantToReadBooks(100, 1) // Get a large batch to increase chances of finding the book
            .map { result -> 
                result.books.firstOrNull { it.id == bookId } 
                    ?: throw NoSuchElementException("Book not found with ID: $bookId")
            }
            .subscribeOn(subscribeScheduler)
            .observeOn(observeScheduler)
            .subscribe(
                { book -> onBookLoaded(book) },
                { error -> onError(error) }
            )

        disposables.add(disposable)
    }

    private fun onBookLoaded(book: Book) {
        updateState { it.copy(book = book, isLoading = false, error = null) }
    }

    private fun onError(error: Throwable) {
        updateState { it.copy(isLoading = false, error = error.message) }
        emitEffect(BookDetailsEffect.ShowError(error.message ?: "Unknown error"))
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}

sealed class BookDetailsIntent {
    object LoadBookDetails : BookDetailsIntent()
    object RetryLoadingBookDetails : BookDetailsIntent()
}

sealed class BookDetailsEffect {
    data class ShowError(val message: String) : BookDetailsEffect()
}
