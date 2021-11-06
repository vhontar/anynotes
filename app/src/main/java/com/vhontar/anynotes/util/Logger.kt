package com.vhontar.anynotes.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.vhontar.anynotes.util.Constants.DEBUG
import com.vhontar.anynotes.util.Constants.TAG

var isUnitTest = false

fun printLogD(className: String?, message: String ) {
    if (DEBUG && !isUnitTest) {
        Log.d(TAG, "$className: $message")
    }
    else if(DEBUG && isUnitTest){
        println("$className: $message")
    }
}

fun sendCrashlytics(message: String?) {
    message?.let {
        FirebaseCrashlytics.getInstance().log(it)
    }
}

