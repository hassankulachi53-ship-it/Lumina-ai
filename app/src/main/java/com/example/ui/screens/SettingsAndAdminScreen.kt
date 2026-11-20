package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.ChallengeSubmission
import com.example.model.CurrencyOption
import com.example.model.PaymentRequest
import com.example.model.PaymentStatus
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.ui.components.GlassCard
import com.example.ui.components.VipCrownBadge
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldVioletBrush

@Composable
fun SettingsAndAdminScreen(
    profile: UserProfile?,
    isAuthenticated: Boolean,
    isAdmin: Boolean,
    darkThemeEnabled: Boolean,
    language: AppLanguage,
    currency: CurrencyOption,
    notificationsEnabled: Boolean,
    autoSuggestEnabled: Boolean,
    myPaymentsState: UiState<List<PaymentRequest>>,
    adminPayments: List<PaymentRequest>,
    adminUsers: List<UserProfile>,
    challengeSubmissions: List<ChallengeSubmission>,
    onToggleDarkTheme: (Boolean) -> Unit,
    onChangeLanguage: (AppLanguage) -> Unit,
    onChangeCurrency: (CurrencyOption) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleAutoSuggest: (Boolean) -> Unit,
    onRedeemReferral: (String) -> Unit,
    onOpenPaywall: () -> Unit,
    onAdminReviewPayment: (PaymentRequest, Boolean, String) -> Unit,
    onAdminToggleFeatured: (ChallengeSubmission) -> Unit,
    onAdminPublishWallpaper: (String, String, Boolean) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onRequestSignIn: () -> Unit
) {
    val context = LocalContext.current
    var referralInput by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showAdminPanel by remember { mutableStateOf(false) }
    var adminTab by remember { mutableIntStateOf(0) } // 0=Payments, 1=Users, 2=Challenge, 3=Wallpapers & Launch Pack
    var wpTitle by remember { mutableStateOf("") }
    var wpCategory by remember { mutableStateOf("AMOLED") }
    var wpVipOnly by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onToggleNotifications(granted)
    }

    val isVip = profile?.hasActiveVip() == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Settings & Account", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

        // Subscription & Profile Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            borderBrush = if (isVip) VipGoldVioletBrush else null
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile?.displayName ?: "Guest Studio Preview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile?.email?.ifBlank { "Signed in with Google" } ?: "Sign in to sync VIP & credits",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isVip) VipCrownBadge("VIP ACTIVE") else VipCrownBadge(profile?.resolvedPlanTier()?.displayName ?: "FREE")
                }

                if (profile != null) {
                    Text(
                        text = "Credits: ${profile.dailyCreditsRemaining} daily + ${profile.bonusCredits} bonus • Streak: ${profile.streakCount} days",
                        style = MaterialTheme.typography.labelLarge,
                        color = VipGold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onOpenPaywall,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VipGold, contentColor = ObsidianBlack),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("settings_manage_vip_button")
                    ) {
                        Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isVip) "Manage VIP" else "Upgrade VIP", fontWeight = FontWeight.Bold)
                    }
                    if (!isAuthenticated) {
                        OutlinedButton(
                            onClick = onRequestSignIn,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) {
                            Text("Sign In")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("sign_out_button")
                        ) {
                            Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sign Out")
                        }
                    }
                }
            }
        }

        // Referral & Growth Card (Invite a friend, both get 3 free VIP days)
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.CardGiftcard, contentDescription = null, tint = VipGold)
                    Text("Invite Friends • Get 3 Free VIP Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                val code = profile?.referralCode ?: "LUMINA26"
                Text(
                    text = "Your Invite Code: $code (Share to unlock 3 free VIP days + 5 HD credits for both!)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Enhance photos & 4K videos in one tap with Lumina AI! Use my code $code for 3 FREE VIP Days: https://lumina.studio/invite/$code"
                            )
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Lumina AI Invite"))
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("share_referral_button")
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share Invite Link")
                }

                if (isAuthenticated && profile?.referredByCode.isNullOrBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = referralInput,
                            onValueChange = { referralInput = it.uppercase().take(20) },
                            label = { Text("Enter Friend's Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("referral_code_input")
                        )
                        Button(
                            onClick = { onRedeemReferral(referralInput) },
                            enabled = referralInput.trim().length >= 4,
                            modifier = Modifier.heightIn(min = 52.dp).testTag("redeem_referral_button")
                        ) {
                            Text("Claim")
                        }
                    }
                }
            }
        }

        // My Manual Payment Verification Status
        if (isAuthenticated && myPaymentsState is UiState.Success && myPaymentsState.data.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("EasyPaisa / JazzCash Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    myPaymentsState.data.forEach { req ->
                        val statusColor = when (PaymentStatus.fromRaw(req.status)) {
                            PaymentStatus.APPROVED -> EmeraldSuccess
                            PaymentStatus.REJECTED -> CoralAlert
                            PaymentStatus.PENDING -> VipGold
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${req.paymentMethod} • ${req.transactionRef}", style = MaterialTheme.typography.bodyMedium)
                            Text(PaymentStatus.fromRaw(req.status).label, color = statusColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Appearance, Language, Currency, Notifications
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Preferences", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Dark Obsidian Mode", style = MaterialTheme.typography.titleMedium)
                        Text("Switch between Dark-First & Light Porcelain", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = darkThemeEnabled,
                        onCheckedChange = onToggleDarkTheme,
                        modifier = Modifier.testTag("theme_toggle_switch")
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Smart Notifications & Seasonal Offers", style = MaterialTheme.typography.titleMedium)
                        Text("Daily Challenge reminders, Eid & New Year deals", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enable ->
                            if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onToggleNotifications(enable)
                            }
                        }
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Auto-Suggest New Gallery Photos", style = MaterialTheme.typography.titleMedium)
                        Text("Opt-in quick enhance prompt for new shots", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = autoSuggestEnabled, onCheckedChange = onToggleAutoSuggest)
                }

                Text("Language", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AppLanguage.entries) { lang ->
                        FilterChip(
                            selected = language == lang,
                            onClick = { onChangeLanguage(lang) },
                            label = { Text("${lang.label} (${lang.nativeLabel})") },
                            modifier = Modifier.testTag("lang_chip_${lang.name.lowercase()}")
                        )
                    }
                }

                Text("Local Currency", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CurrencyOption.entries.forEach { cur ->
                        FilterChip(
                            selected = currency == cur,
                            onClick = { onChangeCurrency(cur) },
                            label = { Text(cur.code) }
                        )
                    }
                }
            }
        }

        // Admin Console Toggle (Unlocked for verified Admin hassankulachi53@gmail.com)
        GlassCard(
            modifier = Modifier.fillMaxWidth().testTag("admin_console_card"),
            cornerRadius = 22.dp,
            borderBrush = if (isAdmin) VipGoldVioletBrush else null,
            onClick = { showAdminPanel = !showAdminPanel }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = VipGold)
                    Column {
                        Text("Admin Dashboard & Launch Pack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isAdmin) "Verified Admin • Payment Approvals & Curation" else "Store Listing, Policy Docs & Admin Info",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(if (showAdminPanel) "Hide" else "Open", color = StudioBlue, fontWeight = FontWeight.Bold)
            }
        }

        if (showAdminPanel) {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp, borderBrush = VipGoldVioletBrush) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Payments", "Users", "Challenge", "Wallpapers & Launch")) { label ->
                            val idx = listOf("Payments", "Users", "Challenge", "Wallpapers & Launch").indexOf(label)
                            FilterChip(selected = adminTab == idx, onClick = { adminTab = idx }, label = { Text(label) })
                        }
                    }

                    when (adminTab) {
                        0 -> {
                            if (!isAdmin) {
                                Text("Sign in with admin account (hassankulachi53@gmail.com) to approve EasyPaisa/JazzCash payments.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else if (adminPayments.isEmpty()) {
                                Text("No manual payment requests in queue yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                adminPayments.forEach { req ->
                                    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("${req.userName} • ${req.paymentMethod} • ${req.amountDisplay}", fontWeight = FontWeight.Bold)
                                            Text("TID: ${req.transactionRef} • Phone: ${req.senderPhone} • Proof: ${req.proofSummary}", style = MaterialTheme.typography.bodyMedium)
                                            Text("Status: ${req.status}", color = VipGold, style = MaterialTheme.typography.labelLarge)
                                            if (req.status == PaymentStatus.PENDING.name) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = { onAdminReviewPayment(req, true, "Approved by Admin") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = ObsidianBlack),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Approve VIP")
                                                    }
                                                    OutlinedButton(
                                                        onClick = { onAdminReviewPayment(req, false, "TID mismatch") },
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Reject")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (!isAdmin) {
                                Text("Admin privileges required to inspect user list.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Text("Registered Users: ${adminUsers.size}", fontWeight = FontWeight.Bold)
                                adminUsers.take(20).forEach { u ->
                                    Text("• ${u.displayName} (${u.planTier}) • Streak ${u.streakCount}d", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        2 -> {
                            Text("Curate Daily Photo Challenge Showcase", fontWeight = FontWeight.Bold)
                            challengeSubmissions.take(15).forEach { sub ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text("${sub.authorName}: ${sub.caption}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text("Score ${(sub.creativityScore + sub.qualityScore) / 2} • ${sub.votesCount} votes", style = MaterialTheme.typography.labelSmall)
                                    }
                                    if (isAdmin) {
                                        TextButton(onClick = { onAdminToggleFeatured(sub) }) {
                                            Text(if (sub.featured) "Unfeature" else "Feature Top")
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            if (isAdmin) {
                                OutlinedTextField(value = wpTitle, onValueChange = { wpTitle = it }, label = { Text("New Wallpaper Title") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = wpCategory, onValueChange = { wpCategory = it }, label = { Text("Category (e.g. AMOLED, Islamic, Cars)") }, modifier = Modifier.fillMaxWidth())
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("VIP Exclusive Wallpaper")
                                    Switch(checked = wpVipOnly, onCheckedChange = { wpVipOnly = it })
                                }
                                Button(
                                    onClick = {
                                        if (wpTitle.isNotBlank()) {
                                            onAdminPublishWallpaper(wpTitle, wpCategory, wpVipOnly)
                                            wpTitle = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Publish Wallpaper") }
                            }
                            Text("Play Store & App Store Launch Pack", fontWeight = FontWeight.Bold, color = VipGold)
                            Text(
                                text = "• App Name: Lumina AI (9 chars, policy compliant)\n" +
                                    "• Feature Graphic (1024x500): Split obsidian-gold before/after skyline with 'iPhone-Quality Photos & 4K Videos in One Tap'\n" +
                                    "• 8 Store Screenshots: (1) One-Tap Smart HDR, (2) Unblur & Natural Skin, (3) 4K Cinema Video, (4) Daily Photo Challenge Prizes, (5) 42 Pro Filters & Tune, (6) 4K Wallpaper Lock Preview, (7) Old Photo Colorize/Restore, (8) Local PKR/USD/GBP/AED VIP Plans.\n" +
                                    "• Privacy & GDPR: Encrypted TLS uploads, zero broad storage permissions (Photo Picker only), instant Delete Account & Data.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Privacy, Terms, Support, Delete Account
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = EmeraldSuccess)
                    Text("Privacy, GDPR & Policies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "All media processing stays private; cloud AI frames are transmitted encrypted and never retained after analysis. Compliant with GDPR & Google Play policies.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showPrivacyPolicy = true }) { Text("Privacy & Terms") }
                    TextButton(onClick = { showPrivacyPolicy = true }) { Text("Refund Policy & FAQ") }
                }
                if (isAuthenticated) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth().testTag("delete_account_button")
                    ) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = CoralAlert, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Delete My Account & Cloud Data", color = CoralAlert)
                    }
                }
            }
        }

        Spacer(Modifier.height(74.dp))
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account & Data?") },
            text = { Text("This permanently deletes your cloud profile, My Work metadata, and preferences in compliance with GDPR.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralAlert, contentColor = Color.White)
                ) { Text("Delete Permanently") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicy = false },
            title = { Text("Lumina AI Legal & FAQ") },
            text = {
                Text(
                    "Privacy Policy: Lumina AI uses Android Photo Picker (no broad storage scan) and deletes temporary AI analysis frames immediately after processing.\n\n" +
                        "Refund Policy: Cancel your 3-day free trial anytime before billing via Google Play Subscriptions. Manual EasyPaisa/JazzCash passes are non-recurring.\n\n" +
                        "Support: hassankulachi53@gmail.com"
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicy = false }) { Text("Close") }
            }
        )
    }
}
