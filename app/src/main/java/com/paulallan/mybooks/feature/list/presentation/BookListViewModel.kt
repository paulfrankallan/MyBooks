package com.paulallan.mybooks.feature.list.presentation

import androidx.lifecycle.ViewModel
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
) : ViewModel() {
    private val _state = MutableStateFlow(BookListState())
    val state: StateFlow<BookListState> = _state.asStateFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        val disposable = getWantToReadBooksUseCase().subscribeOn(Schedulers.io())
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
}