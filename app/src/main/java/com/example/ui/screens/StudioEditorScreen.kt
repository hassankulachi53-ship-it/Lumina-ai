package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CatalogData
import com.example.model.EditParameters
import com.example.model.EnhancementTool
import com.example.model.StrengthLevel
import com.example.ui.components.AnimatedProcessingOverlay
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.GlassCard
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldVioletBrush
import com.example.viewmodel.StudioUiState

@Composable
fun StudioEditorScreen(
    state: StudioUiState,
    isVip: Boolean,
    onPickPhoto: () -> Unit,
    onSmartEnhance: () -> Unit,
    onSelectTool: (EnhancementTool, String?) -> Unit,
    onStrengthChange: (StrengthLevel) -> Unit,
    onUpdateParams: ((EditParameters) -> EditParameters) -> Unit,
    onOpenDownloadSheet: () -> Unit,
    onEnterChallengeWithCurrent: () -> Unit,
    onOpenPaywall: () -> Unit,
    onDismissRating: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var activeTab by remember { mutableIntStateOf(0) } // 0=AI Tools, 1=Filters, 2=Pro Tune, 3=Crop/Text
    var comparePrevious by remember { mutableStateOf(false) }
    var filterGroup by remember { mutableStateOf("All") }
    var customAiPrompt by remember { mutableStateOf("") }
    var ratingStars by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Action Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.sourceTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${state.params.tool.title} • ${state.params.filterName} • ${state.estimatedSizeLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onPickPhoto,
                        modifier = Modifier.testTag("studio_pick_photo_button")
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = "Pick another photo")
                    }
                    if (state.previousVersionBitmap != null) {
                        IconButton(
                            onClick = { comparePrevious = !comparePrevious },
                            modifier = Modifier.testTag("studio_compare_version_button")
                        ) {
                            Icon(
                                Icons.Rounded.Compare,
                                contentDescription = "Compare with previous version",
                                tint = if (comparePrevious) VipGold else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Large Interactive Before / After Slider
            BeforeAfterSlider(
                beforeBitmap = if (comparePrevious) state.previousVersionBitmap else state.beforeBitmap,
                afterBitmap = state.afterBitmap,
                beforeLabel = if (comparePrevious) "PREV VERSION" else "BEFORE",
                afterLabel = if (state.isVideoSource) "4K VIDEO HDR" else "AFTER • ${state.params.strength.label.uppercase()}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            )

            // AI Auto-Mode Insight Banner or Error
            if (state.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else if (state.aiAnalysis != null) {
                GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = VipGold, modifier = Modifier.size(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Auto-Detect: ${state.aiAnalysis.sceneType} • ${state.aiAnalysis.recommendedTool}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.aiAnalysis.reasoning,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (state.statusBanner != null) {
                Surface(
                    color = EmeraldSuccess.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.statusBanner,
                        color = EmeraldSuccess,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Strength Selector (Soft / Natural / Ultra) + One-Tap AI Auto-Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StrengthLevel.entries.forEach { lvl ->
                    FilterChip(
                        selected = state.params.strength == lvl,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onStrengthChange(lvl)
                        },
                        label = { Text(lvl.label) },
                        modifier = Modifier.testTag("strength_chip_${lvl.name.lowercase()}")
                    )
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    onClick = onSmartEnhance,
                    shape = CircleShape,
                    color = StudioBlue,
                    modifier = Modifier.heightIn(min = 40.dp).testTag("studio_smart_ai_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("AI Auto", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Big Glowing Download Button + Submit to Daily Challenge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenDownloadSheet,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1.35f)
                        .heightIn(min = 52.dp)
                        .testTag("studio_download_cta")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .background(
                                if (isVip) VipGoldVioletBrush else Brush.horizontalGradient(listOf(StudioBlue, Color(0xFF5E5CE6))),
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.Download, contentDescription = null, tint = if (isVip) ObsidianBlack else Color.White)
                            Text(
                                text = "Download",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isVip) ObsidianBlack else Color.White
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onEnterChallengeWithCurrent,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.2.dp, VipGold.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .testTag("studio_submit_challenge_button")
                ) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = VipGold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Challenge", color = VipGold, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            // Editor Panel Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("AI Tools", "40+ Filters", "Pro Tune", "Crop & Text").forEachIndexed { idx, label ->
                    FilterChip(
                        selected = activeTab == idx,
                        onClick = { activeTab = idx },
                        label = { Text(label, maxLines = 1) },
                        modifier = Modifier.weight(1f).testTag("editor_tab_$idx")
                    )
                }
            }

            when (activeTab) {
                0 -> {
                    // AI Tools + Optional Custom Instruction for BG / Object Erase / Avatar
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(EnhancementTool.entries) { tool ->
                            val locked = tool.vipOnly && !isVip
                            FilterChip(
                                selected = state.params.tool == tool,
                                onClick = {
                                    if (locked) onOpenPaywall()
                                    else onSelectTool(tool, customAiPrompt.takeIf { it.isNotBlank() })
                                },
                                leadingIcon = if (locked) {
                                    { Icon(Icons.Rounded.Lock, contentDescription = "VIP", tint = VipGold, modifier = Modifier.size(14.dp)) }
                                } else null,
                                label = { Text(tool.title) },
                                modifier = Modifier.testTag("studio_tool_${tool.name.lowercase()}")
                            )
                        }
                    }
                    if (state.params.tool == EnhancementTool.PORTRAIT_LIGHT) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Studio", "Natural", "Contour", "Stage").forEach { style ->
                                FilterChip(
                                    selected = state.params.portraitLightStyle == style,
                                    onClick = { onUpdateParams { it.copy(portraitLightStyle = style) } },
                                    label = { Text(style) }
                                )
                            }
                        }
                    }
                    if (state.params.tool.generativeAi) {
                        OutlinedTextField(
                            value = customAiPrompt,
                            onValueChange = { customAiPrompt = it },
                            label = { Text("Optional AI Studio direction (e.g. 'Replace background with twilight rooftop')") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                1 -> {
                    val groups = listOf("All", "Vivid", "Cinematic", "Film", "Warm", "Cool", "Mono", "Noir", "VIP")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(groups) { grp ->
                            FilterChip(
                                selected = filterGroup == grp,
                                onClick = { filterGroup = grp },
                                label = { Text(grp) }
                            )
                        }
                    }
                    val filtered = remember(filterGroup) {
                        if (filterGroup == "All") CatalogData.filterPresets
                        else CatalogData.filterPresets.filter { it.group == filterGroup }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered) { preset ->
                            val locked = preset.vipOnly && !isVip
                            FilterChip(
                                selected = state.params.filterName == preset.name,
                                onClick = {
                                    if (locked) onOpenPaywall()
                                    else onUpdateParams { it.copy(filterName = preset.name) }
                                },
                                leadingIcon = if (locked) {
                                    { Icon(Icons.Rounded.Lock, contentDescription = "VIP", tint = VipGold, modifier = Modifier.size(14.dp)) }
                                } else null,
                                label = { Text(preset.name) },
                                modifier = Modifier.testTag("filter_chip_${preset.name.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }
                2 -> {
                    // Pro Editor Sliders
                    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("Pro Precision Adjustments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = { onUpdateParams { EditParameters(tool = it.tool) } }) {
                                    Text("Reset")
                                }
                            }
                            ProSliderRow("Exposure", state.params.exposure, -0.6f..0.6f) { v -> onUpdateParams { it.copy(exposure = v) } }
                            ProSliderRow("Brilliance", state.params.brilliance, -0.5f..0.7f) { v -> onUpdateParams { it.copy(brilliance = v) } }
                            ProSliderRow("Highlights", state.params.highlights, -0.6f..0.6f) { v -> onUpdateParams { it.copy(highlights = v) } }
                            ProSliderRow("Shadows", state.params.shadows, -0.4f..0.7f) { v -> onUpdateParams { it.copy(shadows = v) } }
                            ProSliderRow("Contrast", state.params.contrast, -0.4f..0.6f) { v -> onUpdateParams { it.copy(contrast = v) } }
                            ProSliderRow("Saturation", state.params.saturation, -1f..0.8f) { v -> onUpdateParams { it.copy(saturation = v) } }
                            ProSliderRow("Vibrance", state.params.vibrance, 0f..0.8f) { v -> onUpdateParams { it.copy(vibrance = v) } }
                            ProSliderRow("Warmth", state.params.warmth, -0.5f..0.5f) { v -> onUpdateParams { it.copy(warmth = v) } }
                            ProSliderRow("Tint", state.params.tint, -0.4f..0.4f) { v -> onUpdateParams { it.copy(tint = v) } }
                            ProSliderRow("Sharpness", state.params.sharpness, 0f..1f) { v -> onUpdateParams { it.copy(sharpness = v) } }
                            ProSliderRow("Definition", state.params.definition, 0f..1f) { v -> onUpdateParams { it.copy(definition = v) } }
                            ProSliderRow("Skin Natural", state.params.skinRetouch, 0f..0.6f) { v -> onUpdateParams { it.copy(skinRetouch = v) } }
                            ProSliderRow("BG Blur", state.params.backgroundBlur, 0f..1f) { v -> onUpdateParams { it.copy(backgroundBlur = v) } }
                            ProSliderRow("Vignette", state.params.vignette, 0f..0.8f) { v -> onUpdateParams { it.copy(vignette = v) } }
                        }
                    }
                }
                3 -> {
                    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Crop Aspect & Rotate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                OutlinedButton(
                                    onClick = { onUpdateParams { it.copy(rotationDegrees = (it.rotationDegrees + 90) % 360) } },
                                    modifier = Modifier.testTag("rotate_90_button")
                                ) {
                                    Icon(Icons.Rounded.RotateRight, contentDescription = "Rotate 90 degrees", modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Rotate 90°")
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Original", "1:1", "4:5", "9:16", "16:9").forEach { ratio ->
                                    FilterChip(
                                        selected = state.params.aspectRatioLabel == ratio,
                                        onClick = { onUpdateParams { it.copy(aspectRatioLabel = ratio) } },
                                        label = { Text(ratio) }
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = state.params.overlayText,
                                onValueChange = { txt -> onUpdateParams { it.copy(overlayText = txt.take(32)) } },
                                label = { Text("Add Text Watermark / Caption") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("", "✨", "🌙", "🔥", "👑").forEach { sticker ->
                                    FilterChip(
                                        selected = state.params.overlaySticker == sticker,
                                        onClick = { onUpdateParams { it.copy(overlaySticker = sticker) } },
                                        label = { Text(if (sticker.isEmpty()) "None" else sticker) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Smart Rating Request after a happy result
            if (state.showRatingPrompt && ratingStars == 0) {
                GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Love this enhancement?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Tap a star to rate quality", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = {
                                        ratingStars = star
                                        onDismissRating()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Rounded.Star, contentDescription = "Rate $star stars", tint = VipGold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(74.dp))
        }

        if (state.isProcessing) {
            AnimatedProcessingOverlay(
                percent = state.processingPercent,
                tip = state.processingTip,
                isVip = isVip
            )
        }
    }
}

@Composable
private fun ProSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = String.format("%+.2f", value),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range
        )
    }
}
