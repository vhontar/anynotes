package com.vhontar.anynotes.business.domain.model

import android.os.Parcelable
import com.vhontar.anynotes.business.domain.util.DateUtil
import com.vhontar.anynotes.framework.datasource.cache.model.NoteCacheEntity
import com.vhontar.anynotes.framework.datasource.network.model.NoteNetworkEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    val id: String,
    val title: String,
    val body: String,
    val updatedAt: String,
    val createdAt: String
) : Parcelable

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
