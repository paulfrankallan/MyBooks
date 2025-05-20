package com.paulallan.mybooks.data.api

import com.paulallan.mybooks.data.model.BookListResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET(ApiConstants.WANT_TO_READ_ENDPOINT)
    fun getWantToReadBooks(
        @Query("limit") limit: Int = ApiConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = 1
    ): Single<BookListResponse>

    @GET(ApiConstants.CURRENTLY_READING_ENDPOINT)
    fun getCurrentlyReadingBooks(
        @Query("limit") limit: Int = ApiConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = 1
    ): Single<BookListResponse>

    @GET(ApiConstants.ALREADY_READ_ENDPOINT)
    fun getAlreadyReadBooks(
        @Query("limit") limit: Int = ApiConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = 1
    ): Single<BookListResponse>
}
