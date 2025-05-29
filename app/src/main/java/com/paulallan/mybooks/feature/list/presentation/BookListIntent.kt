package com.paulallan.mybooks.feature.list.presentation

import com.paulallan.mybooks.domain.model.BookListType

sealed class BookListIntent {
    object LoadBooks : BookListIntent()
    object LoadMoreBooks : BookListIntent()
    data class ChangeBookListType(val type: BookListType) : BookListIntent()
}

sealed class BookListEffect {
    data class ShowError(val message: String) : BookListEffect()
}