package com.paulallan.mybooks.feature.list.presentation

import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.domain.model.BookListType

data class BookListState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val bookListType: BookListType = BookListType.WANT_TO_READ,
    val currentPage: Int = 1,
    val hasMoreData: Boolean = true,
    val totalCount: Int = 0
) {
    val hasNoMoreData: Boolean
        get() = !hasMoreData

    val canLoadMore: Boolean
        get() = !isLoading && !isLoadingMore && hasMoreData
}
