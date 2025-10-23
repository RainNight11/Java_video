package com.example.java_video

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.java_video.ui.VirtualPresenterApp
import com.example.java_video.ui.theme.Java_videoTheme
import com.example.java_video.viewmodel.VirtualPresenterViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as VirtualPresenterApplication
        setContent {
            Java_videoTheme {
                val presenterViewModel: VirtualPresenterViewModel = viewModel(
                    factory = VirtualPresenterViewModel.provideFactory(app)
                )
                VirtualPresenterApp(presenterViewModel)
            }
        }
    }
}
