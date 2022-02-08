package com.vhontar.anynotes

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.vhontar.anynotes.presentation.TestBaseApplication
import kotlinx.coroutines.FlowPreview

@FlowPreview
class MockTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, TestBaseApplication::class.java.name, context)
    }
}