package com.vhontar.anynotes.business.domain.model

import android.os.Parcelable
import com.vhontar.anynotes.business.domain.util.DateUtil
import com.vhontar.anynotes.datasource.cache.model.NoteCacheEntity
import com.vhontar.anynotes.datasource.network.model.NoteNetworkEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    val id: String,
    val title: String,
    val body: String,
    val updatedAt: String,
    val createdAt: String
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Note

        if (id != other.id) return false
        if (title != other.title) return false
        if (body != other.body) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}

fun Note.toDatabaseEntity(): NoteCacheEntity {
    return NoteCacheEntity(
        id = id,
        title = title,
        body = body,
        updatedAt = updatedAt,
        createdAt = createdAt
    )
}

fun List<Note>.toDatabaseEntities(): List<NoteCacheEntity> = map { it.toDatabaseEntity() }

fun Note.toNetworkEntity(): NoteNetworkEntity {
    return NoteNetworkEntity(
        id = id,
        title = title,
        body = body,
        updatedAt = DateUtil.convertStringDateToFirebaseTimestamp(updatedAt),
        createdAt = DateUtil.convertStringDateToFirebaseTimestamp(createdAt)
    )
}

fun List<Note>.toNetworkEntities(): List<NoteNetworkEntity> = map { it.toNetworkEntity() }
