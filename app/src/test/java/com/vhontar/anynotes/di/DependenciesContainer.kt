package com.vhontar.anynotes.di

import com.vhontar.anynotes.business.data.NoteDataFactory
import com.vhontar.anynotes.business.data.cache.FakeNoteCacheDataSourceImpl
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.FakeNoteNetworkDataSourceImpl
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.business.domain.util.DateUtil
import com.vhontar.anynotes.util.isUnitTest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class DependenciesContainer {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
    val dateUtil = DateUtil(dateFormat)
    lateinit var noteNetworkDataSource: NoteNetworkDataSource
    lateinit var noteCacheDataSource: NoteCacheDataSource
    lateinit var noteFactory: NoteFactory

    private var noteDataFactory: NoteDataFactory? = null
    private var notesData = hashMapOf<String, Note>()

    init {
        isUnitTest = true // for Logger.kt
    }

    fun build() {
        this.javaClass.classLoader?.let {
            noteDataFactory = NoteDataFactory(it)

            notesData = noteDataFactory?.produceNotesAsHashMap() ?: hashMapOf()
        }
        noteFactory = NoteFactory(dateUtil)
        noteNetworkDataSource = FakeNoteNetworkDataSourceImpl(
            notesData = notesData,
            deletedNotesData = HashMap()
        )
        noteCacheDataSource = FakeNoteCacheDataSourceImpl(
            notesData = notesData,
            dateUtil = dateUtil
        )
    }

}
