package com.example

import android.app.Application
import com.example.data.di.AppContainer
import com.example.data.di.DefaultAppContainer

class OsArchitectApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
