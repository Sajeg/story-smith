package com.sajeg.storycreator.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sajeg.storycreator.ChatScreen
import com.sajeg.storycreator.EnterText
import com.sajeg.storycreator.R
import com.sajeg.storycreator.StoryPart
import com.sajeg.storycreator.history

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Home(navController: NavController) {
    val title by remember { mutableStateOf("Story Smith") }
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
                                        modifier = Modifier.padding(horizontal = 5.dp),
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
                                        modifier = Modifier.padding(horizontal = 5.dp),
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
                    EnterText(lastElement = element, isEnded = history.isEnded, navController = navController, onTextSubmitted = {
                        enableSelection = false
                        navController.navigate(
                            ChatScreen("$it, ${selectedIdeas.joinToString()}, ${selectedPlaces.joinToString()}"))
                    })
                }
            }
        }

    }
}