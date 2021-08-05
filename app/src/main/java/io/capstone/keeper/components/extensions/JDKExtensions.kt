package io.capstone.keeper.components.extensions

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

fun Calendar.toTimestamp(): Timestamp {
    return Timestamp(this.time)
}

fun ZonedDateTime.isToday(): Boolean {
    return LocalDate.now().isEqual(this.toLocalDate())
}

fun LocalDateTime.isToday(): Boolean {
    return LocalDate.now().isEqual(this.toLocalDate())
}