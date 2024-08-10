package com.sajeg.storycreator.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
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
import com.sajeg.storycreator.getIdeas
import com.sajeg.storycreator.history
import com.sajeg.storycreator.states.HistoryState
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Home(navController: NavController) {
    val title by remember { mutableStateOf("Story Smith") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var stories: List<StoryTitle>? by remember { mutableStateOf(null) }
    var ideas by remember { mutableStateOf(mutableListOf("")) }
    var historyState by remember { mutableStateOf<HistoryState>(HistoryState.Loading) }
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )
    if (ideas[0] == "") {
        ideas = getIdeas(count = 3).toMutableList()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Text(text = "History", modifier = Modifier.padding(16.dp), fontSize = 20.sp)
                HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))
                DisplayHistory(historyState = historyState, id = -1, navController = navController)
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
            if (historyState == HistoryState.Loading) {
                SaveManager.getStories {
                    stories = it
                    historyState = HistoryState.Success(it)
                }
            }
            CreateBackground()
            Column(
                modifier = contentModifier
            ) {
                val element = StoryPart(role = "Initializer", content = "")
                var enableSelection by remember { mutableStateOf(true) }
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
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        val now = System.currentTimeMillis() / 1000

                        if (stories != null && !WindowInsets.isImeVisible) {
                            if (stories!!.size > 2) {
                                for (i in 0..2) {
                                    val story = stories!![2 - i]
                                    val passedTime = now - story.time

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 15.dp, horizontal = 10.dp)
                                            .clickable {
                                                navController.navigate(
                                                    ChatScreen(
                                                        "",
                                                        story.id
                                                    )
                                                )
                                            },
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            Brush.linearGradient(colors = gradientColors)
                                        ),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 30.dp)
                                                .padding(top = 20.dp, bottom = 10.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Text(text = story.title, fontSize = 20.sp)
                                                Row(
                                                    horizontalArrangement = Arrangement.End,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 5.dp)
                                                ) {
                                                    Text(
                                                        text = formatTime(
                                                            passedTime,
                                                            LocalContext.current
                                                        ),
                                                        fontStyle = FontStyle.Italic,
                                                        fontSize = 15.sp,
                                                        textAlign = TextAlign.End
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 15.dp, horizontal = 10.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        Brush.linearGradient(colors = gradientColors)
                                    ),
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .padding(20.dp),
                                        text = stringResource(R.string.start_tipp)
                                    )
                                }
                            }
                        }
                    }
                    element.suggestions = ideas.toTypedArray()
                    EnterText(
                        lastElement = element,
                        end = history.isEnded,
                        navController = navController,
                        colorOverride = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        onTextSubmitted = {
                            enableSelection = false
                            navController.navigate(
                                ChatScreen(it)
                            )
                        }
                    )
                }
            }
        }
    }
}

fun formatTime(time: Long, context: Context): String {
    val minutes = time / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7

    return when {
        weeks > 0 -> context.getString(R.string.weeks, weeks.toString())
        days > 0 -> context.getString(R.string.days, days.toString())
        hours > 0 -> context.getString(R.string.hours, hours.toString())
        minutes > 5 -> context.getString(R.string.minutes, minutes.toString())
        else -> context.getString(R.string.now)
    }
}

@Composable
fun CreateBackground() {
    val screenHeight = LocalConfiguration.current.screenHeightDp.toFloat()
    var text by remember { mutableStateOf("Story Smith ") }
    var textHeight by remember { mutableIntStateOf(0) }

    if (text == "Story Smith ") {
        SaveManager.getStories {
            for (story in it) {
                text += "${story.title} "
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .offset(y = (100).dp)
        .graphicsLayer {
            rotationZ = 45f
            scaleX = 2.5F
            scaleY = 2.5F
        }) {
        Text(
            modifier = Modifier.onGloballyPositioned { textHeight = it.size.height },
            fontSize = 8.sp,
            lineHeight = 12.sp,
            text = text,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        )
        if (textHeight < screenHeight) {
            text += text
        }
    }
}