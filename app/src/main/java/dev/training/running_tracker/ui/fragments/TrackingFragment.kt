package dev.training.running_tracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.training.running_tracker.R
import dev.training.running_tracker.app_system.constants.Constants
import dev.training.running_tracker.databinding.FragmentTrackingBinding
import dev.training.running_tracker.services.Polyline
import dev.training.running_tracker.services.TrackingService
import dev.training.running_tracker.services.constants.ServiceConstants.ACTION_PAUSE_SERVICE
import dev.training.running_tracker.services.constants.ServiceConstants.ACTION_START_OR_RESUME_SERVICE
import dev.training.running_tracker.services.constants.ServiceConstants.ACTION_STOP_SERVICE
import dev.training.running_tracker.services.utility.TrackingUtils
import dev.training.running_tracker.ui.viewmodels.MainViewModel

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private lateinit var binding: FragmentTrackingBinding

    private val mainViewModel: MainViewModel by viewModels()

    private var isTracking = false

    private var pathPoints = mutableListOf<Polyline>()

    private var googleMap: GoogleMap? = null

    private var curTimeInMillis = 0L

    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentTrackingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGoogleMapInstance(savedInstanceState)

        // Lets test the service
        setupClickListeners()
        //.. Subscribe To Service Observer
        subscribeToObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (curTimeInMillis > 0) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel Run")
            .setMessage("Are u sure you want to cancel the run progress and delete all it data ?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                cancelRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.create()
        dialog.show()
    }

    private fun cancelRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun setupClickListeners() {
        with(binding) {

            btnToggleRun.setOnClickListener {
                toggleRun()
            }

        }
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it

            addLatestPolyline()

            moveCameraToUser()
        }

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            curTimeInMillis = it
            val formattedTime = TrackingUtils.getFormattedStopWatchTime(curTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        }
    }

    private fun toggleRun() = if (isTracking) {
        menu?.getItem(0)?.isVisible = true
        sendCommandToService(ACTION_PAUSE_SERVICE)
    } else {
        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        val buttonText = if (isTracking) getString(R.string.stop) else getString(R.string.start)
        val finishRunVisible = !isTracking
        with(binding) {
            btnToggleRun.text = buttonText
            btnFinishRun.isVisible = finishRunVisible
        }
        menu?.getItem(0)?.isVisible = if (isTracking) true else return
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    Constants.MAP_ZOOM
                )
            )
        }
    }

    private fun addAllPolyline() {
        for (polyline in pathPoints) {
            val polylineOptions = polylineOptions()
                .addAll(polyline)
            googleMap?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLang = pathPoints.last().last()
            val polylineOptions = polylineOptions()
                .add(preLastLatLng)
                .add(lastLatLang)

            googleMap?.addPolyline(polylineOptions)
        }
    }

    private fun polylineOptions() = PolylineOptions()
        .color(Constants.POLYLINE_COLOR)
        .width(Constants.POLYLINE_WIDTH)

    private fun sendCommandToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action

            // this doesn't literately start the service,
            // rather it send an action each time it been called
            requireContext().startService(it)
            // STOPSHIP: vid 11
        }
    }

    private fun setupGoogleMapInstance(savedInstanceState: Bundle?) {
        with(binding) {
            // save the building state to cache it.
            mapView.onCreate(savedInstanceState)

            //..from the widget when its loaded we demand the Actual Map Instance.
            mapView.getMapAsync { googleMap ->
                this@TrackingFragment.googleMap = googleMap
                // we need to use
                addAllPolyline() //.. we make sure it called before any process
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