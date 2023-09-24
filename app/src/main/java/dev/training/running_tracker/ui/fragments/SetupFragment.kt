package dev.training.running_tracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import dev.training.running_tracker.R
import dev.training.running_tracker.app_system.AppUtils
import dev.training.running_tracker.app_system.prefs.AppSharedPreferences
import dev.training.running_tracker.databinding.FragmentSetupBinding
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment() {

    private lateinit var binding: FragmentSetupBinding

    @set:Inject
    var isFirstTimeAppOpened = true

    @Inject
    lateinit var appSharedPreferences: AppSharedPreferences

    private lateinit var navController: NavController
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSetupBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        shouldSkip(savedInstanceState)
        setupListeners()
    }

    private fun shouldSkip(savedInstanceState: Bundle?) {
        if (!isFirstTimeAppOpened) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()

            navController.navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }
    }

    private fun setupListeners() {
        with(binding) {
            tvContinue.setOnClickListener {
                val success = preferences()
                if (success) navController.navigate(R.id.action_setupFragment_to_runFragment)
                else AppUtils.showSnackBar(root, "Check Missing fields!")
            }
        }
    }

    private fun preferences(): Boolean {
        with(binding) {
            val name = etName.text.toString()
            val weight = etWeight.text.toString()
            if (name.isEmpty() || weight.isEmpty()) return false

            appSharedPreferences.writePersonalDataToSharedPreferences(name, weight, false)

            /*sharedPreferences.edit().apply {
                putBoolean(Keys.FIRST_TIME_TOGGLE, false)
                putString(Keys.NAME, name)
                putFloat(Keys.WEIGHT, weight.toFloat())
                apply()
            }*/

            val toolbarText = "Let's run, $name!"
            requireActivity().findViewById<MaterialTextView>(R.id.tvToolbarTitle).text = toolbarText
            return true
        }
    }

}