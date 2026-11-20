package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Apple-inspired Dark-First Obsidian Glass Palette
val ObsidianBlack = Color(0xFF07070B)
val ObsidianSurface = Color(0xFF101018)
val ObsidianCard = Color(0xFF171722)
val ObsidianElevated = Color(0xFF20202E)
val ObsidianBorder = Color(0x28FFFFFF)

// Light Porcelain Mode Palette
val PorcelainWhite = Color(0xFFF6F6FA)
val PorcelainSurface = Color(0xFFFFFFFF)
val PorcelainCard = Color(0xFFF0F0F7)
val PorcelainElevated = Color(0xFFE6E6F0)
val PorcelainBorder = Color(0x18000000)

// Primary Accent (Apple-grade Sapphire / Iris)
val StudioBlue = Color(0xFF0A84FF)
val StudioBlueDark = Color(0xFF0066CC)
val EmeraldSuccess = Color(0xFF30D158)
val AmberStreak = Color(0xFFFF9F0A)
val CoralAlert = Color(0xFFFF453A)

// VIP Exclusive Gold & Violet Palette (Strictly reserved for VIP elements)
val VipGold = Color(0xFFF5C542)
val VipGoldDeep = Color(0xFFD99B1E)
val VipViolet = Color(0xFF8B5CF6)
val VipVioletDeep = Color(0xFF6D28D9)

val VipGoldVioletBrush = Brush.linearGradient(
    colors = listOf(VipGold, Color(0xFFE8A435), VipViolet)
)

val VipGoldBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFFFE07D), VipGold, VipGoldDeep)
)

val VipVioletBrush = Brush.linearGradient(
    colors = listOf(VipViolet, VipVioletDeep)
)

val TextPrimaryDark = Color(0xFFF5F5F7)
val TextSecondaryDark = Color(0xFFA1A1B0)
val TextPrimaryLight = Color(0xFF111118)
val TextSecondaryLight = Color(0xFF636373)
