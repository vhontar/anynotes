package com.vhontar.anynotes.presentation.splash

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.vhontar.anynotes.R
import com.vhontar.anynotes.presentation.common.BaseNoteFragment

class SplashFragment constructor(
    factory: ViewModelProvider.Factory
) : BaseNoteFragment(R.layout.fragment_splash) {
    // private val viewModel: SplashViewModel by viewModels { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    override fun inject() {
        TODO("prepare dagger")
    }

}




























