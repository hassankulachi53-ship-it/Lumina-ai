package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ImageEnhancerEngine
import com.example.model.CatalogData
import com.example.model.ChallengePrizeTier
import com.example.model.ChallengeSubmission
import com.example.model.DailyChallengeTheme
import com.example.model.UiState
import com.example.ui.components.GlassCard
import com.example.ui.components.VipCrownBadge
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldVioletBrush

@Composable
fun DailyChallengeScreen(
    theme: DailyChallengeTheme,
    currentEnhancedBitmap: Bitmap?,
    currentToolTitle: String,
    submissionsState: UiState<List<ChallengeSubmission>>,
    isSubmitting: Boolean,
    lastResult: ChallengeSubmission?,
    isAuthenticated: Boolean,
    onPickChallengePhoto: () -> Unit,
    onOpenStudioForTheme: () -> Unit,
    onSubmitEntry: (Bitmap, String) -> Unit,
    onUpvote: (ChallengeSubmission) -> Unit,
    onRequireSignIn: () -> Unit
) {
    var caption by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme Hero Banner
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 26.dp,
            borderBrush = VipGoldVioletBrush
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(175.dp)) {
                Image(
                    painter = painterResource(id = CatalogData.drawableForKey(theme.sampleDrawableKey)),
                    contentDescription = theme.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x5507070B), Color(0xEE07070B))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VipCrownBadge("TODAY'S THEME")
                        Surface(color = Color(0x9912121B), shape = CircleShape) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Schedule, contentDescription = null, tint = VipGold, modifier = Modifier.size(13.dp))
                                Text(theme.dateKey, style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = theme.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = theme.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 2
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = VipGold, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Daily Prize Pool: ${theme.prizeDescription}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VipGold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onPickChallengePhoto,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("challenge_pick_photo_button")
                    ) {
                        Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Pick Photo")
                    }
                    OutlinedButton(
                        onClick = onOpenStudioForTheme,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("challenge_enhance_in_studio")
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Tune in Studio")
                    }
                }
            }
        }

        // Submit Current Enhanced Photo Card
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Submit Your Enhanced Entry",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (currentEnhancedBitmap != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = currentEnhancedBitmap.asImageBitmap(),
                            contentDescription = "Your enhanced challenge entry",
                            modifier = Modifier
                                .size(82.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ready from Studio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Tool: $currentToolTitle", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Gemini AI Judge will score creativity & theme match.", style = MaterialTheme.typography.labelSmall, color = StudioBlue)
                        }
                    }
                }
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it.take(120) },
                    label = { Text("Entry Caption (e.g. 'Neon rain on Clifton Road')") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("challenge_caption_input")
                )

                Button(
                    onClick = {
                        if (!isAuthenticated) {
                            onRequireSignIn()
                        } else if (currentEnhancedBitmap != null) {
                            onSubmitEntry(currentEnhancedBitmap, caption.ifBlank { "${theme.title} ProHDR" })
                        } else {
                            onPickChallengePhoto()
                        }
                    },
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VipGold, contentColor = ObsidianBlack),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("challenge_submit_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = ObsidianBlack)
                        Spacer(Modifier.width(8.dp))
                        Text("AI Judge Reviewing...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isAuthenticated) "Submit & Claim AI Prize" else "Sign in with Google to Submit",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Latest AI Judge Result Card
        if (lastResult != null) {
            val prize = ChallengePrizeTier.fromRaw(lastResult.prizeAwarded)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                borderBrush = VipGoldVioletBrush
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Verified, contentDescription = null, tint = EmeraldSuccess)
                        Text("AI Judge Result: ${prize.label}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VipGold)
                    }
                    Text("Reward Unlocked: ${prize.rewardSummary}", style = MaterialTheme.typography.labelLarge, color = EmeraldSuccess)
                    Text(
                        text = "Creativity ${lastResult.creativityScore}/100 • Quality ${lastResult.qualityScore}/100",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "\"${lastResult.judgeFeedback}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Top Daily Submissions Showcase
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Top Showcase",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Vote for Favorites",
                style = MaterialTheme.typography.labelMedium,
                color = StudioBlue
            )
        }

        when (submissionsState) {
            is UiState.Loading -> {
                if (!isAuthenticated) {
                    Text(
                        text = "Sign in with Google to view live community submissions and compete for today's VIP Pass.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                Text(
                    text = submissionsState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            is UiState.Success -> {
                if (submissionsState.data.isEmpty()) {
                    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                        Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = VipGold, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("Be today's first featured entry!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Submit an enhanced ${theme.title} photo above to win extra credits or a 24h VIP Pass.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    submissionsState.data.forEach { entry ->
                        ChallengeSubmissionCard(
                            entry = entry,
                            onUpvote = { onUpvote(entry) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(74.dp))
    }
}

@Composable
private fun ChallengeSubmissionCard(
    entry: ChallengeSubmission,
    onUpvote: () -> Unit
) {
    val decodedThumb = remember(entry.previewBase64) {
        ImageEnhancerEngine.decodeBase64ToBitmap(entry.previewBase64)
    }
    val prize = ChallengePrizeTier.fromRaw(entry.prizeAwarded)

    GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("challenge_entry_${entry.id}"),
        cornerRadius = 22.dp,
        borderBrush = if (entry.featured) VipGoldVioletBrush else null
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (decodedThumb != null) {
                    Image(
                        bitmap = decodedThumb.asImageBitmap(),
                        contentDescription = entry.caption,
                        modifier = Modifier.size(76.dp).clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = CatalogData.drawableForKey("img_demo_city_nights")),
                        contentDescription = entry.caption,
                        modifier = Modifier.size(76.dp).clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(entry.authorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (entry.featured) VipCrownBadge("TOP PICK")
                    }
                    Text(entry.caption, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${entry.themeTitle} • ${entry.toolUsed} • Score ${(entry.creativityScore + entry.qualityScore) / 2}/100",
                        style = MaterialTheme.typography.labelSmall,
                        color = VipGold
                    )
                }
            }

            if (entry.judgeFeedback.isNotBlank()) {
                Text(
                    text = "AI Judge: \"${entry.judgeFeedback}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = VipGold.copy(alpha = 0.14f),
                    shape = CircleShape
                ) {
                    Text(
                        text = prize.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = VipGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    onClick = onUpvote,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.heightIn(min = 40.dp).testTag("upvote_button_${entry.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.ThumbUp, contentDescription = "Upvote entry", tint = StudioBlue, modifier = Modifier.size(16.dp))
                        Text("${entry.votesCount}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
