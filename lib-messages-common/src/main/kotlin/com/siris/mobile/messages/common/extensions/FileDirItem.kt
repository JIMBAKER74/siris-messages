package com.siris.mobile.messages.common.extensions

import android.content.Context
import com.siris.mobile.messages.common.models.FileDirItem

fun FileDirItem.isRecycleBinPath(context: Context): Boolean {
    return path.startsWith(context.recycleBinPath)
}
