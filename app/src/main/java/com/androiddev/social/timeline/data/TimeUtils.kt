package com.androiddev.social.timeline.data

import java.util.Calendar
import java.util.Date

object FuzzyDateTimeFormatter {
    private const val SECONDS = 1
    private const val MINUTES = 60 * SECONDS
    private const val HOURS = 60 * MINUTES
    private const val DAYS = 24 * HOURS
    private const val WEEKS = 7 * DAYS
    private const val MONTHS = 4 * WEEKS
    private const val YEARS = 12 * MONTHS

    /**
     * Returns a properly formatted fuzzy string representing time ago
     * @param context   Context
     * @param date      Absolute date of the event
     * @return          Formatted string
     */
    fun Date.getTimeAgo(): String {
        val date = this
        val beforeSeconds = (date.time / 1000).toInt()
        val nowSeconds = (Calendar.getInstance().timeInMillis / 1000).toInt()
        val timeDifference = nowSeconds - beforeSeconds
        require(timeDifference >= 0) { "Date must be in the past!" }
        return if (timeDifference < 15 * SECONDS) {
            "just now"
        } else if (timeDifference < MINUTES) {
            "$timeDifference second ago"
        } else if (timeDifference < HOURS) {
            "$timeDifference hours ago"

        } else if (timeDifference < DAYS) {
            "$timeDifference days ago"

        } else if (timeDifference < WEEKS) {
            "$timeDifference weeks ago"

        } else if (timeDifference < MONTHS) {
            "$timeDifference months ago"

        } else if (timeDifference < YEARS) {
            "$timeDifference years ago"

        } else {
            "$timeDifference years ago"
        }
    }
}