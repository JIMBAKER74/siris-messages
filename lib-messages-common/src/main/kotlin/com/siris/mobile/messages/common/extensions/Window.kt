package com.siris.mobile.messages.common.extensions

import android.view.View
import android.view.Window
import com.siris.mobile.messages.common.helpers.DARK_GREY
import com.siris.mobile.messages.common.extensions.isOreoPlus

fun Window.updateNavigationBarColors(backgroundColor: Int) {
    navigationBarColor = backgroundColor
    updateNavigationBarForegroundColor(backgroundColor)
}

fun Window.updateNavigationBarForegroundColor(backgroundColor: Int) {
    if (isOreoPlus()) {
        if (backgroundColor.getContrastColor() == DARK_GREY) {
            decorView.systemUiVisibility = decorView.systemUiVisibility.addBit(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        } else {
            decorView.systemUiVisibility = decorView.systemUiVisibility.removeBit(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        }
    }
}
