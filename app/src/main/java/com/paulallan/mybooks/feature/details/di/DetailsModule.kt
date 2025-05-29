package com.paulallan.mybooks.feature.details.di

import com.paulallan.mybooks.app.di.IO_SCHEDULER
import com.paulallan.mybooks.app.di.MAIN_SCHEDULER
import com.paulallan.mybooks.feature.details.presentation.BookDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val detailsModule = module {
    // ViewModel
    viewModel { (bookId: String) -> 
        BookDetailsViewModel(
            bookId = bookId,
            bookRepository = get(),
            subscribeScheduler = get(named(IO_SCHEDULER)),
            observeScheduler = get(named(MAIN_SCHEDULER))
        )
    }
}