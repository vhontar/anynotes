package com.vhontar.anynotes.framework.presentation

import com.vhontar.anynotes.di.DaggerTestAppComponent
import kotlinx.coroutines.FlowPreview

@FlowPreview
class TestBaseApplication: BaseApplication() {
    override fun initAppComponent() {
        appComponent = DaggerTestAppComponent.factory().create(this)
    }
}