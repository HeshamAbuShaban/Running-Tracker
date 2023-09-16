package dev.training.running_tracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.training.running_tracker.databinding.FragmentTrackingBinding

class TrackingFragment : Fragment() {

    private lateinit var binding: FragmentTrackingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTrackingBinding.inflate(layoutInflater)
        return binding.root
    }
}