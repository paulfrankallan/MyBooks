package com.paulallan.mybooks.feature.list.presentation

import androidx.lifecycle.ViewModel
import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.domain.model.BookListType
import com.paulallan.mybooks.domain.usecase.GetAlreadyReadBooksUseCase
import com.paulallan.mybooks.domain.usecase.GetCurrentlyReadingBooksUseCase
import com.paulallan.mybooks.domain.usecase.GetWantToReadBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
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
) : ViewModel() {
    private val _state = MutableStateFlow(BookListState())
    val state: StateFlow<BookListState> = _state.asStateFlow()

    init {
        loadBooksForCurrentType()
    }

    private fun loadBooksForCurrentType() {
        val disposable = when (_state.value.bookListType) {
            BookListType.WANT_TO_READ -> getWantToReadBooksUseCase()
            BookListType.CURRENTLY_READING -> getCurrentlyReadingBooksUseCase()
            BookListType.ALREADY_READ -> getAlreadyReadBooksUseCase()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    _state.update { currentState ->
                        currentState.copy(
                            books = result.books,
                        )
                    }
                },
                { error: Throwable ->
                    // Handle error
                }
            )
    }

    fun selectBook(book: Book) {
        _state.update { it.copy(selectedBook = book) }
    }

    fun clearSelectedBook() {
        _state.update { it.copy(selectedBook = null) }
    }

    fun changeBookListType(type: BookListType) {
        if (_state.value.bookListType == type) {
            return
        }

        _state.update {
            it.copy(
                bookListType = type,
                books = emptyList(),
            )
        }

        loadBooksForCurrentType()
    }
}