package com.sajeg.storycreator

import android.Manifest
import android.annotation.SuppressLint
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
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.sajeg.storycreator.ui.theme.StoryCreatorTheme
import kotlinx.coroutines.launch
import org.json.JSONObject

var history: History by mutableStateOf(History("N/A", parts = mutableListOf()))
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}

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
                        SaveManager.readBoolean("micAllowed", this) { value ->
                            if (value == null) {
                                askForMicPermission = true
                            }
                        }
                        if (askForMicPermission) {
                            AlertDialog(
                                onDismissRequest = {
                                    askForMicPermission = false
                                    SaveManager.saveBoolean(
                                        "micAllowed",
                                        false,
                                        this
                                    )
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        askForMicPermission = false
                                        SaveManager.saveBoolean(
                                            "micAllowed",
                                            true,
                                            this
                                        )
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
                        }
                    } else {
                        Log.d("Permission", "Already granted")
                    }
                    Main()
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


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Main() {
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
                    if (history.title != "N/A") {
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
                    }
                },
                actions = {
                    if (history.parts.size != 0) {
                        IconButton(
                            onClick = {
                                if (readAloud) {
                                    readAloud = false
                                    tts.stop()
                                    history.parts[history.parts.lastIndex].wasReadAloud = false
                                } else {
                                    readAloud = true
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
                    }
                }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)

        Column(
            modifier = contentModifier
        ) {
            if (history.parts.size == 0) {
                StartNewStory(
                    onProcessedBeginning = { newHistory ->
                        history = newHistory
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
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
                    modifier = Modifier.weight(0.1f),
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StartNewStory(
    modifier: Modifier = Modifier,
    onProcessedBeginning: (History) -> Unit = {}
) {
    val element = StoryPart(role = "Initializer", content = "")
    var enableSelection by remember { mutableStateOf(true) }
    val places: Array<String> = arrayOf(
        stringResource(R.string.cyberpunk),
        stringResource(R.string.space),
        stringResource(R.string.wild_western),
        stringResource(R.string.city),
        stringResource(R.string.countryside),
        stringResource(R.string.sea),
        stringResource(R.string.fairyland)
    )
    val ideas: Array<String> = arrayOf(
        stringResource(R.string.outlaws),
        stringResource(R.string.robots),
        stringResource(R.string.fairies),
        stringResource(R.string.monsters),
        stringResource(R.string.princesses),
        stringResource(R.string.knights),
        stringResource(R.string.unicorns),
        stringResource(R.string.vampires),
        stringResource(R.string.golems),
        stringResource(R.string.pirates),
        stringResource(R.string.slimes),
        stringResource(R.string.vikings),
        stringResource(R.string.dragons),
        stringResource(R.string.magicians),
        stringResource(R.string.superheros),
        stringResource(R.string.aliens),
    )
    val selectedIdeas = remember { mutableStateListOf<String>() }
    val selectedPlaces = remember { mutableStateListOf<String>() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 60.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (enableSelection) {
                val flowRowModifiers = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 15.dp)
                //item {
                Card(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 50.dp),
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 15.dp)
                            .padding(top = 30.dp)
                            .fillMaxWidth(),
                        text = stringResource(R.string.where_should_the_story_take_place),
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Center
                    )
                    FlowRow(
                        modifier = flowRowModifiers,
                        Arrangement.SpaceBetween
                    ) {
                        for (i in 0..places.lastIndex) {
                            val place = places[i]
                            FilterChip(
                                modifier = modifier.padding(horizontal = 5.dp),
                                selected = selectedIdeas.contains(place),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                                ),
                                onClick = {
                                    if (selectedIdeas.contains(place)) {
                                        selectedIdeas.remove(place)
                                    } else {
                                        selectedIdeas.add(place)
                                    }
                                },
                                label = { Text(text = place) },
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .padding(bottom = 50.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 15.dp)
                            .padding(top = 30.dp)
                            .fillMaxWidth(),
                        text = stringResource(R.string.what_should_the_story_be_about),
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Center
                    )
                    FlowRow(
                        modifier = flowRowModifiers,
                        Arrangement.SpaceBetween
                    ) {
                        for (i in 0..ideas.lastIndex) {
                            val idea = ideas[i]
                            FilterChip(
                                modifier = modifier.padding(horizontal = 5.dp),
                                selected = selectedIdeas.contains(idea),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                                ),
                                onClick = {
                                    if (selectedIdeas.contains(idea)) {
                                        selectedIdeas.remove(idea)
                                    } else {
                                        selectedIdeas.add(idea)
                                    }
                                },
                                label = { Text(text = idea) }
                            )
                        }
                    }
                }
            }
        }
        if (enableSelection) {
            EnterText(lastElement = element, isEnded = history.isEnded, onTextSubmitted = {
                enableSelection = false
                AiCore.initStoryTelling(
                    theme = "$it, ${selectedIdeas.joinToString()}, ${selectedPlaces.joinToString()}",
                    responseFromModel = { response: JSONObject?, error: Boolean, errorDesc: String? ->
                        if (!error) {
                            val story = StoryPart(
                                role = "Gemini",
                                content = response!!.getString("story")
                            )
                            story.parseSuggestions(response.getJSONArray("suggestions"))
                            onProcessedBeginning(
                                History(
                                    title = response.getString("title"),
                                    parts = mutableStateListOf(story)
                                )
                            )
                        } else {
                            val beginning = History(
                                title = "Error",
                                parts = mutableStateListOf(
                                    StoryPart(
                                        role = "Gemini",
                                        content = "An error occurred: $errorDesc",
                                    )
                                ),
                                isEnded = true
                            )
                            onProcessedBeginning(beginning)
                        }
                    })
            })
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