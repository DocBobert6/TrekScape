package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.browser.BrowserScreen
import com.example.ui.browser.BrowserViewModel
import com.example.ui.theme.TrekScapeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            val preferences by viewModel.preferences.collectAsState()
            val isPrivate by viewModel.isPrivateMode.collectAsState()

            TrekScapeTheme(
                themeMode = preferences.themeMode,
                isPrivate = isPrivate
            ) {
                BrowserScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val data: Uri? = intent.data

        if (Intent.ACTION_VIEW == action && data != null) {
            val targetUrl = data.toString()
            viewModel.openTab(url = targetUrl)
        } else if (Intent.ACTION_SEND == action && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                val resolved = viewModel.resolveSearchOrUrl(sharedText)
                viewModel.openTab(url = resolved)
            }
        }
    }
}
