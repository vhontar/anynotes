package com.vhontar.anynotes.presentation

import android.app.Application
import com.vhontar.anynotes.di.AppComponent
import com.vhontar.anynotes.di.DaggerAppComponent
import kotlinx.coroutines.FlowPreview

@FlowPreview
open class BaseApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initAppComponent()
    }

    open fun initAppComponent() {
        appComponent = DaggerAppComponent.factory().create(this)
    }
}