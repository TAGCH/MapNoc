package org.classapp.mapnoc_project

import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.classapp.mapnoc_project.ui.theme.MapNoc_ProjectTheme


@Composable
fun MapScreen() {
    val screenContext = LocalContext.current
    val locationProvider = LocationServices.getFusedLocationProviderClient(screenContext)
    var latValue:Double? by remember { mutableStateOf(0.0) }

    var lonValue:Double? by remember { mutableStateOf(0.0) }

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

    DisposableEffect(key1 = locationProvider) {
        val permissionStatus = ContextCompat.checkSelfPermission(screenContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            getCurrentUserLocation(locationProvider, locationCallback)
        }
        else {
            permissionDialog.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
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
                Text(text = "Events Near Me.")
                LocationCoordinateDisplay(lat = latValue.toString(), lon = lonValue.toString())
                if (latValue!=null && lonValue!=null)
                    mapDisplay(lat=latValue!!, lon=lonValue!!)
                else mapDisplay()
            } //end column scope
        }
    }
}

private suspend fun CameraPositionState.centerOnLocation(location: LatLng) = animate(
    update = CameraUpdateFactory.newLatLngZoom(location, 13f),
    durationMs = 1500
)

@Composable
fun LocationCoordinateDisplay(lat: String, lon: String) {
    ConstraintLayout (modifier = Modifier
        .fillMaxWidth(1f)
        .padding(all = 8.dp)) {
        val (goBtn, latField, lonField) = createRefs()
        Button(onClick = {/*TODO*/}, modifier = Modifier.constrainAs(goBtn) {
            top.linkTo(parent.top, margin = 8.dp)
            end.linkTo(parent.end, margin = 0.dp)
            width = Dimension.wrapContent
        }) {
            Text(text = "GO")
        }
        OutlinedTextField (value = lat, label = { Text(text = "Latitude") },
            onValueChange = {}, modifier = Modifier.constrainAs(latField) {
                top.linkTo(parent.top, margin = 0.dp)
                start.linkTo(parent.start, margin = 0.dp)
                end.linkTo(goBtn.start, margin = 8.dp)
                width = Dimension.fillToConstraints
            } )
        OutlinedTextField (value = lon, label = { Text(text = "Longitude") },
            onValueChange = {}, modifier = Modifier.constrainAs(lonField) {
                top.linkTo(latField.bottom, margin = 0.dp)
                start.linkTo(parent.start, margin = 0.dp)
                end.linkTo(goBtn.start, margin = 8.dp)
                width = Dimension.fillToConstraints
            } )
    }
}

@Composable
fun mapDisplay(lat:Double = 13.74466, lon:Double = 100.53291,
               zoomLevel:Float = 13f, mapType: MapType = MapType.NORMAL)
{
    val location = LatLng(lat, lon)
    /*
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, zoomLevel)
    }
    */
    val cameraState = rememberCameraPositionState()
    LaunchedEffect(key1 = location) {
        cameraState.centerOnLocation(location)
    }

    GoogleMap (modifier = Modifier.fillMaxSize(),
        properties = MapProperties(mapType = mapType),
        cameraPositionState = cameraState
    ){
        // content inside of map
        Marker( state = MarkerState(position = location),
            title = "You are Here", snippet = "Your Location")
    }
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