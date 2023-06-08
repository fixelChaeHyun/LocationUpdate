package ch.demo.locationupdate

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ch.demo.locationupdate.databinding.FragmentLocationUpdateBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "LocationUpdateFragment"

/**
 * Display location information via PendingIntent after permissions are approved.
 *
 * Will suggest "enhanced feature" to enable background location requests if not approved.
 */

@AndroidEntryPoint
class LocationUpdateFragment : Fragment() {

    private var activityListener: Callbacks? = null

    private lateinit var binding: FragmentLocationUpdateBinding

    val locationUpdateViewModel by viewModels<LocationUpdateViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Callbacks) {
            activityListener = context

            /*
            * If fine location permission isn't approved, instructs the parent Activity to replace
            * this fragment with the permission request fragment.
            * */
            if (!context.hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                activityListener?.requestFineLocationPermission()
            }
        } else {
            throw RuntimeException("$context must implement LocationUpdateFragment.Callbacks")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocationUpdateBinding.inflate(inflater, container, false)

        binding.enableBackgroundLocationButton.setOnClickListener {
            activityListener?.requestBackgroundLocationPermission()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationUpdateViewModel.receivingLocationUpdates.observe(
            viewLifecycleOwner,
            Observer { receivingLocation ->
                updateStartOrStopButtonState(receivingLocation)
            }
        )

        locationUpdateViewModel.locationListLiveData.observe(
            viewLifecycleOwner,
            Observer { locations ->
                locations?.let {
                    Log.d(TAG, " >> Location.size: ${locations.size}")

                    if (locations.isEmpty()) {
                        binding.locationOutputTextView.text = getString(R.string.emptyLocationDatabaseMessage)
                    } else {
                        val outputStringBuilder = StringBuilder("")
                        for (location in locations) {
                            outputStringBuilder.append(location.toString() + "\n")
                        }

                        binding.locationOutputTextView.text = outputStringBuilder.toString()
                    }
                }
            }
        )

        binding.deleteDatabase.setOnClickListener {
            Log.w(TAG, " --------- delete all database info --------")
            locationUpdateViewModel.deleteDatabase()
            binding.locationOutputTextView.text = ""
        }
    }

    override fun onResume() {
        super.onResume()
        updateBackgroundButtonState()
    }

    override fun onPause() {
        super.onPause()

        /**
         * Stops location updates if background permissions aren't approved. The FusedLocationClient
         * won't trigger any PendingIntents with location updates anyway if you don't have the
         * background permission approved, but it's best practice to unsubscribing anyway.
         *
         * To simplify the sample, we are unsubscribing from updates here in the fragment, but you
         * could do it at the Activity level if you want to continue receiving location updates
         * while the user is moving between Fragments.
         */
        if ((locationUpdateViewModel.receivingLocationUpdates.value == true) &&
            (!requireContext().hasPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION))) {
            Log.e(TAG, "-- onPause(): Stop updating locations if ACCESS_BACKGROUND_LOCATION hasn't approved. --")
            locationUpdateViewModel.stopLocationUpdates()
        } else if (locationUpdateViewModel.receivingLocationUpdates.value == true) {
            Log.i(TAG, "-- onPause(): It keeps to update locations. --")
        } else {
            Log.i(TAG, "-- onPause(): There is no location updates. --")
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

    private fun updateStartOrStopButtonState(receivingLocation: Boolean) {
        if (receivingLocation) {
            binding.startOrStopLocationUpdatesButton.apply {
                text = getString(R.string.stop_receiving_location)
                setOnClickListener {
                    locationUpdateViewModel.stopLocationUpdates()
                }
            }
        } else {
            binding.startOrStopLocationUpdatesButton.apply {
                text = getString(R.string.start_receiving_location)
                setOnClickListener {
                    locationUpdateViewModel.startLocationUpdates()
                }
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this fragment
     * fragment to allow an interaction in this fragment to be communicated to the
     * activity and potentially other fragments contained in that activity.
     */
    interface Callbacks {
        fun requestFineLocationPermission()
        fun requestBackgroundLocationPermission()
    }

    companion object {
        fun newInstance() = LocationUpdateFragment()
    }
}