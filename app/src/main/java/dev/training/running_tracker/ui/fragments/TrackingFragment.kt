package dev.training.running_tracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.GoogleMap
import dagger.hilt.android.AndroidEntryPoint
import dev.training.running_tracker.databinding.FragmentTrackingBinding
import dev.training.running_tracker.services.TrackingService
import dev.training.running_tracker.services.constants.ServiceConstants.ACTION_START_OR_RESUME_SERVICE
import dev.training.running_tracker.ui.viewmodels.MainViewModel

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private lateinit var binding: FragmentTrackingBinding

    private val mainViewModel: MainViewModel by viewModels()

    private var googleMap: GoogleMap? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTrackingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGoogleMapInstance(savedInstanceState)

        // Lets test the service
        binding.btnToggleRun.setOnClickListener {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }

    }

    private fun sendCommandToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action

            // this doesn't literately start the service,
            // rather it send an action each time it been called
            requireContext().startService(it)}
    }

    private fun setupGoogleMapInstance(savedInstanceState: Bundle?) {
        with(binding) {
            // save the building state to cache it.
            mapView.onCreate(savedInstanceState)

            //..from the widget when its loaded we demand the Actual Map Instance.
            mapView.getMapAsync { googleMap ->
                this@TrackingFragment.googleMap = googleMap
            }

        }
    }

    // Mix the widget lifeCycle with our fragment lifeCycle.

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    /*
        override fun onDestroy() {
            super.onDestroy()
            binding.mapView.onDestroy()
        }
    */

    //.. get the uses of the cached bundle to load faster~.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

}