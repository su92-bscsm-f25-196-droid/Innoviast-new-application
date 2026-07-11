package com.example.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun formatLongDate(timestamp: Long): String {
        if (timestamp == 0L) return "No Due Date"
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        if (timestamp == 0L) return "No Date"
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timeStr: String): String {
        if (timeStr.isEmpty()) return "All Day"
        try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = sdf24.parse(timeStr) ?: return timeStr
            val sdf12 = SimpleDateFormat("h:mm a", Locale.getDefault())
            return sdf12.format(date)
        } catch (e: Exception) {
            return timeStr
        }
    }

    fun isToday(timestamp: Long): Boolean {
        if (timestamp == 0L) return false
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isTomorrow(timestamp: Long): Boolean {
        if (timestamp == 0L) return false
        val cal1 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isOverdue(timestamp: Long, isCompleted: Boolean): Boolean {
        if (timestamp == 0L || isCompleted) return false
        // Standard start of today comparison
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return timestamp < todayStart
    }

    fun isThisWeek(timestamp: Long): Boolean {
        if (timestamp == 0L) return false
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        val endOfWeek = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
        }
        return target.after(now) && target.before(endOfWeek) || isToday(timestamp)
    }

    fun getNextOccurrence(currentTimestamp: Long, recurrence: String): Long {
        if (currentTimestamp == 0L || recurrence == "None") return 0L
        val cal = Calendar.getInstance().apply { timeInMillis = currentTimestamp }
        when (recurrence) {
            "Daily" -> cal.add(Calendar.DAY_OF_YEAR, 1)
            "Weekdays" -> {
                do {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                } while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                )
            }
            "Weekly" -> cal.add(Calendar.DAY_OF_YEAR, 7)
            "Monthly" -> cal.add(Calendar.MONTH, 1)
            "Yearly" -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }
}
