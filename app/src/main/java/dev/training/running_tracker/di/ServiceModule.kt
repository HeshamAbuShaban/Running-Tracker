package dev.training.running_tracker.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dev.training.running_tracker.R
import dev.training.running_tracker.services.constants.ServiceConstants
import dev.training.running_tracker.services.constants.ServiceConstants.NOTIFICATION_CHANNEL_ID
import dev.training.running_tracker.ui.screens.MainActivity

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context,
    ) = LocationServices.getFusedLocationProviderClient(context)


    /** This Sends an intent to the main activity,
     *  which you can check for it and do certain events.
     *  (in my case i'll navigate
     *  form the home desertion
     *  to the
     *  tracking fragment)
     */
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext context: Context,
    ): PendingIntent = PendingIntent
        .getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).also {
                it.action = ServiceConstants.ACTION_SHOW_TRACKING_FRAGMENT
            },
            PendingIntent.FLAG_UPDATE_CURRENT // that means when ever we lunch the pending intent it will update it self instead of re crating
        )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent,
    ): NotificationCompat.Builder =
        NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false) // wont be removed due the user attempts
        .setOngoing(true) // cant be swiped away
        .setSmallIcon(R.drawable.ic_app_launcher_foreground)
        .setContentTitle("Running App") // Title
        .setContentText("00:00:00") // Description
        .setContentIntent(pendingIntent)


}