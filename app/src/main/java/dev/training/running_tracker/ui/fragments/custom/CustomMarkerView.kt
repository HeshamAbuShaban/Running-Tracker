package dev.training.running_tracker.ui.fragments.custom

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import dev.training.running_tracker.database.local.entities.Run
import dev.training.running_tracker.databinding.MarkerViewBinding
import dev.training.running_tracker.services.utility.TrackingUtils
import timber.log.Timber

@SuppressLint("ViewConstructor")
class CustomMarkerView(
    private val runs: List<Run>,
    context: Context,
    layoutId: Int,
) : MarkerView(context, layoutId) {

    private val binding: MarkerViewBinding

    init {
        binding = MarkerViewBinding.inflate(LayoutInflater.from(context))
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    @SuppressLint("SetTextI18n")
    override fun refreshContent(entry: Entry?, highlight: Highlight?) {
        super.refreshContent(entry, highlight)
        if (entry == null) return

        val curRunId = entry.x.toInt()
        val run = runs[curRunId]


        with(binding) {
            tvDate.text = TrackingUtils.formatLongDateToString(run.timestamp)

            tvAvgSpeed.text = "${run.avgSpeedInKMH}km/h"

            tvDistance.text = "${run.distanceInMeters / 1000f}km"

            tvDuration.text = TrackingUtils.getFormattedStopWatchTime(run.timeInMillis)

            tvCaloriesBurned.text = "${run.caloriesBurned}kcal"
        }

        Timber.tag("CustomMarkerView").d("binding.tvDate: %s", binding.tvDate.text)
        Timber.tag("CustomMarkerView").d("binding.tvAvgSpeed.text: %s", binding.tvAvgSpeed.text)
        Timber.tag("CustomMarkerView").d("binding.tvDistance.text: %s", binding.tvDistance.text)
        Timber.tag("CustomMarkerView").d("binding.tvDuration.text: %s", binding.tvDuration.text)
        Timber.tag("CustomMarkerView").d("binding.tvCaloriesBurned.text: %s", binding.tvCaloriesBurned.text)

    }

    /*private fun loopThroughRunValues(run: Run) {
        for (i in 0..<run.javaClass.declaredFields.size) {
            AppUtils.showToast(context, "RunTime: ${run.javaClass.declaredFields[i].name}")
        }
    }*/

}