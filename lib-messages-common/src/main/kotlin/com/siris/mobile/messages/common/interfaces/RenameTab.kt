package com.siris.mobile.messages.common.interfaces

import com.siris.mobile.messages.common.activities.BaseSimpleActivity

interface RenameTab {
    fun initTab(activity: BaseSimpleActivity, paths: ArrayList<String>)

    fun dialogConfirmed(useMediaFileExtension: Boolean, callback: (success: Boolean) -> Unit)
}
