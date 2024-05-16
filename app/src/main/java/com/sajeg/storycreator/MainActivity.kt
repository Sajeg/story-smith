package com.sajeg.storycreator

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajeg.storycreator.ui.theme.StoryCreatorTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoryCreatorTheme {
                Surface(Modifier.fillMaxSize()) {
                    Main(
                        modifier = Modifier
                            .safeDrawingPadding()
                    )
                }
            }
        }
    }
}


@Composable
private fun Main(modifier: Modifier = Modifier) {
    val history = remember { mutableStateListOf<ChatHistory>() }
    val beginnings = remember { mutableStateListOf<ChatHistory>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
            .fillMaxHeight()
    ) {
        if (history.size == 0 && beginnings.size == 0) {
            StartNewStory(
                onProcessedBeginning = {
                    for (card in it) {
                        if (card.endOfChat) {
                            history.add(card)
                        } else {
                            beginnings.add(card)
                        }
                    }
                }
            )
        } else if (history.size == 0) {
            LazyColumn(
                modifier = modifier.weight(1f),
                verticalArrangement = Arrangement.Top,
                state = listState,
                userScrollEnabled = true
            ) {
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                        text = stringResource(R.string.which_beginning_do_you_prefer),
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                    )
                }
                for (element in beginnings) {
                    item {
                        TextList(element = element, onCardClick = {
                            history.add(it)
                            addSelectedBeginning(it.content)
                        })
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = modifier.weight(1f),
                verticalArrangement = Arrangement.Bottom,
                state = listState,
                userScrollEnabled = true
            ) {
                for (element in history) {
                    item {
                        TextList(element = element)
                    }
                }
                coroutineScope.launch {
                    listState.animateScrollToItem(index = history.size)
                }
            }
            EnterText(
                modifier = modifier.weight(0.1f),
                lastElement = history.lastOrNull(),
                onTextSubmitted = {
                    history.add(ChatHistory("Sajeg", it))
                    action(it, responseFromModel = { response: String, error: Boolean ->
                        if (!error) {
                            val parts = response.split("{", "}")
                            val suggestions = parts[1].split(";").toTypedArray()
                            history.add(
                                ChatHistory(
                                    role = "Gemini",
                                    content = parts[0].trimEnd(),
                                )
                            )
                            history.lastOrNull()?.addSuggestions(suggestions)
                        } else {
                            history.add(
                                ChatHistory(
                                    role = "Gemini",
                                    content = "A error occurred: $response",
                                    endOfChat = true
                                )
                            )
                        }
                    })

                    }
                )
            }
        }
    }

