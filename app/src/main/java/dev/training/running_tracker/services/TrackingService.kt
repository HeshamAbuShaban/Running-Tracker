package dev.training.running_tracker.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import dev.training.running_tracker.services.constants.ServiceConstants.ACTION_PAUSE_SERVICE
import dev.training.running_tracker.services.constants.ServiceConstants.ACTION_START_OR_RESUME_SERVICE
import dev.training.running_tracker.services.constants.ServiceConstants.ACTION_STOP_SERVICE
import timber.log.Timber

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


    // its called whenever we send a command to our service form the outside
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // THIS THE SIDE WHERE IT RECEIVE ACTIONS AND ACT ACCORDINGLY
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("Started or Resumed Service.")
                }

                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused Service.")
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped Service.")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

}