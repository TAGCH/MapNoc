package org.classapp.mapnoc_project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class MapNocNavItemInfo (
    val label:String = "",
    val icon:ImageVector = Icons.Filled.Star,
    val route:String = ""
) {
    // Function to manipulate all navigation items
    fun  getAllNavItems() : List<MapNocNavItemInfo> {
        return listOf(
            MapNocNavItemInfo("Appointment", Icons.Filled.DateRange, DestinationScreens.Appointment.route),
            MapNocNavItemInfo("Map", Icons.Filled.LocationOn, DestinationScreens.Map.route),
            MapNocNavItemInfo("Profile", Icons.Filled.AccountCircle, DestinationScreens.Profile.route)
        )
    }
}