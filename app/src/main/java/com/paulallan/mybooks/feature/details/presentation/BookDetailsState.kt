package com.paulallan.mybooks.feature.details.presentation

import com.paulallan.mybooks.domain.model.Book

data class BookDetailsState(
    val book: Book? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)