package dev.training.running_tracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import dev.training.running_tracker.database.local.entities.Run
import dev.training.running_tracker.databinding.ItemRunBinding
import dev.training.running_tracker.services.utility.TrackingUtils
import javax.inject.Inject
import kotlin.math.round

class RunAdapter @Inject constructor(private val glide: RequestManager) :
    RecyclerView.Adapter<RunAdapter.RunViewHolder>() {
    inner class RunViewHolder(private val binding: ItemRunBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(run: Run) {
            with(binding) {
                glide.load(run.img).into(ivRunImage)
                tvDate.text = TrackingUtils.formatLongDateToString(run.timestamp)
                tvAvgSpeed.text = avgSpeedFormat(run.avgSpeedInKMH)
                tvDistance.text = distanceInKMFormat(run.distanceInMeters)
                tvTime.text = TrackingUtils.getFormattedStopWatchTime(run.timeInMillis)
                tvCalories.text = caloriesBurnedFormat(run.caloriesBurned)
            }
        }

        private fun caloriesBurnedFormat(calories: Int) = "${calories}kcal"
        private fun distanceInKMFormat(distance: Int) = "${round(distance / 1000f)}Km"
        private fun avgSpeedFormat(avgSpeed: Float) = "${round(avgSpeed * 10f) / 10f}Km/h"
        /* private fun getRunDate(timestamp: Long): Date = Calendar.getInstance().apply {
             timeInMillis = timestamp
         }.time*/

    }

    private val differ: AsyncListDiffer<Run> = AsyncListDiffer(this, DiffUtilsImpl())

    private inner class DiffUtilsImpl : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }
    }

    var runs: List<Run>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder =
        RunViewHolder(ItemRunBinding.inflate(LayoutInflater.from(parent.context)))

    override fun getItemCount(): Int = runs.size

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = runs[position]
        holder.bindData(run)
    }
}