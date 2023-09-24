package dev.training.running_tracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.training.running_tracker.database.local.entities.Run
import dev.training.running_tracker.repositories.MainRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository,
) : ViewModel() {

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    val runsSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()
    val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()
    val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}