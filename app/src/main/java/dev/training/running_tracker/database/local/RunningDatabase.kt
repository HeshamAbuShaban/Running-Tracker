package dev.training.running_tracker.database.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.training.running_tracker.database.local.daos.RunDao
import dev.training.running_tracker.database.local.entities.Run
import dev.training.running_tracker.database.local.type_converters.BitmapConverter

@Database(
    entities = [Run::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(BitmapConverter::class)
abstract class RunningDatabase : RoomDatabase() {
    abstract fun getRunDao(): RunDao
}