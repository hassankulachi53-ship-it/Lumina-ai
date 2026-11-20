package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.ImageEnhancerEngine
import com.example.firebase.onGoogleSignInClicked
import com.example.model.EnhancementTool
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.GlassCard
import com.example.ui.components.VipCrownBadge
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldVioletBrush
import com.example.ui.theme.VipViolet
import kotlinx.coroutines.delay

private data class DemoSlide(
    val title: String,
    val subtitle: String,
    val drawableRes: Int,
    val mode: EnhancementTool,
    val beforeTag: String,
    val afterTag: String
)

@Composable
fun OnboardingAuthScreen(
    onAuthSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var showSplash by remember { mutableStateOf(true) }
    var slideIndex by remember { mutableIntStateOf(0) }
    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    val slides = remember {
        listOf(
            DemoSlide(
                title = "Blurry to Flagship HD",
                subtitle = "Restore crisp optics, eyelashes & natural skin texture in one tap.",
                drawableRes = R.drawable.img_demo_portrait_studio,
                mode = EnhancementTool.UNBLUR,
                beforeTag = "BLURRY",
                afterTag = "PRO HD"
            ),
            DemoSlide(
                title = "Old Photos Restored & Colorized",
                subtitle = "Breathe lifelike HDR color and clarity into treasured family memories.",
                drawableRes = R.drawable.img_demo_nature_colors,
                mode = EnhancementTool.COLORIZE,
                beforeTag = "FADED B&W",
                afterTag = "RESTORED"
            ),
            DemoSlide(
                title = "Low-Light to 4K Cinema Video",
                subtitle = "1080p to 4K upscaling, night denoise & flagship Apple-grade tone.",
                drawableRes = R.drawable.img_demo_city_nights,
                mode = EnhancementTool.VIDEO_ENHANCE,
                beforeTag = "LOW-RES",
                afterTag = "4K CINEMA"
            )
        )
    }

    var demoBitmaps by remember { mutableStateOf<Map<Int, Pair<Bitmap, Bitmap>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        delay(1250L)
        showSplash = false
    }

    LaunchedEffect(slideIndex) {
        if (!demoBitmaps.containsKey(slideIndex)) {
            val slide = slides[slideIndex]
            val after = ImageEnhancerEngine.loadBitmapFromDrawable(context, slide.drawableRes, 900)
            val before = ImageEnhancerEngine.createDegradedBeforePreview(after, slide.mode)
            demoBitmaps = demoBitmaps + (slideIndex to (before to after))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF090910), Color(0xFF131122), ObsidianBlack)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VipGoldVioletBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = "Lumina AI Emblem",
                            tint = ObsidianBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Lumina AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                VipCrownBadge("PRO STUDIO")
            }

            Spacer(Modifier.height(14.dp))

            val activeSlide = slides[slideIndex]
            val pair = demoBitmaps[slideIndex]

            BeforeAfterSlider(
                beforeBitmap = pair?.first,
                afterBitmap = pair?.second,
                beforeLabel = activeSlide.beforeTag,
                afterLabel = activeSlide.afterTag,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
            )

            Spacer(Modifier.height(14.dp))

            // Carousel Indicators + Next Demo Pill
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                slides.forEachIndexed { idx, _ ->
                    Surface(
                        onClick = { slideIndex = idx },
                        shape = CircleShape,
                        color = if (idx == slideIndex) VipGold else Color.White.copy(alpha = 0.24f),
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (idx == slideIndex) 28.dp else 8.dp)
                            .testTag("onboarding_dot_$idx")
                    ) {}
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = activeSlide.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = activeSlide.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.74f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { slideIndex = (slideIndex + 1) % slides.size },
                modifier = Modifier.testTag("onboarding_next_demo")
            ) {
                Text("Next Demo (${slideIndex + 1}/3) →", color = StudioBlue, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))

            // Welcome VIP 3-Day Free Trial Banner
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                borderBrush = VipGoldVioletBrush
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Rounded.WorkspacePremium,
                        contentDescription = "Welcome VIP Offer",
                        tint = VipGold,
                        modifier = Modifier.size(30.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome Offer: 3-Day Free VIP Trial",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VipGold
                        )
                        Text(
                            text = "4K Video AI, 8K Export & Zero Watermarks • Cancel anytime",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (authError != null) {
                Text(
                    text = authError!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Interactive Credential Manager Google Sign-In Button
            Button(
                onClick = {
                    authError = null
                    isSigningIn = true
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isSigningIn = false
                            onAuthSuccess()
                        },
                        onAuthError = { msg ->
                            isSigningIn = false
                            authError = msg
                        },
                        scope = scope,
                        onAuthCancelled = { isSigningIn = false }
                    )
                },
                enabled = !isSigningIn,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ObsidianBlack),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .testTag("google_sign_in_button")
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = ObsidianBlack)
                } else {
                    Icon(Icons.Rounded.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Continue with Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Guest Preview Mode (No Sign-Up, local-only studio preview)
            OutlinedButton(
                onClick = onContinueAsGuest,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0x38FFFFFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .testTag("guest_mode_button")
            ) {
                Text(
                    text = "Explore Studio in Guest Mode",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }

        // Cinematic Splash Overlay
        AnimatedVisibility(
            visible = showSplash,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF281B4B), ObsidianBlack)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(VipGoldVioletBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = "Lumina AI",
                            tint = ObsidianBlack,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = "Lumina AI",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "iPhone-quality photos and videos, in one tap",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VipGold
                    )
                }
            }
        }
    }
}
