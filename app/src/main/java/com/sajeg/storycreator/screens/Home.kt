package com.sajeg.storycreator.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sajeg.storycreator.ChatScreen
import com.sajeg.storycreator.EnterText
import com.sajeg.storycreator.R
import com.sajeg.storycreator.SaveManager
import com.sajeg.storycreator.StoryPart
import com.sajeg.storycreator.StoryTitle
import com.sajeg.storycreator.history
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Home(navController: NavController) {
    val title by remember { mutableStateOf("Story Smith") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
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
                Text(text = "History", modifier = Modifier.padding(16.dp), fontSize = 20.sp)
                HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))
                var stories: List<StoryTitle>? by remember { mutableStateOf(null) }
                var isRunning by remember { mutableStateOf(false) }
                LazyColumn {
                    if (stories == null && !isRunning) {
                        isRunning = true
                        SaveManager.getStories { stories = it }
                    } else if (stories != null) {
                        for (story in stories!!) {
                            item {
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
                                    selected = false,
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    onClick = {
                                        navController.navigate(
                                            ChatScreen(
                                                "",
                                                story.id
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
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
                )
            }
        ) { innerPadding ->
            val contentModifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(WindowInsets.ime)

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
                    }
                    LazyRow {
                        for (place in places) {
                            item {
                                SuggestionChip(
                                    modifier = Modifier.padding(5.dp),
                                    onClick = { /*TODO*/ },
                                    label = { Text(text = place) }
                                )
                            }
                        }
                    }
                    EnterText(
                        lastElement = element,
                        isEnded = history.isEnded,
                        navController = navController,
                        onTextSubmitted = {
                            enableSelection = false
                            navController.navigate(
                                ChatScreen("$it, ${selectedIdeas.joinToString()}, ${selectedPlaces.joinToString()}")
                            )
                        }
                    )
                }
            }
        }
    }
}