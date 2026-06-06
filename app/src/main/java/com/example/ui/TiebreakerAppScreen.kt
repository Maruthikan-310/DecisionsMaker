package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ComparisonModel
import com.example.data.DecisionEntity
import com.example.data.ProsConsModel
import com.example.data.SwotModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TiebreakerAppScreen(viewModel: DecisionViewModel) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val historyList by viewModel.historyList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentDecision by viewModel.currentDecision.collectAsState()

    // Retrieve selected dynamic translation language
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    var questionInput by remember { mutableStateOf("") }
    var contextInput by remember { mutableStateOf("") }
    var selectedFramework by remember { mutableStateOf("PROS_CONS") } // "PROS_CONS", "COMPARISON", "SWOT"

    val context = LocalContext.current

    val speechLauncherQuestion = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                questionInput = if (questionInput.isBlank()) spokenText else "$questionInput $spokenText"
            }
        }
    }

    val speechLauncherContext = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                contextInput = if (contextInput.isBlank()) spokenText else "$contextInput $spokenText"
            }
        }
    }

    val triggerSpeechInput = { isQuestionField: Boolean ->
        val localeTag = when (currentLanguage) {
            "HI" -> "hi-IN"
            "TE" -> "te-IN"
            else -> "en-US"
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf(localeTag))
            putExtra(RecognizerIntent.EXTRA_PROMPT, when (currentLanguage) {
                "HI" -> "बोलना शुरू करें (Speak in Hindi)..."
                "TE" -> "మాట్లాడటం ప్రారంభించండి (Speak in Telugu)..."
                else -> "Speak now (English)..."
            })
        }
        try {
            if (isQuestionField) {
                speechLauncherQuestion.launch(intent)
            } else {
                speechLauncherContext.launch(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Speech recognition is not available or disabled on this device",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    var isSpeaking by remember { mutableStateOf(false) }
    var currentSpokenText by remember { mutableStateOf("") }

    val tts = remember {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Done initializing
            }
        }
        ttsInstance
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    val speakText: (String) -> Unit = { text ->
        val localeTag = when (currentLanguage) {
            "HI" -> Locale.forLanguageTag("hi-IN")
            "TE" -> Locale.forLanguageTag("te-IN")
            else -> Locale.ENGLISH
        }
        tts?.language = localeTag
        tts?.stop()

        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
            }
            override fun onDone(utteranceId: String?) {
                isSpeaking = false
            }
            override fun onError(utteranceId: String?) {
                isSpeaking = false
            }
        })

        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "verdict_speech_utterance")
        currentSpokenText = text
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "verdict_speech_utterance")
    }

    val stopSpeaking: () -> Unit = {
        tts?.stop()
        isSpeaking = false
        currentSpokenText = ""
    }

    LaunchedEffect(currentLanguage, currentDecision) {
        stopSpeaking()
    }

    // Listen for model generation success event to auto-scroll to the decision analysis section
    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collectLatest { event ->
            if (event is DecisionViewModel.UiEvent.ScrollToResult) {
                focusManager.clearFocus()
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // A. Localized Language Selector (Live toggle between EN, HI, TE)
            LanguageSelectorRow(
                currentLanguage = currentLanguage,
                onLanguageSelected = { viewModel.setLanguage(it) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 1. App Header
            HeaderBlock(currentLanguage = currentLanguage)

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Api Key Warning (if placeholder)
            if (viewModel.isApiKeyPlaceholder()) {
                ApiKeyWarningBlock(currentLanguage = currentLanguage)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 3. User decision inputs Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
                ),
                border = BoxBorder()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = Translations.get("describe_dilemma", currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = questionInput,
                        onValueChange = { questionInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("question_input"),
                        label = { Text(Translations.get("question_label", currentLanguage)) },
                        placeholder = { Text(Translations.get("question_placeholder", currentLanguage)) },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = transparentTextFieldColors(),
                        trailingIcon = {
                            IconButton(
                                onClick = { triggerSpeechInput(true) },
                                modifier = Modifier.testTag("mic_question_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = Translations.get("voice_input_descr", currentLanguage),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = contextInput,
                        onValueChange = { contextInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("context_input"),
                        label = { Text(Translations.get("context_label", currentLanguage)) },
                        placeholder = { Text(Translations.get("context_placeholder", currentLanguage)) },
                        singleLine = false,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = transparentTextFieldColors(),
                        trailingIcon = {
                            IconButton(
                                onClick = { triggerSpeechInput(false) },
                                modifier = Modifier.testTag("mic_context_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = Translations.get("voice_input_descr", currentLanguage),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    // Framework selection Chips block
                    Column {
                        Text(
                            text = Translations.get("choose_framework", currentLanguage),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FrameworkChip(
                                label = Translations.get("pros_cons", currentLanguage),
                                selected = selectedFramework == "PROS_CONS",
                                onClick = { selectedFramework = "PROS_CONS" },
                                modifier = Modifier.weight(1f).testTag("pros_cons_chip")
                            )
                            FrameworkChip(
                                label = Translations.get("comparison", currentLanguage),
                                selected = selectedFramework == "COMPARISON",
                                onClick = { selectedFramework = "COMPARISON" },
                                modifier = Modifier.weight(1f).testTag("comparison_chip")
                            )
                            FrameworkChip(
                                label = Translations.get("swot", currentLanguage),
                                selected = selectedFramework == "SWOT",
                                onClick = { selectedFramework = "SWOT" },
                                modifier = Modifier.weight(1f).testTag("swot_chip")
                            )
                        }
                    }

                    errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.generateDecision(
                                questionInput,
                                contextInput,
                                selectedFramework
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("tiebreaker_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && questionInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = Translations.get("processing", currentLanguage),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            Text(
                                text = Translations.get("analyze_btn", currentLanguage),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Current Verdict Display Section
            currentDecision?.let { decision ->
                DecisionViewer(
                    decision = decision,
                    viewModel = viewModel,
                    currentLanguage = currentLanguage,
                    isSpeaking = isSpeaking,
                    onSpeak = speakText,
                    onStop = stopSpeaking
                )
                Spacer(modifier = Modifier.height(24.dp))
            } ?: run {
                if (!isLoading) {
                    EmptyResultBlock(currentLanguage = currentLanguage)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 5. Saved History Exploration List
            if (historyList.isNotEmpty()) {
                HistorySection(
                    history = historyList,
                    selectedId = currentDecision?.id ?: -1,
                    currentLanguage = currentLanguage,
                    onSelect = { selected ->
                        viewModel.selectDecision(selected)
                        // populate fields so the user can edit or re-analyze if they desire
                        questionInput = selected.question
                        contextInput = selected.context
                        selectedFramework = selected.framework
                    },
                    onDelete = { id -> viewModel.deleteDecision(id) }
                )
            }
        }
    }
}

@Composable
fun LanguageSelectorRow(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Translations.languages.forEach { language ->
            val isSelected = language.code == currentLanguage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
                    )
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
                        RoundedCornerShape(30.dp)
                    )
                    .clickable { onLanguageSelected(language.code) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = language.flag, fontSize = 14.sp)
                    Text(
                        text = language.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BoxBorder(): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    )
}

@Composable
fun HeaderBlock(currentLanguage: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚖️",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = Translations.get("app_title", currentLanguage),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = Translations.get("app_subtitle", currentLanguage),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ApiKeyWarningBlock(currentLanguage: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.error)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Translations.get("api_warning_title", currentLanguage),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = Translations.get("api_warning_desc", currentLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun EmptyResultBlock(currentLanguage: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "💡",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 44.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = Translations.get("ready_title", currentLanguage),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Text(
            text = Translations.get("ready_desc", currentLanguage),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun FrameworkChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 200)
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200)
    )
    val borderColor = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -------------------------------------------------------------
// Core Component: Decision Viewer
// -------------------------------------------------------------
@Composable
fun DecisionViewer(
    decision: DecisionEntity,
    viewModel: DecisionViewModel,
    currentLanguage: String,
    isSpeaking: Boolean,
    onSpeak: (String) -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("result_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header showing Framework and active question
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrameTag(framework = decision.framework, currentLanguage = currentLanguage)
                Text(
                    text = Translations.get("success_marker", currentLanguage),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = decision.question,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (decision.context.isNotBlank()) {
                Text(
                    text = "${Translations.get("context_prefix", currentLanguage)}: ${decision.context}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.61f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Dynamic view based on framework
            when (decision.framework) {
                "PROS_CONS" -> {
                    val parsed = viewModel.getParsedProsCons(decision.responseJson)
                    if (parsed != null) {
                        ProsConsResultLayout(
                            parsed,
                            currentLanguage = currentLanguage,
                            isSpeaking = isSpeaking,
                            onSpeak = onSpeak,
                            onStop = onStop
                        )
                    } else {
                        ParsingErrorBlock(currentLanguage = currentLanguage)
                    }
                }
                "COMPARISON" -> {
                    val parsed = viewModel.getParsedComparison(decision.responseJson)
                    if (parsed != null) {
                        ComparisonResultLayout(
                            parsed,
                            currentLanguage = currentLanguage,
                            isSpeaking = isSpeaking,
                            onSpeak = onSpeak,
                            onStop = onStop
                        )
                    } else {
                        ParsingErrorBlock(currentLanguage = currentLanguage)
                    }
                }
                "SWOT" -> {
                    val parsed = viewModel.getParsedSwot(decision.responseJson)
                    if (parsed != null) {
                        SwotResultLayout(
                            parsed,
                            currentLanguage = currentLanguage,
                            isSpeaking = isSpeaking,
                            onSpeak = onSpeak,
                            onStop = onStop
                        )
                    } else {
                        ParsingErrorBlock(currentLanguage = currentLanguage)
                    }
                }
            }
        }
    }
}

@Composable
fun FrameTag(framework: String, currentLanguage: String) {
    val text = when (framework) {
        "PROS_CONS" -> Translations.get("pros_cons", currentLanguage)
        "COMPARISON" -> Translations.get("comparison", currentLanguage)
        "SWOT" -> Translations.get("swot", currentLanguage)
        else -> "Framework"
    }
    val containerBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    val txtColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerBg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = txtColor
        )
    }
}

@Composable
fun ParsingErrorBlock(currentLanguage: String) {
    Text(
        text = Translations.get("parsed_error", currentLanguage),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

// -------------------------------------------------------------
// Framework 1: Pros vs Cons Render
// -------------------------------------------------------------
@Composable
fun ProsConsResultLayout(
    model: ProsConsModel,
    currentLanguage: String,
    isSpeaking: Boolean,
    onSpeak: (String) -> Unit,
    onStop: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Stats/Scores Block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreMeter(title = Translations.get("confidence_label", currentLanguage), score = model.confidenceScore, activeTag = "confidence")
            ScoreMeter(title = Translations.get("success_prob_label", currentLanguage), score = model.probabilityOfSuccess, activeTag = "success")
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Verdict Card
        VerdictCard(
            verdict = model.verdict,
            currentLanguage = currentLanguage,
            isSpeaking = isSpeaking,
            onSpeak = onSpeak,
            onStop = onStop
        )

        // Pros versus Cons lists
        Text(
            text = Translations.get("structured_point_title", currentLanguage),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Pros Group
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Translations.get("pros_group", currentLanguage),
                        fontWeight = FontWeight.Bold, 
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${model.pros.size} ${Translations.get("items_count", currentLanguage)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                model.pros.forEach { item ->
                    ProConItemRow(item, isPro = true)
                }
                if (model.pros.isEmpty()) {
                    Text(
                        text = Translations.get("no_pros", currentLanguage), 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = Color.Gray
                    )
                }
            }

            // Cons Group
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Translations.get("cons_group", currentLanguage),
                        fontWeight = FontWeight.Bold, 
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${model.cons.size} ${Translations.get("items_count", currentLanguage)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                model.cons.forEach { item ->
                    ProConItemRow(item, isPro = false)
                }
                if (model.cons.isEmpty()) {
                    Text(
                        text = Translations.get("no_cons", currentLanguage),
                        style = MaterialTheme.typography.bodyMedium, 
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ProConItemRow(item: com.example.data.ProConItem, isPro: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isPro) Color(0xFF1B3D23).copy(alpha = 0.1f) else Color(0xFF4C1D24).copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (isPro) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFC62828).copy(alpha = 0.15f),
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isPro) Icons.Default.Add else Icons.Default.Close,
            contentDescription = if (isPro) "Plus" else "Minus",
            tint = if (isPro) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.point,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (item.category.isNotBlank()) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Weight chips (representing importance/stress 1-5 stars)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    if (isPro) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFF44336).copy(alpha = 0.15f),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Weight rating",
                tint = if (isPro) Color(0xFFFFB300) else Color(0xFFFF9800),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = item.weight.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPro) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}

// -------------------------------------------------------------
// Framework 2: Comparison Render
// -------------------------------------------------------------
@Composable
fun ComparisonResultLayout(
    model: ComparisonModel,
    currentLanguage: String,
    isSpeaking: Boolean,
    onSpeak: (String) -> Unit,
    onStop: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Confidence
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ScoreMeter(title = Translations.get("ai_certainty_label", currentLanguage), score = model.confidenceScore, activeTag = "certainty")
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        VerdictCard(
            verdict = model.verdict,
            currentLanguage = currentLanguage,
            isSpeaking = isSpeaking,
            onSpeak = onSpeak,
            onStop = onStop
        )

        // Options details
        val options = model.options
        if (options.size >= 2) {
            Text(
                text = Translations.get("dimension_grid_title", currentLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                model.criteria.forEach { row ->
                    ComparisonCard(row = row, optionALabel = options[0], optionBLabel = options[1], currentLanguage = currentLanguage)
                }
            }
        }
    }
}

@Composable
fun ComparisonCard(row: com.example.data.ComparisonRow, optionALabel: String, optionBLabel: String, currentLanguage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BoxBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Heading criterion name & winner tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.criterion,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Winner Tag
                val cleanWinner = row.winner.trim().lowercase()
                val cleanA = optionALabel.trim().lowercase()
                val cleanB = optionBLabel.trim().lowercase()

                val isWinnerA = cleanWinner == cleanA || (cleanWinner.contains(cleanA) && !cleanWinner.contains(cleanB))
                val isWinnerB = cleanWinner == cleanB || (cleanWinner.contains(cleanB) && !cleanWinner.contains(cleanA))

                val winnerLabel = when {
                    isWinnerA -> "🏆 $optionALabel"
                    isWinnerB -> "🏆 $optionBLabel"
                    else -> Translations.get("draw_badge", currentLanguage)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isWinnerA || isWinnerB) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = winnerLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isWinnerA || isWinnerB) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Option details columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = optionALabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = row.optionAValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .align(Alignment.CenterVertically)
                )

                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = optionBLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = row.optionBValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Framework 3: SWOT Analysis Render
// -------------------------------------------------------------
@Composable
fun SwotResultLayout(
    model: SwotModel,
    currentLanguage: String,
    isSpeaking: Boolean,
    onSpeak: (String) -> Unit,
    onStop: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Stats Meter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ScoreMeter(title = Translations.get("feasibility_label", currentLanguage), score = model.confidenceScore, activeTag = "feasibility")
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        VerdictCard(
            verdict = model.verdict,
            currentLanguage = currentLanguage,
            isSpeaking = isSpeaking,
            onSpeak = onSpeak,
            onStop = onStop
        )

        // SWOT Grid (Strengths, Weaknesses, Opportunities, Threats)
        Text(
            text = Translations.get("strategic_quadrant_title", currentLanguage),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 2x2 Grid Column blocks
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SwotQuadrantCard(
                title = Translations.get("swot_strengths", currentLanguage),
                items = model.strengths,
                headerColor = Color(0xFF2E7D32),
                bgColor = Color(0xFF2E7D32).copy(alpha = 0.05f),
                currentLanguage = currentLanguage
            )

            SwotQuadrantCard(
                title = Translations.get("swot_weaknesses", currentLanguage),
                items = model.weaknesses,
                headerColor = Color(0xFFC62828),
                bgColor = Color(0xFFC62828).copy(alpha = 0.05f),
                currentLanguage = currentLanguage
            )

            SwotQuadrantCard(
                title = Translations.get("swot_opportunities", currentLanguage),
                items = model.opportunities,
                headerColor = Color(0xFF00796B),
                bgColor = Color(0xFF00796B).copy(alpha = 0.05f),
                currentLanguage = currentLanguage
            )

            SwotQuadrantCard(
                title = Translations.get("swot_threats", currentLanguage),
                items = model.threats,
                headerColor = Color(0xFFEF6C00),
                bgColor = Color(0xFFEF6C00).copy(alpha = 0.05f),
                currentLanguage = currentLanguage
            )
        }
    }
}

@Composable
fun SwotQuadrantCard(title: String, items: List<String>, headerColor: Color, bgColor: Color, currentLanguage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, headerColor.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = headerColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (items.isEmpty()) {
                Text(
                    text = Translations.get("none_found", currentLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            } else {
                items.forEach { bullet ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = headerColor,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = bullet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Scoring Component: Visual Score Ring Canvas
// -------------------------------------------------------------
@Composable
fun ScoreMeter(title: String, score: Int, activeTag: String) {
    // Dynamic Sweep animation on load
    var animTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(score) {
        animTriggered = true
    }
    val targetSweep = (score.toFloat() / 100f) * 360f
    val animatedSweep by animateFloatAsState(
        targetValue = if (animTriggered) targetSweep else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(76.dp)
        ) {
            Canvas(modifier = Modifier.size(76.dp)) {
                // Background Track ring
                drawCircle(
                    color = trackColor,
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )

                // Foreground Animated sweeping value Arc
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Text Percentage counter
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun VerdictCard(
    verdict: String,
    currentLanguage: String,
    isSpeaking: Boolean,
    onSpeak: (String) -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = Translations.get("verdict_title", currentLanguage),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                val buttonText = if (isSpeaking) {
                    Translations.get("stop_speak_btn", currentLanguage)
                } else {
                    Translations.get("speak_btn", currentLanguage)
                }

                Button(
                    onClick = {
                        if (isSpeaking) {
                            onStop()
                        } else {
                            onSpeak(verdict)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpeaking) MaterialTheme.colorScheme.errorContainer
                                         else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isSpeaking) MaterialTheme.colorScheme.onErrorContainer
                                       else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(30.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("verdict_tts_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = buttonText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = verdict,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }
    }
}

// -------------------------------------------------------------
// Component: Saved Decisions History Roll
// -------------------------------------------------------------
@Composable
fun HistorySection(
    history: List<DecisionEntity>,
    selectedId: Int,
    currentLanguage: String,
    onSelect: (DecisionEntity) -> Unit,
    onDelete: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📜",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 4.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${Translations.get("past_tiebreakers_title", currentLanguage)} (${history.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            history.forEach { decision ->
                val isSelected = decision.id == selectedId
                val borderStroke = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.outlineVariantColor().copy(alpha = 0.4f))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(decision) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    ),
                    border = borderStroke
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MiniTag(framework = decision.framework, currentLanguage = currentLanguage)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = decision.question,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onDelete(decision.id) },
                            modifier = Modifier.testTag("delete_history_${decision.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniTag(framework: String, currentLanguage: String) {
    val text = when (framework) {
        "PROS_CONS" -> Translations.get("proscons_badge", currentLanguage)
        "COMPARISON" -> Translations.get("comparison_badge", currentLanguage)
        "SWOT" -> Translations.get("swot_badge", currentLanguage)
        else -> ""
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

// Outline color fallback helper
@Composable
fun MaterialTheme.outlineVariantColor(): Color {
    return MaterialTheme.colorScheme.outlineVariant
}

// Helper textfield style
@Composable
fun transparentTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.40f),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)
