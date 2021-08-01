package io.capstone.keeper.components.extensions

import com.google.firebase.Timestamp
import java.util.*

fun Calendar.toTimestamp(): Timestamp {
    return Timestamp(this.time)
}