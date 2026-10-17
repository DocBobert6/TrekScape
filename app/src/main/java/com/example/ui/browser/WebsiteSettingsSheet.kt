package com.example.ui.browser

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.SiteSettingsEntity
import com.example.ui.settings.SettingsToggleRow
import com.example.ui.theme.TrekTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteSettingsSheet(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeTab = viewModel.getActiveTab() ?: return
    val preferences by viewModel.preferences.collectAsState()
    val domain = remember(activeTab.url) {
        Uri.parse(activeTab.url).host.orEmpty().ifBlank { "Current Website" }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isProtectionsDisabled = preferences.disabledProtectionDomains.contains(domain)
    var siteSettings by remember { mutableStateOf(SiteSettingsEntity(domain = domain)) }

    LaunchedEffect(domain) {
        siteSettings = viewModel.siteSettingsRepository.getSettings(domain)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = TrekTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Settings for $domain",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TrekTheme.colors.textPrimary
            )

            // Master Protection Toggle for Broken Sites
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isProtectionsDisabled) Color(0xFFEF4444).copy(alpha = 0.08f) else TrekTheme.colors.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = if (isProtectionsDisabled) Color(0xFFEF4444) else TrekTheme.colors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isProtectionsDisabled) "Protections Disabled for Site" else "Enhanced Protections Active",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TrekTheme.colors.textPrimary
                    )
                    Text(
                        text = if (isProtectionsDisabled) "Turn off protections for broken sites" else "Tracker blocking & fingerprint defenses active",
                        style = MaterialTheme.typography.bodySmall,
                        color = TrekTheme.colors.textSecondary
                    )
                }
                Switch(
                    checked = !isProtectionsDisabled,
                    onCheckedChange = { active ->
                        coroutineScope.launch {
                            viewModel.preferencesRepository.toggleDomainProtection(domain, active)
                            Toast.makeText(context, "Reloading $domain with new protections...", Toast.LENGTH_SHORT).show()
                            activeTab.webView?.reload()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TrekTheme.colors.accent
                    )
                )
            }

            HorizontalDivider(
                color = TrekTheme.colors.separator.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            SettingsToggleRow(
                icon = Icons.Outlined.Computer,
                title = "Request Desktop Website",
                checked = activeTab.desktopMode,
                onCheckedChange = {
                    viewModel.toggleDesktopMode()
                    coroutineScope.launch {
                        viewModel.siteSettingsRepository.updateSettings(
                            siteSettings.copy(desktopMode = it)
                        )
                    }
                }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.DarkMode,
                title = "Dark Pages Override",
                checked = siteSettings.darkPages,
                onCheckedChange = {
                    siteSettings = siteSettings.copy(darkPages = it)
                    coroutineScope.launch {
                        viewModel.siteSettingsRepository.updateSettings(siteSettings)
                    }
                }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.CameraAlt,
                title = "Camera Access",
                subtitle = if (siteSettings.cameraAllowed == true) "Allowed" else "Ask each time",
                checked = siteSettings.cameraAllowed == true,
                onCheckedChange = {
                    siteSettings = siteSettings.copy(cameraAllowed = it)
                    coroutineScope.launch {
                        viewModel.siteSettingsRepository.updateSettings(siteSettings)
                    }
                }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.Mic,
                title = "Microphone Access",
                subtitle = if (siteSettings.micAllowed == true) "Allowed" else "Ask each time",
                checked = siteSettings.micAllowed == true,
                onCheckedChange = {
                    siteSettings = siteSettings.copy(micAllowed = it)
                    coroutineScope.launch {
                        viewModel.siteSettingsRepository.updateSettings(siteSettings)
                    }
                }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.LocationOn,
                title = "Location Access",
                subtitle = if (siteSettings.locationAllowed == true) "Allowed" else "Ask each time",
                checked = siteSettings.locationAllowed == true,
                onCheckedChange = {
                    siteSettings = siteSettings.copy(locationAllowed = it)
                    coroutineScope.launch {
                        viewModel.siteSettingsRepository.updateSettings(siteSettings)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
