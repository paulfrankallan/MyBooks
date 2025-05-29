package com.paulallan.mybooks.app

import android.app.Application
import com.paulallan.mybooks.app.di.appModule
import com.paulallan.mybooks.app.di.imageLoaderModule
import com.paulallan.mybooks.feature.details.di.detailsModule
import com.paulallan.mybooks.feature.list.di.listModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@App)
            modules(
                appModule,
                imageLoaderModule,
                listModule,
                detailsModule
            )
        }
    }
}
