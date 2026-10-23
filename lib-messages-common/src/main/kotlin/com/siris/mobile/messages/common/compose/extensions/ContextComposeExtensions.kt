package com.siris.mobile.messages.common.compose.extensions

import android.app.Activity
import android.content.Context
import com.siris.mobile.messages.common.R
import com.siris.mobile.messages.common.extensions.baseConfig
import com.siris.mobile.messages.common.extensions.redirectToRateUs
import com.siris.mobile.messages.common.extensions.toast
import com.siris.mobile.messages.common.helpers.BaseConfig

val Context.config: BaseConfig get() = BaseConfig.newInstance(applicationContext)

fun Activity.rateStarsRedirectAndThankYou(stars: Int) {
    if (stars == 5) {
        redirectToRateUs()
    }
    toast(R.string.thank_you)
    baseConfig.wasAppRated = true
}
