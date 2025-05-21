package com.paulallan.mybooks.domain.usecase

import com.paulallan.mybooks.data.api.ApiConstants
import com.paulallan.mybooks.domain.model.BookListResult
import com.paulallan.mybooks.domain.repository.BookRepository
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

/**
 * Use case for retrieving books that the user is currently reading.
 *
 * This class follows the single responsibility principle and encapsulates
 * the business logic for fetching the user's in-progress book collection.
 */
class GetCurrentlyReadingBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    /**
     * Executes the use case to retrieve currently reading books.
     *
     * @param limit The maximum number of books to retrieve (default: API default page size)
     * @param page The page number for pagination (default: 1)
     * @return A Single emitting a BookListResult containing the list of currently reading books
     */
    operator fun invoke(
        limit: Int = ApiConstants.DEFAULT_PAGE_SIZE,
        page: Int = 1
    ): Single<BookListResult> {
        return bookRepository.getCurrentlyReadingBooks(limit, page)
    }
}
