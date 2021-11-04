package com.vhontar.anynotes.business.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vhontar.anynotes.business.domain.model.Note

class NoteDataFactory(
    private val testClassLoader: ClassLoader
) {

    fun produceNotesAsList(): List<Note> {
        return Gson().fromJson(
            getRawFromFile("note_list.json"),
            object : TypeToken<List<Note>>() {}.type
        )
    }

    fun produceNotesAsHashMap(): HashMap<String, Note> {
        val map = HashMap<String, Note>()
        val notes = produceNotesAsList()
        notes.forEach {
            map[it.id] = it
        }

        return map
    }

    fun produceEmptyNotesList() = arrayListOf<Note>()

    private fun getRawFromFile(filename: String): String {
        return testClassLoader.getResource(filename).readText()
    }
}