package dev.training.running_tracker.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.training.running_tracker.app_system.constants.SortType
import dev.training.running_tracker.database.local.entities.Run
import dev.training.running_tracker.repositories.MainRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository,
) : ViewModel() {

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()

    val runsSelectedLiveData = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    init {
        runsSelectedLiveData.addSource(runsSortedByDate) { result ->
            if (sortType == SortType.DATE) {
                result?.let {
                    runsSelectedLiveData.value = it
                }
            }
        }
        runsSelectedLiveData.addSource(runsSortedByAvgSpeed) { result ->
            if (sortType == SortType.AVG_SPEED) {
                result?.let {
                    runsSelectedLiveData.value = it
                }
            }
        }
        runsSelectedLiveData.addSource(runsSortedByTimeInMillis) { result ->
            if (sortType == SortType.RUNNING_TIME) {
                result?.let {
                    runsSelectedLiveData.value = it
                }
            }
        }
        runsSelectedLiveData.addSource(runsSortedByDistance) { result ->
            if (sortType == SortType.DISTANCE) {
                result?.let {
                    runsSelectedLiveData.value = it
                }
            }
        }
        runsSelectedLiveData.addSource(runsSortedByCaloriesBurned) { result ->
            if (sortType == SortType.CALORIES_BURNED) {
                result?.let {
                    runsSelectedLiveData.value = it
                }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DATE -> runsSortedByDate.value?.let {
            runsSelectedLiveData.value = it
        }

        SortType.RUNNING_TIME -> runsSortedByTimeInMillis.value?.let {
            runsSelectedLiveData.value = it
        }

        SortType.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let {
            runsSelectedLiveData.value = it
        }

        SortType.AVG_SPEED -> runsSortedByAvgSpeed.value?.let {
            runsSelectedLiveData.value = it
        }

        SortType.DISTANCE -> runsSortedByDistance.value?.let {
            runsSelectedLiveData.value = it
        }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}