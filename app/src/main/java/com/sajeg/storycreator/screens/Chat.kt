package com.sajeg.storycreator.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.sajeg.storycreator.AiCore
import com.sajeg.storycreator.EnterText
import com.sajeg.storycreator.R
import com.sajeg.storycreator.SaveManager
import com.sajeg.storycreator.ShareChat
import com.sajeg.storycreator.StoryPart
import com.sajeg.storycreator.TextList
import com.sajeg.storycreator.history
import com.sajeg.storycreator.initTextToSpeech
import com.sajeg.storycreator.listenToUserInput
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(navController: NavController, prompt: String) {
    val context = LocalContext.current
    val tts = remember { initTextToSpeech(context) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var ttsFinished by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("Story Smith") }
    var readAloud by remember { mutableStateOf(false) }
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            brush = Brush.linearGradient(colors = gradientColors),
                            fontSize = 24.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { ShareChat.exportChat(context, history) },
                        content = {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = stringResource(R.string.open_menu),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                },
                actions = {
                    if (history.parts.size != 0) {
                        var micPermission by remember { mutableStateOf(true) }
                        var pressed by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = {
                                SaveManager.readBoolean("micAllowed", context) { micAllowed ->
                                    if (micAllowed == null) {
                                        return@readBoolean
                                    }
                                    if (readAloud && micAllowed) {
                                        readAloud = false
                                        tts.stop()
                                        history.parts[history.parts.lastIndex].wasReadAloud = false
                                    } else if (micAllowed) {
                                        readAloud = true
                                    } else {
                                        micPermission = false
                                    }
                                    pressed = true
                                }
                            },
                            content = {
                                if (readAloud) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.headset_off),
                                        contentDescription = stringResource(R.string.deactivate_conversation),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.headset),
                                        contentDescription = stringResource(R.string.activate_conversation),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        )
                        if (pressed && ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            )
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            AlertDialog(
                                onDismissRequest = { pressed = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        pressed = false
                                        val intent =
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.fromParts(
                                                    "package",
                                                    context.packageName,
                                                    null
                                                )
                                            }
                                        context.startActivity(intent)
                                    }) {
                                        Text(text = "Settings")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { pressed = false }) {
                                        Text(text = "Dismiss")
                                    }
                                },
                                title = {
                                    Text(text = "Missing Permission")
                                },
                                text = {
                                    Text(text = "To continue you need to give this app permission for accessing the microphone.")
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.mic),
                                        contentDescription = ""
                                    )
                                })
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            // modifier = Modifier.weight(1f),
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.Bottom,
            state = listState,
            userScrollEnabled = true
        ) {
            title = history.title
            for (element in history.parts) {
                item {
                    TextList(element = element, isEnded = history.isEnded)
                }
            }
            coroutineScope.launch {
                listState.animateScrollToItem(index = history.parts.size)
            }
            val lastElement = history.parts[history.parts.lastIndex]
            if (lastElement.isModel() and !lastElement.wasReadAloud and readAloud) {
                ttsFinished = false
                lastElement.wasReadAloud = true
                tts.speak(
                    lastElement.content,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "MODEL_MESSAGE"
                )
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String) {
                        ttsFinished = true
                        Log.d("StorySmithTTS", "Finished")
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                    }

                    override fun onStart(utteranceId: String) {
                    }
                })
            }
            if (ttsFinished and readAloud) {
                ttsFinished = false
                listenToUserInput(context)
            }
        }
        EnterText(
            //modifier = Modifier.weight(0.1f),
            lastElement = history.parts.lastOrNull(),
            isEnded = history.isEnded,
            onTextSubmitted = {
                history.parts.add(StoryPart("Sajeg", it))
                AiCore.action(
                    it,
                    responseFromModel = { response: JSONObject?, error: Boolean, errorDesc: String? ->
                        if (!error) {
                            val story = StoryPart(
                                role = "Gemini",
                                content = response!!.getString("story"),
                            )
                            story.parseSuggestions(response.getJSONArray("suggestions"))
                            history.parts.add(story)
                        } else {
                            history.title = "Error"
                            history.isEnded = true
                            history.parts.add(
                                StoryPart(
                                    role = "Gemini",
                                    content = "A error occurred: $errorDesc",
                                )
                            )
                        }
                    })

            }
        )
    }
}