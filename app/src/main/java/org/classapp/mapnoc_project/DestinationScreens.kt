package org.classapp.mapnoc_project

sealed class DestinationScreens (val route: String) {
    object Appointment : DestinationScreens("Appointment")
    object Map : DestinationScreens("Map")
    object Profile : DestinationScreens("Profile")
}