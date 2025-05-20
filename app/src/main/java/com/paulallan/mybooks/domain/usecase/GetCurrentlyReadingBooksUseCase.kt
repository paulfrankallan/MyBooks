package com.paulallan.mybooks.domain.usecase

import com.paulallan.mybooks.data.api.ApiConstants
import com.paulallan.mybooks.domain.model.BookListResult
import com.paulallan.mybooks.domain.repository.BookRepository
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class GetCurrentlyReadingBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(
        limit: Int = ApiConstants.DEFAULT_PAGE_SIZE,
        page: Int = 1
    ): Single<BookListResult> {
        return bookRepository.getCurrentlyReadingBooks(limit, page)
    }
}