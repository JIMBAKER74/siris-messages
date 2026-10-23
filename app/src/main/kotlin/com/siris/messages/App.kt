package com.siris.messages

import android.app.Application
import com.siris.mobile.messages.common.extensions.checkUseEnglish

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
    }
}
