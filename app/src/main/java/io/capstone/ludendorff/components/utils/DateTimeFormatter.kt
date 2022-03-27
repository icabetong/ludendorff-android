package io.capstone.ludendorff.components.utils

import android.content.Context
import android.text.format.DateFormat
import androidx.annotation.StringRes
import io.capstone.ludendorff.R
import java.time.format.DateTimeFormatter as Formatter

class DateTimeFormatter private constructor() {

    companion object {
        const val FIELD_NANOSECONDS = "_nanoseconds"
        const val FIELD_SECONDS = "_seconds"

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
        private const val FORMAT_DATE_SHORT = "MM d"
        private const val FORMAT_DATE = "MMMM d"
        private const val FORMAT_DATE_WITH_YEAR = "MM d yyyy"
        private const val FORMAT_DATE_WITH_YEAR_SHORT = "MMMM d yyyy"

        fun getTimeFormatter(context: Context): Formatter {
            val pattern = if (DateFormat.is24HourFormat(context))
                FORMAT_TIME_24_HOUR else FORMAT_TIME_12_HOUR
            return Formatter.ofPattern(pattern)
        }

        fun getDateFormatter(isShort: Boolean = false,
                             withYear: Boolean = false): Formatter {
            
            val pattern = if (isShort) {
                if (withYear) {
                    FORMAT_DATE_WITH_YEAR_SHORT
                } else {
                    FORMAT_DATE_SHORT
                }
            } else {
                if (withYear) {
                    FORMAT_DATE_WITH_YEAR
                } else {
                    FORMAT_DATE
                }
            }

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

        @StringRes
        fun getMonthStringRes(number: Int): Int {
            return when(number) {
                0 -> R.string.month_jan
                1 -> R.string.month_feb
                2 -> R.string.month_mar
                3 -> R.string.month_apr
                4 -> R.string.month_may
                5 -> R.string.month_jun
                6 -> R.string.month_jul
                7 -> R.string.month_aug
                8 -> R.string.month_sep
                9 -> R.string.month_oct
                10 -> R.string.month_nov
                11 -> R.string.month_dec
                else -> throw IllegalArgumentException("Invalid month number")
            }
        }
    }
}