package com.sajeg.storycreator


data class SaveManager (
    val userName: String,
    val history: List<List<ChatHistory>>
) {

}

//class AppSettingsSerializer : Serializer<AppSettings> {
//    override fun readFrom(input: InputStream): AppSettings {
//        // ...
//    }
//
//    override fun writeTo(output: OutputStream, value: AppSettings) {
//        // ...
//    }
//}