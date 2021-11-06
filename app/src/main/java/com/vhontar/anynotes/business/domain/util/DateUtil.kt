package com.vhontar.anynotes.business.domain.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

object DateUtil {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)

    fun removeTimeFromDateString(sd: String): String {
        return sd.substring(0, sd.indexOf(""))
    }

    fun convertFirebaseTimestampToStringDate(timestamp: Timestamp): String {
        return dateFormat.format(timestamp.toDate())
    }

    fun convertStringDateToFirebaseTimestamp(sd: String): Timestamp {
        return Timestamp(dateFormat.parse(sd))
    }

    fun getCurrentTimestamp(): String {
        return dateFormat.format(Date())
    }
}