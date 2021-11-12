package com.vhontar.anynotes.framework.datasource.data

import android.app.Application
import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.model.NoteFactory
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDataFactory @Inject constructor(
    private val application: Application,
    private val noteFactory: NoteFactory
) {
    fun produceEmptyListOfNotes(): List<Note> = listOf()

    fun produceListOfNotes(): List<Note> {
        return Gson()
            .fromJson(
                readJsonFromAsset("note_list.json"),
                object : TypeToken<List<Note>>() {}.type
            )
    }

    fun createSingleNote(
        id: String? = null,
        title: String,
        body: String? = null
    ) = noteFactory.createSingleNote(id, title, body)

    fun createNoteLists(numNotes: Int) = noteFactory.createNoteList(numNotes)

    private fun readJsonFromAsset(filename: String): String? {
        var json: String? = null
        json = try {
            val inputStream: InputStream = (application.assets as AssetManager).open(filename)
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        return json
    }
}