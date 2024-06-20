package com.sajeg.storycreator

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets


object ShareChat {

    fun importChat(context: Context, uri: Uri): SnapshotStateList<ChatHistory> {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return mutableStateListOf()
        val byteArray = getByteArrayFromInputStream(inputStream)
        val jsonData = Json.parseToJsonElement(String(byteArray, StandardCharsets.UTF_8))
        val output = mutableStateListOf<ChatHistory>()
        if (jsonData is JsonArray) {
            for (message in jsonData.jsonArray) {
                output.add(parseFromJson(message))
            }
        }
        inputStream.close()
        return output
    }

    fun exportChat(context: Context, data: MutableList<ChatHistory>) {
        val tempFile = File(context.cacheDir, "chat.json")
        val outputStream = FileOutputStream(tempFile)
        val modifiedData = toList(data)
        val byteArray = modifiedData.toString().toByteArray()
        Log.d("ExportChat", modifiedData.toString())
        Log.d("ExportChat", modifiedData.toString().toByteArray().toString())
        outputStream.write(byteArray)
        outputStream.close()
        val uri: Uri =
            FileProvider.getUriForFile(context, "com.sajeg.storycreator.fileprovider", tempFile)

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "application/json"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share you Story")
        startActivity(context, shareIntent, null)
    }


    private fun parseFromJson(data: JsonElement): ChatHistory {
        val suggestions = arrayOf("","","")
        for ((i, suggestion) in data.jsonObject["suggestions"]!!.jsonArray.withIndex()){
            Log.d("Suggestions", suggestion.toString())
            suggestions[i] = suggestion.toString().replace('"', ' ').trim()
        }
        return ChatHistory(
            title = data.jsonObject["title"]!!.jsonPrimitive.content,
            role = data.jsonObject["role"]!!.jsonPrimitive.content,
            content = data.jsonObject["content"]!!.jsonPrimitive.content,
            wasReadAloud = data.jsonObject["wasReadAloud"].toString() == "true",
            endOfChat = data.jsonObject["endOfChat"].toString() == "true",
            suggestions = suggestions
        )
    }

    private fun getByteArrayFromInputStream(inputStream: InputStream): ByteArray {
        var bufferSize = 8192
        val byteArrayOutputStream = ByteArrayOutputStream()
        var buffer = ByteArray(bufferSize)
        var bytesRead: Int = inputStream.read(buffer)
        while (bytesRead != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)

            if (bytesRead == bufferSize) {
                bufferSize *= 2
                buffer = ByteArray(bufferSize)
            }
            bytesRead = inputStream.read(buffer)
        }
        return byteArrayOutputStream.toByteArray()
    }

    private fun toList(data: MutableList<ChatHistory>): List<JsonElement> {
        val output: MutableList<JsonElement> = mutableListOf()
        for (obj in data) {
            output.add(obj.toJsonElement())
        }
        return output
    }
}