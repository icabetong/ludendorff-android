package io.capstone.ludendorff.components.utils

import android.content.Context
import android.text.format.DateFormat
import java.time.format.DateTimeFormatter as Formatter

class DateTimeFormatter private constructor() {

    companion object {
        private const val FORMAT_TIME_12_HOUR = "h:mm a"
        private const val FORMAT_TIME_24_HOUR = "H:mm"
        private const val FORMAT_DATE_TIME_12_HOUR_SHORT = "M-d, h:mm a"
        private const val FORMAT_DATE_TIME_24_HOUR_SHORT = "M-d, H:mm"
        private const val FORMAT_DATE_TIME_WITH_YEAR_12_HOUR_SHORT = "M-d-yy, h:mm a"
        private const val FORMAT_DATE_TIME_WITH_YEAR_24_HOUR_SHORT = "M-d-yy, H:mm"
        private const val FORMAT_DATE_TIME_12_HOUR = "MMMM d, h:mm a"
        private const val FORMAT_DATE_TIME_24_HOUR = "MMMM d, H:mm"
        private const val FORMAT_DATE_TIME_WITH_YEAR_12_HOUR = "MM d yyyy, h:mm a"
        private const val FORMAT_DATE_TIME_WITH_YEAR_24_HOUR = "Mm d yyyy, H:mm"

        fun getTimeFormatter(context: Context): Formatter {
            val pattern = if (DateFormat.is24HourFormat(context))
                FORMAT_TIME_24_HOUR else FORMAT_TIME_12_HOUR
            return Formatter.ofPattern(pattern)
        }

        fun getDateTimeFormatter(context: Context,
                                 isShort: Boolean = false,
                                 withYear: Boolean = false): Formatter {
            val is24Hour = DateFormat.is24HourFormat(context)

            val pattern = if (isShort) {
                if (withYear) {
                    if (is24Hour) FORMAT_DATE_TIME_WITH_YEAR_24_HOUR_SHORT
                    else FORMAT_DATE_TIME_WITH_YEAR_12_HOUR_SHORT
                } else {
                    if (is24Hour) FORMAT_DATE_TIME_24_HOUR_SHORT
                    else FORMAT_DATE_TIME_12_HOUR_SHORT
                }
            } else {
                if (withYear) {
                    if (is24Hour) FORMAT_DATE_TIME_WITH_YEAR_24_HOUR
                    else FORMAT_DATE_TIME_WITH_YEAR_12_HOUR
                } else {
                    if (is24Hour) FORMAT_DATE_TIME_24_HOUR
                    else FORMAT_DATE_TIME_12_HOUR
                }
            }

            return Formatter.ofPattern(pattern)
        }
    }
}