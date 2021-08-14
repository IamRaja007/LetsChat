package com.example.letschat.utils

import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import com.example.letschat.R
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*
import java.util.Calendar.*

    private fun getCurrentLocale(context: Context): Locale {    //Locale represents a language/country/variant combination. Locales are used to alter the presentation of information such as numbers or dates to suit the conventions in the region they describe.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }

    val Date.calendar: Calendar
        get() {
            val cal = getInstance()
            cal.time = this
            return cal
        }
        fun Date.isThisWeek():Boolean{
            val todayCalendar=getInstance()
            val thisWeek=todayCalendar.get(Calendar.WEEK_OF_YEAR)
            val thisYear=todayCalendar.get(YEAR)

            calendar.time = this
            val week=calendar.get(Calendar.WEEK_OF_YEAR)
            val year=calendar.get(YEAR)
            return thisWeek == week && thisYear == year
        }

    fun Date.isToday():Boolean{
        return DateUtils.isToday(this.time)
    }

    fun Date.isYesterday():Boolean{
        val yesterdayCalendar=getInstance()
        yesterdayCalendar.add(DAY_OF_YEAR,-1)
        return yesterdayCalendar.get(YEAR) == calendar.get(YEAR) && yesterdayCalendar.get(
            DAY_OF_YEAR) == calendar.get(DAY_OF_YEAR)

    }

    fun Date.isThisYear():Boolean{
        return calendar.get(YEAR) == getInstance().get(YEAR)
    }

    fun Date.isSameDayAs(date:Date):Boolean{
        return this.calendar.get(DAY_OF_YEAR) == date.calendar.get(DAY_OF_YEAR)
    }

    fun Date.formattingAsTime():String{
        val hour=calendar.get(HOUR).toString().padStart(2,'0')
        val minute=calendar.get(MINUTE).toString().padStart(2,'0')
        return "$hour:$minute"
    }

    fun Date.formattingAsYesterday(context:Context):String{
        return context.getString(R.string.Yesterday)
    }

    fun Date.formattingAsWeekDay(context: Context):String{
        return when(calendar.get(DAY_OF_WEEK)){
            SUNDAY    ->{ context.getString(R.string.Sunday) }
            MONDAY    ->{ context.getString(R.string.Monday) }
            TUESDAY   ->{ context.getString(R.string.Tuesday) }
            WEDNESDAY ->{ context.getString(R.string.Wednesday) }
            THURSDAY  ->{ context.getString(R.string.Thursday) }
            FRIDAY    ->{ context.getString(R.string.Friday) }
            SATURDAY  ->{ context.getString(R.string.Saturday) }
            else -> {
                SimpleDateFormat("d LLL", getCurrentLocale(context)).format(this)
            }
        }
    }

    fun Date.formatAsFull(context: Context, abbreviated: Boolean = false): String {
        val month = if (abbreviated) "LLL" else "LLLL"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("d $month YYYY", getCurrentLocale(context))
                .format(this)
        } else {
        SimpleDateFormat("d $month yyyy", getCurrentLocale(context))
            .format(this)
        }
    }


    fun Date.formatAsListItem(context: Context): String {
        val currentLocale = getCurrentLocale(context)


        return when {
            isToday() -> formattingAsTime()
            isYesterday() -> formattingAsYesterday(context)
            isThisWeek() -> formattingAsWeekDay(context)
            isThisYear() -> {
                SimpleDateFormat("d LLL", currentLocale).format(this)
            }
            else -> {
                formatAsFull(context, abbreviated = true)
            }
        }
    }

    fun Date.formatAsHeader(context: Context): String {
        return when {
            isToday() -> context.getString(R.string.Today)
            isYesterday() -> formattingAsYesterday(context)
            isThisWeek() -> formattingAsWeekDay(context)
            isThisYear() -> {
                SimpleDateFormat("d LLLL", getCurrentLocale(context))
                    .format(this)
            }
            else -> {
                formatAsFull(context, abbreviated = false)
            }
        }
    }



