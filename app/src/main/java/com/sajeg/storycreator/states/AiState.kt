package com.sajeg.storycreator.states

sealed class AiState {
    data object Waiting: AiState()
    data object Generating: AiState()
}