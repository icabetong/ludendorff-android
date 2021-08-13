package io.capstone.ludendorff.components.extensions

import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun Timestamp.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.ofInstant(this.toDate().toInstant(), ZoneId.systemDefault())
}

fun Timestamp.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(this.toDate().toInstant(), ZoneId.systemDefault())
}

fun Timestamp.isToday(): Boolean {
    return this.toLocalDateTime().isToday()
}