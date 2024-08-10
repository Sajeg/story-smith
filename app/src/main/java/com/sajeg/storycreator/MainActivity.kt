package com.sajeg.storycreator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.SpeechRecognizer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sajeg.storycreator.ui.theme.StoryCreatorTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

var history: History by mutableStateOf(History("N/A", parts = mutableListOf()))
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permissionAccepted ->
            Log.d("PermissionManager", permissionAccepted.toString())
            if (permissionAccepted) {
                SaveManager.saveInt(
                    "micAllowed",
                    true,
                    this
                )
            } else {
                SaveManager.saveInt(
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
                    navController = rememberNavController()
                    SetupNavGraph(navController = navController)
                    TTS.initTextToSpeech(LocalContext.current)
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        var askForMicPermission by remember { mutableStateOf(false) }
                        var alreadyAsked by remember { mutableStateOf(false) }
                        if (askForMicPermission) {
                            AlertDialog(
                                onDismissRequest = {
                                    SaveManager.saveInt(
                                        "micAllowed",
                                        false,
                                        this
                                    )
                                    askForMicPermission = false
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        askForMicPermission = false
                                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }) {
                                        Text(text = stringResource(R.string.confirm))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        SaveManager.saveInt(
                                            "micAllowed",
                                            false,
                                            this
                                        )
                                        askForMicPermission = false
                                    }) {
                                        Text(text = stringResource(R.string.dismiss))
                                    }
                                },
                                title = {
                                    Text(text = stringResource(R.string.permission_request))
                                },
                                text = {
                                    Text(
                                        text = if (SpeechRecognizer.isOnDeviceRecognitionAvailable(
                                                this
                                            )
                                        ) stringResource(R.string.mic_usage_desc) else stringResource(
                                            R.string.mic_usage_desc_not_device
                                        )
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
        SpeechRecognition.initRecognition(this)
        if (intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_VIEW) {
            Log.d("Import", "Import started")
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            if (uri != null) {
                val context = this
                try {
                    SaveManager.getNewId { id ->
                        val story = ShareChat.importChat(this, uri)!!
                        SaveManager.saveStory(story, id) {
                            navController.navigate(ChatScreen(id = id))
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context, getString(R.string.story_imported),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this, getString(R.string.error_importing_story),
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("ImportChat", "Chat import failed with: $e")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TTS.destroy()
        SpeechRecognition.destroy()
    }
}