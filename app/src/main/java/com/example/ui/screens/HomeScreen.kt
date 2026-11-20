package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CameraEnhance
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FaceRetouchingNatural
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.HdrOn
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LayersClear
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PhotoSizeSelectLarge
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ImageEnhancerEngine
import com.example.model.AppLanguage
import com.example.model.CatalogData
import com.example.model.DailyChallengeTheme
import com.example.model.EnhancementTool
import com.example.model.TrendingStyleSpec
import com.example.model.UserProfile
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.GlassCard
import com.example.ui.components.VipCrownBadge
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldVioletBrush
import com.example.ui.theme.VipViolet

@Composable
fun HomeScreen(
    profile: UserProfile?,
    language: AppLanguage,
    isAuthenticated: Boolean,
    challengeTheme: DailyChallengeTheme,
    onPickPhoto: () -> Unit,
    onPickVideo: () -> Unit,
    onPickBatch: () -> Unit,
    onSelectTool: (EnhancementTool) -> Unit,
    onApplyTrendingStyle: (TrendingStyleSpec) -> Unit,
    onOpenDailyChallenge: () -> Unit,
    onOpenWallpapers: () -> Unit,
    onClaimDailyStreak: () -> Unit,
    onOpenPaywall: () -> Unit,
    onRequestSignIn: () -> Unit
) {
    val context = LocalContext.current
    val isVip = profile?.hasActiveVip() == true
    val credits = profile?.totalCredits() ?: 5
    val streak = profile?.streakCount ?: 1

    var heroBefore by remember { mutableStateOf<Bitmap?>(null) }
    var heroAfter by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        val after = ImageEnhancerEngine.loadBitmapFromDrawable(context, R.drawable.img_demo_city_nights, 960)
        val before = ImageEnhancerEngine.createDegradedBeforePreview(after, EnhancementTool.NIGHT_MODE)
        heroAfter = after
        heroBefore = before
    }

    val coreTools = remember {
        listOf(
            EnhancementTool.SMART_ENHANCE,
            EnhancementTool.UNBLUR,
            EnhancementTool.UPSCALE,
            EnhancementTool.FACE_ENHANCE,
            EnhancementTool.PORTRAIT_LIGHT,
            EnhancementTool.NIGHT_MODE,
            EnhancementTool.AUTO_HDR,
            EnhancementTool.COLORIZE,
            EnhancementTool.RESTORE,
            EnhancementTool.BG_REMOVE,
            EnhancementTool.VIDEO_ENHANCE,
            EnhancementTool.WALLPAPER_HD
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = CatalogData.localizedHomeGreeting(language),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = CatalogData.localizedTagline(language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = {
                        if (isAuthenticated) onClaimDailyStreak() else onRequestSignIn()
                    },
                    shape = CircleShape,
                    color = AmberStreak.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, AmberStreak.copy(alpha = 0.45f)),
                    modifier = Modifier.heightIn(min = 40.dp).testTag("streak_pill_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Daily Streak",
                            tint = AmberStreak,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${streak}d",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = AmberStreak
                        )
                    }
                }
                Surface(
                    onClick = onOpenPaywall,
                    shape = CircleShape,
                    color = if (isVip) VipGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (isVip) VipGold else MaterialTheme.colorScheme.outline),
                    modifier = Modifier.heightIn(min = 40.dp).testTag("credits_pill_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.WorkspacePremium,
                            contentDescription = "Credits and VIP",
                            tint = if (isVip) VipGold else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isVip) "VIP" else "$credits HD",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Guest Mode Banner if not signed in
        if (!isAuthenticated) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                onClick = onRequestSignIn
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Guest Preview Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sign in with Google for Cloud Sync, Streaks & Daily Challenge prizes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Sign In →",
                        style = MaterialTheme.typography.labelLarge,
                        color = StudioBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Hero Live Before/After Preview Card
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 26.dp) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BeforeAfterSlider(
                    beforeBitmap = heroBefore,
                    afterBitmap = heroAfter,
                    beforeLabel = "RAW LOW-LIGHT",
                    afterLabel = "LUMINA PRO HDR",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPickPhoto,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioBlue, contentColor = Color.White),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 50.dp)
                            .testTag("home_enhance_photo_button")
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Enhance Photo", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            if (isVip) onPickVideo() else onOpenPaywall()
                        },
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, VipGold.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp)
                            .testTag("home_enhance_video_button")
                    ) {
                        Icon(Icons.Rounded.Videocam, contentDescription = null, tint = VipGold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("4K Video", color = VipGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Daily Photo Challenge Spotlight Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_daily_challenge_card"),
            cornerRadius = 24.dp,
            borderBrush = VipGoldVioletBrush,
            onClick = onOpenDailyChallenge
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(VipGoldVioletBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.EmojiEvents,
                        contentDescription = "Daily Photo Challenge Trophy",
                        tint = ObsidianBlack,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "DAILY PHOTO CHALLENGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VipGold
                        )
                        VipCrownBadge("PRIZE")
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "Today's Theme: ${challengeTheme.title}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Win ${challengeTheme.prizeDescription}. Tap to enter & vote!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }

        // 12-Tool Flagship Studio Grid (rendered cleanly in 3-column rows inside scroll)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI Studio Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "12 Flagship Modes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        coreTools.chunked(3).forEach { rowTools ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowTools.forEach { tool ->
                    ToolTile(
                        tool = tool,
                        isVipUser = isVip,
                        onClick = {
                            if (tool == EnhancementTool.WALLPAPER_HD) onOpenWallpapers()
                            else if (tool.vipOnly && !isVip) onOpenPaywall()
                            else onSelectTool(tool)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // VIP Pro Suite (Batch 100, AI Avatar, Pro Headshot, Cinema Video, Object Erase)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            borderBrush = VipGoldVioletBrush
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VIP Creator Suite",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    VipCrownBadge("VIP PRO")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionPill(
                        label = "Batch 100",
                        icon = Icons.Rounded.PhotoLibrary,
                        onClick = { if (isVip) onPickBatch() else onOpenPaywall() },
                        modifier = Modifier.weight(1f).testTag("vip_batch_button")
                    )
                    QuickActionPill(
                        label = "AI Avatar",
                        icon = Icons.Rounded.FaceRetouchingNatural,
                        onClick = { if (isVip) onSelectTool(EnhancementTool.AI_AVATAR) else onOpenPaywall() },
                        modifier = Modifier.weight(1f).testTag("vip_avatar_button")
                    )
                    QuickActionPill(
                        label = "Headshot",
                        icon = Icons.Rounded.CameraEnhance,
                        onClick = { if (isVip) onSelectTool(EnhancementTool.PORTRAIT_HEADSHOT) else onOpenPaywall() },
                        modifier = Modifier.weight(1f).testTag("vip_headshot_button")
                    )
                }
            }
        }

        // Trending Styles Updated Weekly + Photo of the Day
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trending This Week",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "One-tap apply",
                style = MaterialTheme.typography.labelMedium,
                color = StudioBlue
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(CatalogData.trendingStyles) { style ->
                GlassCard(
                    modifier = Modifier
                        .width(172.dp)
                        .testTag("trending_style_${style.title.lowercase().replace(" ", "_")}"),
                    cornerRadius = 20.dp,
                    borderBrush = if (style.vipOnly) VipGoldVioletBrush else null,
                    onClick = {
                        if (style.vipOnly && !isVip) onOpenPaywall() else onApplyTrendingStyle(style)
                    }
                ) {
                    Box(modifier = Modifier.height(112.dp).fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = CatalogData.drawableForKey(style.sampleDrawableKey)),
                            contentDescription = style.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xBB07070B))))
                        )
                        Surface(
                            color = if (style.vipOnly) VipGold else Color(0xCC0A84FF),
                            shape = CircleShape,
                            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                        ) {
                            Text(
                                text = style.tag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (style.vipOnly) ObsidianBlack else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = style.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = style.filterName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Bottom spacing for floating VIP button
        Spacer(Modifier.height(74.dp))
    }
}

