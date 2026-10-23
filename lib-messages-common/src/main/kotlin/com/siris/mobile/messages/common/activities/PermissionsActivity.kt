package com.siris.mobile.messages.common.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.siris.mobile.messages.common.extensions.hasAllPermissions
import com.siris.mobile.messages.common.extensions.hasPermission
import com.siris.mobile.messages.common.extensions.isTiramisuPlus
import com.siris.mobile.messages.common.extensions.isUpsideDownCakePlus
import kotlinx.coroutines.flow.MutableStateFlow

abstract class PermissionsActivity(
    TAG: String = "${PermissionsActivity::class.simpleName}",
    private val defaultPermissions: MutableList<String> = mutableListOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.RECEIVE_MMS,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.READ_SYNC_SETTINGS
    ),
    private val killOnPermissionDenied: Boolean = true
) : BaseActivity(TAG) {

    /**
     * Any permission requested will use this code. There is currently no need for unique request codes.
     */
    val GENERIC_PERMISSIONS_REQUEST_CODE = 100

    /**
     * Callback invoked when all permissions inside [defaultPermissions] have been granted. This callback is invoked
     */
    var onAllPermissionsGranted: (() -> Unit)? = null

    var onGrantResultCallback: ((granted: Boolean) -> Unit)? = null

    val _isRequestingPermissions = MutableStateFlow(false)

    init {
        if (isTiramisuPlus()) {
            defaultPermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            defaultPermissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            defaultPermissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            defaultPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            defaultPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            defaultPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (isUpsideDownCakePlus()) {
            defaultPermissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        }
    }

    override fun onResume() {
        super.onResume()
        requestPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        _isRequestingPermissions.value = false

        if (requestCode != GENERIC_PERMISSIONS_REQUEST_CODE || grantResults.isNotEmpty()) return
        LOG_DEBUG("onRequestPermissionsResult(requestCode=$requestCode, permissions=${permissions[0]}, grantResults=${grantResults[0]})")

        val granted = (PackageManager.PERMISSION_GRANTED == grantResults[0])
        if (!granted) {
            if (killOnPermissionDenied) {
                // TODO: Show Dialog letting user know that this permission is required and what exactly it is used for.
                finish()
            } else {
                onGrantResultCallback?.invoke(false)
            }
        } else {
            onGrantResultCallback?.invoke(true)

            // Regardless of who requested for what permission, always check to see if there are any default permissions not yet granted and request for them.
            val allDefaultPermissionsGranted = defaultPermissions.all { isPermissionGranted(it) }
            if (!allDefaultPermissionsGranted)
                requestPermissions()
            else onAllPermissionsGranted?.invoke()
        }
    }

    fun handlePartialMediaPermissions(permissionIds: Collection<String>, force: Boolean = false, callback: (granted: Boolean) -> Unit) {
        onGrantResultCallback = null
        if (isUpsideDownCakePlus()) {
            if (hasPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) && !force) {
                callback(true)
            } else {
                onGrantResultCallback = callback
                _isRequestingPermissions.value = true
                ActivityCompat.requestPermissions(this, permissionIds.toTypedArray(), GENERIC_PERMISSIONS_REQUEST_CODE)
            }
        } else {
            if (hasAllPermissions(permissionIds)) {
                callback(true)
            } else {
                onGrantResultCallback = callback
                _isRequestingPermissions.value = true
                ActivityCompat.requestPermissions(this, permissionIds.toTypedArray(), GENERIC_PERMISSIONS_REQUEST_CODE)
            }
        }
    }

    fun handleNotificationPermission(callback: (granted: Boolean) -> Unit) {
        if (!isTiramisuPlus()) {
            callback(true)
        } else {
            requestPermission(Manifest.permission.POST_NOTIFICATIONS, onGrantResult = { result ->
                callback(result)
            })
        }
    }

    fun isPermissionGranted(permission: String) = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    fun requestPermissions() {
        if (_isRequestingPermissions.value) {
            LOG_DEBUG("requestPermissions(): Request already ongoing, skipping this call...")
            return
        }
        val allDefaultPermissionsGranted = defaultPermissions.all { isPermissionGranted(it) }
        if (allDefaultPermissionsGranted) {
            LOG_DEBUG("requestPermissions(): All default permissions granted")
            onAllPermissionsGranted?.invoke()
        } else {
            defaultPermissions.forEach { permission ->
                if (requestPermission(permission)) return
            }
        }
    }

    /**
     * If [permission] has not yet been granted, will make a request to the system for that permission.
     *
     * @return True if a request was made, false otherwise.
     */
    fun requestPermission(permission: String, onGrantResult: ((granted: Boolean) -> Unit)? = null): Boolean {
        if (isPermissionGranted(permission)) {
            LOG_DEBUG("requestPermission(permission=$permission): GRANTED")
            onGrantResult?.invoke(true)
        } else {
            LOG_DEBUG("requestPermission(permission=$permission): REQUESTING")
            onGrantResultCallback = onGrantResult
            _isRequestingPermissions.value = true
            ActivityCompat.requestPermissions(this, arrayOf(permission), GENERIC_PERMISSIONS_REQUEST_CODE)
            return true
        }
        return false
    }
}
