package com.sajeg.storycreator.states

sealed class ActionState {
    data object Speaking: ActionState()
    data object Listening: ActionState()
    data object Thinking: ActionState()
    data object Waiting: ActionState()
    data class Error(val code: Int): ActionState()
}