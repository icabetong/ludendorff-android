package io.capstone.ludendorff.components.extensions

import android.content.Context
import com.google.firebase.Timestamp
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.utils.DateTimeFormatter
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.ofInstant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

fun Timestamp.toJSONObject(): JSONObject {
    return JSONObject().also {
        it.put("_nanoseconds", nanoseconds)
        it.put("_seconds", seconds)
    }
}

fun Timestamp.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.ofInstant(this.toDate().toInstant(), ZoneId.systemDefault())
}

fun Timestamp.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(this.toDate().toInstant(), ZoneId.systemDefault())
}

fun Timestamp.toLocalDate(): LocalDate {
    val calendar = Calendar.getInstance()
    calendar.time = this.toDate()
    return LocalDate.of(calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
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