package com.vhontar.anynotes.di

import com.vhontar.anynotes.datasource.cache.NoteDaoServiceTests
import com.vhontar.anynotes.datasource.network.NoteFirestoreServiceTests
import com.vhontar.anynotes.presentation.TestBaseApplication
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@Singleton
@Component(
    modules = [
        AppModule::class,
        TestModule::class
    ]
)
interface TestAppComponent : AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: TestBaseApplication): TestAppComponent
    }

    fun inject(noteFirestoreServiceTests: NoteFirestoreServiceTests)
    fun inject(noteDaoServiceTests: NoteDaoServiceTests)
}