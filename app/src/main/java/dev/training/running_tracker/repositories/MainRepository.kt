package dev.training.running_tracker.repositories

import dev.training.running_tracker.database.local.daos.RunDao
import dev.training.running_tracker.database.local.entities.Run
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDao: RunDao,
) {

    suspend fun insertRun(run: Run) = runDao.insertRun(run)
    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)


    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()


    // Calculation Methods Goes here

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

}