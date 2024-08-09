package com.sajeg.storycreator.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.sajeg.storycreator.AiCore
import com.sajeg.storycreator.ChatScreen
import com.sajeg.storycreator.EnterText
import com.sajeg.storycreator.History
import com.sajeg.storycreator.HomeScreen
import com.sajeg.storycreator.R
import com.sajeg.storycreator.SaveManager
import com.sajeg.storycreator.ShareChat
import com.sajeg.storycreator.StoryActionRecognitionListener
import com.sajeg.storycreator.StoryPart
import com.sajeg.storycreator.history
import com.sajeg.storycreator.states.HistoryState
import kotlinx.coroutines.launch
import org.json.JSONObject

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(navController: NavController, prompt: String = "", paramId: Int = -1) {
    val context = LocalContext.current
    val tts = remember { initTextToSpeech(context) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var ttsFinished by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("Story Smith") }
    var id by remember { mutableIntStateOf(-1) }
    var readAloud by remember { mutableStateOf(false) }
    var lastElement by remember { mutableStateOf(StoryPart("Placeholder", "")) }
    var requestOngoing by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var historyState by remember { mutableStateOf<HistoryState>(HistoryState.Loading) }
    var isEditing by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = title,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 32.sp,
                    lineHeight = 32.sp
                )
                HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))
                NavigationDrawerItem(
                    label = {
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.share),
                                contentDescription = ""
                            )
                            Text(
                                text = stringResource(R.string.share),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    },
                    selected = false,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        ShareChat.exportChat(context, history)
                        coroutineScope.launch {
                            drawerState.apply { if (isClosed) open() else close() }
                        }
                    }
                )
                NavigationDrawerItem(
                    label = {
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.edit),
                                contentDescription = ""
                            )
                            Text(
                                text = stringResource(R.string.change_title),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    },
                    selected = false,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        isEditing = true
                        coroutineScope.launch {
                            drawerState.apply { if (isClosed) open() else close() }
                        }
                    }
                )
                NavigationDrawerItem(
                    label = {
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = ""
                            )
                            Text(
                                text = stringResource(R.string.delete),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    },
                    selected = false,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        isDeleting = true
                        coroutineScope.launch {
                            drawerState.apply { if (isClosed) open() else close() }
                        }
                    }
                )
                Text(
                    text = stringResource(R.string.history),
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp
                )
                HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))
                DisplayHistory(historyState, id, navController)
            }
        })
    {
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
                        if (!lastElement.isPlaceholder()) {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    drawerState.apply { if (isClosed) open() else close() }
                                }
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.menu),
                                    contentDescription = "Open drawer"
                                )
                            }
                        }
                    },
                    actions = {
                        if (!lastElement.isPlaceholder()) {
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
                                            history.parts[history.parts.lastIndex].wasReadAloud =
                                                false
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
                                            Text(text = stringResource(R.string.settings))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { pressed = false }) {
                                            Text(text = stringResource(id = R.string.dismiss))
                                        }
                                    },
                                    title = {
                                        Text(text = stringResource(R.string.missing_permission))
                                    },
                                    text = {
                                        Text(text = stringResource(R.string.mic_ask_again))
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
            val contentModifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(WindowInsets.ime)
            if (isEditing) {
                var newTitle by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { isEditing = false },
                    confirmButton = {
                        TextButton(onClick = {
                            SaveManager.changeTitle(id, newTitle); isEditing = false; title =
                            newTitle
                        }) {
                            Text(text = stringResource(id = R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isEditing = false }) {
                            Text(text = stringResource(id = R.string.dismiss))
                        }
                    },
                    text = {
                        TextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text(text = title) },
                            singleLine = true
                        )
                    },
                    title = { Text(text = stringResource(R.string.edit_title)) },
                )
            }
            if (isDeleting) {
                AlertDialog(
                    onDismissRequest = { isDeleting = false },
                    confirmButton = {
                        TextButton(onClick = {
                            navController.navigate(HomeScreen); SaveManager.deleteStory(
                            id
                        )
                        }) {
                            Text(text = stringResource(id = R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isDeleting = false }) {
                            Text(text = stringResource(id = R.string.dismiss))
                        }
                    },
                    text = { Text(text = stringResource(R.string.confirmation_delete)) },
                    title = { Text(text = stringResource(R.string.delete_story)) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = ""
                        )
                    }
                )
            }

            if (lastElement.isPlaceholder()) {
                Column(
                    modifier = contentModifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
                if (!requestOngoing && paramId == -1) {
                    requestOngoing = true
                    SaveManager.getNewId { newId ->
                        AiCore.action(
                            "Start the story with the following places and theme: $prompt " +
                                    "and with the following language: ${Locale.current}",
                            responseFromModel = { response: JSONObject?, error: Boolean, errorDesc: String? ->
                                if (!error) {
                                    val story = StoryPart(
                                        role = "Gemini",
                                        content = response!!.getString("story"),
                                    )
                                    story.parseSuggestions(response.getJSONArray("suggestions"))
                                    history = History(
                                        title = response.getString("title"),
                                        parts = mutableStateListOf(story)
                                    )
                                    title = history.title
                                    lastElement = story
                                    SaveManager.saveStory(history, newId) {
                                        SaveManager.getStories { historyState = HistoryState.Success(it) }
                                    }
                                } else {
                                    val story = StoryPart(
                                        role = "Gemini",
                                        content = "A error occurred: $errorDesc",
                                    )
                                    history.title = "Error"
                                    history.isEnded = true
                                    history.parts.add(story)
                                    lastElement = story
                                    SaveManager.getStories { HistoryState.Success(it) }
                                }
                                requestOngoing = false
                                id = newId
                            }
                        )
                    }
                } else if (!requestOngoing && paramId > 0) {
                    id = paramId
                    requestOngoing = true
                    SaveManager.loadStory(id) { newHistory ->
                        history = newHistory
                        lastElement = history.parts.last()
                        title = newHistory.title
                        requestOngoing = false
                    }
                    SaveManager.getStories { historyState = HistoryState.Success(it) }
                }
            } else {
                Column(
                    modifier = contentModifier
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f),
                        verticalArrangement = Arrangement.Bottom,
                        state = listState,
                        userScrollEnabled = true
                    ) {
                        for (element in history.parts) {
                            item {
                                TextList(element = element, isEnded = history.isEnded)
                            }
                        }
                        coroutineScope.launch {
                            listState.animateScrollToItem(index = history.parts.size)
                        }
                        if (lastElement.isModel() and !lastElement.wasReadAloud and readAloud) {
                            ttsFinished = false
                            lastElement.wasReadAloud = true
                            tts.speak(
                                lastElement.content,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "MODEL_MESSAGE"
                            )
                            tts.setOnUtteranceProgressListener(object :
                                UtteranceProgressListener() {
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
                        modifier = Modifier.weight(0.1f),
                        lastElement = history.parts.lastOrNull(),
                        isEnded = history.isEnded,
                        navController = navController,
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
                                        SaveManager.saveStory(history, id) {}
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
                                }
                            )
                        }
                    )
                }
            }
        }
    }
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
fun DisplayHistory(historyState: HistoryState, id: Int, navController: NavController) {
    LazyColumn {
        item {
            NavigationDrawerItem(
                label = {
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = ""
                        )
                        Text(
                            text = stringResource(id = R.string.new_story),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                },
                selected = false,
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = {
                    navController.navigate(HomeScreen)
                }
            )
        }
        when (historyState) {
            is HistoryState.Loading -> {
                Log.d("HistoryState", "Changed to Loading")
                item {
                    NavigationDrawerItem(label = { Text(text = stringResource(R.string.loading), fontStyle = FontStyle.Italic) }, selected = false, onClick = {})
                }
            }

            is HistoryState.Error -> {
                item {
                    NavigationDrawerItem(label = { Text(text = "Unknown error occurred")}, selected = false, onClick = {})
                }
            }

            is HistoryState.Success -> {
                Log.d("HistoryState", "Changed to Success")
                items(historyState.stories) { story ->
                    NavigationDrawerItem(
                        label = {
                            Row {
                                Icon(
                                    painter = painterResource(id = R.drawable.book),
                                    contentDescription = ""
                                )
                                Text(
                                    text = story.title,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        },
                        selected = story.id == id,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        onClick = { navController.navigate(ChatScreen(id = story.id)) }
                    )
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
        Locale.current.language + "-" + Locale.current.region
    )

    stt.setRecognitionListener(listener)
    stt.startListening(intent)
}