package org.classapp.mapnoc_project

import android.os.Bundle
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.classapp.mapnoc_project.ui.theme.MapNoc_ProjectTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapNoc_ProjectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.wrapContentSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreenWithBottomNavBar()
                }
            }
        }
        Toast.makeText(this, "Welcome to MapNoc!!", Toast.LENGTH_LONG).show()
        schedulePeriodicWork()
    }
    private fun schedulePeriodicWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<NotificationsHelper>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(periodicWorkRequest)
    }
}


@Composable
fun MainScreenWithBottomNavBar() {
    val context = LocalContext.current
    val navController = rememberNavController()
    var navSelectedItem by remember {
        mutableStateOf(1)
    }
    Scaffold (bottomBar = {
        NavigationBar {
            MapNocNavItemInfo().getAllNavItems().forEachIndexed { index, itemInfo ->
                NavigationBarItem(selected = (index==navSelectedItem),
                    onClick = {
                        navSelectedItem = index
                        navController.navigate(itemInfo.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(imageVector = itemInfo.icon, contentDescription = itemInfo.label) },
                    label = { Text(text = itemInfo.label)})
            }
        }
    }) {paddingValues -> NavHost(navController = navController,
        startDestination = DestinationScreens.Map.route,
        modifier = Modifier.padding(paddingValues)) {
        // Navigation Builder
        composable(route = DestinationScreens.Appointment.route) {
            AppointmentScreen()
        }
        composable(route = DestinationScreens.Map.route) {
            MapScreen()
        }
        composable(route = DestinationScreens.Profile.route) {
            ProfileScreen(context)
        }
    }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Button(onClick = { /*TODO*/ }) {
        Text(text = "Click")
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MapNoc_ProjectTheme {
        MainScreenWithBottomNavBar()
    }
}
