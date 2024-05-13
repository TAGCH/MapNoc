package org.classapp.mapnoc_project

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.classapp.mapnoc_project.ui.theme.MapNoc_ProjectTheme
import java.text.SimpleDateFormat

private fun getEventsFromFirebase(
    onSuccess: (List<Event>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = Firebase.firestore
    db.collection("events").get()
        .addOnSuccessListener { result ->
            val events = result.documents.mapNotNull { document ->
                document.toObject(Event::class.java)
            }
            onSuccess(events)
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

data class Event(
    val mark_name: String = "",
    val mark_description: String = "",
    val time: String = "",
    val location: GeoPoint? = null
)

enum class QueryState {
    Loading, Success, Error
}

@Composable
fun EventItem(event: Event) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.md_theme_background)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(8.dp)
        ) {
            Text(
                text = event.mark_name,
                style = TextStyle(
                    color = colorResource(id = R.color.md_theme_primary),
                    fontSize = 20.sp
                )
            )
            Text(
                text = event.mark_description,
                style = TextStyle(
                    color = colorResource(id = R.color.md_theme_secondary),
                    fontSize = 18.sp
                )
            )
            Row {
                Text(
                    text = "At: ",
                    style = TextStyle(
                        color = colorResource(id = R.color.md_theme_secondary),
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = event.time,
                    style = TextStyle(fontSize = 18.sp)
                )
            }
            event.location?.let { geoPoint ->
                Text(
                    text = "Latitude: ${geoPoint.latitude}, Longitude: ${geoPoint.longitude}",
                    style = TextStyle(
                        color = colorResource(id = R.color.md_theme_secondary),
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
fun EventList(events: List<Event>) {
    LazyColumn(contentPadding = PaddingValues(all = 4.dp)) {
        items(items = events) { event ->
            EventItem(event = event)
        }
    }
}

@Composable
fun AppointmentScreen() {
    val queryState = remember { mutableStateOf(QueryState.Loading) }
    val screenContext = LocalContext.current
    val eventList = remember { mutableStateListOf<Event>() }

    val onFirebaseQueryField = { e: Exception ->
        Toast.makeText(screenContext, e.message, Toast.LENGTH_LONG).show()
    }

    val onFirebaseQuerySuccess = { events: List<Event> ->
        eventList.addAll(events)
        queryState.value = QueryState.Success
    }

    DisposableEffect(Unit) {
        getEventsFromFirebase(onFirebaseQuerySuccess, onFirebaseQueryField)
        onDispose { }
    }

    var showDialog by remember { mutableStateOf(false) }

    MapNoc_ProjectTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "MapNoc", fontSize = 40.sp, fontWeight = FontWeight.Bold)

                // Render UI based on query state
                when (queryState.value) {
                    QueryState.Loading -> {
                        // Show loading indicator
                        Text(text = "Loading...")
                    }
                    QueryState.Success -> {
                        // Show data list
                        EventList(events = eventList)
                    }
                    QueryState.Error -> {
                        // Show error message
                        Text(text = "Error fetching data")
                    }
                }
            }

            // Button for adding new data (Fixed Floating Action Button)
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .padding(start = 300.dp, end = 10.dp, top = 600.dp, bottom = 10.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }

            // Popup dialog for adding new data
            if (showDialog) {
                Dialog(
                    onDismissRequest = { showDialog = false },
                    content = {
                        AddEventDataForm(
                            onDismiss = { showDialog = false },
                            onSubmit = { eventData ->
                                // Add new event data to the list
                                eventList.add(eventData)
                                // Send the data to Firestore
                                addEventDataToFirestore(eventData)
                                // Dismiss the dialog
                                showDialog = false
                            }
                        )
                    }
                )
            }
        }
    }
}



@Composable
fun AddEventDataForm(
    onDismiss: () -> Unit,
    onSubmit: (Event) -> Unit
) {
    var markName by remember { mutableStateOf("") }
    var markDescription by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var latitudeText by remember { mutableStateOf("") }
    var longitudeText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = markName,
                onValueChange = { markName = it },
                label = { Text("Mark Name") }
            )

            TextField(
                value = markDescription,
                onValueChange = { markDescription = it },
                label = { Text("Mark Description") }
            )

            TextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (DD/MM/YYYY)") }
            )

            TextField(
                value = latitudeText,
                onValueChange = { latitudeText = it },
                label = { Text("Latitude") }
            )

            TextField(
                value = longitudeText,
                onValueChange = { longitudeText = it },
                label = { Text("Longitude") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onDismiss
                ) {
                    Text(text = "Cancel")
                }

                Button(
                    onClick = {
                        val latitude = latitudeText.toDoubleOrNull() ?: 0.0
                        val longitude = longitudeText.toDoubleOrNull() ?: 0.0
                        val event = Event(
                            mark_name = markName,
                            mark_description = markDescription,
                            time = date,
                            location = GeoPoint(latitude, longitude)
                        )
                        onSubmit(event)
                    }
                ) {
                    Text(text = "Submit")
                }
            }
        }
    }
}




fun addEventDataToFirestore(eventData: Event) {
    val db = Firebase.firestore
    db.collection("events")
        .add(eventData)
        .addOnSuccessListener { documentReference ->
            // Document added successfully
        }
        .addOnFailureListener { e ->
            // Error adding document
        }
}

