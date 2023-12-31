package dev.training.running_tracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import dev.training.running_tracker.R
import dev.training.running_tracker.app_system.permissions.TrackingUtility
import dev.training.running_tracker.services.constants.ServiceConstants
import dev.training.running_tracker.services.constants.ServiceConstants.ACTION_PAUSE_SERVICE
import dev.training.running_tracker.services.constants.ServiceConstants.ACTION_START_OR_RESUME_SERVICE
import dev.training.running_tracker.services.constants.ServiceConstants.ACTION_STOP_SERVICE
import dev.training.running_tracker.services.constants.ServiceConstants.NOTIFICATION_CHANNEL_ID
import dev.training.running_tracker.services.constants.ServiceConstants.NOTIFICATION_CHANNEL_NAME
import dev.training.running_tracker.services.constants.ServiceConstants.NOTIFICATION_ID
import dev.training.running_tracker.services.utility.TrackingUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias ManyPolyline = MutableList<Polyline>

/**
 * In typical Android development, when we mention a "service," it often brings to mind
 * either an IntentService or a regular background service. These services are typically used
 * to perform tasks independently of the user interface.
 *
 * However, in our project, we are going to use a different type of service called a
 * "LifecycleService." The reason for this choice is our need to observe LiveData objects
 * within this service.
 *
 * To put it succinctly, the LifecycleService provides a connection to the Android LifecycleOwner,
 * which is crucial for observing LiveData objects within a service context. This allows us to
 * seamlessly integrate LiveData functionality into our service, providing real-time updates
 * and responsiveness to changes.
 */
@AndroidEntryPoint
class TrackingService : LifecycleService() {

    /**
     * In our application, we need a way to establish a connection between the service and other parts
     * of the application, such as activities or fragments. This connection allows data and information
     * to flow between these components seamlessly.
     *
     * When it comes to communication from an activity or fragment to the service, one common approach is
     * to use an Intent. This involves sending broadcast messages or explicit Intents to trigger specific
     * actions in the service.
     *
     * On the other hand, when we need to send information or updates from the service to an activity or
     * fragment (for example, sending tracking coordinates to update the user interface), we have a few
     * options:
     *
     * 1. Singleton: We can create a Singleton class where properties are declared as static fields.
     *    This Singleton instance can be accessed and modified from different parts of the application.
     *
     * 2. BoundService: This is another option, though it can be more complex. BoundService allows for
     *    direct, bidirectional communication between the service and other components. It provides
     *    a way for activities or fragments to bind to the service and interact with it through a
     *    defined interface. While it offers more control, it can be more challenging to implement.
     *
     * The choice between these approaches depends on the specific requirements and complexity of the
     * application's communication needs.
     */

    private var isFirstRun = true
    private var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    private lateinit var curNotificationBuilder: NotificationCompat.Builder

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<ManyPolyline>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        curNotificationBuilder = baseNotificationBuilder
        isTracking.observe(this) {
            updateLocationTracking(it)
            updateNotificationTrackingStateWithActions(it)
        }
    }

    @Suppress("DEPRECATION")
    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    // its called whenever we send a command to our service form the outside
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // THIS THE SIDE WHERE IT RECEIVE ACTIONS AND ACT ACCORDINGLY
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        //..this the first call for this service
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming Service")
                        startTimer()
                    }
                }

                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused Service.")
                    pauseService()
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped Service.")
                    killService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondsTimestamp = 0L

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                //..time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                //..post new lap time
                timeRunInMillis.postValue(timeRun + lapTime)

                if (timeRunInMillis.value!! >= lastSecondsTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondsTimestamp += 1000L
                }
                delay(ServiceConstants.TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingStateWithActions(isTracking: Boolean) {
        val notificationActionText =
            if (isTracking) getString(R.string.pause) else getString(R.string.resume)

        /**..we create a pendingIntent to hold an action for future call
         * and we made what the pending intent needs
         * which is an intent object
         * in our case we needs 2 intent object
         * because we have 2 possible actions
         * */
        val notificationActionIcon: Int
        val pendingIntentAction = if (isTracking) {
            notificationActionIcon = R.drawable.ic_pause_black_24dp
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        } else {
            notificationActionIcon = R.drawable.ic_run
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        }

        // We need to tell the notificationManager to have these updates we made
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        /**
         * this is a stackoverflow method
         *  to empty the actions in the notification,
         *  *(in our case we need that
         *  due the continuously updating the foreground service
         *  with data(timer)
         *  and so we need action attached
         *  for each update)*
         *
         * */
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!serviceKilled) {
            //.. set the copy to the base copy and add actions to it
            curNotificationBuilder = baseNotificationBuilder
                .addAction(
                    notificationActionIcon,
                    notificationActionText,
                    pendingIntentAction
                )

            //..Now lets update the notification by posting the edited one
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = LocationRequest().apply {
                    interval = ServiceConstants.LOCATION_UPDATE_INTERVAL
                    fastestInterval = ServiceConstants.FASTEST_LOCATION_INTERVAL
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(it.latitude, it.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    //.. Lets make it a foreground service
    /**
     * well ofc you'll need a
     * 1.notification
     * that means you also need a
     * 2.notification channel
     * for higher than android 8
     *
     * */

    private fun startForegroundService() {
        //...start Timer
        startTimer()
        isTracking.postValue(true)
        //..get a reference to our notificationManager
        //..    so we can call the create notification fun
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // we are on android 8 or higher so lets use our fun to create a channel
            createNotificationChannel(notificationManager)
        }

        // After the channel been created i crated the notification and set it to be shown as foreground
        createNotification()

        //..Update the time into the notify
        timeRunInSeconds.observe(this) {
            if (!serviceKilled) {
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtils.getFormattedStopWatchTime(it * 1000))
                //..pushes the updated one
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        }
    }

    private fun createNotification() {

        /**
         * to build a notification we use the builder of the NotificationCompat
         * and give it the id of the channel
         * that our notification will be in
         * */

        //###Moved To Dagger.
        /*val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) // wont be removed due the user attempts
            .setOngoing(true) // cant be swiped away
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle("Running App") // Title
            .setContentText("00:00:00") // Description
            .setContentIntent(getMainActivityPendingIntent())
            .build()*/

        // this to show the notification and stick it as foreground
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
    }

    //###Moved To Dagger.
    /*// This Sends an intent to the main activity which you can check for it and do certain events. (in my case i'll navigate form the home desertion to the tracking fragment)
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ServiceConstants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT // that means when ever we lunch the pending intent it will update it self instead of re crating
    )*/

    //...Creating a Channel for above than android8 >
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        /**
         * the reason for setting to low
         * that we want to spam the channel constantly
         * whenever we do that WE DO NOT NEED SOUND WITH THE NOTIFICATION
         */

        notificationManager.createNotificationChannel(channel)
    }

}