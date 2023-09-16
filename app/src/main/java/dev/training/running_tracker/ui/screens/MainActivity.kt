package dev.training.running_tracker.ui.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.training.running_tracker.R
import dev.training.running_tracker.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // set the custom toolbar that in the xml.
        setSupportActionBar(binding.toolbar)
        //.. init setups.
        init()
    }

    private fun init() {
        setupBottomNavView()
    }

    private fun setupBottomNavView() {
        val mainNavHostFragment = supportFragmentManager
            .findFragmentById(R.id.mainNavHostFragment)!!
        val navController = mainNavHostFragment
            .findNavController()
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

}