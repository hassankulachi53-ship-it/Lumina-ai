package com.example.model

import com.example.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CatalogData {

    fun todayDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun drawableForKey(key: String): Int = when (key) {
        "img_demo_city_nights" -> R.drawable.img_demo_city_nights
        "img_demo_nature_colors" -> R.drawable.img_demo_nature_colors
        "img_demo_foodie_focus" -> R.drawable.img_demo_foodie_focus
        "img_demo_portrait_studio" -> R.drawable.img_demo_portrait_studio
        else -> R.drawable.img_demo_city_nights
    }

    val dailyChallengeThemes: List<DailyChallengeTheme> = listOf(
        DailyChallengeTheme(
            dateKey = todayDateKey(),
            title = "City Nights",
            subtitle = "Capture glowing city streets, neon reflections, or evening skylines enhanced with crisp HDR & Night Mode.",
            recommendedTool = EnhancementTool.NIGHT_MODE,
            sampleDrawableKey = "img_demo_city_nights",
            prizeDescription = "24h VIP Pass + 10 Bonus HD Credits"
        ),
        DailyChallengeTheme(
            dateKey = "theme_nature",
            title = "Nature's Colors",
            subtitle = "Bring out alpine blues, sunset golds, and lush greens with balanced highlights and lifelike vibrance.",
            recommendedTool = EnhancementTool.AUTO_HDR,
            sampleDrawableKey = "img_demo_nature_colors",
            prizeDescription = "24h VIP Pass + 10 Bonus HD Credits"
        ),
        DailyChallengeTheme(
            dateKey = "theme_foodie",
            title = "Foodie Focus",
            subtitle = "Make culinary shots mouth-watering with studio brilliance, warm textures, and subtle depth blur.",
            recommendedTool = EnhancementTool.SMART_ENHANCE,
            sampleDrawableKey = "img_demo_foodie_focus",
            prizeDescription = "24h VIP Pass + 10 Bonus HD Credits"
        ),
        DailyChallengeTheme(
            dateKey = "theme_portrait",
            title = "Golden Portraits",
            subtitle = "Illuminate faces with natural skin texture, catchlights, and soft studio portrait glow.",
            recommendedTool = EnhancementTool.PORTRAIT_LIGHT,
            sampleDrawableKey = "img_demo_portrait_studio",
            prizeDescription = "24h VIP Pass + 10 Bonus HD Credits"
        )
    )

    fun activeChallengeTheme(): DailyChallengeTheme {
        val dayIndex = ((System.currentTimeMillis() / 86_400_000L) % dailyChallengeThemes.size).toInt()
        return dailyChallengeThemes[dayIndex].copy(dateKey = todayDateKey())
    }

    val subscriptionPlans: List<SubscriptionPlanSpec> = listOf(
        SubscriptionPlanSpec(
            planTier = PlanTier.VIP_YEARLY,
            title = "Yearly VIP",
            badge = "BEST VALUE • SAVE 80%",
            trialText = "3-Day Free Trial, then billed yearly",
            pkrPrice = "1,000 PKR",
            usdPrice = "$3.99",
            gbpPrice = "£2.99",
            aedPrice = "14.99 AED",
            billingPeriod = "/ year"
        ),
        SubscriptionPlanSpec(
            planTier = PlanTier.VIP_MONTHLY,
            title = "Monthly VIP",
            badge = "MOST POPULAR",
            trialText = "Cancel anytime",
            pkrPrice = "500 PKR",
            usdPrice = "$1.99",
            gbpPrice = "£1.49",
            aedPrice = "7.49 AED",
            billingPeriod = "/ month"
        ),
        SubscriptionPlanSpec(
            planTier = PlanTier.VIP_WEEKLY,
            title = "Weekly VIP",
            badge = null,
            trialText = "Full VIP access for 7 days",
            pkrPrice = "200 PKR",
            usdPrice = "$0.99",
            gbpPrice = "£0.79",
            aedPrice = "3.99 AED",
            billingPeriod = "/ week"
        )
    )

    val trendingStyles: List<TrendingStyleSpec> = listOf(
        TrendingStyleSpec(
            title = "iPhone 16 ProRAW",
            tag = "Weekly #1",
            sampleDrawableKey = "img_demo_city_nights",
            tool = EnhancementTool.AUTO_HDR,
            filterName = "Vivid Pro",
            vipOnly = false
        ),
        TrendingStyleSpec(
            title = "Golden Hour Portrait",
            tag = "Natural Skin",
            sampleDrawableKey = "img_demo_portrait_studio",
            tool = EnhancementTool.PORTRAIT_LIGHT,
            filterName = "Warm Gold",
            vipOnly = false
        ),
        TrendingStyleSpec(
            title = "4K Cinema Grade",
            tag = "VIP Exclusive",
            sampleDrawableKey = "img_demo_nature_colors",
            tool = EnhancementTool.VIDEO_ENHANCE,
            filterName = "Cinematic",
            vipOnly = true
        ),
        TrendingStyleSpec(
            title = "Michelin Crisp",
            tag = "Foodie Hit",
            sampleDrawableKey = "img_demo_foodie_focus",
            tool = EnhancementTool.SMART_ENHANCE,
            filterName = "Brilliance",
            vipOnly = false
        )
    )

    val wallpaperCategories: List<String> = listOf(
        "All", "AMOLED", "Nature", "Cars", "Islamic", "Anime", "Gaming", "Space",
        "Minimal", "Luxury", "Sports", "Quotes", "Architecture", "Abstract",
        "Neon", "Ocean", "Mountains", "Vintage", "Gold", "Dark Mode", "Festive"
    )

    val builtInWallpapers: List<WallpaperCatalogItem> = listOf(
        WallpaperCatalogItem(id = "wp_1", title = "Obsidian Skyline 4K", category = "AMOLED", vipOnly = false, drawableKey = "img_demo_city_nights", resolutionLabel = "4K OLED", downloadsCount = 18420),
        WallpaperCatalogItem(id = "wp_2", title = "Emerald Glacial Peak", category = "Nature", vipOnly = false, drawableKey = "img_demo_nature_colors", resolutionLabel = "4K UHD", downloadsCount = 14210),
        WallpaperCatalogItem(id = "wp_3", title = "Dubai Twilight Reflections", category = "Luxury", vipOnly = true, drawableKey = "img_demo_city_nights", resolutionLabel = "8K Live", downloadsCount = 9830),
        WallpaperCatalogItem(id = "wp_4", title = "Alpine Dawn Mirror", category = "Mountains", vipOnly = false, drawableKey = "img_demo_nature_colors", resolutionLabel = "4K UHD", downloadsCount = 11300),
        WallpaperCatalogItem(id = "wp_5", title = "Noir Studio Portrait", category = "Minimal", vipOnly = false, drawableKey = "img_demo_portrait_studio", resolutionLabel = "4K UHD", downloadsCount = 7490),
        WallpaperCatalogItem(id = "wp_6", title = "Saffron Slate Macro", category = "Dark Mode", vipOnly = false, drawableKey = "img_demo_foodie_focus", resolutionLabel = "4K UHD", downloadsCount = 6120),
        WallpaperCatalogItem(id = "wp_7", title = "Midnight Neon Harbor", category = "Neon", vipOnly = true, drawableKey = "img_demo_city_nights", resolutionLabel = "4K Parallax", downloadsCount = 12890),
        WallpaperCatalogItem(id = "wp_8", title = "Crescent Gold Serenity", category = "Islamic", vipOnly = false, drawableKey = "img_demo_nature_colors", resolutionLabel = "4K UHD", downloadsCount = 15640)
    )

    // 42 Pro & Creative Filter Presets across Vivid, Cinematic, Film, Warm, Cool, Mono, Noir, VIP
    val filterPresets: List<FilterPresetSpec> = listOf(
        FilterPresetSpec("Original", "Core", false),
        FilterPresetSpec("iPhone Natural", "Vivid", false, exposure = 0.06f, contrast = 0.10f, saturation = 0.12f),
        FilterPresetSpec("Vivid", "Vivid", false, exposure = 0.08f, contrast = 0.18f, saturation = 0.28f),
        FilterPresetSpec("Vivid Warm", "Vivid", false, exposure = 0.06f, contrast = 0.16f, saturation = 0.26f, warmth = 0.18f),
        FilterPresetSpec("Vivid Cool", "Vivid", false, exposure = 0.06f, contrast = 0.16f, saturation = 0.24f, warmth = -0.16f),
        FilterPresetSpec("Brilliance", "Vivid", false, exposure = 0.12f, contrast = 0.20f, saturation = 0.18f),
        FilterPresetSpec("Pop HDR", "Vivid", false, exposure = 0.10f, contrast = 0.28f, saturation = 0.30f),
        FilterPresetSpec("Cinematic", "Cinematic", false, exposure = -0.02f, contrast = 0.26f, saturation = 0.08f, warmth = -0.05f, vignette = 0.28f),
        FilterPresetSpec("Teal & Orange", "Cinematic", false, contrast = 0.24f, saturation = 0.16f, warmth = 0.14f, tint = -0.10f, vignette = 0.22f),
        FilterPresetSpec("Blockbuster", "Cinematic", false, exposure = -0.05f, contrast = 0.32f, saturation = -0.06f, warmth = -0.10f, vignette = 0.35f),
        FilterPresetSpec("Anamorphic", "Cinematic", true, contrast = 0.28f, saturation = 0.12f, warmth = -0.12f, tint = 0.08f, vignette = 0.30f),
        FilterPresetSpec("Dubai Gold", "Cinematic", true, exposure = 0.08f, contrast = 0.22f, saturation = 0.20f, warmth = 0.28f, vignette = 0.18f),
        FilterPresetSpec("London Mist", "Cinematic", false, exposure = 0.04f, contrast = 0.06f, saturation = -0.18f, warmth = -0.15f),
        FilterPresetSpec("Islamabad Dusk", "Cinematic", false, exposure = 0.02f, contrast = 0.20f, saturation = 0.15f, warmth = 0.16f, tint = 0.08f),
        FilterPresetSpec("Film 35mm", "Film", false, exposure = 0.04f, contrast = 0.15f, saturation = -0.05f, warmth = 0.12f, vignette = 0.18f),
        FilterPresetSpec("Kodak Portra", "Film", false, exposure = 0.08f, contrast = 0.10f, saturation = 0.08f, warmth = 0.15f),
        FilterPresetSpec("Fuji Velvia", "Film", false, contrast = 0.22f, saturation = 0.32f, warmth = -0.04f),
        FilterPresetSpec("Vintage 90s", "Film", false, exposure = 0.05f, contrast = -0.05f, saturation = -0.15f, warmth = 0.22f, tint = 0.06f),
        FilterPresetSpec("Polaroid Fade", "Film", false, exposure = 0.10f, contrast = -0.08f, saturation = -0.12f, warmth = 0.14f),
        FilterPresetSpec("Super 8", "Film", true, contrast = 0.18f, saturation = -0.08f, warmth = 0.20f, vignette = 0.32f),
        FilterPresetSpec("Warm Gold", "Warm", false, exposure = 0.06f, contrast = 0.12f, saturation = 0.14f, warmth = 0.26f),
        FilterPresetSpec("Sunset Amber", "Warm", false, exposure = 0.04f, contrast = 0.16f, saturation = 0.20f, warmth = 0.32f),
        FilterPresetSpec("Honey Glow", "Warm", false, exposure = 0.10f, contrast = 0.08f, saturation = 0.10f, warmth = 0.22f),
        FilterPresetSpec("Desert Rose", "Warm", false, exposure = 0.05f, contrast = 0.10f, saturation = 0.12f, warmth = 0.18f, tint = 0.14f),
        FilterPresetSpec("Caramel Tone", "Warm", false, contrast = 0.18f, saturation = 0.05f, warmth = 0.28f),
        FilterPresetSpec("Terracotta", "Warm", true, contrast = 0.20f, saturation = 0.15f, warmth = 0.30f, tint = 0.06f),
        FilterPresetSpec("Cool Sapphire", "Cool", false, exposure = 0.02f, contrast = 0.16f, saturation = 0.10f, warmth = -0.26f),
        FilterPresetSpec("Arctic Ice", "Cool", false, exposure = 0.10f, contrast = 0.14f, saturation = -0.10f, warmth = -0.30f),
        FilterPresetSpec("Cyber Blue", "Cool", false, contrast = 0.24f, saturation = 0.22f, warmth = -0.24f, tint = 0.12f),
        FilterPresetSpec("Moonlight", "Cool", false, exposure = -0.04f, contrast = 0.20f, saturation = -0.12f, warmth = -0.22f, vignette = 0.24f),
        FilterPresetSpec("Nordic Slate", "Cool", false, contrast = 0.12f, saturation = -0.22f, warmth = -0.18f),
        FilterPresetSpec("Ocean Breeze", "Cool", true, exposure = 0.06f, contrast = 0.14f, saturation = 0.16f, warmth = -0.16f, tint = -0.08f),
        FilterPresetSpec("Mono Silver", "Mono", false, contrast = 0.22f, saturation = -1.0f),
        FilterPresetSpec("Silvertone", "Mono", false, exposure = 0.08f, contrast = 0.16f, saturation = -1.0f, warmth = 0.05f),
        FilterPresetSpec("Leica Mono", "Mono", false, contrast = 0.30f, saturation = -1.0f, vignette = 0.20f),
        FilterPresetSpec("Soft B&W", "Mono", false, exposure = 0.10f, contrast = -0.04f, saturation = -1.0f),
        FilterPresetSpec("Noir Deep", "Noir", false, exposure = -0.08f, contrast = 0.42f, saturation = -1.0f, vignette = 0.45f),
        FilterPresetSpec("Noir Shadow", "Noir", false, exposure = -0.12f, contrast = 0.38f, saturation = -1.0f, vignette = 0.55f),
        FilterPresetSpec("Midnight Noir", "Noir", false, exposure = -0.06f, contrast = 0.35f, saturation = -0.85f, warmth = -0.15f, vignette = 0.40f),
        FilterPresetSpec("VIP Champagne", "VIP", true, exposure = 0.08f, contrast = 0.20f, saturation = 0.12f, warmth = 0.20f, vignette = 0.15f),
        FilterPresetSpec("VIP Royal Violet", "VIP", true, contrast = 0.24f, saturation = 0.18f, warmth = -0.10f, tint = 0.24f, vignette = 0.25f),
        FilterPresetSpec("VIP ProRAW 8K", "VIP", true, exposure = 0.05f, contrast = 0.22f, saturation = 0.16f)
    )

    fun localizedTagline(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> "iPhone-quality photos and videos, in one tap"
        AppLanguage.UR -> "ایک ٹیپ میں آئی فون جیسی ایچ ڈی تصاویر اور ویڈیوز"
        AppLanguage.HI -> "एक टैप में iPhone जैसी HD फ़ोटो और वीडियो"
        AppLanguage.AR -> "صور وفيديوهات بجودة آيفون بلمسة واحدة"
        AppLanguage.ES -> "Fotos y videos con calidad iPhone, en un toque"
    }

    fun localizedHomeGreeting(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> "Lumina Studio"
        AppLanguage.UR -> "لومینا اسٹوڈیو"
        AppLanguage.HI -> "ल्यूमिना स्टूडियो"
        AppLanguage.AR -> "استوديو لومينا"
        AppLanguage.ES -> "Estudio Lumina"
    }
}
