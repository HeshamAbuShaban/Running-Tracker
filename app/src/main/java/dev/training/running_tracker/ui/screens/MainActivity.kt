package dev.training.running_tracker.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.training.running_tracker.R
import dev.training.running_tracker.databinding.ActivityMainBinding
import dev.training.running_tracker.services.constants.ServiceConstants

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // to instantiate the navController value so we could use it.
        initNavController()
        // check if the activity wasn't alive and been called from the foreground service than whe need it to take this action
        receivedIntentActionToNavigateToTrackingFragment(intent)
        // set the custom toolbar that in the xml.
        setSupportActionBar(binding.toolbar)
        //.. init setups.
        init()
    }

    private fun init() {
        setupBottomNavView()
    }

    /**
     * this if the activity wasn't dead
     * and it received a new intent
     * so we act accordingly
     * */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        receivedIntentActionToNavigateToTrackingFragment(intent)
    }

    private fun setupBottomNavView() {
        val bnv = binding.bottomNavigationView

        // set it up
        bnv.setupWithNavController(navController)
        // guard navigation to change visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.settingsFragment,
                R.id.runFragment,
                R.id.statisticsFragment,
                -> bnv.isVisible = true

                else -> bnv.isVisible = false
            }
        }
    }

    private fun initNavController() {
        val mainNavHostFragment = supportFragmentManager
            .findFragmentById(R.id.mainNavHostFragment)!!
        navController = mainNavHostFragment
            .findNavController()
    }

    private fun receivedIntentActionToNavigateToTrackingFragment(intent: Intent?) {
        if (intent?.action == ServiceConstants.ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate(R.id.globalAction_to_trackingFragment)
        }
    }

}