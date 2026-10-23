package com.siris.mobile.messages.common.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.siris.mobile.messages.common.R
import com.siris.mobile.messages.common.compose.extensions.enableEdgeToEdgeSimple
import com.siris.mobile.messages.common.compose.screens.donation.DonationScreen
import com.siris.mobile.messages.common.compose.screens.donation.SirisCryptoAddresses
import com.siris.mobile.messages.common.compose.screens.donation.SirisDonationPlatforms
import com.siris.mobile.messages.common.compose.theme.AppThemeSurface
import com.siris.mobile.messages.common.extensions.openWebsiteIntent
import com.siris.mobile.messages.common.extensions.toast

class DonationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            val clipboardManager = LocalClipboardManager.current
            AppThemeSurface {
                DonationScreen(
                    donationOptions = SirisDonationPlatforms,
                    cryptoAddresses = SirisCryptoAddresses,
                    goBack = ::finish,
                    openWebsite = ::openWebsiteIntent,
                    copyToClipboard = {
                        clipboardManager.setText(AnnotatedString(it))
                        toast(R.string.value_copied_to_clipboard)
                    },
                )
            }
        }
    }
}
