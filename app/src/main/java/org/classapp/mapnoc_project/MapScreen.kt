package org.classapp.mapnoc_project

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.classapp.mapnoc_project.ui.theme.MapNoc_ProjectTheme
import kotlin.random.Random

private fun getEventsFromFirebase3(
    onSuccess: (List<Event3>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = Firebase.firestore
    db.collection("events").get()
        .addOnSuccessListener { result ->
            val events = result.documents.mapNotNull { document ->
                document.toObject(Event3::class.java)
            }
            onSuccess(events)
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

data class Event3(
    val mark_name: String = "",
    val mark_description: String = "",
    val time: String = "",
    val location: GeoPoint? = null
)

enum class QueryState3 {
    Loading, Success, Error
}

@Composable
fun MapScreen() {
    val screenContext = LocalContext.current
    val locationProvider = LocationServices.getFusedLocationProviderClient(screenContext)
    var latValue:Double? by remember { mutableStateOf(0.0) }

    var lonValue:Double? by remember { mutableStateOf(0.0) }
    var isAddingMarker by remember { mutableStateOf(false) }
    val balooChettanFontFamily = FontFamily(
        Font(R.font.baloo_chettan)
    )

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            latValue = p0.lastLocation?.latitude
            lonValue = p0.lastLocation?.longitude
        }
    }
    val permissionDialog = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                /* Get user location */
                getCurrentUserLocation(locationProvider, locationCallback)
            }
        }
    )
    val queryState = remember { mutableStateOf(QueryState.Loading) }
    val eventList = remember { mutableStateListOf<Event3>() }

    val onFirebaseQueryField3 = { e: Exception ->
        Toast.makeText(screenContext, e.message, Toast.LENGTH_LONG).show()
    }

    val onFirebaseQuerySuccess3 = { events: List<Event3> ->
        eventList.addAll(events)
        queryState.value = QueryState.Success
    }

    DisposableEffect(key1 = locationProvider, Unit) {
        val permissionStatus = ContextCompat.checkSelfPermission(screenContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            getCurrentUserLocation(locationProvider, locationCallback)
        }
        else {
            permissionDialog.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        getEventsFromFirebase3(onFirebaseQuerySuccess3, onFirebaseQueryField3)
        onDispose {
            //remove observer if any
            locationProvider.removeLocationUpdates(locationCallback)
        }
    }
    MapNoc_ProjectTheme {
        Surface (
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column (
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "MapNoc", fontSize = 40.sp, fontWeight = FontWeight.Bold)
                //LocationCoordinateDisplay(lat = latValue.toString(), lon = lonValue.toString())
                if (latValue!=null && lonValue!=null)
                    mapDisplay(lat=latValue!!, lon=lonValue!!, events = eventList)
                else mapDisplay(events = eventList)
            } //end column scope
        }
    }
}

private suspend fun CameraPositionState.centerOnLocation(location: LatLng) = animate(
    update = CameraUpdateFactory.newLatLngZoom(location, 13f),
    durationMs = 1500
)

@Composable
fun mapDisplay(
    lat: Double = 13.74466,
    lon: Double = 100.53291,
    zoomLevel: Float = 13f,
    mapType: MapType = MapType.NORMAL,
    events: List<Event3> = emptyList()
) {
    val location = LatLng(lat, lon)
    val cameraState = rememberCameraPositionState()
    LaunchedEffect(key1 = location) {
        cameraState.centerOnLocation(location)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        properties = MapProperties(mapType = mapType),
        cameraPositionState = cameraState
    ) {
        // content inside of map
        Marker(
            state = MarkerState(position = location),
            title = "You are Here",
            snippet = "Your Location"
        )
        events.forEach { event ->
            event.location?.let { geoPoint ->
                val eventLocation = LatLng(geoPoint.latitude, geoPoint.longitude)
                // val markerColor = getRandomColor()
                // val hue = (markerColor.toArgb() % 360).toFloat()
                // val markerBitmap = BitmapDescriptorFactory.defaultMarker(hue)
                Marker(
                    state = MarkerState(position = eventLocation),
                    title = event.mark_name,
                    snippet = event.time,
                    // icon = markerBitmap // Set the custom marker icon
                )
            }
        }
    }
}

private val markerColors = listOf(
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Yellow,
    Color.Magenta,
    Color.Cyan,
    Color.Gray,
    Color.Black,
    Color.White,
    Color(0xFF800080) // Purple
)

fun getRandomColor(): Color {
    return markerColors.random()
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MapScreenPreview() {
    MapScreen()
}

@SuppressLint("MissingPermission")
private fun getCurrentUserLocation(localProvider: FusedLocationProviderClient,
                                   locationCb : LocationCallback)
{
    val locationReq = LocationRequest.Builder( Priority.PRIORITY_HIGH_ACCURACY, 0).build()
    localProvider.requestLocationUpdates(locationReq, locationCb, null)
}