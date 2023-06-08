package ch.demo.locationupdate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ch.demo.locationupdate.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * This app allows a user to receive location updates in the background.
 *
 * Users have four options in Android 11+ regarding location:
 *
 * * One time only
 * * Allow while app is in use, i.e., while app is in foreground
 * * Allow all the time
 * * Not allow location at all
 *
 * IMPORTANT NOTE: You should generally prefer 'while-in-use' for location updates, i.e., receiving
 * location updates while the app is in use and create a foreground service (tied to a Notification)
 *
 * when the user navigates away from the app. To lean how to do that instead, review the
 * @see <a href="https://codelabs.developers.google.com/codelabs/while-in-use-location/index.html?index=..%2F..index#0">
 * Receive location updates in Android 10 with kotlin</a> codelab.
 *
 * If you do have an approved use case for receiving location updates in the background, it will
 * require an additional permission (android.permission.ACCESS_BACKGROUND_LOCATION).
 *
 *
 * Best practices require you to spread out your first fine/course request and your background
 * request.
 */

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PermissionRequestFragment.Callbacks,
    LocationUpdateFragment.Callbacks {
    val viewModel: MainViewModel by viewModels<MainViewModel>()
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)    // R.layout.activity_main

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) {
            val fragment = LocationUpdateFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }

    }

    // Triggered from the permission that it's the app has permissions to display the location
    // fragment.
    override fun displayLocationUI() {
        val fragment = LocationUpdateFragment.newInstance()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Triggers a splash screen (fragment) to help users decide if they want to approve the missing
    // fine location permission.
    override fun requestFineLocationPermission() {
        val fragment = PermissionRequestFragment.newInstance(PermissionRequestType.FINE_LOCATION)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun requestBackgroundLocationPermission() {
        val fragment = PermissionRequestFragment.newInstance(
            PermissionRequestType.BACKGROUND_LOCATION
        )

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}