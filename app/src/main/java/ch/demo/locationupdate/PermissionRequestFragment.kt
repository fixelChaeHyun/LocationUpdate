package ch.demo.locationupdate

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.demo.locationupdate.databinding.FragmentPermissionRequestBinding
import com.google.android.material.snackbar.Snackbar

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@SuppressLint("InlinedApi")
class PermissionRequestFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentPermissionRequestBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_permission_request, container, false)
    }

    private fun requestBackgroundLocationPermission() {
        /* Background 일때만 권한부여되어있는지 확인.. */
        val permissionApproved = context?.hasPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }


    interface Callbacks {
        fun displayLocationUI()
    }

    companion object {
        private const val ARG_PERMISSION_REQUEST_TYPE =
            "ch.demo.locationupdate.PERMISSION_REQUEST_TYPE"

        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56

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