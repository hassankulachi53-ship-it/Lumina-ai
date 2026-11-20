package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesomeMosaic
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ImageEnhancerEngine
import com.example.model.CatalogData
import com.example.model.ExportFormat
import com.example.model.MediaType
import com.example.model.SavedWorkItem
import com.example.model.UiState
import com.example.ui.components.GlassCard
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.VipGold
import kotlinx.coroutines.launch

@Composable
fun MyWorkScreen(
    savedWorkState: UiState<List<SavedWorkItem>>,
    isAuthenticated: Boolean,
    onToggleFavorite: (SavedWorkItem) -> Unit,
    onDeleteItem: (SavedWorkItem) -> Unit,
    onReEditInStudio: (SavedWorkItem) -> Unit,
    onRequestSignIn: () -> Unit,
    onStatusMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf("All") }
    var recapPreview by remember { mutableStateOf<Bitmap?>(null) }

    val tabs = listOf("All", "Photos", "Videos", "Wallpapers", "Favorites")

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
                Text("My Work", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(
                        Icons.Rounded.CloudDone,
                        contentDescription = "Cloud Sync",
                        tint = if (isAuthenticated) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isAuthenticated) "Cloud Backup & Sync Active" else "Sign in for Cloud Sync",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val samples = listOf(
                            ImageEnhancerEngine.loadBitmapFromDrawable(context, R.drawable.img_demo_city_nights, 540),
                            ImageEnhancerEngine.loadBitmapFromDrawable(context, R.drawable.img_demo_nature_colors, 540),
                            ImageEnhancerEngine.loadBitmapFromDrawable(context, R.drawable.img_demo_foodie_focus, 540),
                            ImageEnhancerEngine.loadBitmapFromDrawable(context, R.drawable.img_demo_portrait_studio, 540)
                        )
                        val collage = ImageEnhancerEngine.createRecapCollage(samples)
                        recapPreview = collage
                        ImageEnhancerEngine.saveBitmapToGallery(context, collage, "Lumina_Monthly_Recap", ExportFormat.JPG)
                        onStatusMessage("Monthly Best Recap Collage saved to Pictures/Lumina AI!")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("my_work_recap_button")
            ) {
                Icon(Icons.Rounded.AutoAwesomeMosaic, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("Monthly Recap")
            }
        }

        if (recapPreview != null) {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Monthly Best Recap Collage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VipGold)
                    Image(
                        bitmap = recapPreview!!.asImageBitmap(),
                        contentDescription = "Monthly Recap Collage",
                        modifier = Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tabs) { tab ->
                FilterChip(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    label = { Text(tab) },
                    modifier = Modifier.testTag("my_work_tab_${tab.lowercase()}")
                )
            }
        }

        if (!isAuthenticated) {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cloud Gallery Sync", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Sign in with Google to save enhanced photos, videos, favorites, and folders across devices.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onRequestSignIn,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Sign in with Google")
                    }
                }
            }
        } else {
            when (savedWorkState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Text(savedWorkState.message, color = MaterialTheme.colorScheme.error)
                }
                is UiState.Success -> {
                    val items = remember(savedWorkState.data, selectedTab) {
                        when (selectedTab) {
                            "Photos" -> savedWorkState.data.filter { it.mediaType == MediaType.PHOTO.name }
                            "Videos" -> savedWorkState.data.filter { it.mediaType == MediaType.VIDEO.name }
                            "Wallpapers" -> savedWorkState.data.filter { it.mediaType == MediaType.WALLPAPER.name }
                            "Favorites" -> savedWorkState.data.filter { it.favorite }
                            else -> savedWorkState.data
                        }
                    }

                    if (items.isEmpty()) {
                        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = StudioBlue, modifier = Modifier.size(36.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No Saved Edits Yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Enhance any photo or video in Studio and tap Download to sync it here.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    var exported = 0
                                    items.forEachIndexed { i, work ->
                                        val bmp = ImageEnhancerEngine.decodeBase64ToBitmap(work.thumbnailBase64)
                                        if (bmp != null) {
                                            ImageEnhancerEngine.saveBitmapToGallery(context, bmp, "${work.title}_$i", ExportFormat.JPG)
                                            exported++
                                        }
                                    }
                                    onStatusMessage("Exported $exported items to Pictures/Lumina AI album")
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("download_all_work_button")
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Download All (${items.size}) to Lumina AI Album")
                        }

                        items.forEach { item ->
                            val thumb = remember(item.thumbnailBase64) {
                                ImageEnhancerEngine.decodeBase64ToBitmap(item.thumbnailBase64)
                            }
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().testTag("work_item_${item.id}"),
                                cornerRadius = 20.dp,
                                onClick = { onReEditInStudio(item) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (thumb != null) {
                                        Image(
                                            bitmap = thumb.asImageBitmap(),
                                            contentDescription = item.title,
                                            modifier = Modifier.size(68.dp).clip(RoundedCornerShape(14.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = CatalogData.drawableForKey("img_demo_city_nights")),
                                            contentDescription = item.title,
                                            modifier = Modifier.size(68.dp).clip(RoundedCornerShape(14.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(item.enhancementSummary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${item.qualityLabel} • ${item.folderName}", style = MaterialTheme.typography.labelSmall, color = StudioBlue)
                                    }
                                    IconButton(onClick = { onToggleFavorite(item) }) {
                                        Icon(
                                            if (item.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                            contentDescription = "Toggle Favorite",
                                            tint = if (item.favorite) VipGold else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { onDeleteItem(item) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete Work")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(74.dp))
    }
}
