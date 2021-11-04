package com.vhontar.anynotes.business.domain.state

interface StateEvent {
    fun errorInfo(): String
    fun eventName(): String
    fun shouldDisplayProgressBar(): Boolean
}