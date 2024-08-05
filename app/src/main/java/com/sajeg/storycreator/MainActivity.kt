package com.sajeg.storycreator

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

                    val navController = rememberNavController()
                    SetupNavGraph(navController = navController)
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

fun initTextToSpeech(context: Context): TextToSpeech {
    val tts = TextToSpeech(context) { status ->
        if (status != TextToSpeech.SUCCESS) {
            Log.e("StorySmithTTS", "Error Initializing TTS engine")
        }
    }
    return tts
}

fun listenToUserInput(context: Context) {
    Log.d("StorySmithSTT", "Start Listing")
    val stt = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
    val listener = StoryActionRecognitionListener()
    val intent = Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE)
    intent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
    )
    intent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE,
        AiCore.locale.language + "-" + AiCore.locale.region
    )

    stt.setRecognitionListener(listener)
    stt.startListening(intent)
}

@Composable
fun TextList(
    modifier: Modifier = Modifier,
    element: StoryPart,
    isEnded: Boolean,
) {
    val colors: CardColors = if (element.isModel() and isEnded) {
        CardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            disabledContainerColor = CardDefaults.cardColors().disabledContainerColor,
            disabledContentColor = CardDefaults.cardColors().disabledContentColor
        )
    } else if (element.isModel()) {
        CardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = CardDefaults.cardColors().disabledContainerColor,
            disabledContentColor = CardDefaults.cardColors().disabledContentColor
        )
    } else {
        CardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            disabledContainerColor = CardDefaults.cardColors().disabledContainerColor,
            disabledContentColor = CardDefaults.cardColors().disabledContentColor
        )
    }
    Card(
        content = { Text(modifier = Modifier.padding(15.dp), text = element.content) },
        colors = colors,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
            .padding(top = 10.dp)
    )

}

@Composable
fun EnterText(
    modifier: Modifier = Modifier,
    lastElement: StoryPart?,
    isEnded: Boolean,
    onTextSubmitted: (text: String) -> Unit,
) {
    var value by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var enableInput by remember { mutableStateOf(true) }
    val context = LocalContext.current as Activity

    Column {
        if (lastElement != null) {
            if (isEnded) {
                enableInput = false
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .padding(top = 5.dp),
                    onClick = { context.recreate() },
                    content = { Text(text = stringResource(R.string.new_story)) }
                )
            } else if (!lastElement.isInitializer()) {
                enableInput = lastElement.isModel()
            }
        } else {
            enableInput = false
        }
        LazyRow(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .padding(top = 5.dp)
        ) {
            if (lastElement != null && lastElement.hasSuggestions()) {
                for (idea in lastElement.suggestions) {
                    item {
                        SuggestionChip(
                            modifier = modifier
                                .padding(horizontal = 5.dp),
                            onClick = {
                                focusManager.clearFocus()
                                onTextSubmitted(idea)
                            },
                            label = {
                                Text(text = idea, maxLines = 1)
                            }
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .padding(top = 5.dp, bottom = 15.dp)
                .safeDrawingPadding(),
        ) {
            TextField(
                value = value,
                onValueChange = { value = it },
                label = {
                    if (lastElement != null) {
                        if (lastElement.isInitializer()) {
                            Text(text = stringResource(R.string.what_should_the_story_be_about))
                        } else {
                            Text(stringResource(R.string.what_do_you_want_to_do))
                        }
                    } else {
                        Text(stringResource(R.string.what_do_you_want_to_do))
                    }
                },
                shape = MaterialTheme.shapes.extraLarge,
                singleLine = true,
                enabled = enableInput,
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (value != "" && !lastElement?.isInitializer()!!) {
                        onTextSubmitted(value)
                    } else if (lastElement?.isInitializer()!!) {
                        onTextSubmitted(value)
                    }
                    value = ""
                }
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .padding(end = 10.dp)
                    .weight(0.1f)

            )
            IconButton(
                onClick = {
                    focusManager.clearFocus()
                    if (value != "" && !lastElement?.isInitializer()!!) {
                        onTextSubmitted(value)
                    } else if (lastElement?.isInitializer()!!) {
                        onTextSubmitted(value)
                    }
                    value = ""
                },
                enabled = enableInput,
                colors = IconButtonDefaults.filledIconButtonColors(),
                content = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(34.dp),
                    )
                },
                modifier = Modifier.size(TextFieldDefaults.MinHeight),

                )

        }
    }
}