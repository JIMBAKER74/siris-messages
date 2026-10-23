package com.siris.mobile.messages.common.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.siris.mobile.messages.common.extensions.applyColorFilter
import com.siris.mobile.messages.common.extensions.getContrastColor

class MyFloatingActionButton : FloatingActionButton {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        backgroundTintList = ColorStateList.valueOf(accentColor)
        applyColorFilter(accentColor.getContrastColor())
    }
}
