package com.vhontar.anynotes.business.domain.model

import com.vhontar.anynotes.business.domain.util.DateUtil
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteFactory @Inject constructor(
    private val dateUtil: DateUtil
) {
    fun createSingleNote(
        id: String? = null,
        title: String,
        body: String? = null
    ): Note {
        return Note(
            id = id ?: UUID.randomUUID().toString(),
            title = title,
            body = body ?: "",
            updatedAt = dateUtil.getCurrentTimestamp(),
            createdAt = dateUtil.getCurrentTimestamp()
        )
    }

    /**
     * Used for jUnit testing
     * */
    fun createNoteList(numNotes: Int): List<Note> {
        val list = arrayListOf<Note>()
        for (i in 0 until numNotes) {
            list.add(
                createSingleNote(null, title = UUID.randomUUID().toString())
            )
        }

        return list
    }
}