package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.AppLanguage
import com.example.model.CatalogData
import com.example.model.ChallengeAiEvaluation
import com.example.model.ChallengePrizeTier
import com.example.model.ChallengeSubmission
import com.example.model.CurrencyOption
import com.example.model.DailyChallengeTheme
import com.example.model.PaymentMethod
import com.example.model.PaymentRequest
import com.example.model.PaymentStatus
import com.example.model.PlanTier
import com.example.model.SavedWorkItem
import com.example.model.UserProfile
import com.example.model.WallpaperCatalogItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing cloud data.")
    }

    fun isCurrentUserAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.email == "hassankulachi53@gmail.com" && user.isEmailVerified
    }

    suspend fun ensureUserProfile(): Result<UserProfile> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docPath = "users/$uid"
        return try {
            val docRef = db.collection("users").document(uid)
            val snap = docRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (existing != null) {
                    val today = CatalogData.todayDateKey()
                    if (existing.lastCheckInDate != today) {
                        // Refresh daily credits when a new day starts
                        docRef.update(
                            mapOf(
                                "dailyCreditsRemaining" to 5,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                    return Result.success(existing)
                }
            }
            val currentUser = auth.currentUser
            val safeName = currentUser?.displayName?.takeIf { it.isNotBlank() }?.take(70) ?: "Lumina Creator"
            val safeEmail = currentUser?.email?.take(110) ?: ""
            val refCode = "LUM${uid.take(5).uppercase()}"
            val today = CatalogData.todayDateKey()
            val payload = mapOf(
                "userId" to uid,
                "displayName" to safeName,
                "email" to safeEmail,
                "avatarTheme" to "gold",
                "planTier" to PlanTier.FREE.name,
                "vipExpiresAtMs" to 0L,
                "dailyCreditsRemaining" to 5,
                "bonusCredits" to 2,
                "streakCount" to 1,
                "lastCheckInDate" to today,
                "referralCode" to refCode,
                "referralCount" to 0,
                "preferredCurrency" to CurrencyOption.PKR.name,
                "preferredLanguage" to AppLanguage.EN.name,
                "darkThemeEnabled" to true,
                "notificationsEnabled" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(
                UserProfile(
                    id = uid,
                    userId = uid,
                    displayName = safeName,
                    email = safeEmail,
                    planTier = PlanTier.FREE.name,
                    dailyCreditsRemaining = 5,
                    bonusCredits = 2,
                    streakCount = 1,
                    lastCheckInDate = today,
                    referralCode = refCode
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { it.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(fsErr ?: error, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun updatePreferences(
        userId: String,
        currency: CurrencyOption? = null,
        language: AppLanguage? = null,
        darkThemeEnabled: Boolean? = null,
        notificationsEnabled: Boolean? = null
    ): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user preference write." }
            val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
            currency?.let { updates["preferredCurrency"] = it.name }
            language?.let { updates["preferredLanguage"] = it.name }
            darkThemeEnabled?.let { updates["darkThemeEnabled"] = it }
            notificationsEnabled?.let { updates["notificationsEnabled"] = it }
            db.document(docPath).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun claimDailyCheckIn(profile: UserProfile): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Sign in required"))
        val docPath = "users/$uid"
        return try {
            val today = CatalogData.todayDateKey()
            if (profile.lastCheckInDate == today && profile.streakCount >= 1) {
                return Result.success("Today's streak already claimed! (${profile.streakCount} days)")
            }
            val newStreak = profile.streakCount + 1
            val updates = mutableMapOf<String, Any>(
                "streakCount" to newStreak,
                "lastCheckInDate" to today,
                "dailyCreditsRemaining" to 5,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            var message = "Day $newStreak streak! +5 daily HD credits unlocked."
            if (newStreak % 7 == 0) {
                val expires = System.currentTimeMillis() + 86_400_000L
                updates["planTier"] = PlanTier.VIP_CHALLENGE_PASS.name
                updates["vipExpiresAtMs"] = expires
                message = "7-Day Streak Milestone! Unlocked 1 FREE day of VIP!"
            }
            db.document(docPath).update(updates).await()
            Result.success(message)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun activateThreeDayTrial(userId: String): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            val expires = System.currentTimeMillis() + 3L * 86_400_000L
            db.document(docPath).update(
                mapOf(
                    "planTier" to PlanTier.VIP_TRIAL.name,
                    "vipExpiresAtMs" to expires,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun consumeCredit(profile: UserProfile): Result<Unit> {
        if (profile.hasActiveVip()) return Result.success(Unit)
        val docPath = "users/${profile.userId}"
        return try {
            val uid = requireUserId()
            check(uid == profile.userId)
            val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
            when {
                profile.dailyCreditsRemaining > 0 ->
                    updates["dailyCreditsRemaining"] = profile.dailyCreditsRemaining - 1
                profile.bonusCredits > 0 ->
                    updates["bonusCredits"] = profile.bonusCredits - 1
                else -> return Result.failure(IllegalStateException("No HD credits remaining today. Upgrade to VIP or invite a friend!"))
            }
            db.document(docPath).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun redeemReferralCode(profile: UserProfile, code: String): Result<String> {
        val docPath = "users/${profile.userId}"
        return try {
            val uid = requireUserId()
            check(uid == profile.userId)
            val clean = code.trim().uppercase()
            require(clean.length in 4..20) { "Enter a valid invite code (4-20 chars)." }
            require(clean != profile.referralCode) { "You cannot redeem your own referral code." }
            require(profile.referredByCode.isNullOrBlank()) { "Referral reward already claimed on this account." }

            val trialExpiry = maxOf(profile.vipExpiresAtMs, System.currentTimeMillis()) + 3L * 86_400_000L
            db.document(docPath).update(
                mapOf(
                    "referredByCode" to clean,
                    "planTier" to PlanTier.VIP_TRIAL.name,
                    "vipExpiresAtMs" to trialExpiry,
                    "bonusCredits" to (profile.bonusCredits + 5),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success("Invite accepted! 3 Free VIP Days + 5 Bonus Credits added.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    // --- My Work Projects ---

    fun observeSavedWork(userId: String): Flow<List<SavedWorkItem>> = flow {
        require(userId.isNotBlank())
        val path = "users/$userId/projects"
        emitAll(
            db.collection("users").document(userId).collection("projects")
                .snapshots()
                .map { snap ->
                    snap.toObjects(SavedWorkItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveWorkItem(item: SavedWorkItem): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Sign in required"))
        val docId = "work_${UUID.randomUUID().toString().take(12)}"
        val path = "users/$uid/projects/$docId"
        return try {
            val payload = mapOf(
                "userId" to uid,
                "title" to item.title.take(90).ifBlank { "Enhanced Photo" },
                "mediaType" to item.mediaType,
                "folderName" to item.folderName.take(36).ifBlank { "All Edits" },
                "favorite" to item.favorite,
                "toolUsed" to item.toolUsed.take(55),
                "filterName" to item.filterName.take(36),
                "qualityLabel" to item.qualityLabel,
                "enhancementSummary" to item.enhancementSummary.take(280),
                "localUri" to item.localUri?.take(480),
                "thumbnailBase64" to item.thumbnailBase64?.take(110_000),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            db.document(path).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(userId: String, projectId: String, favorite: Boolean): Result<Unit> {
        val path = "users/$userId/projects/$projectId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            db.document(path).update(
                mapOf("favorite" to favorite, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteWorkItem(userId: String, projectId: String): Result<Unit> {
        val path = "users/$userId/projects/$projectId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            db.document(path).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- Daily Photo Challenge ---

    fun observeChallengeSubmissions(): Flow<List<ChallengeSubmission>> = flow {
        val path = "challengeSubmissions"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "PUBLIC")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ChallengeSubmission::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(
                            compareByDescending<ChallengeSubmission> { it.featured }
                                .thenByDescending { it.creativityScore + it.votesCount }
                        )
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser != null) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun submitChallengeEntry(
        profile: UserProfile,
        theme: DailyChallengeTheme,
        caption: String,
        toolUsed: String,
        evaluation: ChallengeAiEvaluation,
        previewBase64: String?
    ): Result<ChallengeSubmission> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Sign in required"))
        val subId = "sub_${UUID.randomUUID().toString().take(12)}"
        val path = "challengeSubmissions/$subId"
        return try {
            val cleanCaption = caption.trim().take(130).ifBlank { "${theme.title} HDR Edit" }
            val payload = mapOf(
                "userId" to uid,
                "authorName" to profile.displayName.take(55).ifBlank { "Creator" },
                "dateKey" to theme.dateKey,
                "themeTitle" to theme.title.take(55),
                "caption" to cleanCaption,
                "toolUsed" to toolUsed.take(55),
                "creativityScore" to evaluation.creativityScore.coerceIn(0, 100),
                "qualityScore" to evaluation.qualityScore.coerceIn(0, 100),
                "judgeFeedback" to evaluation.judgeFeedback.take(280),
                "prizeAwarded" to evaluation.prizeTier.name,
                "votesCount" to 0,
                "featured" to false,
                "visibility" to "PUBLIC",
                "previewBase64" to previewBase64?.take(110_000),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            db.document(path).set(payload).await()

            // Grant challenge reward on user's own profile
            val userDocPath = "users/$uid"
            when (evaluation.prizeTier) {
                ChallengePrizeTier.VIP_DAY_PASS -> {
                    val expiry = maxOf(profile.vipExpiresAtMs, System.currentTimeMillis()) + 86_400_000L
                    db.document(userDocPath).update(
                        mapOf(
                            "planTier" to PlanTier.VIP_CHALLENGE_PASS.name,
                            "vipExpiresAtMs" to expiry,
                            "bonusCredits" to (profile.bonusCredits + 10),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
                ChallengePrizeTier.EXTRA_CREDITS -> {
                    db.document(userDocPath).update(
                        mapOf(
                            "bonusCredits" to (profile.bonusCredits + 5),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
                ChallengePrizeTier.NONE -> {
                    db.document(userDocPath).update(
                        mapOf(
                            "bonusCredits" to (profile.bonusCredits + 1),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }

            Result.success(
                ChallengeSubmission(
                    id = subId,
                    userId = uid,
                    authorName = profile.displayName,
                    dateKey = theme.dateKey,
                    themeTitle = theme.title,
                    caption = cleanCaption,
                    toolUsed = toolUsed,
                    creativityScore = evaluation.creativityScore,
                    qualityScore = evaluation.qualityScore,
                    judgeFeedback = evaluation.judgeFeedback,
                    prizeAwarded = evaluation.prizeTier.name,
                    votesCount = 0,
                    featured = false,
                    previewBase64 = previewBase64
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun upvoteChallengeSubmission(submission: ChallengeSubmission): Result<Unit> {
        val path = "challengeSubmissions/${submission.id}"
        return try {
            requireUserId()
            db.document(path).update(
                mapOf(
                    "votesCount" to (submission.votesCount + 1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- EasyPaisa / JazzCash Manual Payments ---

    fun observeMyPayments(userId: String): Flow<List<PaymentRequest>> = flow {
        require(userId.isNotBlank())
        val path = "paymentRequests"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(PaymentRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun submitManualPayment(
        profile: UserProfile,
        method: PaymentMethod,
        planTier: PlanTier,
        amountDisplay: String,
        transactionRef: String,
        senderPhone: String,
        proofSummary: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Sign in required"))
        val reqId = "pay_${UUID.randomUUID().toString().take(12)}"
        val path = "paymentRequests/$reqId"
        return try {
            require(transactionRef.trim().length >= 3) { "Transaction ID must be at least 3 characters." }
            require(senderPhone.trim().length >= 6) { "Enter a valid mobile wallet number." }
            val payload = mapOf(
                "userId" to uid,
                "userEmail" to profile.email.take(115),
                "userName" to profile.displayName.take(75).ifBlank { "Creator" },
                "paymentMethod" to method.name,
                "planId" to planTier.name,
                "amountDisplay" to amountDisplay.take(28),
                "transactionRef" to transactionRef.trim().take(60),
                "senderPhone" to senderPhone.trim().take(22),
                "proofSummary" to proofSummary.trim().take(190).ifBlank { "Screenshot attached" },
                "status" to PaymentStatus.PENDING.name,
                "adminNote" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.document(path).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- Admin Dashboard Operations ---

    fun observeAllPaymentsForAdmin(): Flow<List<PaymentRequest>> = flow {
        val path = "paymentRequests"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap ->
                    snap.toObjects(PaymentRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAllUsersForAdmin(): Flow<List<UserProfile>> = flow {
        val path = "users"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap ->
                    snap.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun adminReviewPayment(request: PaymentRequest, approve: Boolean, note: String): Result<Unit> {
        val payPath = "paymentRequests/${request.id}"
        return try {
            requireUserId()
            val newStatus = if (approve) PaymentStatus.APPROVED.name else PaymentStatus.REJECTED.name
            db.document(payPath).update(
                mapOf(
                    "status" to newStatus,
                    "adminNote" to note.take(190).ifBlank { if (approve) "Verified by Admin" else "Unable to verify TID" },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (approve) {
                val durationDays = when (PlanTier.fromRaw(request.planId)) {
                    PlanTier.VIP_YEARLY -> 365L
                    PlanTier.VIP_MONTHLY -> 30L
                    else -> 7L
                }
                val expiry = System.currentTimeMillis() + durationDays * 86_400_000L
                db.collection("users").document(request.userId).update(
                    mapOf(
                        "planTier" to request.planId,
                        "vipExpiresAtMs" to expiry,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, payPath)
            Result.failure(e)
        }
    }

    suspend fun adminToggleFeaturedChallenge(submission: ChallengeSubmission, featured: Boolean): Result<Unit> {
        val path = "challengeSubmissions/${submission.id}"
        return try {
            requireUserId()
            db.document(path).update(
                mapOf("featured" to featured, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeWallpapers(): Flow<List<WallpaperCatalogItem>> = flow {
        val path = "wallpapers"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "PUBLIC")
                .snapshots()
                .map { snap ->
                    snap.toObjects(WallpaperCatalogItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser != null) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun adminAddWallpaper(title: String, category: String, vipOnly: Boolean): Result<Unit> {
        val wpId = "wp_${UUID.randomUUID().toString().take(10)}"
        val path = "wallpapers/$wpId"
        return try {
            requireUserId()
            val payload = mapOf(
                "title" to title.trim().take(75),
                "category" to category.trim().take(36),
                "vipOnly" to vipOnly,
                "drawableKey" to "img_demo_city_nights",
                "resolutionLabel" to if (vipOnly) "8K Live" else "4K UHD",
                "downloadsCount" to 0,
                "visibility" to "PUBLIC",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.document(path).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteMyAccountData(userId: String): Result<Unit> {
        val userPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            val projectsSnap = db.collection("users").document(uid).collection("projects").get().await()
            for (doc in projectsSnap.documents) {
                doc.reference.delete().await()
            }
            db.document(userPath).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, userPath)
            Result.failure(e)
        }
    }
}
