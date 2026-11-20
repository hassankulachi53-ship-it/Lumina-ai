package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class PlanTier(val displayName: String, val vipAccess: Boolean) {
    FREE("Free Studio", false),
    VIP_TRIAL("3-Day VIP Trial", true),
    VIP_WEEKLY("VIP Weekly", true),
    VIP_MONTHLY("VIP Monthly", true),
    VIP_YEARLY("VIP Yearly", true),
    VIP_CHALLENGE_PASS("Challenge VIP Pass", true);

    companion object {
        fun fromRaw(raw: String?): PlanTier =
            entries.firstOrNull { it.name == raw } ?: FREE
    }
}

enum class CurrencyOption(val symbol: String, val code: String, val countryHint: String) {
    PKR("Rs.", "PKR", "Pakistan"),
    USD("$", "USD", "USA / Global"),
    GBP("£", "GBP", "United Kingdom"),
    AED("AED ", "AED", "Middle East / UAE");

    companion object {
        fun fromRaw(raw: String?): CurrencyOption =
            entries.firstOrNull { it.name == raw } ?: PKR
    }
}

enum class AppLanguage(val label: String, val nativeLabel: String) {
    EN("English", "English"),
    UR("Urdu", "اردو"),
    HI("Hindi", "हिन्दी"),
    AR("Arabic", "العربية"),
    ES("Spanish", "Español");

    companion object {
        fun fromRaw(raw: String?): AppLanguage =
            entries.firstOrNull { it.name == raw } ?: EN
    }
}

enum class MediaType {
    PHOTO, VIDEO, WALLPAPER;

    companion object {
        fun fromRaw(raw: String?): MediaType =
            entries.firstOrNull { it.name == raw } ?: PHOTO
    }
}

enum class QualityOption(val label: String, val scaleFactor: Float, val vipOnly: Boolean) {
    ORIGINAL("Original", 1.0f, false),
    HD("HD 1080p", 1.25f, false),
    UHD_4K("4K Ultra", 2.0f, true),
    UHD_8K("8K ProRAW", 3.0f, true);

    companion object {
        fun fromRaw(raw: String?): QualityOption =
            entries.firstOrNull { it.name == raw } ?: HD
    }
}

enum class ExportFormat(val ext: String, val mimeType: String, val label: String) {
    JPG("jpg", "image/jpeg", "JPG"),
    PNG("png", "image/png", "PNG"),
    HEIC("webp", "image/webp", "HEIC"),
    MP4("mp4", "video/mp4", "MP4"),
    MOV("mov", "video/quicktime", "MOV")
}

enum class StrengthLevel(val label: String, val multiplier: Float) {
    SOFT("Soft", 0.55f),
    NATURAL("Natural", 1.0f),
    ULTRA("Ultra", 1.45f)
}

enum class EnhancementTool(
    val title: String,
    val subtitle: String,
    val vipOnly: Boolean,
    val generativeAi: Boolean = false
) {
    SMART_ENHANCE("Smart Enhance", "Auto-detect & HDR", false),
    UNBLUR("Unblur", "Deblur motion & focus", false),
    UPSCALE("Upscale 4x", "Crisp 2x & 4x clarity", false),
    FACE_ENHANCE("Face Enhance", "Natural skin & eyes", false),
    PORTRAIT_LIGHT("Portrait Light", "Studio & stage light", false),
    NIGHT_MODE("Night Mode", "Low-light lift & denoise", false),
    AUTO_HDR("Auto HDR", "iPhone Smart Tone", false),
    COLORIZE("Colorize", "B&W to natural color", false, true),
    RESTORE("Restore", "Fix old & scratched", false, true),
    BG_REMOVE("BG Studio", "Replace or blur backdrop", false, true),
    OBJECT_REMOVE("Object Erase", "Remove people/objects", false, true),
    VIDEO_ENHANCE("Video 4K", "1080p→4K & stabilize", true),
    CINEMATIC_VIDEO("Cinema Video", "Depth blur & 60fps", true),
    AI_AVATAR("AI Avatar", "Studio art & portraits", true, true),
    PORTRAIT_HEADSHOT("Pro Headshot", "LinkedIn studio look", true, true),
    WALLPAPER_HD("Wallpaper 4K", "Exact phone screen fit", false)
}

