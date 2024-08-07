package com.sajeg.storycreator.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.rememberTextMeasurer
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
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Home(navController: NavController) {
    val title by remember { mutableStateOf("Story Smith") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var stories: List<StoryTitle>? by remember { mutableStateOf(null) }
    var isRunning by remember { mutableStateOf(false) }
    val ideas = getIdeas(count = 5)
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
                        if (stories != null) {
                            if (stories!!.size > 2) {
                                for (i in 0..2) {
                                    val story = stories!![2 - i]
                                    val passedTime = now - story.time

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(15.dp)
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
                                                        text = formatTime(passedTime),
                                                        fontStyle = FontStyle.Italic,
                                                        fontSize = 15.sp,
                                                        textAlign = TextAlign.End
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LazyRow(
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        for (idea in ideas) {
                            item {
                                SuggestionChip(
                                    modifier = Modifier.padding(5.dp),
                                    onClick = { navController.navigate(ChatScreen(idea)) },
                                    label = { Text(text = idea) }
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
                                ChatScreen(it)
                            )
                        }
                    )
                }
            }
        }
    }
}

fun formatTime(time: Long): String {
    val minutes = time / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7

    return when {
        weeks > 0 -> "$weeks weeks ago"
        days > 0 -> "$days days ago"
        hours > 0 -> "$hours hours ago"
        minutes > 5 -> "$minutes minutes ago"
        else -> "now"
    }
}

@Composable
fun CreateBackground() {
    val height = LocalConfiguration.current.screenHeightDp.toFloat()
    val width = LocalConfiguration.current.screenWidthDp.toFloat()
    val text = "The butterfly"
    val density = LocalDensity.current
    val colors = MaterialTheme.colorScheme.onBackground
    val fontSize = 15.sp
//    Box(modifier = Modifier.rotate(0f)) {
//        Text(
//            text = "The iridescent dragonfly hovered, a jewel suspended in the amber light filtering through the canopy, below, a labyrinth of ferns unfurled, their fronds whispering secrets to the wind, a solitary deer emerged from the mist, its eyes twin pools of ancient wisdom, the forest held its breath, a hushed cathedral of green, far above, a hawk circled, its keen gaze scanning the emerald expanse, a flash of crimson disrupted the tranquility as a squirrel darted up a towering oak, its bushy tail a blur of defiance, a family of badgers, their fur matted with dew, emerged from their burrow, their noses twitching with anticipation, in a secluded glade, a crystalline stream meandered, its surface rippled by the gentle caress of a breeze, a solitary frog perched on a lily pad, its bulging eyes reflecting the world above, the sweet scent of wildflowers filled the air, a heady perfume that lured a butterfly from its slumber, as the sun began its descent, casting long shadows across the forest floor, a symphony of chirps and rustles erupted, owls hooted their nocturnal greeting, their haunting melodies echoing through the trees, a lone wolf emerged from the twilight, its silhouette a dark specter against the fading light, the forest was a world unto itself, a realm of magic and mystery, it cradled life in all its forms, a sanctuary for the wild and free, as darkness enveloped the land, the forest awakened to a different rhythm, a world of shadows and secrets, the old oak stood sentinel, its gnarled branches reaching towards the starlit sky, it had witnessed centuries of change, its heartwood a repository of memories, with the first light of dawn, the forest would stir once more, its inhabitants emerging to greet a new day, and so, the cycle of life continued, an endless tapestry woven with threads of green and gold.",
//            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
//        )
//    }
    val textMeasurer = rememberTextMeasurer()
    val rotationAngle: Float = 45f
    val textStyle = TextStyle(fontSize = 24.sp, color = colors)

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val textLayoutResult = textMeasurer.measure(text, textStyle)
            val textSize = textLayoutResult.size

            val diagonal = sqrt(canvasWidth * canvasWidth + canvasHeight * canvasHeight)

            val repeatX = (diagonal / textSize.width).toInt() + 1
            val repeatY = (diagonal / textSize.height).toInt() + 1
//            rotate(rotationAngle) {
//                        drawText(
//                            textMeasurer = textMeasurer,
//                            text = text,
//                            style = textStyle,
//                            topLeft = Offset(
//                                x = -1000f,
//                                y = 180f
//                            )
//                        )
//                    }
//            rotate(rotationAngle) {
//                drawText(
//                    textMeasurer = textMeasurer,
//                    text = text,
//                    style = textStyle,
//                    topLeft = Offset(
//                        x = -1000f,
//                        y = 2000f
//                    )
//                )
//            }
//            rotate(rotationAngle) {
//                drawText(
//                    textMeasurer = textMeasurer,
//                    text = text,
//                    style = textStyle,
//                    topLeft = Offset(
//                        x = -1000f,
//                        y = 2000f
//                    )
//                )
//            }
            for (i in 0..repeatY) {
                for (j in 0..repeatX) {
                    rotate(rotationAngle) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = text,
                            style = textStyle,
                            topLeft = Offset(
                                x = j * textSize.width.toFloat(),
                                y = i * textSize.height.toFloat()
                            )
                        )
                    }
                }
            }
        }
    }
}