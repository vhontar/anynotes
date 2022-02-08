package com.vhontar.anynotes.datasource.cache.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vhontar.anynotes.business.domain.model.Note

@Entity(tableName = "notes")
data class NoteCacheEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "body") var body: String,
    @ColumnInfo(name = "updated_at") var updatedAt: String,
    @ColumnInfo(name = "created_at") var createdAt: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NoteCacheEntity

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
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }

    companion object {
        fun nullTitleError(): String {
            return "You must enter a title."
        }

        fun nullIdError(): String {
            return "NoteEntity object has a null id. This should not be possible. Check local database."
        }
    }
}

fun NoteCacheEntity.toDomainModel(): Note {
    return Note(
        id = id,
        title = title,
        body = body,
        updatedAt = updatedAt,
        createdAt = createdAt
    )
}

fun List<NoteCacheEntity>.toDomainModels(): List<Note> = map { it.toDomainModel() }