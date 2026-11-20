package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ExportFormat
import com.example.model.QualityOption
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldVioletBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadResultSheet(
    isVip: Boolean,
    isVideoSource: Boolean,
    selectedQuality: QualityOption,
    selectedFormat: ExportFormat,
    estimatedSizeLabel: String,
    isDownloading: Boolean,
    downloadProgress: Float,
    downloadSuccess: Boolean,
    adWatermarkRemovedOnce: Boolean,
    onSelectQualityFormat: (QualityOption, ExportFormat) -> Unit,
    onWatchRewardAdForWatermark: () -> Unit,
    onOpenPaywall: () -> Unit,
    onDownloadToGallery: () -> Unit,
    onSaveAsWallpaper: () -> Unit,
    onShare: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val formatOptions = if (isVideoSource) {
        listOf(ExportFormat.MP4, ExportFormat.MOV, ExportFormat.JPG)
    } else {
        listOf(ExportFormat.JPG, ExportFormat.PNG, ExportFormat.HEIC)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0E0E17),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VIP Export Studio",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Estimated size: $estimatedSizeLabel • Saved to Pictures/Lumina AI",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.68f)
                    )
                }
                if (isVip) VipCrownBadge("NO WATERMARK")
            }

            // Quality Options
            Text("Resolution & Quality", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QualityOption.entries.forEach { quality ->
                    val locked = quality.vipOnly && !isVip
                    FilterChip(
                        selected = selectedQuality == quality,
                        onClick = {
                            if (locked) onOpenPaywall() else onSelectQualityFormat(quality, selectedFormat)
                        },
                        leadingIcon = if (locked) {
                            { Icon(Icons.Rounded.Lock, contentDescription = "VIP", modifier = Modifier.size(14.dp), tint = VipGold) }
                        } else null,
                        label = { Text(quality.label.substringBefore(" ")) },
                        modifier = Modifier.testTag("quality_chip_${quality.name.lowercase()}")
                    )
                }
            }

            // Format Options
            Text("File Format", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                formatOptions.forEach { fmt ->
                    FilterChip(
                        selected = selectedFormat == fmt,
                        onClick = { onSelectQualityFormat(selectedQuality, fmt) },
                        label = { Text(fmt.label) },
                        modifier = Modifier.testTag("format_chip_${fmt.name.lowercase()}")
                    )
                }
            }

            // Watermark Removal Banner for Free Users
            if (!isVip && !adWatermarkRemovedOnce) {
                Surface(
                    color = Color(0x1FF5C542),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, VipGold.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Free Export Includes Small Watermark",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Watch a 5s sponsor tip to remove once, or unlock VIP",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        }
                        TextButton(
                            onClick = onWatchRewardAdForWatermark,
                            modifier = Modifier.testTag("remove_watermark_ad_button")
                        ) {
                            Icon(Icons.Rounded.PlayCircle, contentDescription = null, tint = VipGold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Remove", color = VipGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (isDownloading) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = VipGold,
                        trackColor = Color.White.copy(alpha = 0.14f)
                    )
                    Text(
                        text = "Saving ${selectedQuality.label} ${selectedFormat.label} ($estimatedSizeLabel)...",
                        style = MaterialTheme.typography.labelMedium,
                        color = VipGold
                    )
                }
            }

            // Big Glowing Download Button with animated success checkmark
            AnimatedContent(targetState = downloadSuccess, label = "downloadButtonState") { saved ->
                if (saved) {
                    Surface(
                        color = EmeraldSuccess.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, EmeraldSuccess),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = "Saved", tint = EmeraldSuccess)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Saved to Lumina AI Album!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onDownloadToGallery,
                        enabled = !isDownloading,
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 58.dp)
                            .testTag("confirm_download_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 58.dp)
                                .background(
                                    brush = if (isVip) VipGoldVioletBrush else Brush.horizontalGradient(
                                        listOf(StudioBlue, Color(0xFF5E5CE6))
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Download,
                                    contentDescription = "Save to Gallery",
                                    tint = if (isVip) ObsidianBlack else Color.White
                                )
                                Text(
                                    text = "Save to Gallery • $estimatedSizeLabel",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isVip) ObsidianBlack else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // One-tap Save as Wallpaper + Social Share Targets
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onSaveAsWallpaper,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("sheet_set_wallpaper_button")
                ) {
                    Icon(Icons.Rounded.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Set Wallpaper")
                }
                OutlinedButton(
                    onClick = { onShare("Share Sheet") },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("sheet_share_button")
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share All")
                }
            }

            Text("Quick Share Platforms", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.7f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("WhatsApp", "Instagram", "Snapchat", "TikTok", "Facebook").forEach { platform ->
                    Surface(
                        onClick = { onShare(platform) },
                        shape = CircleShape,
                        color = Color(0xFF1D1D2B),
                        modifier = Modifier.weight(1f).heightIn(min = 42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                            Text(
                                text = platform.take(5),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
