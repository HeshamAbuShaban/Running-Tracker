package dev.training.running_tracker.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.training.running_tracker.R
import dev.training.running_tracker.adapters.RunAdapter
import dev.training.running_tracker.app_system.constants.Constants
import dev.training.running_tracker.app_system.constants.SortType
import dev.training.running_tracker.app_system.permissions.TrackingUtility
import dev.training.running_tracker.database.local.entities.Run
import dev.training.running_tracker.databinding.FragmentRunBinding
import dev.training.running_tracker.ui.viewmodels.MainViewModel
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class RunFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentRunBinding

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var runAdapter: RunAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRunBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        requestPermissions()
        setupListeners()
        setupRunRecycler()
        subscribeToObservers()
    }

    private fun setupListeners() {
        with(binding) {
            fab.setOnClickListener {
                findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
            }
        }
    }

    private fun setupRunRecycler() {
        with(binding.rvRuns) {
            adapter = runAdapter
        }
    }

    private fun displayRuns(runs: List<Run>) {
        runAdapter.runs = runs
    }

    private fun subscribeToObservers() {

        with(binding.spFilter) {
            when (mainViewModel.sortType) {
                SortType.DATE -> setSelection(0)
                SortType.RUNNING_TIME -> setSelection(1)
                SortType.DISTANCE -> setSelection(2)
                SortType.AVG_SPEED -> setSelection(3)
                SortType.CALORIES_BURNED -> setSelection(4)
            }
        }

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> mainViewModel.sortRuns(SortType.DATE)
                    1 -> mainViewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> mainViewModel.sortRuns(SortType.DISTANCE)
                    3 -> mainViewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> mainViewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) = Unit
        }

        mainViewModel.runsSelectedLiveData.observe(viewLifecycleOwner) {
            displayRuns(it)
        }

    }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermission(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You Need to accept Location Permissions To, use this app.",
                Constants.REQUEST_CODE_LOCATION_PERMISSIONS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You Need to accept Location Permissions To, use this app.",
                Constants.REQUEST_CODE_LOCATION_PERMISSIONS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) = Unit

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}