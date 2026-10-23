package com.siris.mobile.messages.common.interfaces

import androidx.biometric.auth.AuthPromptHost
import com.siris.mobile.messages.common.views.MyScrollView

interface SecurityTab {
    fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    )

    fun visibilityChanged(isVisible: Boolean)
}
