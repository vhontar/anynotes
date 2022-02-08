package com.vhontar.anynotes

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.vhontar.anynotes.presentation.TestBaseApplication
import kotlinx.coroutines.FlowPreview

@FlowPreview
abstract class BaseTest {
    // dependencies
    val application: TestBaseApplication = ApplicationProvider.getApplicationContext<Context>() as TestBaseApplication

    abstract fun injectTest()
}