package com.vhontar.anynotes.di

import com.vhontar.anynotes.presentation.BaseApplication
import com.vhontar.anynotes.presentation.MainActivity
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@Singleton
@FlowPreview
@Component(
    modules = [
        AppModule::class,
        ProductionModule::class,
        NoteViewModelModule::class,
        NoteFragmentFactoryModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: BaseApplication): AppComponent
    }

    fun inject(mainActivity: MainActivity)
}