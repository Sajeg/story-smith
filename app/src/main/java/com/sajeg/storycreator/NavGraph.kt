package com.sajeg.storycreator

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sajeg.storycreator.screens.Chat
import com.sajeg.storycreator.screens.Home
import kotlinx.serialization.Serializable


@Composable
fun SetupNavGraph(
    navController: NavHostController,
) {
    NavHost(navController = navController, startDestination = HomeScreen) {
        composable<HomeScreen> {
            Home(navController = navController)
        }
        composable<ChatScreen> {
            val params = it.toRoute<ChatScreen>()
            Chat(navController, params.prompt)
        }
    }
}

@Serializable
object HomeScreen

@Serializable
data class ChatScreen(
    val prompt: String
)