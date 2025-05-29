package com.paulallan.mybooks.feature.list.di

import com.paulallan.mybooks.app.di.IO_SCHEDULER
import com.paulallan.mybooks.app.di.MAIN_SCHEDULER
import com.paulallan.mybooks.domain.usecase.GetAlreadyReadBooksUseCase
import com.paulallan.mybooks.domain.usecase.GetCurrentlyReadingBooksUseCase
import com.paulallan.mybooks.domain.usecase.GetWantToReadBooksUseCase
import com.paulallan.mybooks.feature.list.presentation.BookListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val listModule = module {
    // Use cases
    factory { GetWantToReadBooksUseCase(get()) }
    factory { GetCurrentlyReadingBooksUseCase(get()) }
    factory { GetAlreadyReadBooksUseCase(get()) }
    
    // ViewModel
    viewModel { 
        BookListViewModel(
            getWantToReadBooksUseCase = get(),
            getCurrentlyReadingBooksUseCase = get(),
            getAlreadyReadBooksUseCase = get(),
            subscribeScheduler = get(named(IO_SCHEDULER)),
            observeScheduler = get(named(MAIN_SCHEDULER))
        ) 
    }
}