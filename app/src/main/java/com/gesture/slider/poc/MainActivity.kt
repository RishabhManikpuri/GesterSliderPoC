package com.gesture.slider.poc

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.gesture.slider.ui.SliderViewModel
import com.gesture.slider.ui.screen.GestureSliderScreen
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import com.gesture.slider.ui.screen.SwipeGestureScreen
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: SliderViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setContent { AppNavigation(viewModel) }
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            setContent { AppNavigation(viewModel) }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

@Composable
fun AppNavigation(viewModel: SliderViewModel) {
    var currentScreen by remember { mutableStateOf("slider") }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.gestureRepository.start(lifecycleOwner)
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.gestureRepository.stop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.gestureRepository.stop()
        }
    }

    if (currentScreen == "slider") {
        GestureSliderScreen(
            viewModel = viewModel,
            onNavigateToSwipe = { currentScreen = "swipe" }
        )
    } else if (currentScreen == "swipe") {
        SwipeGestureScreen(
            viewModel = viewModel,
            onNavigateBack = { currentScreen = "slider" }
        )
    }
}
