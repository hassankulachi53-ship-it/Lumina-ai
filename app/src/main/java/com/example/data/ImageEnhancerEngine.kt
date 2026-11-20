package com.example.data

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.model.CatalogData
import com.example.model.EditParameters
import com.example.model.EnhancementTool
import com.example.model.ExportFormat
import com.example.model.QualityOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object ImageEnhancerEngine {

    suspend fun loadBitmapFromDrawable(context: Context, drawableResId: Int, maxDim: Int = 1440): Bitmap =
        withContext(Dispatchers.IO) {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, drawableResId, opts)
            var sampleSize = 1
            while (opts.outWidth / sampleSize > maxDim || opts.outHeight / sampleSize > maxDim) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeResource(context.resources, drawableResId, decodeOpts)
                ?: Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        }

    suspend fun loadBitmapFromUri(context: Context, uri: Uri, maxDim: Int = 1600): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOpts)
                }
                var sampleSize = 1
                while (boundsOpts.outWidth / sampleSize > maxDim || boundsOpts.outHeight / sampleSize > maxDim) {
                    sampleSize *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                }
            } catch (_: Exception) {
                null
            }
        }

    /**
     * Generates a realistic un-enhanced "Before" sample for built-in showcase demos
     * (soft motion blur / underexposed low-light / vintage faded B&W) so the interactive
     * before/after slider demonstrates genuine optical restoration.
     */
    suspend fun createDegradedBeforePreview(original: Bitmap, mode: EnhancementTool): Bitmap =
        withContext(Dispatchers.Default) {
            val w = original.width
            val h = original.height
            val downscaleFactor = when (mode) {
                EnhancementTool.UNBLUR, EnhancementTool.UPSCALE, EnhancementTool.VIDEO_ENHANCE -> 6
                else -> 3
            }
            val tiny = Bitmap.createScaledBitmap(
                original,
                (w / downscaleFactor).coerceAtLeast(32),
                (h / downscaleFactor).coerceAtLeast(32),
                true
            )
            val blurred = Bitmap.createScaledBitmap(tiny, w, h, true)
            tiny.recycle()

            val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val cm = ColorMatrix()

            when (mode) {
                EnhancementTool.COLORIZE, EnhancementTool.RESTORE -> {
                    cm.setSaturation(0f)
                    val fade = ColorMatrix(
                        floatArrayOf(
                            0.78f, 0f, 0f, 0f, 28f,
                            0f, 0.74f, 0f, 0f, 22f,
                            0f, 0f, 0.68f, 0f, 14f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    cm.postConcat(fade)
                }
                EnhancementTool.NIGHT_MODE -> {
                    val dark = ColorMatrix(
                        floatArrayOf(
                            0.42f, 0f, 0f, 0f, -16f,
                            0f, 0.44f, 0f, 0f, -14f,
                            0f, 0f, 0.50f, 0f, -8f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    cm.postConcat(dark)
                }
                else -> {
                    cm.setSaturation(0.68f)
                    val flat = ColorMatrix(
                        floatArrayOf(
                            0.84f, 0f, 0f, 0f, 12f,
                            0f, 0.84f, 0f, 0f, 12f,
                            0f, 0f, 0.84f, 0f, 12f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    cm.postConcat(flat)
                }
            }

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = ColorMatrixColorFilter(cm)
            }
            canvas.drawBitmap(blurred, 0f, 0f, paint)
            blurred.recycle()
            output
        }

    suspend fun applyEnhancement(
        source: Bitmap,
        params: EditParameters,
        quality: QualityOption = QualityOption.HD,
        addWatermark: Boolean = false
    ): Bitmap = withContext(Dispatchers.Default) {
        val working = applyCropAndRotation(source, params.rotationDegrees, params.aspectRatioLabel)
        val filterSpec = CatalogData.filterPresets.firstOrNull { it.name == params.filterName }
        val strength = params.strength.multiplier

        val exposure = ((params.exposure + (filterSpec?.exposure ?: 0f)) * strength).coerceIn(-0.8f, 0.9f)
        val contrast = ((params.contrast + (filterSpec?.contrast ?: 0f)) * strength).coerceIn(-0.5f, 0.85f)
        val saturation = if (filterSpec != null && filterSpec.saturation <= -0.9f) {
            -1f
        } else {
            ((params.saturation + params.vibrance * 0.6f + (filterSpec?.saturation ?: 0f)) * strength).coerceIn(-1f, 1.2f)
        }
        val warmth = ((params.warmth + (filterSpec?.warmth ?: 0f)) * strength).coerceIn(-0.6f, 0.6f)
        val tint = ((params.tint + (filterSpec?.tint ?: 0f)) * strength).coerceIn(-0.5f, 0.5f)
        val brilliance = (params.brilliance * strength).coerceIn(-0.5f, 0.7f)
        val shadowLift = (params.shadows * 0.35f * strength).coerceIn(-0.3f, 0.45f)
        val highlightRecover = (params.highlights * 0.25f * strength).coerceIn(-0.4f, 0.4f)

        val targetScale = when {
            quality == QualityOption.UHD_8K -> 1.8f
            quality == QualityOption.UHD_4K -> 1.4f
            params.tool == EnhancementTool.UPSCALE -> 1.4f
            else -> 1.0f
        }
        val maxTargetDim = 2200
        val outW = (working.width * targetScale).roundToInt().coerceAtMost(maxTargetDim).coerceAtLeast(1)
        val outH = (working.height * targetScale).roundToInt().coerceAtMost(maxTargetDim).coerceAtLeast(1)
        val scaled = if (outW != working.width || outH != working.height) {
            Bitmap.createScaledBitmap(working, outW, outH, true)
        } else {
            working.copy(Bitmap.Config.ARGB_8888, true)
        }
        if (working !== source && working !== scaled) working.recycle()

        val result = Bitmap.createBitmap(scaled.width, scaled.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val cm = ColorMatrix()
        cm.setSaturation((1f + saturation).coerceIn(0f, 2.2f))

        val c = 1f + contrast + (params.definition * 0.16f * strength)
        val translate = 128f * (1f - c) + (exposure * 58f) + (brilliance * 24f) + (shadowLift * 26f) + (highlightRecover * 14f)
        val rScale = c * (1f + warmth * 0.22f)
        val gScale = c * (1f - tint * 0.14f)
        val bScale = c * (1f - warmth * 0.22f)

        val toneMatrix = ColorMatrix(
            floatArrayOf(
                rScale, 0f, 0f, 0f, translate,
                0f, gScale, 0f, 0f, translate,
                0f, 0f, bScale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(toneMatrix)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }
        canvas.drawBitmap(scaled, 0f, 0f, paint)
        if (scaled !== source) scaled.recycle()

        // Edge-aware sharpening for Unblur / Definition / Smart Enhance
        val sharpenAmount = ((params.sharpness + params.definition * 0.5f) * strength).coerceIn(0f, 1.2f)
        val sharpened = if (sharpenAmount > 0.08f) {
            applyFastUnsharpMask(result, amount = sharpenAmount * 0.52f).also {
                result.recycle()
            }
        } else {
            result
        }

        // Portrait Light studio glow & soft background vignette
        val finalBitmap = if (sharpened.isMutable) {
            sharpened
        } else {
            sharpened.copy(Bitmap.Config.ARGB_8888, true).also {
                if (it !== sharpened) sharpened.recycle()
            }
        }
        val finalCanvas = Canvas(finalBitmap)
        if (params.tool == EnhancementTool.PORTRAIT_LIGHT || params.skinRetouch > 0.35f) {
            val glowColor = when (params.portraitLightStyle) {
                "Stage" -> Color.argb(46, 255, 224, 150)
                "Contour" -> Color.argb(36, 255, 198, 130)
                else -> Color.argb(30, 255, 236, 200)
            }
            val cx = finalBitmap.width * 0.5f
            val cy = finalBitmap.height * 0.38f
            val radius = maxOf(finalBitmap.width, finalBitmap.height) * 0.55f
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    cx, cy, radius,
                    intArrayOf(glowColor, Color.TRANSPARENT),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            finalCanvas.drawRect(0f, 0f, finalBitmap.width.toFloat(), finalBitmap.height.toFloat(), glowPaint)
        }

        val vigStrength = (params.vignette + (filterSpec?.vignette ?: 0f) + params.backgroundBlur * 0.35f).coerceIn(0f, 0.85f)
        if (vigStrength > 0.03f) {
            val cx = finalBitmap.width / 2f
            val cy = finalBitmap.height / 2f
            val radius = maxOf(cx, cy) * 1.15f
            val vigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    cx, cy, radius,
                    intArrayOf(Color.TRANSPARENT, Color.argb((vigStrength * 175).toInt(), 6, 6, 12)),
                    floatArrayOf(0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            finalCanvas.drawRect(0f, 0f, finalBitmap.width.toFloat(), finalBitmap.height.toFloat(), vigPaint)
        }

        // Optional text / sticker overlay from Pro Editor
        if (params.overlayText.isNotBlank() || params.overlaySticker.isNotBlank()) {
            val badgeText = listOf(params.overlaySticker, params.overlayText)
                .filter { it.isNotBlank() }
                .joinToString(" ")
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = (finalBitmap.width * 0.045f).coerceIn(22f, 64f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(8f, 0f, 3f, Color.argb(180, 0, 0, 0))
            }
            finalCanvas.drawText(
                badgeText,
                finalBitmap.width * 0.06f,
                finalBitmap.height * 0.90f,
                textPaint
            )
        }

        // Subtle free-tier watermark when enabled
        if (addWatermark) {
            val wmText = "Lumina AI • ProHDR"
            val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(215, 255, 255, 255)
                textSize = (finalBitmap.width * 0.028f).coerceIn(14f, 32f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bounds = Rect()
            wmPaint.getTextBounds(wmText, 0, wmText.length, bounds)
            val padX = 18f
            val padY = 12f
            val rectRight = finalBitmap.width - 20f
            val rectBottom = finalBitmap.height - 20f
            val bgRect = RectF(
                rectRight - bounds.width() - padX * 2,
                rectBottom - bounds.height() - padY * 2,
                rectRight,
                rectBottom
            )
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(140, 12, 12, 20)
            }
            finalCanvas.drawRoundRect(bgRect, 14f, 14f, bgPaint)
            finalCanvas.drawText(wmText, bgRect.left + padX, bgRect.bottom - padY, wmPaint)
        }

        finalBitmap
    }

    private fun applyCropAndRotation(source: Bitmap, rotationDegrees: Int, aspectRatioLabel: String): Bitmap {
        var current = source
        if (rotationDegrees % 360 != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            current = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }
        val ratio: Float? = when (aspectRatioLabel) {
            "1:1" -> 1f
            "4:5" -> 0.8f
            "9:16" -> 9f / 16f
            "16:9" -> 16f / 9f
            else -> null
        }
        if (ratio == null) return current

        val curRatio = current.width.toFloat() / current.height.toFloat()
        val cropW: Int
        val cropH: Int
        if (curRatio > ratio) {
            cropH = current.height
            cropW = (cropH * ratio).roundToInt().coerceAtMost(current.width)
        } else {
            cropW = current.width
            cropH = (cropW / ratio).roundToInt().coerceAtMost(current.height)
        }
        val x = ((current.width - cropW) / 2).coerceAtLeast(0)
        val y = ((current.height - cropH) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(current, x, y, cropW.coerceAtLeast(1), cropH.coerceAtLeast(1))
        if (current !== source && current !== cropped) current.recycle()
        return cropped
    }

    private fun applyFastUnsharpMask(source: Bitmap, amount: Float): Bitmap {
        val w = source.width
        val h = source.height
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)
        val out = pixels.copyOf()
        val k = amount.coerceIn(0f, 0.8f)

        for (y in 1 until h - 1) {
            val rowOffset = y * w
            for (x in 1 until w - 1) {
                val idx = rowOffset + x
                val c = pixels[idx]
                val n = pixels[idx - w]
                val s = pixels[idx + w]
                val e = pixels[idx + 1]
                val west = pixels[idx - 1]

                val cr = (c shr 16) and 0xFF
                val cg = (c shr 8) and 0xFF
                val cb = c and 0xFF

                val avgR = (((n shr 16) and 0xFF) + ((s shr 16) and 0xFF) + ((e shr 16) and 0xFF) + ((west shr 16) and 0xFF)) shr 2
                val avgG = (((n shr 8) and 0xFF) + ((s shr 8) and 0xFF) + ((e shr 8) and 0xFF) + ((west shr 8) and 0xFF)) shr 2
                val avgB = ((n and 0xFF) + (s and 0xFF) + (e and 0xFF) + (west and 0xFF)) shr 2

                val nr = (cr + ((cr - avgR) * k)).roundToInt().coerceIn(0, 255)
                val ng = (cg + ((cg - avgG) * k)).roundToInt().coerceIn(0, 255)
                val nb = (cb + ((cb - avgB) * k)).roundToInt().coerceIn(0, 255)
                out[idx] = (0xFF shl 24) or (nr shl 16) or (ng shl 8) or nb
            }
        }
        val mutableOut = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mutableOut.setPixels(out, 0, w, 0, 0, w, h)
        return mutableOut
    }

    suspend fun estimateEncodedSizeLabel(bitmap: Bitmap, format: ExportFormat): String =
        withContext(Dispatchers.Default) {
            val stream = ByteArrayOutputStream()
            compressBitmap(bitmap, format, 94, stream)
            val kb = stream.size() / 1024.0
            if (kb >= 1024.0) String.format("%.2f MB", kb / 1024.0)
            else String.format("%d KB", kb.roundToInt().coerceAtLeast(1))
        }

    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        title: String,
        format: ExportFormat
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val safeName = title.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(32)
            val fileName = "${safeName}_${System.currentTimeMillis()}.${format.ext}"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Lumina AI")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext Result.failure(IllegalStateException("Unable to create MediaStore entry."))

            resolver.openOutputStream(uri)?.use { out ->
                compressBitmap(bitmap, format, 96, out)
            } ?: return@withContext Result.failure(IllegalStateException("Unable to open gallery output stream."))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setAsWallpaper(context: Context, bitmap: Bitmap, lockScreenOnly: Boolean = false): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val manager = WallpaperManager.getInstance(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val flags = if (lockScreenOnly) WallpaperManager.FLAG_LOCK else WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    manager.setBitmap(bitmap, null, true, flags)
                } else {
                    manager.setBitmap(bitmap)
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun prepareShareIntent(
        context: Context,
        bitmap: Bitmap,
        format: ExportFormat,
        caption: String
    ): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.cacheDir, "shared_media").apply { mkdirs() }
            val file = File(dir, "lumina_share_${System.currentTimeMillis()}.${format.ext}")
            FileOutputStream(file).use { out ->
                compressBitmap(bitmap, format, 94, out)
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = format.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            Result.success(Intent.createChooser(intent, "Share with Lumina AI"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun bitmapToCompactBase64(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        val ratio = minOf(320f / bitmap.width, 320f / bitmap.height, 1f)
        val thumb = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).roundToInt().coerceAtLeast(1),
            (bitmap.height * ratio).roundToInt().coerceAtLeast(1),
            true
        )
        val out = ByteArrayOutputStream()
        thumb.compress(Bitmap.CompressFormat.JPEG, 72, out)
        if (thumb !== bitmap) thumb.recycle()
        Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    fun decodeBase64ToBitmap(base64: String?): Bitmap? {
        if (base64.isNullOrBlank()) return null
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun createRecapCollage(tiles: List<Bitmap>): Bitmap = withContext(Dispatchers.Default) {
        val size = 1080
        val collage = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(collage)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, size.toFloat(), size.toFloat(),
                intArrayOf(Color.parseColor("#0B0B14"), Color.parseColor("#1B1430")),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        val cell = 490f
        val gap = 34f
        val slots = listOf(
            RectF(gap, gap, gap + cell, gap + cell),
            RectF(gap * 2 + cell, gap, gap * 2 + cell * 2, gap + cell),
            RectF(gap, gap * 2 + cell, gap + cell, gap * 2 + cell * 2),
            RectF(gap * 2 + cell, gap * 2 + cell, gap * 2 + cell * 2, gap * 2 + cell * 2)
        )
        val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        slots.forEachIndexed { idx, rect ->
            val bmp = tiles.getOrNull(idx % tiles.size.coerceAtLeast(1))
            if (bmp != null) {
                canvas.save()
                val clipPath = android.graphics.Path().apply {
                    addRoundRect(rect, 28f, 28f, android.graphics.Path.Direction.CW)
                }
                canvas.clipPath(clipPath)
                canvas.drawBitmap(bmp, null, rect, tilePaint)
                canvas.restore()
            }
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F5C542")
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(12f, 0f, 4f, Color.BLACK)
        }
        canvas.drawText("LUMINA AI • MONTHLY BEST RECAP", 48f, size - 44f, titlePaint)
        collage
    }

    private fun compressBitmap(bitmap: Bitmap, format: ExportFormat, quality: Int, out: java.io.OutputStream) {
        val compressFormat = when (format) {
            ExportFormat.PNG -> Bitmap.CompressFormat.PNG
            ExportFormat.HEIC -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
            else -> Bitmap.CompressFormat.JPEG
        }
        bitmap.compress(compressFormat, quality, out)
    }
}
