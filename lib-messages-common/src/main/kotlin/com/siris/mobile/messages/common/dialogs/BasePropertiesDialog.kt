package com.siris.mobile.messages.common.dialogs

import android.app.Activity
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.siris.mobile.messages.common.R
import com.siris.mobile.messages.common.databinding.DialogPropertiesBinding
import com.siris.mobile.messages.common.databinding.ItemPropertyBinding
import com.siris.mobile.messages.common.extensions.copyToClipboard
import com.siris.mobile.messages.common.extensions.getProperTextColor
import com.siris.mobile.messages.common.extensions.showLocationOnMap
import com.siris.mobile.messages.common.extensions.value

abstract class BasePropertiesDialog(activity: Activity) {
    protected val mInflater: LayoutInflater
    protected val mPropertyView: ViewGroup
    protected val mResources: Resources
    protected val mActivity: Activity = activity
    protected val mDialogView: DialogPropertiesBinding

    init {
        mInflater = LayoutInflater.from(activity)
        mResources = activity.resources
        mDialogView = DialogPropertiesBinding.inflate(mInflater, null, false)
        mPropertyView = mDialogView.propertiesHolder
    }

    protected fun addProperty(labelId: Int, value: String?, viewId: Int = 0) {
        if (value == null) {
            return
        }

        ItemPropertyBinding.inflate(mInflater, null, false).apply {
            propertyValue.setTextColor(mActivity.getProperTextColor())
            propertyLabel.setTextColor(mActivity.getProperTextColor())

            propertyLabel.text = mResources.getString(labelId)
            propertyValue.text = value
            mPropertyView.findViewById<LinearLayout>(R.id.properties_holder).addView(root)

            root.setOnLongClickListener {
                mActivity.copyToClipboard(propertyValue.value)
                true
            }

            if (labelId == R.string.gps_coordinates) {
                root.setOnClickListener {
                    mActivity.showLocationOnMap(value)
                }
            }

            if (viewId != 0) {
                root.id = viewId
            }
        }
    }
}
