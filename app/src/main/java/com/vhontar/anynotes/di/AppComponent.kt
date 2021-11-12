package com.vhontar.anynotes.di

import com.vhontar.anynotes.framework.presentation.BaseApplication
import com.vhontar.anynotes.framework.presentation.MainActivity
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@Singleton
@FlowPreview
@Component(
    modules = [
        AppModule::class,
        ProductionModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: BaseApplication): AppComponent
    }

    fun inject(mainActivity: MainActivity)
}