package com.paulallan.mybooks.app.di

import com.paulallan.mybooks.data.api.ApiConstants
import com.paulallan.mybooks.data.api.ApiService
import com.paulallan.mybooks.data.repository.BookRepositoryImpl
import com.paulallan.mybooks.domain.repository.BookRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

// Qualifiers
const val IO_SCHEDULER = "io_scheduler"
const val MAIN_SCHEDULER = "main_scheduler"

val appModule = module {
    // Moshi
    single { 
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build() 
    }

    // Retrofit
    single { 
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build() 
    }

    // API Service
    single { get<Retrofit>().create(ApiService::class.java) }

    // Repository
    single<BookRepository> { BookRepositoryImpl(get()) }

    // Schedulers
    single(named(IO_SCHEDULER)) { Schedulers.io() as Scheduler }
    single(named(MAIN_SCHEDULER)) { AndroidSchedulers.mainThread() as Scheduler }
}
