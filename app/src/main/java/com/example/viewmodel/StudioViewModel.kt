package com.example.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.AiEnhancementService
import com.example.data.ImageEnhancerEngine
import com.example.model.AiSceneAnalysis
import com.example.model.CatalogData
import com.example.model.EditParameters
import com.example.model.EnhancementTool
import com.example.model.ExportFormat
import com.example.model.QualityOption
import com.example.model.StrengthLevel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "StudioViewModel"

data class StudioUiState(
    val sourceTitle: String = "City Nights Demo",
    val beforeBitmap: Bitmap? = null,
    val rawSourceBitmap: Bitmap? = null,
    val afterBitmap: Bitmap? = null,
    val previousVersionBitmap: Bitmap? = null,
    val params: EditParameters = EditParameters(),
    val isVideoSource: Boolean = false,
    val isProcessing: Boolean = false,
    val processingPercent: Int = 0,
    val processingTip: String = "Analyzing dynamic range & skin textures...",
    val aiAnalysis: AiSceneAnalysis? = null,
    val errorMessage: String? = null,
    val statusBanner: String? = null,
    val estimatedSizeLabel: String = "1.4 MB",
    val selectedQuality: QualityOption = QualityOption.HD,
    val selectedFormat: ExportFormat = ExportFormat.JPG,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadSuccess: Boolean = false,
    val adWatermarkRemovedOnce: Boolean = false,
    val batchUris: List<Uri> = emptyList(),
    val batchProcessedCount: Int = 0,
    val showRatingPrompt: Boolean = false,
    val hasSuccessfulEnhance: Boolean = false
)

