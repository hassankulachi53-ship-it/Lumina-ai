package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.ImageEnhancerEngine
import com.example.data.LocalPrefsState
import com.example.data.UserPreferencesStore
import com.example.firebase.attemptAutoSignIn
import com.example.firebase.authStateFlow
import com.example.firebase.signOut
import com.example.model.CatalogData
import com.example.model.EnhancementTool
import com.example.model.MediaType
import com.example.model.UiState
import com.example.ui.components.DownloadResultSheet
import com.example.ui.components.FloatingVipButton
import com.example.ui.components.VipPaywallModal
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyWorkScreen
import com.example.ui.screens.OnboardingAuthScreen
import com.example.ui.screens.SettingsAndAdminScreen
import com.example.ui.screens.StudioEditorScreen
import com.example.ui.screens.WallpapersScreen
import com.example.ui.theme.LuminaTheme
import com.example.viewmodel.CloudAccountViewModel
import com.example.viewmodel.StudioViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

enum class LuminaTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    STUDIO("Studio", Icons.Rounded.AutoFixHigh),
    CHALLENGE("Challenge", Icons.Rounded.EmojiEvents),
    WALLPAPERS("Walls", Icons.Rounded.Wallpaper),
    MY_WORK("My Work", Icons.Rounded.Collections)
}

@Composable
fun LuminaApp(sharedIncomingImageUri: Uri? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsStore = remember { UserPreferencesStore(context.applicationContext) }
    val localPrefs by prefsStore.prefsFlow.collectAsStateWithLifecycle(initialValue = LocalPrefsState())
    val credentialManager = remember { CredentialManager.create(context) }

    val currentUser by Firebase.auth.authStateFlow().collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)
    var guestModeActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { guestModeActive = false },
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val studioViewModel: StudioViewModel = viewModel(factory = StudioViewModel.Factory)

    // Preload default demo into Studio on first launch or load incoming shared image
    LaunchedEffect(sharedIncomingImageUri) {
        if (sharedIncomingImageUri != null) {
            studioViewModel.loadUserMediaUri(context, sharedIncomingImageUri, isVideo = false)
        } else if (studioViewModel.uiState.value.rawSourceBitmap == null) {
            studioViewModel.loadDemoSample(
                context = context,
                drawableResId = R.drawable.img_demo_city_nights,
                title = "City Nights Demo",
                tool = EnhancementTool.SMART_ENHANCE
            )
        }
    }

    val user = currentUser
    if (user == null && !guestModeActive) {
        LuminaTheme(darkTheme = true) {
            OnboardingAuthScreen(
                onAuthSuccess = {
                    guestModeActive = false
                    scope.launch { prefsStore.setOnboardingCompleted(true) }
                },
                onContinueAsGuest = {
                    guestModeActive = true
                    scope.launch { prefsStore.setOnboardingCompleted(true) }
                }
            )
        }
    } else if (user != null) {
        key(user.uid) {
            val cloudVm: CloudAccountViewModel = viewModel(
                key = user.uid,
                factory = CloudAccountViewModel.provideFactory(user.uid)
            )
            MainAuthenticatedOrGuestShell(
                context = context,
                studioViewModel = studioViewModel,
                cloudVm = cloudVm,
                isAuthenticated = true,
                localPrefs = localPrefs,
                prefsStore = prefsStore,
                sharedIncomingImageUri = sharedIncomingImageUri,
                onSignOut = {
                    signOut(credentialManager, scope) {
                        guestModeActive = false
                    }
                },
                onRequestSignIn = { guestModeActive = false }
            )
        }
    } else {
        MainAuthenticatedOrGuestShell(
            context = context,
            studioViewModel = studioViewModel,
            cloudVm = null,
            isAuthenticated = false,
            localPrefs = localPrefs,
            prefsStore = prefsStore,
            sharedIncomingImageUri = sharedIncomingImageUri,
            onSignOut = { guestModeActive = false },
            onRequestSignIn = { guestModeActive = false }
        )
    }
}

