
package com.example.vyorius_assisment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.expressions.dsl.generated.zoom
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var mapView: MapView
    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }
    private var onMoveListener = object : OnMoveListener {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDissmised()
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {
        }
    }

    private fun onCameraTrackingDissmised() {
        Toast.makeText(this@MainActivity, "onCameraTrackingDissmised", Toast.LENGTH_SHORT).show()
        mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions { onMapReady() }
    }

    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().zoom(14.0).build())
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            setupGestureListener()
            initLocationComponent()
        }
    }

    private fun setupGestureListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_launcher_background
                ),
                shadowImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_launcher_foreground
                ),
                scaleExpression = interpolator {
                    linear {
                        zoom()
                        stop {
                            literal(0.0)
                            literal(0.6)
                        }
                        stop {
                            literal(20.0)
                            literal(1.0)
                        }
                    }
                }
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    private fun stop(function: () -> Expression): Expression {
        return function()
    }

    private fun linear(block: Expression.() -> Unit): Expression {
        val expression = Expression.linear()
        return expression
    }

    private fun interpolator(block: Expression.() -> Unit): String {
        val expression = linear(block)
        return expression.toString()
    }

    private fun LocationPuck2D(bearingImage: Drawable?, shadowImage: Drawable?, scaleExpression: String?): LocationPuck2D {
        // Implement your logic here
        // For now, simply returning a LocationPuck2D instance
        return LocationPuck2D(bearingImage, shadowImage, scaleExpression)
    }
}