enum class PaymentMethod(val displayName: String, val accountTitle: String, val accountNumber: String) {
    EASYPAISA("EasyPaisa", "Lumina Studio PK", "0345-8821940"),
    JAZZCASH("JazzCash", "Lumina Studio PK", "0301-7749210");

    companion object {
        fun fromRaw(raw: String?): PaymentMethod =
            entries.firstOrNull { it.name == raw } ?: EASYPAISA
    }
}

enum class PaymentStatus(val label: String) {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    companion object {
        fun fromRaw(raw: String?): PaymentStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class ChallengePrizeTier(val label: String, val rewardSummary: String) {
    NONE("Participant", "+1 Bonus Credit"),
    EXTRA_CREDITS("Creative Pick", "+5 Bonus HD Credits"),
    VIP_DAY_PASS("Daily Winner", "24h Free VIP Pass + 10 Credits");

    companion object {
        fun fromRaw(raw: String?): ChallengePrizeTier =
            entries.firstOrNull { it.name == raw } ?: NONE
    }
}

data class EditParameters(
    val tool: EnhancementTool = EnhancementTool.SMART_ENHANCE,
    val strength: StrengthLevel = StrengthLevel.NATURAL,
    val filterName: String = "Original",
    val exposure: Float = 0.08f,      // -1f..1f
    val brilliance: Float = 0.18f,    // -1f..1f
    val highlights: Float = -0.10f,   // -1f..1f
    val shadows: Float = 0.20f,       // -1f..1f
    val contrast: Float = 0.14f,      // -1f..1f
    val saturation: Float = 0.12f,    // -1f..1f
    val vibrance: Float = 0.18f,      // -1f..1f
    val warmth: Float = 0.05f,        // -1f..1f
    val tint: Float = 0.0f,           // -1f..1f
    val sharpness: Float = 0.35f,     // 0f..1f
    val definition: Float = 0.25f,    // 0f..1f
    val noiseReduction: Float = 0.20f,// 0f..1f
    val vignette: Float = 0.0f,       // 0f..1f
    val skinRetouch: Float = 0.20f,   // 0f..1f
    val backgroundBlur: Float = 0.0f, // 0f..1f
    val portraitLightStyle: String = "Natural",
    val rotationDegrees: Int = 0,     // 0, 90, 180, 270
    val aspectRatioLabel: String = "Original",
    val overlayText: String = "",
    val overlaySticker: String = ""
)

data class AiSceneAnalysis(
    val sceneType: String = "Portrait",
    val recommendedTool: String = "Smart Enhance",
    val exposureBoost: Float = 0.12f,
    val contrastBoost: Float = 0.15f,
    val warmthShift: Float = 0.04f,
    val sharpnessLevel: Float = 0.40f,
    val vibranceBoost: Float = 0.18f,
    val shadowLift: Float = 0.22f,
    val highlightRecovery: Float = -0.12f,
    val skinRetouch: Float = 0.22f,
    val filterSuggestion: String = "iPhone Natural",
    val reasoning: String = "Balanced dynamic range with natural skin detail."
)

data class ChallengeAiEvaluation(
    val creativityScore: Int = 88,
    val qualityScore: Int = 90,
    val themeMatchScore: Int = 92,
    val judgeFeedback: String = "Strong lighting balance and sharp subject focus aligned with today's theme.",
    val prizeTier: ChallengePrizeTier = ChallengePrizeTier.EXTRA_CREDITS
)

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Creator",
    val email: String = "",
    val avatarTheme: String = "gold",
    val planTier: String = PlanTier.FREE.name,
    val vipExpiresAtMs: Long = 0L,
    val dailyCreditsRemaining: Int = 5,
    val bonusCredits: Int = 2,
    val streakCount: Int = 1,
    val lastCheckInDate: String = "",
    val referralCode: String = "LUMINA26",
    val referredByCode: String? = null,
    val referralCount: Int = 0,
    val preferredCurrency: String = CurrencyOption.PKR.name,
    val preferredLanguage: String = AppLanguage.EN.name,
    val darkThemeEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun resolvedPlanTier(): PlanTier = PlanTier.fromRaw(planTier)
    fun hasActiveVip(nowMs: Long = System.currentTimeMillis()): Boolean {
        val tier = resolvedPlanTier()
        if (!tier.vipAccess) return false
        return vipExpiresAtMs == 0L || vipExpiresAtMs > nowMs
    }
    fun totalCredits(): Int = (dailyCreditsRemaining + bonusCredits).coerceAtLeast(0)
}

@Keep
@IgnoreExtraProperties
data class SavedWorkItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "Enhanced Photo",
    val mediaType: String = MediaType.PHOTO.name,
    val folderName: String = "All Edits",
    val favorite: Boolean = false,
    val toolUsed: String = "Smart Enhance",
    val filterName: String = "Original",
    val qualityLabel: String = "HD",
    val enhancementSummary: String = "",
    val localUri: String? = null,
    val thumbnailBase64: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PaymentRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val paymentMethod: String = PaymentMethod.EASYPAISA.name,
    val planId: String = PlanTier.VIP_MONTHLY.name,
    val amountDisplay: String = "500 PKR",
    val transactionRef: String = "",
    val senderPhone: String = "",
    val proofSummary: String = "",
    val status: String = PaymentStatus.PENDING.name,
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChallengeSubmission(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "Creator",
    val dateKey: String = "",
    val themeTitle: String = "",
    val caption: String = "",
    val toolUsed: String = "Smart Enhance",
    val creativityScore: Int = 85,
    val qualityScore: Int = 88,
    val judgeFeedback: String = "",
    val prizeAwarded: String = ChallengePrizeTier.NONE.name,
    val votesCount: Int = 0,
    val featured: Boolean = false,
    val visibility: String = "PUBLIC",
    val previewBase64: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WallpaperCatalogItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "AMOLED",
    val vipOnly: Boolean = false,
    val drawableKey: String = "img_demo_city_nights",
    val resolutionLabel: String = "4K UHD",
    val downloadsCount: Int = 0,
    val visibility: String = "PUBLIC",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class DailyChallengeTheme(
    val dateKey: String,
    val title: String,
    val subtitle: String,
    val recommendedTool: EnhancementTool,
    val sampleDrawableKey: String,
    val prizeDescription: String
)

data class SubscriptionPlanSpec(
    val planTier: PlanTier,
    val title: String,
    val badge: String?,
    val trialText: String?,
    val pkrPrice: String,
    val usdPrice: String,
    val gbpPrice: String,
    val aedPrice: String,
    val billingPeriod: String
) {
    fun priceFor(currency: CurrencyOption): String = when (currency) {
        CurrencyOption.PKR -> pkrPrice
        CurrencyOption.USD -> usdPrice
        CurrencyOption.GBP -> gbpPrice
        CurrencyOption.AED -> aedPrice
    }
}

data class FilterPresetSpec(
    val name: String,
    val group: String,
    val vipOnly: Boolean,
    val exposure: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val warmth: Float = 0f,
    val tint: Float = 0f,
    val vignette: Float = 0f
)

data class TrendingStyleSpec(
    val title: String,
    val tag: String,
    val sampleDrawableKey: String,
    val tool: EnhancementTool,
    val filterName: String,
    val vipOnly: Boolean
)
