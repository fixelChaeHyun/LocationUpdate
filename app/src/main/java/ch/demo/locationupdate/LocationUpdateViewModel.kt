package ch.demo.locationupdate

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class LocationUpdateViewModel @Inject constructor(
    @ApplicationContext appContext: Context
): ViewModel() {

//    private val locationRepository = LocationRepository.getInstnace(
//        appContext,
//        Executors.newSingleThreadExecutor()
//    )
//
//    val receivingLocationUpdates: LiveData<Boolean> = locationRepository.receivingLocationUpdates

}