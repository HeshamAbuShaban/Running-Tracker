package dev.training.running_tracker.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.training.running_tracker.app_system.constants.Constants.RUNNING_DATABASE_NAME
import dev.training.running_tracker.database.local.RunningDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext applicationContext: Context,
    ) = Room.databaseBuilder(
        context = applicationContext,
        klass = RunningDatabase::class.java,
        name = RUNNING_DATABASE_NAME
    ).build()


    @Singleton
    @Provides
    fun provideRunningDao(db:RunningDatabase) = db.getRunDao()

    
}