@Composable
private fun ToolTile(
    tool: EnhancementTool,
    isVipUser: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .heightIn(min = 98.dp)
            .testTag("tool_tile_${tool.name.lowercase()}"),
        cornerRadius = 20.dp,
        borderBrush = if (tool.vipOnly) VipGoldVioletBrush else null,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            if (tool.vipOnly) VipGoldVioletBrush
                            else Brush.linearGradient(listOf(StudioBlue.copy(alpha = 0.24f), VipViolet.copy(alpha = 0.24f)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForTool(tool),
                        contentDescription = tool.title,
                        tint = if (tool.vipOnly) ObsidianBlack else StudioBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (tool.vipOnly && !isVipUser) {
                    Text(
                        text = "VIP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VipGold
                    )
                }
            }
            Text(
                text = tool.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = tool.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionPill(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = VipGold, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

private fun iconForTool(tool: EnhancementTool): ImageVector = when (tool) {
    EnhancementTool.SMART_ENHANCE -> Icons.Rounded.AutoAwesome
    EnhancementTool.UNBLUR -> Icons.Rounded.BlurOn
    EnhancementTool.UPSCALE -> Icons.Rounded.PhotoSizeSelectLarge
    EnhancementTool.FACE_ENHANCE -> Icons.Rounded.FaceRetouchingNatural
    EnhancementTool.PORTRAIT_LIGHT -> Icons.Rounded.WbSunny
    EnhancementTool.NIGHT_MODE -> Icons.Rounded.Bedtime
    EnhancementTool.AUTO_HDR -> Icons.Rounded.HdrOn
    EnhancementTool.COLORIZE -> Icons.Rounded.ColorLens
    EnhancementTool.RESTORE -> Icons.Rounded.History
    EnhancementTool.BG_REMOVE -> Icons.Rounded.LayersClear
    EnhancementTool.OBJECT_REMOVE -> Icons.Rounded.AutoFixHigh
    EnhancementTool.VIDEO_ENHANCE -> Icons.Rounded.Videocam
    EnhancementTool.CINEMATIC_VIDEO -> Icons.Rounded.MovieFilter
    EnhancementTool.AI_AVATAR -> Icons.Rounded.Flare
    EnhancementTool.PORTRAIT_HEADSHOT -> Icons.Rounded.CameraEnhance
    EnhancementTool.WALLPAPER_HD -> Icons.Rounded.Wallpaper
}