@Composable
private fun MainAuthenticatedOrGuestShell(
    context: Context,
    studioViewModel: StudioViewModel,
    cloudVm: CloudAccountViewModel?,
    isAuthenticated: Boolean,
    localPrefs: LocalPrefsState,
    prefsStore: UserPreferencesStore,
    sharedIncomingImageUri: Uri?,
    onSignOut: () -> Unit,
    onRequestSignIn: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val studioState by studioViewModel.uiState.collectAsStateWithLifecycle()

    val profileState = cloudVm?.userProfileState?.collectAsStateWithLifecycle()?.value
    val profile = (profileState as? UiState.Success)?.data
    val savedWorkState = cloudVm?.savedWorkState?.collectAsStateWithLifecycle()?.value ?: UiState.Success(emptyList())
    val challengeState = cloudVm?.challengeSubmissionsState?.collectAsStateWithLifecycle()?.value ?: UiState.Loading
    val myPaymentsState = cloudVm?.myPaymentsState?.collectAsStateWithLifecycle()?.value ?: UiState.Success(emptyList())
    val cloudWallpapers = cloudVm?.cloudWallpapers?.collectAsStateWithLifecycle()?.value ?: emptyList()
    val adminPayments = cloudVm?.adminPaymentsState?.collectAsStateWithLifecycle()?.value ?: emptyList()
    val adminUsers = cloudVm?.adminUsersState?.collectAsStateWithLifecycle()?.value ?: emptyList()
    val challengeSubmitting = cloudVm?.challengeSubmitting?.collectAsStateWithLifecycle()?.value ?: false
    val lastChallengeResult = cloudVm?.lastChallengeResult?.collectAsStateWithLifecycle()?.value
    val cloudToast = cloudVm?.toastMessage?.collectAsStateWithLifecycle()?.value

    val darkTheme = profile?.darkThemeEnabled ?: localPrefs.darkThemeEnabled
    val currency = profile?.let { com.example.model.CurrencyOption.fromRaw(it.preferredCurrency) } ?: localPrefs.currency
    val language = profile?.let { com.example.model.AppLanguage.fromRaw(it.preferredLanguage) } ?: localPrefs.language
    val notificationsEnabled = profile?.notificationsEnabled ?: localPrefs.notificationsEnabled
    val isVip = profile?.hasActiveVip() == true
    val isAdmin = cloudVm?.isAdmin == true
    val activeChallengeTheme = remember { CatalogData.activeChallengeTheme() }

    var activeTab by remember { mutableStateOf(if (sharedIncomingImageUri != null) LuminaTab.STUDIO else LuminaTab.HOME) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    var showDownloadSheet by remember { mutableStateOf(false) }

    if (showSettingsScreen) {
        BackHandler { showSettingsScreen = false }
    } else if (activeTab != LuminaTab.HOME) {
        BackHandler { activeTab = LuminaTab.HOME }
    }

    LaunchedEffect(cloudToast) {
        if (cloudToast != null) {
            snackbarHostState.showSnackbar(cloudToast)
            cloudVm?.clearToast()
        }
    }

    // Zero-Permission Android Photo Pickers
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            studioViewModel.loadUserMediaUri(context, uri, isVideo = false)
            activeTab = LuminaTab.STUDIO
            showSettingsScreen = false
        }
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            studioViewModel.loadUserMediaUri(context, uri, isVideo = true)
            activeTab = LuminaTab.STUDIO
            showSettingsScreen = false
        }
    }

    val batchPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(100)
    ) { uris ->
        if (uris.isNotEmpty()) {
            studioViewModel.runBatchQueue(context, uris)
            activeTab = LuminaTab.STUDIO
        }
    }

    LuminaTheme(darkTheme = darkTheme) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets(top = 0.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (showSettingsScreen) {
                                    IconButton(onClick = { showSettingsScreen = false }) {
                                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                                    }
                                    Text("Settings & Admin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                } else {
                                    Text(
                                        text = when (activeTab) {
                                            LuminaTab.HOME -> "Lumina AI"
                                            LuminaTab.STUDIO -> "AI Studio"
                                            LuminaTab.CHALLENGE -> "Daily Challenge"
                                            LuminaTab.WALLPAPERS -> "Wallpapers"
                                            LuminaTab.MY_WORK -> "My Work"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 6.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showSettingsScreen = !showSettingsScreen },
                                modifier = Modifier.testTag("top_settings_button")
                            ) {
                                Icon(
                                    Icons.Rounded.Settings,
                                    contentDescription = "Settings and Admin Dashboard"
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    if (!useNavigationRail && !showSettingsScreen) {
                        NavigationBar {
                            LuminaTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = activeTab == tab,
                                    onClick = { activeTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label, maxLines = 1) },
                                    modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail && !showSettingsScreen) {
                        NavigationRail(modifier = Modifier.fillMaxHeight()) {
                            LuminaTab.entries.forEach { tab ->
                                NavigationRailItem(
                                    selected = activeTab == tab,
                                    onClick = { activeTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label) }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        if (showSettingsScreen) {
                            SettingsAndAdminScreen(
                                profile = profile,
                                isAuthenticated = isAuthenticated,
                                isAdmin = isAdmin,
                                darkThemeEnabled = darkTheme,
                                language = language,
                                currency = currency,
                                notificationsEnabled = notificationsEnabled,
                                autoSuggestEnabled = localPrefs.autoSuggestEnabled,
                                myPaymentsState = myPaymentsState,
                                adminPayments = adminPayments,
                                adminUsers = adminUsers,
                                challengeSubmissions = (challengeState as? UiState.Success)?.data ?: emptyList(),
                                onToggleDarkTheme = { enabled ->
                                    scope.launch { prefsStore.setDarkTheme(enabled) }
                                    cloudVm?.updatePreferences(darkThemeEnabled = enabled)
                                },
                                onChangeLanguage = { lang ->
                                    scope.launch { prefsStore.setLanguage(lang) }
                                    cloudVm?.updatePreferences(language = lang)
                                },
                                onChangeCurrency = { cur ->
                                    scope.launch { prefsStore.setCurrency(cur) }
                                    cloudVm?.updatePreferences(currency = cur)
                                },
                                onToggleNotifications = { enabled ->
                                    scope.launch { prefsStore.setNotificationsEnabled(enabled) }
                                    cloudVm?.updatePreferences(notificationsEnabled = enabled)
                                },
                                onToggleAutoSuggest = { enabled ->
                                    scope.launch { prefsStore.setAutoSuggestEnabled(enabled) }
                                },
                                onRedeemReferral = { code -> cloudVm?.redeemReferralCode(code) },
                                onOpenPaywall = { showPaywall = true },
                                onAdminReviewPayment = { req, ok, note -> cloudVm?.adminReviewPayment(req, ok, note) },
                                onAdminToggleFeatured = { sub -> cloudVm?.adminToggleFeatured(sub) },
                                onAdminPublishWallpaper = { t, c, vip -> cloudVm?.adminPublishWallpaper(t, c, vip) },
                                onSignOut = onSignOut,
                                onDeleteAccount = {
                                    cloudVm?.deleteAccountData { onSignOut() }
                                },
                                onRequestSignIn = onRequestSignIn
                            )
                        } else {
                            when (activeTab) {
                                LuminaTab.HOME -> HomeScreen(
                                    profile = profile,
                                    language = language,
                                    isAuthenticated = isAuthenticated,
                                    challengeTheme = activeChallengeTheme,
                                    onPickPhoto = {
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onPickVideo = {
                                        videoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    },
                                    onPickBatch = {
                                        batchPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onSelectTool = { tool ->
                                        studioViewModel.applyTool(tool, isVip = isVip)
                                        activeTab = LuminaTab.STUDIO
                                    },
                                    onApplyTrendingStyle = { style ->
                                        studioViewModel.loadDemoSample(
                                            context = context,
                                            drawableResId = CatalogData.drawableForKey(style.sampleDrawableKey),
                                            title = style.title,
                                            tool = style.tool,
                                            filterName = style.filterName
                                        )
                                        activeTab = LuminaTab.STUDIO
                                    },
                                    onOpenDailyChallenge = { activeTab = LuminaTab.CHALLENGE },
                                    onOpenWallpapers = { activeTab = LuminaTab.WALLPAPERS },
                                    onClaimDailyStreak = { cloudVm?.claimDailyStreak() },
                                    onOpenPaywall = { showPaywall = true },
                                    onRequestSignIn = onRequestSignIn
                                )

                                LuminaTab.STUDIO -> StudioEditorScreen(
                                    state = studioState,
                                    isVip = isVip,
                                    onPickPhoto = {
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onSmartEnhance = { studioViewModel.runSmartEnhanceWithAi(isVip = isVip) },
                                    onSelectTool = { tool, prompt -> studioViewModel.applyTool(tool, prompt, isVip) },
                                    onStrengthChange = { studioViewModel.setStrength(it) },
                                    onUpdateParams = { studioViewModel.updateParameters(it) },
                                    onOpenDownloadSheet = { showDownloadSheet = true },
                                    onEnterChallengeWithCurrent = { activeTab = LuminaTab.CHALLENGE },
                                    onOpenPaywall = { showPaywall = true },
                                    onDismissRating = {
                                        studioViewModel.dismissRatingPrompt()
                                        scope.launch { snackbarHostState.showSnackbar("Thank you for rating Lumina AI!") }
                                    }
                                )

                                LuminaTab.CHALLENGE -> DailyChallengeScreen(
                                    theme = activeChallengeTheme,
                                    currentEnhancedBitmap = studioState.afterBitmap,
                                    currentToolTitle = studioState.params.tool.title,
                                    submissionsState = challengeState,
                                    isSubmitting = challengeSubmitting,
                                    lastResult = lastChallengeResult,
                                    isAuthenticated = isAuthenticated,
                                    onPickChallengePhoto = {
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onOpenStudioForTheme = {
                                        studioViewModel.loadDemoSample(
                                            context = context,
                                            drawableResId = CatalogData.drawableForKey(activeChallengeTheme.sampleDrawableKey),
                                            title = "${activeChallengeTheme.title} Challenge",
                                            tool = activeChallengeTheme.recommendedTool
                                        )
                                        activeTab = LuminaTab.STUDIO
                                    },
                                    onSubmitEntry = { bmp, caption ->
                                        cloudVm?.submitToDailyChallenge(
                                            bitmap = bmp,
                                            theme = activeChallengeTheme,
                                            toolUsed = studioState.params.tool.title,
                                            caption = caption
                                        )
                                    },
                                    onUpvote = { sub ->
                                        if (!isAuthenticated) onRequestSignIn()
                                        else cloudVm?.upvoteChallengeSubmission(sub)
                                    },
                                    onRequireSignIn = onRequestSignIn
                                )

                                LuminaTab.WALLPAPERS -> WallpapersScreen(
                                    cloudWallpapers = cloudWallpapers,
                                    isVip = isVip,
                                    dailyAutoWallpaperEnabled = localPrefs.dailyAutoWallpaperEnabled,
                                    onToggleDailyAutoWallpaper = { enabled ->
                                        scope.launch { prefsStore.setDailyAutoWallpaperEnabled(enabled) }
                                    },
                                    onConvertMyPhotoToWallpaper = {
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onOpenPaywall = { showPaywall = true },
                                    onStatusMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                )

                                LuminaTab.MY_WORK -> MyWorkScreen(
                                    savedWorkState = savedWorkState,
                                    isAuthenticated = isAuthenticated,
                                    onToggleFavorite = { item -> cloudVm?.toggleWorkFavorite(item) },
                                    onDeleteItem = { item -> cloudVm?.deleteWorkItem(item) },
                                    onReEditInStudio = { item ->
                                        val tool = EnhancementTool.entries.firstOrNull { it.title == item.toolUsed }
                                            ?: EnhancementTool.SMART_ENHANCE
                                        studioViewModel.applyTool(tool, isVip = isVip)
                                        activeTab = LuminaTab.STUDIO
                                    },
                                    onRequestSignIn = onRequestSignIn,
                                    onStatusMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                )
                            }
                        }

                        // Floating Gold VIP Button
                        if (!showSettingsScreen) {
                            FloatingVipButton(
                                isVip = isVip,
                                onClick = { showPaywall = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showPaywall) {
            VipPaywallModal(
                selectedCurrency = currency,
                isAuthenticated = isAuthenticated,
                onCurrencyChange = { cur ->
                    scope.launch { prefsStore.setCurrency(cur) }
                    cloudVm?.updatePreferences(currency = cur)
                },
                onStartFreeTrial = { cloudVm?.activateThreeDayTrial() },
                onSubmitManualPayment = { method, plan, amt, tid, phone, proof ->
                    cloudVm?.submitManualPayment(method, plan, amt, tid, phone, proof)
                },
                onRequireSignIn = {
                    showPaywall = false
                    onRequestSignIn()
                },
                onDismiss = { showPaywall = false }
            )
        }

        if (showDownloadSheet) {
            DownloadResultSheet(
                isVip = isVip,
                isVideoSource = studioState.isVideoSource,
                selectedQuality = studioState.selectedQuality,
                selectedFormat = studioState.selectedFormat,
                estimatedSizeLabel = studioState.estimatedSizeLabel,
                isDownloading = studioState.isDownloading,
                downloadProgress = studioState.downloadProgress,
                downloadSuccess = studioState.downloadSuccess,
                adWatermarkRemovedOnce = studioState.adWatermarkRemovedOnce,
                onSelectQualityFormat = { q, f -> studioViewModel.selectQualityAndFormat(q, f) },
                onWatchRewardAdForWatermark = { studioViewModel.unlockAdWatermarkRemoval() },
                onOpenPaywall = {
                    showDownloadSheet = false
                    showPaywall = true
                },
                onDownloadToGallery = {
                    studioViewModel.downloadCurrentResult(context, isVip) { bmp, params, quality ->
                        cloudVm?.saveEnhancedWork(
                            bitmap = bmp,
                            title = studioState.sourceTitle,
                            params = params,
                            quality = quality,
                            mediaType = if (studioState.isVideoSource) MediaType.VIDEO else MediaType.PHOTO
                        )
                    }
                },
                onSaveAsWallpaper = {
                    val bmp = studioState.afterBitmap ?: return@DownloadResultSheet
                    scope.launch {
                        val res = ImageEnhancerEngine.setAsWallpaper(context, bmp)
                        snackbarHostState.showSnackbar(
                            if (res.isSuccess) "Set as device wallpaper!" else "Could not set wallpaper."
                        )
                    }
                },
                onShare = { platform ->
                    val bmp = studioState.afterBitmap ?: return@DownloadResultSheet
                    scope.launch {
                        val shareRes = ImageEnhancerEngine.prepareShareIntent(
                            context = context,
                            bitmap = bmp,
                            format = studioState.selectedFormat,
                            caption = "Enhanced with Lumina AI ($platform) ✨"
                        )
                        if (shareRes.isSuccess) {
                            context.startActivity(shareRes.getOrNull())
                        }
                    }
                },
                onDismiss = { showDownloadSheet = false }
            )
        }
    }
}
