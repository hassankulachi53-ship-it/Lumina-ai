package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.CatalogData
import com.example.model.CurrencyOption
import com.example.model.PaymentMethod
import com.example.model.PlanTier
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldVioletBrush
import com.example.ui.theme.VipViolet
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VipPaywallModal(
    selectedCurrency: CurrencyOption,
    isAuthenticated: Boolean,
    onCurrencyChange: (CurrencyOption) -> Unit,
    onStartFreeTrial: () -> Unit,
    onSubmitManualPayment: (PaymentMethod, PlanTier, String, String, String, String) -> Unit,
    onRequireSignIn: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPlan by remember { mutableStateOf(CatalogData.subscriptionPlans.first()) }
    var showExitOffer by remember { mutableStateOf(false) }
    var showManualPaymentForm by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.EASYPAISA) }
    var txRef by remember { mutableStateOf("") }
    var senderPhone by remember { mutableStateOf("") }
    var proofFileLabel by remember { mutableStateOf("receipt_screenshot.png") }
    var infoNotice by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = { showExitOffer = true },
        sheetState = sheetState,
        containerColor = Color(0xFF0D0D15),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VipCrownBadge(label = "LUMINA VIP PRO")
                IconButton(
                    onClick = { showExitOffer = true },
                    modifier = Modifier.testTag("paywall_close_button")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close Paywall", tint = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Unlock Flagship 8K & Cinema Video",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "4K Video AI, Batch 100 Photos, AI Avatars, Pro Headshots & Zero Watermarks.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f)
            )

            Spacer(Modifier.height(14.dp))

            // Local Currency Switcher (PKR, USD, GBP, AED)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CurrencyOption.entries.forEach { cur ->
                    FilterChip(
                        selected = cur == selectedCurrency,
                        onClick = { onCurrencyChange(cur) },
                        label = { Text("${cur.code} (${cur.symbol.trim()})") },
                        modifier = Modifier.testTag("currency_chip_${cur.code.lowercase()}")
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Free vs VIP Quick Comparison
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Feature", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            Text("Free", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                            Text("VIP", style = MaterialTheme.typography.labelMedium, color = VipGold, fontWeight = FontWeight.Bold)
                        }
                    }
                    ComparisonLine("Smart HDR, Unblur, 2x/4x Photo", "5/day", "Unlimited")
                    ComparisonLine("4K & 8K No-Watermark Export", "Watermark", "Unlocked")
                    ComparisonLine("AI Video 4K & Cinema Frame Boost", "Locked", "VIP Fast")
                    ComparisonLine("Batch Enhance (up to 100 photos)", "1 photo", "100 Batch")
                    ComparisonLine("AI Avatars, Headshots & Live Walls", "Limited", "All Access")
                }
            }

            Spacer(Modifier.height(14.dp))

            // Plan Cards
            CatalogData.subscriptionPlans.forEach { plan ->
                val isSelected = plan.planTier == selectedPlan.planTier
                Surface(
                    onClick = { selectedPlan = plan },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF1D1830) else ObsidianCard,
                    border = if (isSelected) BorderStroke(1.8.dp, VipGoldVioletBrush) else BorderStroke(1.dp, Color(0x22FFFFFF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .testTag("plan_card_${plan.planTier.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (plan.badge != null) {
                                Text(
                                    text = plan.badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = VipGold
                                )
                                Spacer(Modifier.height(2.dp))
                            }
                            Text(
                                text = plan.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (plan.trialText != null) {
                                Text(
                                    text = plan.trialText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.70f)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = plan.priceFor(selectedCurrency),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = VipGold
                            )
                            Text(
                                text = plan.billingPeriod,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            if (!showManualPaymentForm) {
                VipGoldGradientButton(
                    text = if (selectedPlan.planTier == PlanTier.VIP_YEARLY) {
                        "Start 3-Day Free Trial • ${selectedPlan.priceFor(selectedCurrency)}"
                    } else {
                        "Activate ${selectedPlan.title} • ${selectedPlan.priceFor(selectedCurrency)}"
                    },
                    onClick = {
                        if (!isAuthenticated) {
                            onRequireSignIn()
                        } else if (selectedPlan.planTier == PlanTier.VIP_YEARLY) {
                            onStartFreeTrial()
                            onDismiss()
                        } else {
                            infoNotice = "Google Play Billing in test mode — use 3-Day Trial or EasyPaisa/JazzCash below for instant approval."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_vip_trial_button")
                )

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        if (!isAuthenticated) onRequireSignIn() else showManualPaymentForm = true
                    },
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("manual_payment_button")
                ) {
                    Icon(
                        Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Pay via EasyPaisa / JazzCash (Pakistan)",
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                // EasyPaisa / JazzCash Manual Verification Form
                GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp, borderBrush = VipGoldVioletBrush) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "EasyPaisa / JazzCash Verification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VipGold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PaymentMethod.entries.forEach { method ->
                                FilterChip(
                                    selected = selectedPaymentMethod == method,
                                    onClick = { selectedPaymentMethod = method },
                                    label = { Text(method.displayName) },
                                    modifier = Modifier.testTag("method_chip_${method.name.lowercase()}")
                                )
                            }
                        }
                        Text(
                            text = "Send ${selectedPlan.priceFor(selectedCurrency)} to ${selectedPaymentMethod.accountTitle} (${selectedPaymentMethod.accountNumber}) and enter your TID below:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        OutlinedTextField(
                            value = txRef,
                            onValueChange = { txRef = it },
                            label = { Text("Transaction ID (TID)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("payment_tid_input")
                        )
                        OutlinedTextField(
                            value = senderPhone,
                            onValueChange = { senderPhone = it },
                            label = { Text("Sender Mobile Number (e.g. 03001234567)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("payment_phone_input")
                        )
                        OutlinedTextField(
                            value = proofFileLabel,
                            onValueChange = { proofFileLabel = it },
                            label = { Text("Receipt Screenshot Reference / Note") },
                            leadingIcon = { Icon(Icons.Rounded.UploadFile, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("payment_proof_input")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = { showManualPaymentForm = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }
                            Button(
                                onClick = {
                                    onSubmitManualPayment(
                                        selectedPaymentMethod,
                                        selectedPlan.planTier,
                                        selectedPlan.priceFor(selectedCurrency),
                                        txRef,
                                        senderPhone,
                                        proofFileLabel
                                    )
                                    showManualPaymentForm = false
                                    onDismiss()
                                },
                                enabled = txRef.trim().length >= 3 && senderPhone.trim().length >= 6,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = ObsidianBlack),
                                modifier = Modifier.weight(1.4f).testTag("submit_manual_payment_button")
                            ) {
                                Text("Submit for Approval", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (infoNotice != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = infoNotice!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VipGold
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Cancel anytime • Restore Purchases • Terms & Privacy",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showExitOffer) {
        ExitOfferCountdownDialog(
            currency = selectedCurrency,
            onClaimOffer = {
                showExitOffer = false
                if (!isAuthenticated) {
                    onRequireSignIn()
                } else {
                    onStartFreeTrial()
                    onDismiss()
                }
            },
            onDismissAll = {
                showExitOffer = false
                onDismiss()
            }
        )
    }
}

@Composable
private fun ComparisonLine(feature: String, freeVal: String, vipVal: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = freeVal, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = VipGold, modifier = Modifier.size(14.dp))
                Text(text = vipVal, style = MaterialTheme.typography.labelSmall, color = VipGold, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ExitOfferCountdownDialog(
    currency: CurrencyOption,
    onClaimOffer: () -> Unit,
    onDismissAll: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(299) }
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
    }
    val mins = secondsLeft / 60
    val secs = secondsLeft % 60

    AlertDialog(
        onDismissRequest = onDismissAll,
        containerColor = Color(0xFF141422),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(VipGoldVioletBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.LocalOffer, contentDescription = "Special Offer", tint = ObsidianBlack)
            }
        },
        title = {
            Text("Wait! Exclusive 80% VIP Welcome", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Unlock 3 FREE days of VIP right now — 8K export, Cinema Video AI & zero watermarks.")
                Surface(
                    color = Color(0x28F5C542),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = VipGold, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Offer expires in %02d:%02d • Yearly ${CatalogData.subscriptionPlans.first().priceFor(currency)}".format(mins, secs),
                            style = MaterialTheme.typography.labelLarge,
                            color = VipGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onClaimOffer,
                colors = ButtonDefaults.buttonColors(containerColor = VipGold, contentColor = ObsidianBlack),
                modifier = Modifier.testTag("claim_exit_offer_button")
            ) {
                Icon(Icons.Rounded.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Claim 3-Day Free VIP", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissAll) {
                Text("No thanks", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}
