package ch.demo.locationupdate

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.demo.locationupdate.databinding.FragmentPermissionRequestBinding
import com.google.android.material.snackbar.Snackbar

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "PermissionRequestFrag"
class PermissionRequestFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentPermissionRequestBinding

    // Type of permission to request (fine or background). Set by calling Activity.
    private var permissionRequestType: PermissionRequestType? = null
    private var activityListener: Callbacks? = null

    private val fineLocationRationalSnackbar by lazy {
        Snackbar.make(
            binding.frameLayout,
            R.string.fine_location_permission_rationale,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.ok) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private val backgroundRationalSnackbar by lazy {
        Snackbar.make(
            binding.frameLayout,
            R.string.background_location_permission_rationale,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.ok) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Callbacks) {
            activityListener = context
        } else {
            throw RuntimeException("$context must implement PermissionRequestFragment.Callbacks")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set a fine or background location permission
        permissionRequestType = arguments?.getSerializable(ARG_PERMISSION_REQUEST_TYPE) as PermissionRequestType
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPermissionRequestBinding.inflate(inflater, container, false)
        when (permissionRequestType) {
            PermissionRequestType.FINE_LOCATION, null -> {
                binding.apply {
                    iconImageView.setImageResource(R.drawable.ic_location_on_24px)

                    titleTextView.text =
                        getString(R.string.fine_location_access_rationale_title_text)

                    detailsTextView.text =
                        getString(R.string.fine_location_access_rationale_details_text)

                    permissionRequestButton.text =
                        getString(R.string.enable_fine_location_button_text)
                }
            }
            PermissionRequestType.BACKGROUND_LOCATION -> {
                binding.apply {
                    iconImageView.setImageResource(R.drawable.ic_my_location_24px)

                    titleTextView.text =
                        getString(R.string.background_location_access_rationale_title_text)

                    detailsTextView.text =
                        getString(R.string.background_location_access_rationale_details_text)

                    permissionRequestButton.text =
                        getString(R.string.enable_background_location_button_text)
                }
            }
        }

        binding.permissionRequestButton.setOnClickListener {
            when (permissionRequestType) {
                PermissionRequestType.FINE_LOCATION, null -> {
                    requestFineLocationPermission()
                }
                PermissionRequestType.BACKGROUND_LOCATION -> {
                    requestBackgroundLocationPermission()
                }
            }
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
        activityListener = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE -> when {
                    grantResults.isEmpty() -> {
                        //If user interaction was interrupted, the permission request is cancelled and you receive an empty array.
                        Log.d(TAG, "User Interaction for the permission was cancelled.")
                    }

                    grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                        // 권한이 부여 됐다는 것을 확인 한 뒤에 Location 획득 후 의 UI로 변경 명령 전달
                        activityListener?.displayLocationUI()
                    }

                    else -> {
                         val permissionDeniedExplanation =
                             if (requestCode == REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
                                 R.string.fine_permission_denied_explanation
                             } else {
                                 R.string.background_permission_denied_explanation
                             }
                         Snackbar.make(
                             binding.frameLayout,
                             permissionDeniedExplanation,
                             Snackbar.LENGTH_LONG
                         ).setAction(R.string.settings) {
                             // Build intent that display the App Settings screen.
                             val intent = Intent()
                             intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                             val uri = Uri.fromParts(
                                 "packager",
                                 BuildConfig.APPLICATION_ID,
                                 null
                             )
                             intent.data = uri
                             intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                             startActivity(intent)
                         }
                         .show()
                    }


                }
        }
    }

    private fun requestFineLocationPermission() {
        val permissionApproved = context?.hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ?: return

        if (permissionApproved) {
            activityListener?.displayLocationUI()
        } else {
            requestPermissionWithRationale(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                fineLocationRationalSnackbar
            )
        }
    }

    private fun requestBackgroundLocationPermission() {
        /* Background 일때만 권한부여되어있는지 확인.. */
        val permissionApproved = context?.hasPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) ?: return

        if (permissionApproved) {
            activityListener?.displayLocationUI()
        } else {
            requestPermissionWithRationale(
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
                backgroundRationalSnackbar
            )
        }
    }

    /**
     * This interface must be implemented by activities that contain this fragment
     * to allow an interaction in this fragment to be communicated to the activity
     * and potentially other fragments contained in that activity
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */

    interface Callbacks {
        fun displayLocationUI()
    }

    companion object {
        private const val ARG_PERMISSION_REQUEST_TYPE =
            "ch.demo.locationupdate.PERMISSION_REQUEST_TYPE"

        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56

        /**
         * Use this factory method to create a new instance of this fragment
         * using the provided parameters
         * */
        @JvmStatic
        fun newInstance(permissionRequestType: PermissionRequestType) =
            PermissionRequestFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PERMISSION_REQUEST_TYPE, permissionRequestType)
                }
            }
    }
}

enum class PermissionRequestType {
    FINE_LOCATION, BACKGROUND_LOCATION
}