package com.example.data

import android.graphics.Bitmap
import android.util.Log
import com.example.model.AiSceneAnalysis
import com.example.model.ChallengeAiEvaluation
import com.example.model.ChallengePrizeTier
import com.example.model.DailyChallengeTheme
import com.example.model.EnhancementTool
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiEnhancementService"

class AiEnhancementService {

    private val sceneAnalysisSchema = Schema.obj(
        properties = mapOf(
            "sceneType" to Schema.string(description = "Detected scene category: Portrait, Night, Landscape, Food, Old Photo, or Street"),
            "recommendedTool" to Schema.string(description = "Best tool name from: Smart Enhance, Portrait Light, Night Mode, Auto HDR, Unblur, Colorize, Restore"),
            "exposureBoost" to Schema.double(description = "Recommended exposure adjustment between -0.3 and 0.45"),
            "contrastBoost" to Schema.double(description = "Recommended contrast boost between 0.0 and 0.35"),
            "warmthShift" to Schema.double(description = "Recommended warmth shift between -0.25 and 0.25"),
            "sharpnessLevel" to Schema.double(description = "Recommended sharpness between 0.15 and 0.75"),
            "vibranceBoost" to Schema.double(description = "Recommended vibrance between 0.05 and 0.40"),
            "shadowLift" to Schema.double(description = "Shadow lift between 0.0 and 0.45"),
            "highlightRecovery" to Schema.double(description = "Highlight recovery between -0.35 and 0.0"),
            "skinRetouch" to Schema.double(description = "Natural skin polish between 0.0 and 0.35, preserving real texture"),
            "filterSuggestion" to Schema.string(description = "Best matching filter name like iPhone Natural, Vivid, Cinematic, Dubai Gold, or Warm Gold"),
            "reasoning" to Schema.string(description = "Concise 1-sentence flagship camera tuning summary")
        )
    )

    private val challengeJudgeSchema = Schema.obj(
        properties = mapOf(
            "creativityScore" to Schema.integer(description = "Creativity score from 65 to 100"),
            "qualityScore" to Schema.integer(description = "Technical HDR clarity and color score from 65 to 100"),
            "themeMatchScore" to Schema.integer(description = "How well the photo matches the daily challenge theme from 60 to 100"),
            "judgeFeedback" to Schema.string(description = "Concise 1-2 sentence encouraging photography critique"),
            "prizeTier" to Schema.enumeration(
                values = listOf("VIP_DAY_PASS", "EXTRA_CREDITS", "NONE"),
                description = "Award VIP_DAY_PASS for exceptional creativity+match (>=90 avg), EXTRA_CREDITS for strong entries (>=78 avg), else NONE"
            )
        )
    )

