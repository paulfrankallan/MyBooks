package com.paulallan.mybooks.domain.model

data class BookListResult(
    val books: List<Book>,
    val totalCount: Int
)