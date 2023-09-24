package dev.training.running_tracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import dev.training.running_tracker.R
import dev.training.running_tracker.app_system.AppUtils
import dev.training.running_tracker.app_system.prefs.AppSharedPreferences
import dev.training.running_tracker.databinding.FragmentSettingsBinding
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var appSharedPreferences: AppSharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadOldPersonalData()
        setupListeners()
    }

    private fun setupListeners() {
        with(binding) {
            btnApplyChanges.setOnClickListener {
                val success = preferences()
                if (success) AppUtils.showToast(requireContext(), "Changes Applied Successfully.")
                else AppUtils.showSnackBar(root, "Check Missing fields!")
            }
        }
    }

    private fun loadOldPersonalData() {
        with(binding) {
            val (name, weight) = appSharedPreferences.readPersonalDataToSharedPreferences()
            etName.setText(name)
            etWeight.setText(weight.toString())
        }
    }

    private fun preferences(): Boolean {
        with(binding) {
            val name = etName.text.toString()
            val weight = etWeight.text.toString()
            if (name.isEmpty() || weight.isEmpty()) return false

            appSharedPreferences.writePersonalDataToSharedPreferences(name, weight)

            val toolbarText = "Let's run, $name!"
            requireActivity().findViewById<MaterialTextView>(R.id.tvToolbarTitle).text = toolbarText
            return true
        }
    }
}