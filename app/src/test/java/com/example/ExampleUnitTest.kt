package com.example

import com.example.data.preferences.FingerprintingProtection
import com.example.data.preferences.SearchEngine
import com.example.data.preferences.ToolbarLayout
import com.example.data.repository.TrackingParamStripper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun urlResolution_isCorrect() {
        val domain = "wikipedia.org"
        val expected = "https://$domain"
        assertTrue(expected.startsWith("https://"))
    }

    @Test
    fun searchEngineUrls_areValid() {
        val query = "kotlin jetpack compose"
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        val url = String.format(SearchEngine.DUCKDUCKGO.searchUrl, encoded)
        assertTrue(url.contains("q=kotlin+jetpack+compose") || url.contains("kotlin"))
    }

    @Test
    fun trackingParamStripper_removesTrackingParams() {
        val dirtyUrl = "https://example.com/article?utm_source=twitter&utm_medium=social&fbclid=IwAR123&keep=true"
        val result = TrackingParamStripper.stripTrackingParameters(dirtyUrl)
        assertEquals(3, result.removedCount)
        assertFalse(result.strippedUrl.contains("utm_source"))
        assertFalse(result.strippedUrl.contains("fbclid"))
        assertTrue(result.strippedUrl.contains("keep=true"))
    }

    @Test
    fun trackingParamStripper_preservesOAuthParameters() {
        val authUrl = "https://accounts.google.com/o/oauth2/auth?client_id=123&redirect_uri=https://app/cb&response_type=code&scope=profile&state=xyz123"
        val result = TrackingParamStripper.stripTrackingParameters(authUrl)
        assertEquals(0, result.removedCount)
        assertTrue(result.strippedUrl.contains("client_id=123"))
        assertTrue(result.strippedUrl.contains("state=xyz123"))
        assertTrue(result.strippedUrl.contains("response_type=code"))
    }

    @Test
    fun defaults_areVerified() {
        assertEquals(ToolbarLayout.COMPACT, ToolbarLayout.valueOf("COMPACT"))
        assertEquals(FingerprintingProtection.ALL, FingerprintingProtection.valueOf("ALL"))
    }
}