    private val analysisModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = sceneAnalysisSchema
            }
        )
    }

    private val challengeJudgeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = challengeJudgeSchema
            }
        )
    }

    private val imageEditModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-nano-banana-2.1",
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.IMAGE)
            }
        )
    }

    suspend fun analyzePhotoForSmartEnhance(bitmap: Bitmap): Result<AiSceneAnalysis> {
        return try {
            val scaled = scaleForAi(bitmap, 768)
            val response = withTimeout(45_000L) {
                analysisModel.generateContent(
                    content {
                        image(scaled)
                        text(
                            "Analyze this photo as an Apple ProRAW & flagship computational photography director. " +
                                "Identify the scene type and return optimal natural HDR tuning parameters. " +
                                "Keep faces completely natural without plastic smoothing."
                        )
                    }
                )
            }
            val jsonText = response.text ?: throw IllegalStateException("AI returned an empty analysis.")
            val json = JSONObject(jsonText)
            Result.success(
                AiSceneAnalysis(
                    sceneType = json.optString("sceneType", "Portrait"),
                    recommendedTool = json.optString("recommendedTool", "Smart Enhance"),
                    exposureBoost = json.optDouble("exposureBoost", 0.10).toFloat().coerceIn(-0.35f, 0.45f),
                    contrastBoost = json.optDouble("contrastBoost", 0.15).toFloat().coerceIn(-0.1f, 0.4f),
                    warmthShift = json.optDouble("warmthShift", 0.04).toFloat().coerceIn(-0.3f, 0.3f),
                    sharpnessLevel = json.optDouble("sharpnessLevel", 0.38).toFloat().coerceIn(0.1f, 0.85f),
                    vibranceBoost = json.optDouble("vibranceBoost", 0.18).toFloat().coerceIn(0f, 0.45f),
                    shadowLift = json.optDouble("shadowLift", 0.20).toFloat().coerceIn(0f, 0.5f),
                    highlightRecovery = json.optDouble("highlightRecovery", -0.10).toFloat().coerceIn(-0.4f, 0.1f),
                    skinRetouch = json.optDouble("skinRetouch", 0.18).toFloat().coerceIn(0f, 0.4f),
                    filterSuggestion = json.optString("filterSuggestion", "iPhone Natural"),
                    reasoning = json.optString("reasoning", "Balanced ProRAW dynamic range and crisp micro-contrast.")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Smart analysis timed out", e)
            Result.failure(Exception("AI scene analysis timed out. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Smart analysis failed", e)
            Result.failure(e)
        }
    }

    suspend fun judgeChallengeEntry(
        bitmap: Bitmap,
        theme: DailyChallengeTheme,
        toolUsed: String,
        caption: String
    ): Result<ChallengeAiEvaluation> {
        return try {
            val scaled = scaleForAi(bitmap, 768)
            val response = withTimeout(45_000L) {
                challengeJudgeModel.generateContent(
                    content {
                        image(scaled)
                        text(
                            "You are the Daily Photo Challenge judge for Lumina AI. " +
                                "Today's theme is '${theme.title}' (${theme.subtitle}). " +
                                "The user enhanced this photo with '$toolUsed' and caption: '$caption'. " +
                                "Score creativity (65-100), technical quality (65-100), theme match (60-100), " +
                                "provide encouraging feedback, and pick prizeTier: VIP_DAY_PASS if overall avg >= 89, " +
                                "EXTRA_CREDITS if avg >= 76, or NONE otherwise."
                        )
                    }
                )
            }
            val jsonText = response.text ?: throw IllegalStateException("AI Judge returned an empty response.")
            val json = JSONObject(jsonText)
            Result.success(
                ChallengeAiEvaluation(
                    creativityScore = json.optInt("creativityScore", 86).coerceIn(60, 100),
                    qualityScore = json.optInt("qualityScore", 89).coerceIn(60, 100),
                    themeMatchScore = json.optInt("themeMatchScore", 88).coerceIn(60, 100),
                    judgeFeedback = json.optString(
                        "judgeFeedback",
                        "Well-balanced exposure and crisp detail tailored for ${theme.title}."
                    ),
                    prizeTier = ChallengePrizeTier.fromRaw(json.optString("prizeTier", "EXTRA_CREDITS"))
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Challenge judge timed out", e)
            Result.failure(Exception("AI Judge timed out. Please retry submission."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Challenge judge failed", e)
            Result.failure(e)
        }
    }

    suspend fun generativePhotoTransform(
        inputBitmap: Bitmap,
        tool: EnhancementTool,
        customInstruction: String? = null
    ): Result<Bitmap> {
        return try {
            val scaled = scaleForAi(inputBitmap, 1024)
            val prompt = when (tool) {
                EnhancementTool.COLORIZE ->
                    "Colorize this photo with completely natural, historically accurate lifelike skin tones, foliage, and sky. Preserve the exact faces and details."
                EnhancementTool.RESTORE ->
                    "Restore this old or damaged photograph: remove scratches, dust, creases, and blur while keeping the person's authentic facial identity and natural texture."
                EnhancementTool.BG_REMOVE ->
                    customInstruction?.takeIf { it.isNotBlank() }
                        ?: "Keep the main foreground subject sharp and replace the background with a clean, soft luxury studio gradient backdrop with natural edge lighting."
                EnhancementTool.OBJECT_REMOVE ->
                    customInstruction?.takeIf { it.isNotBlank() }
                        ?: "Cleanly remove distracting background clutter or stray objects while seamlessly reconstructing the natural scene."
                EnhancementTool.AI_AVATAR ->
                    customInstruction?.takeIf { it.isNotBlank() }
                        ?: "Transform this portrait into a stylized ultra-clean modern editorial 3D digital art avatar while retaining recognizable facial features."
                EnhancementTool.PORTRAIT_HEADSHOT ->
                    "Transform this photo into a flagship executive studio headshot with soft key lighting, tailored dark neutral blazer styling, natural skin texture, and clean bokeh backdrop."
                else ->
                    customInstruction?.takeIf { it.isNotBlank() }
                        ?: "Enhance this photograph to flagship iPhone ProRAW HDR quality with crisp micro-detail, balanced highlights and shadows, and natural skin tones."
            }

            val response = withTimeout(60_000L) {
                imageEditModel.generateContent(
                    content {
                        image(scaled)
                        text(prompt)
                    }
                )
            }

            val imagePart = response.candidates.firstOrNull()
                ?.content?.parts?.filterIsInstance<ImagePart>()
                ?.firstOrNull()

            val resultBitmap = imagePart?.image
            if (resultBitmap != null) {
                Result.success(resultBitmap)
            } else {
                Result.failure(IllegalStateException("Generative model returned no image output."))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Generative edit timed out", e)
            Result.failure(Exception("Generative studio timed out after 60s. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Generative edit failed", e)
            Result.failure(e)
        }
    }

    private fun scaleForAi(source: Bitmap, maxDim: Int): Bitmap {
        val width = source.width
        val height = source.height
        if (width <= maxDim && height <= maxDim) return source
        val ratio = minOf(maxDim.toFloat() / width, maxDim.toFloat() / height)
        val newW = (width * ratio).toInt().coerceAtLeast(1)
        val newH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, newW, newH, true)
    }
}
