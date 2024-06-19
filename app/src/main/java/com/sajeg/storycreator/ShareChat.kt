package com.sajeg.storycreator

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import java.io.File

object ShareChat {

    fun importChat() {
        //ToDo
    }

    fun exportChat(context: Context, data: MutableList<ChatHistory>) {
        val tempFile = File(context.cacheDir, "chat.json")
        val uri: Uri = Uri.fromFile(tempFile)

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/json"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share you Story")
        startActivity(context, shareIntent, null)
    }
}