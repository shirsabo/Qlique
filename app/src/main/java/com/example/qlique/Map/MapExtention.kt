package com.example.qlique.Map

import android.content.ContentValues.TAG
import android.util.Log
import androidx.fragment.app.DialogFragment.STYLE_NORMAL
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


const val TASK_AWAIT = 120L
const val MAP_CAMERA_ZOOM = 11f
const val MAP_CAMERA_ZOOM_INT = 11

/**
 * This fun is used to move map
 * @receiver GoogleMap
 * @param latLng LatLng?
 */
fun GoogleMap.moveCameraOnMap(latLng: LatLng?) {
    this.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_CAMERA_ZOOM))
}

/**
 * This fun is used to move map
 * @receiver GoogleMap
 * @param latLng LatLng?
 */
fun GoogleMap.moveCameraOnMapBound(latLng: LatLngBounds?) {
    this.animateCamera(CameraUpdateFactory.newLatLngBounds(latLng, MAP_CAMERA_ZOOM_INT))
}

/**
 * This fun is used to get auto complete fields
 * @param mGeoDataClient GeoDataClient
 * @param constraint CharSequence
 * @return ArrayList<AutocompletePrediction>?
 */
fun getAutocomplete(mPlacesClient: PlacesClient, constraint: CharSequence): List<AutocompletePrediction> {
    var list = listOf<AutocompletePrediction>()
    val token = AutocompleteSessionToken.newInstance()
    val request = FindAutocompletePredictionsRequest.builder().setTypeFilter(TypeFilter.CITIES).setSessionToken(token).setQuery(constraint.toString()).build()
    val prediction = mPlacesClient.findAutocompletePredictions(request)
    val autocompletePredictions: Task<FindAutocompletePredictionsResponse> =
        mPlacesClient.findAutocompletePredictions(request)
    try {
        Tasks.await(autocompletePredictions, TASK_AWAIT, TimeUnit.SECONDS)
    } catch (e: ExecutionException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    } catch (e: TimeoutException) {
        e.printStackTrace()
    }

    if (prediction.isSuccessful) {
        val findAutocompletePredictionsResponse = prediction.result
        findAutocompletePredictionsResponse.let {
            list = findAutocompletePredictionsResponse.autocompletePredictions
        }
        return list
    }
    return list
}
/*
private fun getPredictions(mPlacesClient: PlacesClient, constraint: CharSequence): ArrayList<PlaceAutocomplete>? {
    val resultList: ArrayList<PlaceAutocomplete> = ArrayList()

    // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
    // and once again when the user makes a selection (for example when calling fetchPlace()).
    val token = AutocompleteSessionToken.newInstance()

    //https://gist.github.com/graydon/11198540
    // Use the builder to create a FindAutocompletePredictionsRequest.
    val request =
        FindAutocompletePredictionsRequest.builder() // Call either setLocationBias() OR setLocationRestriction().
            //.setLocationBias(bounds)
            //.setCountry("BD")
            //.setTypeFilter(TypeFilter.ADDRESS)
            .setSessionToken(token)
            .setQuery(constraint.toString())
            .build()
    val autocompletePredictions: Task<FindAutocompletePredictionsResponse> =
        mPlacesClient.findAutocompletePredictions(request)

    // This method should have been called off the main UI thread. Block and wait for at most
    // 60s for a result from the API.
    try {
        Tasks.await(
            autocompletePredictions,
            60,
            TimeUnit.SECONDS
        )
    } catch (e: ExecutionException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    } catch (e: TimeoutException) {
        e.printStackTrace()
    }
    return if (autocompletePredictions.isSuccessful) {
        val findAutocompletePredictionsResponse =
            autocompletePredictions.result
        if (findAutocompletePredictionsResponse != null) for (prediction in findAutocompletePredictionsResponse.autocompletePredictions) {
            Log.i(TAG, prediction.placeId)
            resultList.add(
                PlaceAutocomplete(
                    prediction.placeId,
                    prediction.getPrimaryText(STYLE_NORMAL).toString(),
                    prediction.getFullText(STYLE_BOLD).toString()
                )
            )
        }
        resultList
    } else {
        resultList
    }
}
*/