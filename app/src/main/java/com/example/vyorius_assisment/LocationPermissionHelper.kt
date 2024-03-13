package com.example.vyorius_assisment

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import java.lang.ref.WeakReference

class LocationPermissionHelper(val activity: WeakReference<Activity>) {
    private lateinit var permissionsManager: PermissionsManager

    fun checkPermissions(onMapReady: () -> Unit) {
        val context = activity.get()
        if (context != null && PermissionsManager.areLocationPermissionsGranted(context)) {
            onMapReady()
        } else {
            permissionsManager = PermissionsManager(object : PermissionsListener {
                override fun onExplanationNeeded(permissionsToExplain: List<String>) {
                    val context = activity.get()
                    if (context != null) {
                        Toast.makeText(
                            context, "You need to accept location permissions.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onPermissionResult(granted: Boolean) {
                    if (granted) {
                        onMapReady()
                    } else {
                        val context = activity.get()
                        if (context != null) {
                            context.finish()
                        }
                    }
                }
            })
            val context = activity.get()
            if (context != null) {
                permissionsManager.requestLocationPermissions(context)
            }
        }
    }

    fun onRequestPermissionResult(
        requestCode: Int,
        permission: Array<String>,
        grantResult: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permission, grantResult)
    }
}
