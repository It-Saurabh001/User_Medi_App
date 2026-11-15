package com.saurabh.mediuserapp.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.saurabh.mediuserapp.repo.FilePart

/** Reads a picked image/PDF into a [FilePart] (also usable as ImagePart). Null if it cannot be read. */
fun Uri.toFilePart(context: Context): FilePart? {
    val resolver = context.contentResolver
    val bytes = try {
        resolver.openInputStream(this)?.use { it.readBytes() }
    } catch (_: Exception) {
        null
    } ?: return null
    val mime = resolver.getType(this) ?: "application/octet-stream"
    val name = resolver.query(this, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
        if (c.moveToFirst()) c.getString(0) else null
    } ?: "upload"
    return FilePart(name, mime, bytes)
}
