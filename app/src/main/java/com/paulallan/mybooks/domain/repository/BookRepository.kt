package com.paulallan.mybooks.domain.repository

import com.paulallan.mybooks.data.api.ApiConstants
import com.paulallan.mybooks.domain.model.BookListResult
import io.reactivex.rxjava3.core.Single

interface BookRepository {
    fun getWantToReadBooks(
        limit: Int = ApiConstants.DEFAULT_PAGE_SIZE,
        page: Int = 1
    ): Single<BookListResult>

    fun getCurrentlyReadingBooks(
        limit: Int = ApiConstants.DEFAULT_PAGE_SIZE,
        page: Int = 1
    ): Single<BookListResult>

    fun getAlreadyReadBooks(
        limit: Int = ApiConstants.DEFAULT_PAGE_SIZE,
        page: Int = 1
    ): Single<BookListResult>
}
