package com.example.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.AiEnhancementService
import com.example.data.FirestoreRepository
import com.example.data.ImageEnhancerEngine
import com.example.model.AppLanguage
import com.example.model.CatalogData
import com.example.model.ChallengeAiEvaluation
import com.example.model.ChallengePrizeTier
import com.example.model.ChallengeSubmission
import com.example.model.CurrencyOption
import com.example.model.DailyChallengeTheme
import com.example.model.EditParameters
import com.example.model.MediaType
import com.example.model.PaymentMethod
import com.example.model.PaymentRequest
import com.example.model.PlanTier
import com.example.model.QualityOption
import com.example.model.SavedWorkItem
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.model.WallpaperCatalogItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "CloudAccountVM"

class CloudAccountViewModel(
    private val repository: FirestoreRepository,
    private val aiService: AiEnhancementService,
    private val currentUserId: String
) : ViewModel() {

    val isAdmin: Boolean = repository.isCurrentUserAdmin()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _challengeSubmitting = MutableStateFlow(false)
    val challengeSubmitting: StateFlow<Boolean> = _challengeSubmitting.asStateFlow()

    private val _lastChallengeResult = MutableStateFlow<ChallengeSubmission?>(null)
    val lastChallengeResult: StateFlow<ChallengeSubmission?> = _lastChallengeResult.asStateFlow()

    val userProfileState: StateFlow<UiState<UserProfile>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile>> { profile ->
            if (profile != null) UiState.Success(profile) else UiState.Loading
        }
        .catch { error ->
            val fsErr = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsErr == null && error is CancellationException) throw error
            val root = fsErr ?: error
            Log.w(TAG, "Profile stream error", root)
            emit(UiState.Error(root.message ?: "Failed to load profile"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val savedWorkState: StateFlow<UiState<List<SavedWorkItem>>> = repository.observeSavedWork(currentUserId)
        .map<List<SavedWorkItem>, UiState<List<SavedWorkItem>>> { UiState.Success(it) }
        .catch { error ->
            val fsErr = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsErr == null && error is CancellationException) throw error
            emit(UiState.Error((fsErr ?: error).message ?: "Failed to load My Work"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val challengeSubmissionsState: StateFlow<UiState<List<ChallengeSubmission>>> =
        repository.observeChallengeSubmissions()
            .map<List<ChallengeSubmission>, UiState<List<ChallengeSubmission>>> { UiState.Success(it) }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                emit(UiState.Error((fsErr ?: error).message ?: "Failed to load challenge showcase"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val myPaymentsState: StateFlow<UiState<List<PaymentRequest>>> = repository.observeMyPayments(currentUserId)
        .map<List<PaymentRequest>, UiState<List<PaymentRequest>>> { UiState.Success(it) }
        .catch { error ->
            val fsErr = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsErr == null && error is CancellationException) throw error
            emit(UiState.Error((fsErr ?: error).message ?: "Failed to load payment status"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val cloudWallpapers: StateFlow<List<WallpaperCatalogItem>> = repository.observeWallpapers()
        .catch { error ->
            if (error is CancellationException) throw error
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val adminPaymentsState: StateFlow<List<PaymentRequest>> =
        if (isAdmin) {
            repository.observeAllPaymentsForAdmin()
                .catch { if (it is CancellationException) throw it; emit(emptyList()) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
        } else {
            MutableStateFlow<List<PaymentRequest>>(emptyList()).asStateFlow()
        }

    val adminUsersState: StateFlow<List<UserProfile>> =
        if (isAdmin) {
            repository.observeAllUsersForAdmin()
                .catch { if (it is CancellationException) throw it; emit(emptyList()) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
        } else {
            MutableStateFlow<List<UserProfile>>(emptyList()).asStateFlow()
        }

    init {
        viewModelScope.launch {
            repository.ensureUserProfile()
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun clearLastChallengeResult() {
        _lastChallengeResult.value = null
    }

    fun updatePreferences(
        currency: CurrencyOption? = null,
        language: AppLanguage? = null,
        darkThemeEnabled: Boolean? = null,
        notificationsEnabled: Boolean? = null
    ) {
        viewModelScope.launch {
            val res = repository.updatePreferences(
                currentUserId, currency, language, darkThemeEnabled, notificationsEnabled
            )
            if (res.isFailure) {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Preference update failed."
            }
        }
    }

    fun claimDailyStreak() {
        val profile = (userProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            val res = repository.claimDailyCheckIn(profile)
            _toastMessage.value = res.getOrElse { it.localizedMessage ?: "Check-in failed" }
        }
    }

    fun activateThreeDayTrial() {
        viewModelScope.launch {
            val res = repository.activateThreeDayTrial(currentUserId)
            _toastMessage.value = if (res.isSuccess) {
                "3-Day VIP Free Trial activated!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Could not activate trial."
            }
        }
    }

    fun redeemReferralCode(code: String) {
        val profile = (userProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            val res = repository.redeemReferralCode(profile, code)
            _toastMessage.value = res.getOrElse { it.localizedMessage ?: "Invalid referral code." }
        }
    }

    fun saveEnhancedWork(
        bitmap: Bitmap,
        title: String,
        params: EditParameters,
        quality: QualityOption,
        mediaType: MediaType = MediaType.PHOTO,
        folderName: String = "All Edits"
    ) {
        val profile = (userProfileState.value as? UiState.Success)?.data
        viewModelScope.launch {
            val thumb = ImageEnhancerEngine.bitmapToCompactBase64(bitmap)
            if (profile != null && !profile.hasActiveVip()) {
                repository.consumeCredit(profile)
            }
            val res = repository.saveWorkItem(
                SavedWorkItem(
                    userId = currentUserId,
                    title = title,
                    mediaType = mediaType.name,
                    folderName = folderName,
                    favorite = false,
                    toolUsed = params.tool.title,
                    filterName = params.filterName,
                    qualityLabel = quality.label.substringBefore(" ").uppercase().let {
                        if (it in listOf("HD", "4K", "8K", "ORIGINAL")) it else "HD"
                    },
                    enhancementSummary = "${params.tool.title} • ${params.filterName} • ${params.strength.label}",
                    thumbnailBase64 = thumb
                )
            )
            if (res.isFailure) {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Cloud sync failed."
            }
        }
    }

    fun toggleWorkFavorite(item: SavedWorkItem) {
        viewModelScope.launch {
            repository.toggleFavorite(currentUserId, item.id, !item.favorite)
        }
    }

    fun deleteWorkItem(item: SavedWorkItem) {
        viewModelScope.launch {
            val res = repository.deleteWorkItem(currentUserId, item.id)
            if (res.isSuccess) _toastMessage.value = "Removed from My Work"
        }
    }

    fun submitManualPayment(
        method: PaymentMethod,
        planTier: PlanTier,
        amountDisplay: String,
        transactionRef: String,
        senderPhone: String,
        proofSummary: String
    ) {
        val profile = (userProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            val res = repository.submitManualPayment(
                profile, method, planTier, amountDisplay, transactionRef, senderPhone, proofSummary
            )
            _toastMessage.value = if (res.isSuccess) {
                "Payment proof submitted! Pending admin approval."
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Submission failed."
            }
        }
    }

    fun submitToDailyChallenge(
        bitmap: Bitmap,
        theme: DailyChallengeTheme,
        toolUsed: String,
        caption: String
    ) {
        val profile = (userProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            _challengeSubmitting.value = true
            try {
                val aiEvalRes = aiService.judgeChallengeEntry(bitmap, theme, toolUsed, caption)
                val evaluation = aiEvalRes.getOrElse { err ->
                    // Surface real error if Gemini call fails rather than faking judge scores
                    _toastMessage.value = "AI Judge unavailable: ${err.localizedMessage ?: "Check connection"}"
                    _challengeSubmitting.value = false
                    return@launch
                }
                val thumb = ImageEnhancerEngine.bitmapToCompactBase64(bitmap)
                val submitRes = repository.submitChallengeEntry(
                    profile = profile,
                    theme = theme,
                    caption = caption,
                    toolUsed = toolUsed,
                    evaluation = evaluation,
                    previewBase64 = thumb
                )
                if (submitRes.isSuccess) {
                    val entry = submitRes.getOrNull()!!
                    _lastChallengeResult.value = entry
                    val prize = ChallengePrizeTier.fromRaw(entry.prizeAwarded)
                    _toastMessage.value = "Submitted! ${prize.label}: ${prize.rewardSummary}"
                } else {
                    _toastMessage.value = submitRes.exceptionOrNull()?.localizedMessage ?: "Challenge submission failed."
                }
            } finally {
                _challengeSubmitting.value = false
            }
        }
    }

    fun upvoteChallengeSubmission(submission: ChallengeSubmission) {
        viewModelScope.launch {
            val res = repository.upvoteChallengeSubmission(submission)
            if (res.isFailure) {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Could not vote."
            }
        }
    }

    fun adminReviewPayment(request: PaymentRequest, approve: Boolean, note: String) {
        viewModelScope.launch {
            val res = repository.adminReviewPayment(request, approve, note)
            _toastMessage.value = if (res.isSuccess) {
                if (approve) "Approved & VIP activated for ${request.userName}" else "Marked payment rejected"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Admin action failed"
            }
        }
    }

    fun adminToggleFeatured(submission: ChallengeSubmission) {
        viewModelScope.launch {
            val res = repository.adminToggleFeaturedChallenge(submission, !submission.featured)
            if (res.isSuccess) {
                _toastMessage.value = if (!submission.featured) "Featured in Daily Top Picks" else "Removed from Featured"
            }
        }
    }

    fun adminPublishWallpaper(title: String, category: String, vipOnly: Boolean) {
        viewModelScope.launch {
            val res = repository.adminAddWallpaper(title, category, vipOnly)
            _toastMessage.value = if (res.isSuccess) "Published wallpaper to catalog" else "Failed to publish wallpaper"
        }
    }

    fun deleteAccountData(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val res = repository.deleteMyAccountData(currentUserId)
            if (res.isSuccess) {
                onDeleted()
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Account deletion failed"
            }
        }
    }

    companion object {
        fun provideFactory(currentUserId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val dbId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(dbId)
                CloudAccountViewModel(
                    repository = FirestoreRepository(db),
                    aiService = AiEnhancementService(),
                    currentUserId = currentUserId
                )
            }
        }
    }
}
