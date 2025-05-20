package com.paulallan.mybooks.feature.list.presentation

import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.domain.model.BookListType

data class BookListState(
    val books: List<Book> = emptyList(),
    val bookListType: BookListType = BookListType.WANT_TO_READ,
)