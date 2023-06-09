package ch.demo.locationupdate

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ch.demo.locationupdate.databinding.FragmentForegroundUpdateBinding
import ch.demo.locationupdate.foreground.Constants
import ch.demo.locationupdate.foreground.TestForegroundService
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ForegroundUpdateFragment: Fragment() {
    val TAG = this.javaClass.simpleName

    private var activityListener: LocationUpdateFragment.Callbacks? = null
    private lateinit var binding: FragmentForegroundUpdateBinding

    val locationUpdateViewModel by viewModels<LocationUpdateViewModel>()


    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Callbacks) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForegroundUpdateBinding.inflate(inflater, container, false)
        binding.enableBackgroundLocationButton.setOnClickListener {
            activityListener?.requestBackgroundLocationPermission()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.deleteDatabase.setOnClickListener {
            Log.w(TAG, " --------- delete all database info --------")
            locationUpdateViewModel.deleteDatabase()
            binding.locationOutputTextView.text = ""
        }

        binding.startOrStopLocationUpdatesButton.tag = "N"
        binding.startOrStopLocationUpdatesButton.text = getString(R.string.start_receiving_location)
        binding.startOrStopLocationUpdatesButton.setOnClickListener {
            if (binding.startOrStopLocationUpdatesButton.tag == "N") {
                Log.d(TAG, " -> Start a Service clicked-!")
                binding.startOrStopLocationUpdatesButton.tag = "Y"
                binding.startOrStopLocationUpdatesButton.text = getString(R.string.stop_receiving_location)
                startLocationService()
            } else {
                Log.d(TAG, " -> Stop a Service clicked-!")
                binding.startOrStopLocationUpdatesButton.tag = "N"
                binding.startOrStopLocationUpdatesButton.text = getString(R.string.start_receiving_location)
                stopLocationService()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        activityListener = null
    }

    private fun showBackgroundButton(): Boolean {
        return !requireContext().hasPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private fun updateBackgroundButtonState() {
        if (showBackgroundButton()) {
            binding.enableBackgroundLocationButton.visibility = View.VISIBLE
        } else {
            binding.enableBackgroundLocationButton.visibility = View.GONE
        }
    }

    private fun isLocationServiceRunning() : Boolean {
        val activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (activityManager != null) {
            for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
                if (TestForegroundService::class.java.name == service.service.className) {
                    if (service.foreground) {
                        return true
                    }
                }
            }
            return false
        }
        return false
    }

    private fun startLocationService() {
        if (!isLocationServiceRunning()) {
            val intent = Intent(requireActivity().applicationContext, TestForegroundService::class.java)
            intent.action = Constants.ACTION_START_LOCATION_SERVICE
            requireActivity().startService(intent)
            Toast.makeText(requireContext(), "Location service started", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLocationService() {
        if (isLocationServiceRunning()) {
            val intent = Intent(requireActivity().applicationContext, TestForegroundService::class.java)
            intent.action = Constants.ACTION_STOP_LOCATION_SERVICE
            requireActivity().startService(intent)
            Toast.makeText(requireContext(), "Location service stopped", Toast.LENGTH_SHORT).show()
        }
    }

    interface Callbacks {
        fun requestFineLocationPermission()
        fun requestBackgroundLocationPermission()
    }

    companion object {
        fun newInstance() = ForegroundUpdateFragment()
    }
}