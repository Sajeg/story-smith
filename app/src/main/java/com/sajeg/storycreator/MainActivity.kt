package com.sajeg.storycreator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import com.sajeg.storycreator.ui.theme.StoryCreatorTheme

var history: History by mutableStateOf(History("N/A", parts = mutableListOf()))
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permissionAccepted ->
            if (permissionAccepted) {
                SaveManager.saveBoolean(
                    "micAllowed",
                    true,
                    this
                )
            } else {
                SaveManager.saveBoolean(
                    "micAllowed",
                    false,
                    this
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoryCreatorTheme {
                Surface(Modifier.fillMaxSize()) {
                    SaveManager.initDatabase(this)
                    val navController = rememberNavController()
                    SetupNavGraph(navController = navController)

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        var askForMicPermission by remember { mutableStateOf(false) }
                        var alreadyAsked by remember { mutableStateOf(false) }
                        if (askForMicPermission) {
                            AlertDialog(
                                onDismissRequest = {
                                    SaveManager.saveBoolean(
                                        "micAllowed",
                                        false,
                                        this
                                    )
                                    askForMicPermission = false
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        askForMicPermission = false
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }) {
                                        Text(text = "Confirm")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        SaveManager.saveBoolean(
                                            "micAllowed",
                                            false,
                                            this
                                        )
                                        askForMicPermission = false
                                    }) {
                                        Text(text = "Dismiss")
                                    }
                                },
                                title = {
                                    Text(text = "Permission Request")
                                },
                                text = {
                                    Text(
                                        text = "The microphone is used for a more immersive experience. " +
                                                "The app can read the story to you and then you can respond with your voice. " +
                                                "Your voice is then transcribed on the device and then deleted."
                                    )
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.mic),
                                        contentDescription = ""
                                    )
                                }
                            )
                        } else if (!alreadyAsked) {
                            alreadyAsked = true
                            SaveManager.readBoolean("micAllowed", this) { value ->
                                askForMicPermission = value == null
                            }
                        }
                    } else {
                        Log.d("Permission", "Already granted")
                    }
                }
            }
        }
        if (intent.action == Intent.ACTION_SEND) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            if (uri != null) {
                try {
                    history = ShareChat.importChat(this, uri)!!
                    Toast.makeText(
                        this, "Story imported",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this, "Error importing Story",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("ImportChat", "Chat import failed with: $e")
                }
            }
        }
    }
}