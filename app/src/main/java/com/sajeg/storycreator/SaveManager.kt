package com.sajeg.storycreator



class SaveManager (
    val uniqueId: Int,
    val lastEdited: Long = System.currentTimeMillis(),
    val userName: String,
    val content: List<ChatHistory>
) {

}