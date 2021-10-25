package com.example.myapplication.businessLogic

import android.annotation.TargetApi
import android.location.Location
import android.os.Build
import android.os.SystemClock
import com.example.myapplication.MyUtility

class UtilityImp : MyUtility {

    override fun ageMs(last: Location): Long {
        //method to calculate the milliseconds since the last location
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) ageMsApi17(last) else ageMsApiPre17(
            last
        )
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun ageMsApi17(last: Location): Long {
        return (SystemClock.elapsedRealtimeNanos() - last.elapsedRealtimeNanos) / 1000000
    }

    private fun ageMsApiPre17(last: Location): Long {
        return System.currentTimeMillis() - last.time
    }
}