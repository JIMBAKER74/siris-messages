package com.siris.mobile.messages.common.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.siris.mobile.lib.smsmms.base.BaseClass
import com.siris.mobile.messages.common.extensions.applicationName

abstract class BaseActivity(var TAG: String) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TAG = "[${applicationContext.applicationName()}][ACTIVITY:$TAG]"
    }

    fun LOG_VERBOSE(message: String) = BaseClass.LOG_VERBOSE(TAG, message)

    fun LOG_DEBUG(message: String) = BaseClass.LOG_DEBUG(TAG, message)

    fun LOG_INFO(message: String) = BaseClass.LOG_INFO(TAG, message)

    fun LOG_WARN(message: String) = BaseClass.LOG_WARN(TAG, message)

    fun LOG_ERROR(message: String) = BaseClass.LOG_ERROR(TAG, message)

    fun LOG_ASSERT(message: String) = BaseClass.LOG_ASSERT(TAG, message)
}
