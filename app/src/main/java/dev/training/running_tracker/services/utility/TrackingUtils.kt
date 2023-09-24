package dev.training.running_tracker.services.utility

import android.location.Location
import dev.training.running_tracker.services.Polyline
import java.util.concurrent.TimeUnit

object TrackingUtils {

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        if (!includeMillis) {
            return timeFormat(hours, minutes, seconds)
        }

        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10

        return timeFormat(
            hours, minutes, seconds
        ) + ":${if (milliseconds < 10) "0" else ""}$milliseconds"
    }

    private fun timeFormat(hours: Long, minutes: Long, seconds: Long) =
        "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds"


    fun calculatePolylineLength(polyline: Polyline): Float {
        var distance = 0f
        for (i in 0..polyline.size - 2) {
            val firstPos = polyline[i]
            val secondPos = polyline[i + 1]

            val result = FloatArray(1)
            Location.distanceBetween(
                firstPos.latitude,
                firstPos.longitude,
                secondPos.latitude,
                secondPos.longitude,
                result
            )

            distance += result[0]
        }
        return distance
    }
}