@Composable
fun StartNewStory(
    modifier: Modifier = Modifier,
    onProcessedBeginning: (MutableList<ChatHistory>) -> Unit = {}
) {
    val userName = "Sajeg"
    val element = ChatHistory(role = "Initializer", content = "")
    var enableSelection by remember { mutableStateOf(true) }
    val moods: Array<String> = arrayOf(
        stringResource(R.string.happy),
        stringResource(R.string.dark),
        stringResource(R.string.thrilling),
        stringResource(R.string.funny)
    )
    val places: Array<String> = arrayOf(
        stringResource(R.string.cyberpunk),
        stringResource(R.string.space),
        stringResource(R.string.wild_western),
        stringResource(R.string.city),
        stringResource(R.string.countryside),
        stringResource(R.string.sea)
    )
    val ideas: Array<String> = arrayOf(
        stringResource(R.string.outlaws),
        stringResource(R.string.robots),
        stringResource(R.string.monsters),
        stringResource(R.string.princesses),
        stringResource(R.string.knights),
        stringResource(R.string.unicorns),
        stringResource(R.string.vampires),
        stringResource(R.string.golems),
        stringResource(R.string.fairies),
        stringResource(R.string.slimes),
        stringResource(R.string.vikings),
        stringResource(R.string.dragons),
        stringResource(R.string.magicians),
        stringResource(R.string.superheros),
        stringResource(R.string.aliens),
        stringResource(R.string.pirates)
    )
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )
    val isKeyboardOpen by keyboardAsState()
    val selectedIdeas = remember { mutableStateListOf<String>() }
    val selectedPlaces = remember { mutableStateListOf<String>() }
    val selectedMoods = remember { mutableStateListOf<String>() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.Top
    ) {
        AnimatedVisibility(!isKeyboardOpen) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .padding(top = 40.dp),
                text = stringResource(R.string.hello, userName),
                style = TextStyle(brush = Brush.linearGradient(colors = gradientColors)),
                fontSize = 57.sp,
                lineHeight = 64.sp,
            )
            Text(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .padding(bottom = 10.dp, top = 120.dp),
                text = stringResource(R.string.let_s_create_a_new_story),
                fontSize = 22.sp,
                lineHeight = 28.sp,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (enableSelection) {
                val flowRowModifiers = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 15.dp)
                    .safeDrawingPadding()
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .padding(top = 15.dp)
                                .fillMaxWidth(),
                            text = stringResource(R.string.where_should_the_story_take_place),
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            textAlign = TextAlign.Center
                        )
                        LazyRow(
                            modifier = flowRowModifiers,
                        ) {
                            for (i in 0..places.lastIndex) {
                                val place = places[i]
                                item {
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
                                        label = { Text(text = place) }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 15.dp).padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .padding(top = 15.dp)
                                .fillMaxWidth(),
                            text = stringResource(R.string.what_should_the_story_be_about),
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            textAlign = TextAlign.Center
                        )
                        LazyRow(
                            modifier = flowRowModifiers
                        ) {
                            for (i in 0..ideas.lastIndex) {
                                val idea = ideas[i]
                                item {
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
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 15.dp)
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .padding(top = 15.dp)
                                .fillMaxWidth(),
                            text = stringResource(R.string.what_is_the_mood_of_the_story),
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            textAlign = TextAlign.Center
                        )
                        LazyRow(
                            modifier = flowRowModifiers
                        ) {
                            for (i in 0..moods.lastIndex) {
                                val mood = moods[i]
                                item {
                                    FilterChip(
                                        modifier = modifier.padding(horizontal = 5.dp),
                                        selected = selectedMoods.contains(mood),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                                        ),
                                        onClick = {
                                            if (selectedMoods.contains(mood)) {
                                                selectedMoods.remove(mood)
                                            } else {
                                                selectedMoods.add(mood)
                                            }
                                        },
                                        label = { Text(text = mood) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if(enableSelection) {
            EnterText(lastElement = element, onTextSubmitted = {
                enableSelection = false
                initStoryTelling(
                    theme = "$it, ${selectedIdeas.joinToString()}, ${selectedPlaces.joinToString()}",
                    responseFromModel = { response: MutableList<String>, error: Boolean ->
                        if (!error) {
                            val beginning: MutableList<ChatHistory> = mutableListOf()
                            for (candidate in response) {
                                val parts = candidate.split("{", "}")
                                val suggestions = parts[1].split(";").toTypedArray()
                                beginning.add(
                                    ChatHistory(
                                        role = "Gemini",
                                        content = parts[0].trimEnd(),
                                    )
                                )
                                beginning[beginning.lastIndex].addSuggestions(suggestions)
                            }
                            onProcessedBeginning(beginning)
                        } else {
                            val beginning = ChatHistory(
                                role = "Gemini",
                                content = "An error occurred: $response",
                                endOfChat = true
                            )
                            onProcessedBeginning(mutableListOf(beginning))
                        }
                    })
            })
        }
    }
}

@Composable
fun TextToSpeech(text: String) {
    //TODO
}

@Composable
fun TextList(
    modifier: Modifier = Modifier,
    element: ChatHistory,
    onCardClick: (beginning: ChatHistory) -> Unit = {}
) {
    val colors: CardColors = if (element.isModel() and element.endOfChat) {
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
        onClick = { onCardClick(element) },
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
    lastElement: ChatHistory?,
    onTextSubmitted: (text: String) -> Unit,
) {
    var value by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var enableInput by remember { mutableStateOf(true) }
    val context = LocalContext.current as Activity

    Column {
        if (lastElement != null) {
            if (lastElement.endOfChat) {
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
                .safeDrawingPadding()
        ) {
            if (lastElement != null && lastElement.hasSuggestions()) {
                for (idea in lastElement.getSuggestions()) {
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
                shape = CircleShape,
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
                modifier = Modifier.size(56.dp),

                )

        }
    }
}

@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}
