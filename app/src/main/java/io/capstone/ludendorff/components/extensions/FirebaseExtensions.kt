package io.capstone.ludendorff.components.extensions

import android.content.Context
import com.google.firebase.Timestamp
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.utils.DateTimeFormatter
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

fun Timestamp?.format(context: Context, isShort: Boolean = false): String? {
    if (this == null)
        return null

    return if (this.isToday())
        String.format(context.getString(R.string.concat_today_at),
            DateTimeFormatter.getTimeFormatter(context)
                .format(this.toLocalDateTime()))
    else DateTimeFormatter.getDateTimeFormatter(context, isShort)
        .format(this.toLocalDateTime())
}