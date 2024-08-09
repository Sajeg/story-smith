package com.sajeg.storycreator.states

sealed class StoryState {
    data object Loading: StoryState()
    data object Response: StoryState()
    data object Error: StoryState()
    data object Idle: StoryState()
}