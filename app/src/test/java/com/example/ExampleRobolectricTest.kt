package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.SearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TrekScape", appName)
    }

    @Test
    fun `search engines configured properly`() {
        assertEquals("DuckDuckGo", SearchEngine.DUCKDUCKGO.displayName)
        assertTrue(SearchEngine.DUCKDUCKGO.searchUrl.contains("duckduckgo.com"))
        assertTrue(SearchEngine.GOOGLE.searchUrl.contains("google.com"))
    }
}
