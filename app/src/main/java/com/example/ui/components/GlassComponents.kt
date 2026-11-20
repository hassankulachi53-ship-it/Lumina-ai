package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalLuminaColors
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldBrush
import com.example.ui.theme.VipGoldVioletBrush
import com.example.ui.theme.VipViolet
import kotlin.math.roundToInt

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderBrush: Brush? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val luminaColors = LocalLuminaColors.current
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .shadow(elevation = if (luminaColors.isDark) 12.dp else 6.dp, shape = shape, clip = false)
        .clip(shape)
        .background(luminaColors.glassFill)
        .then(
            if (borderBrush != null) Modifier.border(1.2.dp, borderBrush, shape)
            else Modifier.border(1.dp, luminaColors.glassBorder, shape)
        )
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )

    Column(modifier = baseModifier, content = content)
}

@Composable
fun VipCrownBadge(
    label: String = "VIP",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(VipGoldVioletBrush)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.WorkspacePremium,
            contentDescription = "VIP Crown",
            tint = ObsidianBlack,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = ObsidianBlack
        )
    }
}

@Composable
fun FloatingVipButton(
    isVip: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val infinite = rememberInfiniteTransition(label = "vipPulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = CircleShape,
        shadowElevation = 14.dp,
        modifier = modifier
            .heightIn(min = 48.dp)
            .graphicsLayer {
                scaleX = if (isVip) 1f else pulse
                scaleY = if (isVip) 1f else pulse
            }
            .testTag("floating_vip_button"),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color(0x66FFFFFF))
    ) {
        Row(
            modifier = Modifier
                .background(if (isVip) VipGoldBrush else VipGoldVioletBrush)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = "VIP Crown",
                tint = ObsidianBlack,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (isVip) "VIP Active" else "Unlock VIP",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = ObsidianBlack
            )
        }
    }
}

@Composable
fun VipGoldGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = ObsidianBlack
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.heightIn(min = 52.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .background(VipGoldVioletBrush, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.WorkspacePremium,
                    contentDescription = null,
                    tint = ObsidianBlack,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BeforeAfterSlider(
    beforeBitmap: Bitmap?,
    afterBitmap: Bitmap?,
    modifier: Modifier = Modifier,
    beforeLabel: String = "BEFORE",
    afterLabel: String = "AFTER • HDR",
    initialFraction: Float = 0.52f
) {
    var fraction by remember(beforeBitmap, afterBitmap) { mutableFloatStateOf(initialFraction) }
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0E0E16))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val width = size.width.toFloat().coerceAtLeast(1f)
                    val prev = fraction
                    val next = (change.position.x / width).coerceIn(0.06f, 0.94f)
                    if ((prev < 0.5f && next >= 0.5f) || (prev > 0.5f && next <= 0.5f)) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    fraction = next
                }
            }
            .testTag("before_after_slider")
    ) {
        val density = LocalDensity.current
        val totalWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
        val handleRadiusPx = with(density) { 22.dp.toPx() }
        val splitX = totalWidthPx * fraction

        // Full "After" image underneath
        if (afterBitmap != null) {
            Image(
                bitmap = afterBitmap.asImageBitmap(),
                contentDescription = "Enhanced result image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // Clipped "Before" image on the left
        if (beforeBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(with(density) { splitX.toDp() })
                    .clipToBounds()
            ) {
                Image(
                    bitmap = beforeBitmap.asImageBitmap(),
                    contentDescription = "Original before image",
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(this@BoxWithConstraints.maxWidth),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Divider Line & Handle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width * fraction
            drawLine(
                color = Color.White.copy(alpha = 0.92f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 3f
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset((splitX - handleRadiusPx).roundToInt(), 0) }
                .align(Alignment.CenterStart)
                .size(44.dp)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xEEFFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CompareArrows,
                contentDescription = "Drag to compare before and after",
                tint = ObsidianBlack,
                modifier = Modifier.size(22.dp)
            )
        }

        // Labels
        Surface(
            color = Color(0x9909090E),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Text(
                text = beforeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Surface(
            color = Color(0x9909090E),
            shape = CircleShape,
            border = BorderStroke(1.dp, VipGold.copy(alpha = 0.7f)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Text(
                text = afterLabel,
                style = MaterialTheme.typography.labelSmall,
                color = VipGold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun AnimatedProcessingOverlay(
    percent: Int,
    tip: String,
    isVip: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC07070B)),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.88f),
            borderBrush = if (isVip) VipGoldVioletBrush else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isVip) {
                    VipCrownBadge(label = "VIP FAST LANE")
                    Spacer(Modifier.height(14.dp))
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(88.dp)) {
                    CircularProgressIndicator(
                        progress = { (percent / 100f).coerceIn(0.05f, 1f) },
                        modifier = Modifier.fillMaxSize(),
                        color = if (isVip) VipGold else MaterialTheme.colorScheme.primary,
                        trackColor = Color.White.copy(alpha = 0.14f),
                        strokeWidth = 6.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = if (isVip) VipGold else VipViolet,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Enhancing in Studio",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
        }
    }
}
