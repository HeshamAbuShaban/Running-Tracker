package dev.training.running_tracker.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import dev.training.running_tracker.R
import dev.training.running_tracker.databinding.FragmentStatisticsBinding
import dev.training.running_tracker.services.utility.TrackingUtils
import dev.training.running_tracker.ui.fragments.custom.CustomMarkerView
import dev.training.running_tracker.ui.viewmodels.StatisticsViewModel
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private lateinit var binding: FragmentStatisticsBinding

    private val statisticsViewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentStatisticsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
    }

    /**
     * Sets up the configuration for the bar chart, customizing its appearance and labels.
     */
    private fun setupBarChart() {
        // Use 'with(binding)' to conveniently access views within the binding object

        with(binding.barChart) {
            // Customize the appearance and position of the X-Axis (bottom)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawLabels(false) // Hide X-Axis labels
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                setDrawGridLines(false) // Hide X-Axis grid lines
            }

            // Customize the appearance of the left Y-Axis
            axisLeft.apply {
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                setDrawGridLines(false) // Hide left Y-Axis grid lines
            }

            // Customize the appearance of the right Y-Axis
            axisRight.apply {
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                setDrawGridLines(false) // Hide right Y-Axis grid lines
            }

            // Additional chart configurations
            apply {
                description.text = "Avg Speed Over Time" // Set chart description
                legend.isEnabled = false // Disable chart legend
            }
        }
    }


    private fun subscribeToObservers() {

        fun caloriesBurnedFormat(calories: Int) = "${calories}kcal"
        fun distanceInKMFormat(distance: Int) = "${round((distance / 1000f) * 10f) / 10f}Km"
        fun avgSpeedFormat(avgSpeed: Float) = "${round(avgSpeed * 10f) / 10f}Km/h"


        statisticsViewModel.totalTimeRun.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvTotalTime.text = TrackingUtils.getFormattedStopWatchTime(it)
            }
        }
        statisticsViewModel.totalDistance.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvTotalDistance.text = distanceInKMFormat(it)
            }
        }
        statisticsViewModel.totalAvgSpeed.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvAverageSpeed.text = avgSpeedFormat(it)
            }
        }
        statisticsViewModel.totalCaloriesBurned.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvTotalCalories.text = caloriesBurnedFormat(it)
            }
        }

        /**
         * Observes changes in the runsSortedByDate LiveData from the statisticsViewModel
         * and updates the bar chart with the new data when changes occur.
         */
        statisticsViewModel.runsSortedByDate.observe(viewLifecycleOwner) { runs ->
            runs?.let {
                // Create BarEntry objects from the list of runs to represent Avg Speed Over Time
                val allAvgSpeeds = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }

                // Create a BarDataSet with the Avg Speed data and customize its appearance
                val barDataSet = BarDataSet(allAvgSpeeds, "Avg Speed Over Time").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }

                // Access the binding object to set data and markers for the bar chart
                with(binding.barChart) {
                    data = BarData(barDataSet) // Set the data for the bar chart
                    marker = CustomMarkerView(it.reversed(),requireContext(), R.layout.marker_view) // Set a custom marker view
                    invalidate() // Refresh the chart to apply changes
                }
            }
        }

    }
}
