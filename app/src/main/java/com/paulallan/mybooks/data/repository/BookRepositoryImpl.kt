package com.paulallan.mybooks.data.repository

import com.paulallan.mybooks.data.api.ApiService
import com.paulallan.mybooks.data.mappers.toBookListResult
import com.paulallan.mybooks.domain.model.BookListResult
import com.paulallan.mybooks.domain.repository.BookRepository
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : BookRepository {

    override fun getWantToReadBooks(
        limit: Int,
        page: Int
    ): Single<BookListResult> {
        return apiService.getWantToReadBooks(limit, page)
            .map { response -> response.toBookListResult() }
    }

    override fun getCurrentlyReadingBooks(
        limit: Int,
        page: Int
    ): Single<BookListResult> {
        return apiService.getCurrentlyReadingBooks(limit, page)
            .map { response -> response.toBookListResult() }
    }

    override fun getAlreadyReadBooks(
        limit: Int,
        page: Int
    ): Single<BookListResult> {
        return apiService.getAlreadyReadBooks(limit, page)
            .map { response -> response.toBookListResult() }
    }
}