class StudioViewModel(
    private val aiService: AiEnhancementService = AiEnhancementService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private var renderJob: Job? = null

    fun loadDemoSample(
        context: Context,
        drawableResId: Int,
        title: String,
        tool: EnhancementTool = EnhancementTool.SMART_ENHANCE,
        filterName: String = "iPhone Natural"
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    processingPercent = 18,
                    processingTip = "Preparing flagship optical baseline...",
                    errorMessage = null,
                    downloadSuccess = false,
                    isVideoSource = tool == EnhancementTool.VIDEO_ENHANCE || tool == EnhancementTool.CINEMATIC_VIDEO
                )
            }
            val original = ImageEnhancerEngine.loadBitmapFromDrawable(context, drawableResId, 1280)
            val before = ImageEnhancerEngine.createDegradedBeforePreview(original, tool)
            val initialParams = defaultParamsForTool(tool).copy(filterName = filterName)
            val enhanced = ImageEnhancerEngine.applyEnhancement(original, initialParams, QualityOption.HD, false)
            val sizeLabel = ImageEnhancerEngine.estimateEncodedSizeLabel(enhanced, _uiState.value.selectedFormat)

            _uiState.update {
                it.copy(
                    sourceTitle = title,
                    rawSourceBitmap = original,
                    beforeBitmap = before,
                    afterBitmap = enhanced,
                    previousVersionBitmap = it.afterBitmap,
                    params = initialParams,
                    isProcessing = false,
                    processingPercent = 100,
                    estimatedSizeLabel = sizeLabel,
                    hasSuccessfulEnhance = true
                )
            }
        }
    }

    fun loadUserMediaUri(context: Context, uri: Uri, isVideo: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    processingPercent = 15,
                    processingTip = if (isVideo) "Extracting 4K keyframe & color profile..." else "Loading full-resolution image...",
                    errorMessage = null,
                    downloadSuccess = false,
                    isVideoSource = isVideo
                )
            }
            val loaded: Bitmap? = if (isVideo) {
                extractVideoKeyFrame(context, uri)
            } else {
                ImageEnhancerEngine.loadBitmapFromUri(context, uri, 1440)
            }
            if (loaded == null) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Could not decode the selected media file."
                    )
                }
                return@launch
            }
            val tool = if (isVideo) EnhancementTool.VIDEO_ENHANCE else _uiState.value.params.tool
            val nextParams = defaultParamsForTool(tool)
            _uiState.update {
                it.copy(
                    sourceTitle = if (isVideo) "Video 4K Frame" else "Studio Photo",
                    rawSourceBitmap = loaded,
                    beforeBitmap = loaded,
                    params = nextParams,
                    processingPercent = 45,
                    processingTip = "Running Lumina AI Smart Enhance..."
                )
            }
            runSmartEnhanceWithAi(isVip = false)
        }
    }

    fun runSmartEnhanceWithAi(isVip: Boolean) {
        val raw = _uiState.value.rawSourceBitmap ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    processingPercent = 22,
                    processingTip = if (isVip) "VIP Fast Lane • Gemini Vision scene tuning..." else "AI analyzing lighting, faces & scene...",
                    errorMessage = null,
                    downloadSuccess = false
                )
            }
            val progressTicker = launch {
                val tips = listOf(
                    "Preserving natural skin texture & pores...",
                    "Balancing iPhone-style ProRAW highlights...",
                    "Applying edge-aware micro-contrast..."
                )
                var p = 28
                var idx = 0
                while (p < 88) {
                    delay(450)
                    p += 12
                    _uiState.update { s ->
                        s.copy(processingPercent = p.coerceAtMost(88), processingTip = tips[idx % tips.size])
                    }
                    idx++
                }
            }

            try {
                val aiResult = aiService.analyzePhotoForSmartEnhance(raw)
                progressTicker.cancel()
                val currentParams = _uiState.value.params
                val tunedParams: EditParameters
                val analysis: AiSceneAnalysis?
                var banner: String? = null

                if (aiResult.isSuccess) {
                    analysis = aiResult.getOrNull()!!
                    tunedParams = currentParams.copy(
                        exposure = analysis.exposureBoost,
                        contrast = analysis.contrastBoost,
                        warmth = analysis.warmthShift,
                        sharpness = analysis.sharpnessLevel,
                        vibrance = analysis.vibranceBoost,
                        shadows = analysis.shadowLift,
                        highlights = analysis.highlightRecovery,
                        skinRetouch = analysis.skinRetouch,
                        filterName = if (CatalogData.filterPresets.any { it.name == analysis.filterSuggestion }) {
                            analysis.filterSuggestion
                        } else {
                            currentParams.filterName
                        }
                    )
                    banner = "AI Auto-Mode: ${analysis.sceneType} • ${analysis.recommendedTool}"
                } else {
                    // Surface real AI error cleanly while still completing DSP optical enhancement
                    val err = aiResult.exceptionOrNull()
                    Log.e(TAG, "AI analysis returned error", err)
                    analysis = null
                    tunedParams = currentParams
                    banner = "Applied optical HDR pass (AI Scene Assist: ${err?.localizedMessage ?: "offline"})"
                }

                val enhanced = ImageEnhancerEngine.applyEnhancement(
                    raw, tunedParams, _uiState.value.selectedQuality, false
                )
                val sizeLabel = ImageEnhancerEngine.estimateEncodedSizeLabel(enhanced, _uiState.value.selectedFormat)
                _uiState.update {
                    it.copy(
                        afterBitmap = enhanced,
                        previousVersionBitmap = it.afterBitmap,
                        params = tunedParams,
                        aiAnalysis = analysis,
                        isProcessing = false,
                        processingPercent = 100,
                        estimatedSizeLabel = sizeLabel,
                        statusBanner = banner,
                        hasSuccessfulEnhance = true,
                        showRatingPrompt = true
                    )
                }
            } catch (e: CancellationException) {
                progressTicker.cancel()
                throw e
            } catch (e: Exception) {
                progressTicker.cancel()
                Log.e(TAG, "Enhancement error", e)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = e.localizedMessage ?: "Enhancement failed."
                    )
                }
            }
        }
    }

    fun applyTool(tool: EnhancementTool, customInstruction: String? = null, isVip: Boolean = false) {
        val raw = _uiState.value.rawSourceBitmap ?: return
        if (tool.generativeAi) {
            runGenerativeAiTool(raw, tool, customInstruction, isVip)
            return
        }
        val nextParams = defaultParamsForTool(tool)
        _uiState.update {
            it.copy(
                params = nextParams,
                isVideoSource = tool == EnhancementTool.VIDEO_ENHANCE || tool == EnhancementTool.CINEMATIC_VIDEO,
                downloadSuccess = false
            )
        }
        renderWithCurrentParams(showProgress = true, isVip = isVip)
    }

    private fun runGenerativeAiTool(
        raw: Bitmap,
        tool: EnhancementTool,
        customInstruction: String?,
        isVip: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    processingPercent = 25,
                    processingTip = if (isVip) "VIP Fast Lane • Running Gemini Nano Banana Studio..." else "Running Gemini Generative Studio (up to 45s)...",
                    errorMessage = null,
                    downloadSuccess = false
                )
            }
            val result = aiService.generativePhotoTransform(raw, tool, customInstruction)
            if (result.isSuccess) {
                val generated = result.getOrNull()!!
                val sizeLabel = ImageEnhancerEngine.estimateEncodedSizeLabel(generated, _uiState.value.selectedFormat)
                _uiState.update {
                    it.copy(
                        rawSourceBitmap = generated,
                        afterBitmap = generated,
                        previousVersionBitmap = it.afterBitmap,
                        params = it.params.copy(tool = tool),
                        isProcessing = false,
                        processingPercent = 100,
                        estimatedSizeLabel = sizeLabel,
                        statusBanner = "${tool.title} completed via Gemini AI",
                        hasSuccessfulEnhance = true,
                        showRatingPrompt = true
                    )
                }
            } else {
                val errMsg = result.exceptionOrNull()?.localizedMessage ?: "Generative AI edit failed."
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = errMsg
                    )
                }
            }
        }
    }

    fun updateParameters(transform: (EditParameters) -> EditParameters) {
        _uiState.update { it.copy(params = transform(it.params), downloadSuccess = false) }
        renderWithCurrentParams(showProgress = false, isVip = false)
    }

    fun setStrength(strength: StrengthLevel) {
        updateParameters { it.copy(strength = strength) }
    }

    fun selectQualityAndFormat(quality: QualityOption, format: ExportFormat) {
        _uiState.update { it.copy(selectedQuality = quality, selectedFormat = format, downloadSuccess = false) }
        renderWithCurrentParams(showProgress = false, isVip = false)
    }

    fun unlockAdWatermarkRemoval() {
        _uiState.update {
            it.copy(
                adWatermarkRemovedOnce = true,
                statusBanner = "Watermark removed for this export!"
            )
        }
    }

    fun runBatchQueue(context: Context, uris: List<Uri>) {
        if (uris.isEmpty()) return
        val capped = uris.take(100)
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    batchUris = capped,
                    batchProcessedCount = 0,
                    isProcessing = true,
                    processingPercent = 5,
                    processingTip = "VIP Batch Enhance • Processing 0/${capped.size}..."
                )
            }
            var savedCount = 0
            capped.forEachIndexed { idx, uri ->
                val bmp = ImageEnhancerEngine.loadBitmapFromUri(context, uri, 1280)
                if (bmp != null) {
                    val out = ImageEnhancerEngine.applyEnhancement(bmp, _uiState.value.params, QualityOption.UHD_4K, false)
                    ImageEnhancerEngine.saveBitmapToGallery(context, out, "Lumina_Batch_${idx + 1}", ExportFormat.JPG)
                    savedCount++
                }
                val pct = (((idx + 1).toFloat() / capped.size) * 100).toInt().coerceIn(5, 100)
                _uiState.update {
                    it.copy(
                        batchProcessedCount = savedCount,
                        processingPercent = pct,
                        processingTip = "VIP Batch Enhance • Saved $savedCount/${capped.size} to Lumina AI album"
                    )
                }
            }
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    statusBanner = "Batch Complete: $savedCount photos saved to Pictures/Lumina AI"
                )
            }
        }
    }

    fun downloadCurrentResult(
        context: Context,
        isVip: Boolean,
        onSavedToGallery: (Bitmap, EditParameters, QualityOption) -> Unit
    ) {
        val raw = _uiState.value.rawSourceBitmap ?: _uiState.value.afterBitmap ?: return
        val state = _uiState.value
        val addWatermark = !isVip && !state.adWatermarkRemovedOnce
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true, downloadProgress = 0.15f, downloadSuccess = false, errorMessage = null) }
            val finalBitmap = ImageEnhancerEngine.applyEnhancement(raw, state.params, state.selectedQuality, addWatermark)
            _uiState.update { it.copy(downloadProgress = 0.65f) }
            delay(250)
            val res = ImageEnhancerEngine.saveBitmapToGallery(
                context = context,
                bitmap = finalBitmap,
                title = state.sourceTitle,
                format = state.selectedFormat
            )
            if (res.isSuccess) {
                onSavedToGallery(finalBitmap, state.params, state.selectedQuality)
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgress = 1f,
                        downloadSuccess = true,
                        adWatermarkRemovedOnce = false,
                        statusBanner = "Saved to Pictures/Lumina AI (${state.estimatedSizeLabel})"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgress = 0f,
                        errorMessage = res.exceptionOrNull()?.localizedMessage ?: "Failed to save image."
                    )
                }
            }
        }
    }

    fun dismissRatingPrompt() {
        _uiState.update { it.copy(showRatingPrompt = false) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, statusBanner = null) }
    }

    private fun renderWithCurrentParams(showProgress: Boolean, isVip: Boolean) {
        val raw = _uiState.value.rawSourceBitmap ?: return
        renderJob?.cancel()
        renderJob = viewModelScope.launch {
            if (showProgress) {
                _uiState.update {
                    it.copy(
                        isProcessing = true,
                        processingPercent = 40,
                        processingTip = if (isVip) "VIP Fast Lane • Rendering HDR..." else "Applying studio optics..."
                    )
                }
                delay(180)
            }
            val s = _uiState.value
            val out = ImageEnhancerEngine.applyEnhancement(raw, s.params, s.selectedQuality, false)
            val sizeLabel = ImageEnhancerEngine.estimateEncodedSizeLabel(out, s.selectedFormat)
            _uiState.update {
                it.copy(
                    afterBitmap = out,
                    isProcessing = false,
                    processingPercent = 100,
                    estimatedSizeLabel = sizeLabel,
                    hasSuccessfulEnhance = true
                )
            }
        }
    }

    private fun defaultParamsForTool(tool: EnhancementTool): EditParameters = when (tool) {
        EnhancementTool.UNBLUR -> EditParameters(tool = tool, sharpness = 0.72f, definition = 0.50f, contrast = 0.16f)
        EnhancementTool.UPSCALE -> EditParameters(tool = tool, sharpness = 0.58f, definition = 0.45f, contrast = 0.14f, vibrance = 0.16f)
        EnhancementTool.FACE_ENHANCE -> EditParameters(tool = tool, skinRetouch = 0.30f, sharpness = 0.36f, brilliance = 0.18f, warmth = 0.06f)
        EnhancementTool.PORTRAIT_LIGHT -> EditParameters(tool = tool, portraitLightStyle = "Stage", skinRetouch = 0.25f, backgroundBlur = 0.35f, warmth = 0.08f)
        EnhancementTool.NIGHT_MODE -> EditParameters(tool = tool, exposure = 0.28f, shadows = 0.42f, highlights = -0.18f, contrast = 0.18f, noiseReduction = 0.45f, vibrance = 0.22f)
        EnhancementTool.AUTO_HDR -> EditParameters(tool = tool, brilliance = 0.26f, highlights = -0.22f, shadows = 0.30f, contrast = 0.20f, vibrance = 0.24f)
        EnhancementTool.VIDEO_ENHANCE -> EditParameters(tool = tool, exposure = 0.12f, contrast = 0.22f, sharpness = 0.50f, vibrance = 0.24f, filterName = "Cinematic")
        EnhancementTool.CINEMATIC_VIDEO -> EditParameters(tool = tool, contrast = 0.28f, backgroundBlur = 0.40f, vignette = 0.30f, filterName = "Teal & Orange")
        else -> EditParameters(tool = tool)
    }

    private fun extractVideoKeyFrame(context: Context, uri: Uri): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val frame = retriever.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.frameAtTime
            retriever.release()
            frame
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { StudioViewModel(AiEnhancementService()) }
        }
    }
}
