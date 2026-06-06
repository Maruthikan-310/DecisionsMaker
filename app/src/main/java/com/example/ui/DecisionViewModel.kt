package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.ComparisonModel
import com.example.data.DecisionEntity
import com.example.data.DecisionRepository
import com.example.data.ProsConsModel
import com.example.data.SwotModel
import com.example.net.Content
import com.example.net.GeminiClient
import com.example.net.GeminiRequest
import com.example.net.GenerationConfig
import com.example.net.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DecisionViewModel(
    application: Application,
    private val repository: DecisionRepository
) : AndroidViewModel(application) {

    // List of past decisions reactively loaded from Room Database
    val historyList: StateFlow<List<DecisionEntity>> = repository.allDecisions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentDecision = MutableStateFlow<DecisionEntity?>(null)
    val currentDecision: StateFlow<DecisionEntity?> = _currentDecision.asStateFlow()

    // Shared flow to trigger UI events like scrolling to results
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    private val moshi = GeminiClient.getMoshiInstance()

    private val _currentLanguage = MutableStateFlow("EN")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    sealed class UiEvent {
        object ScrollToResult : UiEvent()
    }

    // Check if the user has replaced the default template placeholder API key
    fun isApiKeyPlaceholder(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isBlank() || key == "MY_GEMINI_API_KEY" || key == "GEMINI_API_KEY"
    }

    fun selectDecision(decision: DecisionEntity) {
        _currentDecision.value = decision
    }

    fun deleteDecision(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteById(id)
            if (_currentDecision.value?.id == id) {
                _currentDecision.value = null
            }
        }
    }

    fun generateDecision(question: String, contextInput: String, framework: String) {
        val lang = _currentLanguage.value
        if (question.isBlank()) {
            _errorMessage.value = Translations.get("validation_empty_query", lang)
            return
        }

        if (isApiKeyPlaceholder()) {
            _errorMessage.value = Translations.get("validation_api_required", lang)
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val promptText = buildPrompt(question, contextInput, framework)
                val languageInstruction = when (lang) {
                    "HI" -> "IMPORTANT: You MUST write ALL text values, descriptions, categories, points, and sentences inside the JSON output in Hindi (devanagari script) language (मगर JSON के कीज़ 'keys' जैसे 'verdict', 'point', 'category', 'options', 'criteria', 'optionAValue', 'optionBValue', 'winner', 'strengths', 'weaknesses', 'opportunities', 'threats' को अंग्रेजी में वैसा ही रखें ताकि पार्सिंग सुचारू रूप से कार्य करे, केवल उनके मान Translating details to Hindi). Always explain values and write sentences in Hindi."
                    "TE" -> "IMPORTANT: You MUST write ALL text values, descriptions, categories, points, and sentences inside the JSON output in Telugu language (కానీ JSON కీలు 'keys' 'verdict', 'point', 'category', 'options', 'criteria', 'optionAValue', 'optionBValue', 'winner', 'strengths', 'weaknesses', 'opportunities', 'threats' లను మార్చకుండా ఇంగ్లీషు లోనే ఉంచండి. వాటి విలువలు 'values' మాత్రమే తెలుగులో వ్రాయండి). Always explain values and write sentences in Telugu."
                    else -> "All textual content in the JSON output must be in English."
                }
                val systemInstructionText = "You are 'Decisions Maker', a highly analytic, non-biased decision intelligence expert. Your job is to break analysis paralysis by analyzing user decisions using structured methodologies. Rely heavily on numerical weights and offer sound recommendations, with transparent and concise strategic tie-breaking verdicts. Make sure to adhere strictly to the JSON schema output format. $languageInstruction"

                val request = GeminiRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = promptText)))
                    ),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.2f
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiClient.service.generateContent(
                        apiKey = BuildConfig.GEMINI_API_KEY,
                        request = request
                    )
                }

                val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (resultText != null) {
                    val cleanedText = cleanJson(resultText)
                    // Quick validation of the JSON format by trying to parse it
                    val isValid = validateJson(cleanedText, framework)
                    if (isValid) {
                        val entity = DecisionEntity(
                            question = question,
                            context = contextInput,
                            framework = framework,
                            responseJson = cleanedText,
                            timestamp = System.currentTimeMillis()
                        )
                        val insertedId = withContext(Dispatchers.IO) {
                            repository.insert(entity)
                        }
                        _currentDecision.value = entity.copy(id = insertedId.toInt())
                        _eventFlow.emit(UiEvent.ScrollToResult)
                    } else {
                        _errorMessage.value = "Received an invalid model response format. Please try submitting again."
                    }
                } else {
                    _errorMessage.value = "Failed to generate decision analysis. Model returned empty results."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Network or API Error: ${e.localizedMessage ?: "Unknown error occurred"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun buildPrompt(question: String, context: String, framework: String): String {
        val extraContext = if (context.isNotBlank()) "Additional context: '$context'." else ""
        return when (framework) {
            "PROS_CONS" -> """
                Analyze the following decision query:
                Decision Query: "$question"
                $extraContext
                
                Analyze it using a 'Pros and Cons List' framework. Return your response in JSON format.
                
                You must return a raw JSON object matching this exact schema:
                {
                  "verdict": "Your absolute decision recommendation as a clear, authoritative tie-breaker summary of 2-3 sentences.",
                  "confidenceScore": 85, // Integer 0 to 100 on how confident you are in this specific verdict
                  "probabilityOfSuccess": 75, // Integer 0 to 100 representing probability of the decision's positive outcome
                  "pros": [
                    {
                      "point": "Crucial pro point here",
                      "category": "e.g., Financial, Lifestyle, Career, Short-term, Long-term",
                      "weight": 4 // Importance rating from 1 (low) to 5 (extreme)
                    }
                  ],
                  "cons": [
                    {
                      "point": "Crucial con point here",
                      "category": "e.g., Financial, Lifestyle, Career, Short-term, Long-term",
                      "weight": 3 // Cost/risk rating from 1 (low) to 5 (extreme)
                    }
                  ]
                }
            """.trimIndent()

            "COMPARISON" -> """
                Analyze the following decision query:
                Decision Query: "$question"
                $extraContext
                
                Evaluate this as a comparison between Option A and Option B (derive two distinct elegant names representing the options under comparison from the query). Compare them on 4 to 6 strategic criteria.
                
                You must return a raw JSON object matching this exact schema:
                {
                  "verdict": "Your clear, authoritative recommendation or strategy to break the tie in 2-3 sentences.",
                  "confidenceScore": 90, // Integer 0-100 representing confidence
                  "options": ["Determined Option A Label", "Determined Option B Label"], // Be descriptive but under 25 chars
                  "criteria": [
                    {
                      "criterion": "Name of dimension (e.g. Upfront Cost, Career Growth, Daily Effort)",
                      "optionAValue": "Value/Details of option A for this dimension",
                      "optionBValue": "Value/Details of option B for this dimension",
                      "winner": "Must be exactly matching one of your options array strings (defined in the 'options' field), or 'Tie'"
                    }
                  ]
                }
            """.trimIndent()

            "SWOT" -> """
                Analyze the following decision/project:
                Decision Query: "$question"
                $extraContext
                
                Analyze it using a comprehensive SWOT Analysis framework (Strengths, Weaknesses, Opportunities, and Threats) to evaluate taking this specific action or path.
                
                You must return a raw JSON object matching this exact schema:
                {
                  "verdict": "Your tactical, authoritative recommendations on how to proceed or avoid in 2-3 sentences.",
                  "confidenceScore": 80, // Integer 0-100 indicating probability of successful path execution
                  "strengths": ["Detail of strength 1", "Detail of strength 2", "Detail of strength 3"],
                  "weaknesses": ["Detail of weakness 1", "Detail of weakness 2", "Detail of weakness 3"],
                  "opportunities": ["Detail of opportunity 1", "Detail of opportunity 2"],
                  "threats": ["Detail of threat 1", "Detail of threat 2"]
                }
            """.trimIndent()

            else -> ""
        }
    }

    fun cleanJson(rawJson: String): String {
        var str = rawJson.trim()
        
        // Remove markdown formatting if present
        if (str.startsWith("```")) {
            str = str.replaceFirst(Regex("^```[a-zA-Z]*\\s*"), "")
            if (str.endsWith("```")) {
                str = str.substring(0, str.length - 3).trim()
            }
        } else {
            // Sometime there may be raw text before the first '{' and after the last '}'
            val firstBrace = str.indexOf('{')
            val lastBrace = str.lastIndexOf('}')
            if (firstBrace in 0 until lastBrace) {
                str = str.substring(firstBrace, lastBrace + 1)
            }
        }
        return str.trim()
    }

    private fun validateJson(json: String, framework: String): Boolean {
        val cleaned = cleanJson(json)
        return try {
            when (framework) {
                "PROS_CONS" -> {
                    val adapter = moshi.adapter(ProsConsModel::class.java)
                    adapter.fromJson(cleaned) != null
                }
                "COMPARISON" -> {
                    val adapter = moshi.adapter(ComparisonModel::class.java)
                    adapter.fromJson(cleaned) != null
                }
                "SWOT" -> {
                    val adapter = moshi.adapter(SwotModel::class.java)
                    adapter.fromJson(cleaned) != null
                }
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Public exposure of model parsing with safety cleanJson handling
    fun getParsedProsCons(json: String): ProsConsModel? {
        val cleaned = cleanJson(json)
        return try {
            moshi.adapter(ProsConsModel::class.java).fromJson(cleaned)
        } catch (e: Exception) {
            null
        }
    }

    fun getParsedComparison(json: String): ComparisonModel? {
        val cleaned = cleanJson(json)
        return try {
            moshi.adapter(ComparisonModel::class.java).fromJson(cleaned)
        } catch (e: Exception) {
            null
        }
    }

    fun getParsedSwot(json: String): SwotModel? {
        val cleaned = cleanJson(json)
        return try {
            moshi.adapter(SwotModel::class.java).fromJson(cleaned)
        } catch (e: Exception) {
            null
        }
    }
}

class DecisionViewModelFactory(
    private val application: Application,
    private val repository: DecisionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DecisionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DecisionViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
