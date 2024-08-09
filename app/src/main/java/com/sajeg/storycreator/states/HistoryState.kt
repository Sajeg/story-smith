package com.sajeg.storycreator.states

import com.sajeg.storycreator.StoryTitle

sealed class HistoryState {
    data object Loading: HistoryState()
    data class Success(val stories: List<StoryTitle>): HistoryState()
    data object Error: HistoryState()
}