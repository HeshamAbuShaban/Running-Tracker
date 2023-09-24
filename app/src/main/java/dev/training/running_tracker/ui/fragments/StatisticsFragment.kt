package dev.training.running_tracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.training.running_tracker.databinding.FragmentStatisticsBinding
import dev.training.running_tracker.services.utility.TrackingUtils
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


    }
}