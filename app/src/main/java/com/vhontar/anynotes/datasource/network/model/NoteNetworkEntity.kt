package com.vhontar.anynotes.datasource.network.model

import com.google.firebase.Timestamp
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.util.DateUtil

data class NoteNetworkEntity(
    var id: String = "",
    var title: String = "",
    var body: String = "",
    var updatedAt: Timestamp = Timestamp.now(),
    var createdAt: Timestamp = Timestamp.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NoteNetworkEntity

        if (id != other.id) return false
        if (title != other.title) return false
        if (body != other.body) return false
//        if (updated_at != other.updated_at) return false // ignore
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }

    companion object {
        const val UPDATED_AT_FIELD = "updated_at"
        const val TITLE_FIELD = "title"
        const val BODY_FIELD = "body"
    }
}

fun NoteNetworkEntity.toDomainModel(): Note {
    return Note(
        id = id,
        title = title,
        body = body,
        updatedAt = DateUtil.convertFirebaseTimestampToStringDate(updatedAt),
        createdAt = DateUtil.convertFirebaseTimestampToStringDate(createdAt)
    )
}

fun List<NoteNetworkEntity>.toDomainModels(): List<Note> = map { it.toDomainModel() }
