package org.classapp.mapnoc_project

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import org.classapp.mapnoc_project.ui.theme.MapNoc_ProjectTheme

@Composable
fun ProfileScreen(context: Context) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

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

                Spacer(modifier = Modifier.height(250.dp))

                currentUser?.let {
                    Text(text = "Email: ${it.email}")

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        auth.signOut()
                        context.startActivity(Intent(context, LogInViewActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }) {
                        Text("SIGN OUT")
                    }
                }
            }
        }
    }
}

/*
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    // Pass a context for preview purposes
    ProfileScreen(Context())
}*/
