package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ImageEnhancerEngine
import com.example.model.CatalogData
import com.example.model.WallpaperCatalogItem
import com.example.ui.components.GlassCard
import com.example.ui.components.VipCrownBadge
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldVioletBrush
import kotlinx.coroutines.launch

@Composable
fun WallpapersScreen(
    cloudWallpapers: List<WallpaperCatalogItem>,
    isVip: Boolean,
    dailyAutoWallpaperEnabled: Boolean,
    onToggleDailyAutoWallpaper: (Boolean) -> Unit,
    onConvertMyPhotoToWallpaper: () -> Unit,
    onOpenPaywall: () -> Unit,
    onStatusMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allWallpapers = remember(cloudWallpapers) {
        (cloudWallpapers + CatalogData.builtInWallpapers).distinctBy { it.title }
    }
    var selectedCategory by remember { mutableStateOf("All") }
    val filtered = remember(allWallpapers, selectedCategory) {
        if (selectedCategory == "All") allWallpapers
        else allWallpapers.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }
    var selectedWallpaper by remember(filtered) {
        mutableStateOf(filtered.firstOrNull() ?: CatalogData.builtInWallpapers.first())
    }
    var previewHomeScreen by remember { mutableStateOf(false) }
    var parallaxEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Wallpaper 4K Studio",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Exact screen fit • Lock & Home preview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = onConvertMyPhotoToWallpaper,
                shape = CircleShape,
                modifier = Modifier.testTag("wallpaper_convert_photo_button")
            ) {
                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("My Photo")
            }
        }

        // 20+ Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(CatalogData.wallpaperCategories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    modifier = Modifier.testTag("wp_cat_${cat.lowercase().replace(" ", "_")}")
                )
            }
        }

        // iPhone-style Lock Screen / Home Screen Live Preview Frame
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(230.dp)
                    .height(410.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(ObsidianBlack)
            ) {
                Image(
                    painter = painterResource(id = CatalogData.drawableForKey(selectedWallpaper.drawableKey)),
                    contentDescription = selectedWallpaper.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0x55000000), Color.Transparent, Color(0x88000000))))
                )

                if (!previewHomeScreen) {
                    // Lock Screen Clock + Widgets
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 34.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Friday, Nov 20",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                        Text(
                            text = "09:41",
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(color = Color(0x6612121B), shape = RoundedCornerShape(14.dp)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Rounded.WbSunny, contentDescription = null, tint = VipGold, modifier = Modifier.size(14.dp))
                                    Text("22°C", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            }
                            Surface(color = Color(0x6612121B), shape = RoundedCornerShape(14.dp)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null, tint = Color(0xFF30D158), modifier = Modifier.size(14.dp))
                                    Text("96%", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    // Home Screen App Icons Grid Preview
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 38.dp, start = 20.dp, end = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        repeat(3) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                repeat(4) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xAAFFFFFF))
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Resolution Badge
                Surface(
                    color = Color(0x9907070B),
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp)
                ) {
                    Text(
                        text = "${selectedWallpaper.title} • ${selectedWallpaper.resolutionLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Lock vs Home Preview Toggle + One-Tap Set Wallpaper
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { previewHomeScreen = !previewHomeScreen },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("toggle_wallpaper_preview_mode")
            ) {
                Icon(Icons.Rounded.Smartphone, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (previewHomeScreen) "Lock Preview" else "Home Preview")
            }

            Button(
                onClick = {
                    if (selectedWallpaper.vipOnly && !isVip) {
                        onOpenPaywall()
                    } else {
                        scope.launch {
                            val bmp = ImageEnhancerEngine.loadBitmapFromDrawable(
                                context,
                                CatalogData.drawableForKey(selectedWallpaper.drawableKey),
                                1920
                            )
                            val res = ImageEnhancerEngine.setAsWallpaper(context, bmp, lockScreenOnly = false)
                            onStatusMessage(
                                if (res.isSuccess) "Wallpaper set: ${selectedWallpaper.title}!"
                                else res.exceptionOrNull()?.localizedMessage ?: "Unable to set wallpaper"
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedWallpaper.vipOnly && !isVip) VipGold else StudioBlue,
                    contentColor = if (selectedWallpaper.vipOnly && !isVip) ObsidianBlack else Color.White
                ),
                modifier = Modifier.weight(1.2f).heightIn(min = 48.dp).testTag("set_wallpaper_button")
            ) {
                Icon(
                    if (selectedWallpaper.vipOnly && !isVip) Icons.Rounded.Lock else Icons.Rounded.Wallpaper,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (selectedWallpaper.vipOnly && !isVip) "VIP Wallpaper" else "Set Wallpaper",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Wallpaper Picker Carousel
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { wp ->
                val active = wp.id == selectedWallpaper.id
                GlassCard(
                    modifier = Modifier.width(126.dp).testTag("wallpaper_item_${wp.id}"),
                    cornerRadius = 18.dp,
                    borderBrush = if (active) VipGoldVioletBrush else null,
                    onClick = { selectedWallpaper = wp }
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                        Image(
                            painter = painterResource(id = CatalogData.drawableForKey(wp.drawableKey)),
                            contentDescription = wp.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (wp.vipOnly) {
                            VipCrownBadge(
                                label = "VIP",
                                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(wp.title, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                        Text(wp.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // VIP Live/Parallax & Daily Auto Changer
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp, borderBrush = VipGoldVioletBrush) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Live & Parallax Depth (VIP)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("3D gyroscopic depth motion", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = parallaxEnabled,
                        onCheckedChange = {
                            if (!isVip) onOpenPaywall() else parallaxEnabled = it
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Auto Wallpaper Changer (VIP)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Fresh 4K wallpaper every morning", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = dailyAutoWallpaperEnabled,
                        onCheckedChange = {
                            if (!isVip) onOpenPaywall() else onToggleDailyAutoWallpaper(it)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(74.dp))
    }
}
