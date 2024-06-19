package com.sajeg.storycreator

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import kotlinx.serialization.json.JsonElement
import java.io.File
import java.io.FileOutputStream


object ShareChat {

    fun importChat() {
        //ToDo
    }

    fun exportChat(context: Context, data: MutableList<ChatHistory>) {
        val tempFile = File(context.cacheDir, "chat.json")
        val outputStream = FileOutputStream(tempFile)
        val modifiedData = toList(data)
        val byteArray = modifiedData.toString().toByteArray()
        Log.d("ExportChat", modifiedData.toString())
        Log.d("ExportChat", modifiedData.toString().toByteArray().toString())
        //outputStream.close()
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

    private fun toList(data: MutableList<ChatHistory>): List<JsonElement> {
        val output: MutableList<JsonElement> = mutableListOf()
        for(obj in data){
            output.add(obj.toJsonElement())
        }
        return output
    }
}