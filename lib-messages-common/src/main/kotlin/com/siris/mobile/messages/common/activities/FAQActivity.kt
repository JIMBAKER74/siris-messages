package com.siris.mobile.messages.common.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.toImmutableList
import com.siris.mobile.messages.common.compose.extensions.enableEdgeToEdgeSimple
import com.siris.mobile.messages.common.compose.screens.FAQScreen
import com.siris.mobile.messages.common.compose.theme.AppThemeSurface
import com.siris.mobile.messages.common.helpers.APP_FAQ
import com.siris.mobile.messages.common.models.FAQItem

class FAQActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            AppThemeSurface {
                val faqItems = remember { intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem> }
                FAQScreen(
                    goBack = ::finish,
                    faqItems = faqItems.toImmutableList()
                )
            }
        }
    }
}
