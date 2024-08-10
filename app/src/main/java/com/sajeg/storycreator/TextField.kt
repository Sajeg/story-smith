package com.sajeg.storycreator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ChipColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun EnterText(
    modifier: Modifier = Modifier,
    lastElement: StoryPart?,
    end: Boolean,
    navController: NavController,
    colorOverride: ChipColors? = null,
    isActive: Boolean = true,
    onTextSubmitted: (text: String) -> Unit,
) {
    var value by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var isEnded by remember { mutableStateOf(end) }
    var enableInput by remember { mutableStateOf(isActive) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (lastElement != null) {
            if (isEnded) {
                if (navController.currentDestination!!.route != "com.sajeg.storycreator.HomeScreen") {
                    enableInput = false
                    Button(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp)
                            .padding(top = 5.dp),
                        onClick = { navController.navigate(HomeScreen); isEnded = false },
                        content = { Text(text = stringResource(R.string.new_story)) }
                    )
                }
            } else if (!lastElement.isInitializer()) {
                enableInput = lastElement.isModel()
            }
        } else {
            enableInput = false
        }

        if (!enableInput && !isEnded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                LinearProgressIndicator(modifier.fillMaxWidth())
            }
        }

        if (lastElement != null && lastElement.hasSuggestions()) {
            LazyRow(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .padding(top = 5.dp)
            ) {
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
                            },
                            colors = colorOverride ?: SuggestionChipDefaults.suggestionChipColors()
                        )
                    }
                }
            }
        }
        TextField(
            value = value,
            onValueChange = { value = it },
            placeholder = {
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
                .padding(horizontal = 15.dp)
                .padding(top = 5.dp, bottom = 15.dp)
                .height(52.dp)
                .safeDrawingPadding()
                .fillMaxWidth(),
            trailingIcon = {
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
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.send),
                            contentDescription = "Send"
                        )
                    })
            }

        )

    }
}