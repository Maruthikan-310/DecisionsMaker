package com.example.ui

object Translations {
    val languages = listOf(
        LanguageOption("EN", "English", "🇺🇸"),
        LanguageOption("HI", "हिन्दी", "🇮🇳"),
        LanguageOption("TE", "తెలుగు", "🇮🇳")
    )

    fun get(key: String, lang: String): String {
        val map = translationMap[key] ?: return key
        return map[lang] ?: map["EN"] ?: key
    }

    private val translationMap = mapOf(
        "app_title" to mapOf(
            "EN" to "Decisions Maker",
            "HI" to "डिसिशन्स मेकर",
            "TE" to "డెసిషన్స్ మేకర్"
        ),
        "app_subtitle" to mapOf(
            "EN" to "AI Decision Intelligence Platform",
            "HI" to "एआई निर्णय बुद्धिमत्ता मंच",
            "TE" to "AI నిర్ణయ మేధస్సు ప్లాట్‌ఫారమ్"
        ),
        "api_warning_title" to mapOf(
            "EN" to "API Key Configuration Required",
            "HI" to "एपीआई कुंजी कॉन्फ़िगरेशन आवश्यक है",
            "TE" to "API కీ కాన్ఫిగరేషన్ అవసరం"
        ),
        "api_warning_desc" to mapOf(
            "EN" to "This app relies on the Gemini AI API. Please click the Secrets panel in AI Studio and configure GEMINI_API_KEY with your custom API key to enable instant intelligence breaks.",
            "HI" to "यह ऐप जेमिनी एआई एपीआई पर निर्भर है। टाईब्रेकर को सक्रिय करने के लिए कृपया एआई स्टूडियो में अपने एपीआई कुंजी को कॉन्फ़िगर करें।",
            "TE" to "ఈ యాప్ జెమిని AI API పై ఆధారపడి ఉంటుంది. క్షణాల్లో టైలను బ్రేక్ చేయడానికి దయచేసి AI స్టూడియోలో మీ API కీని కాన్ఫిగర్ చేయండి."
        ),
        "describe_dilemma" to mapOf(
            "EN" to "Describe Your Dilemma",
            "HI" to "अपनी दुविधा का वर्णन करें",
            "TE" to "మీ సందిగ్ధతను వివరించండి"
        ),
        "question_label" to mapOf(
            "EN" to "What decision are you trying to make?",
            "HI" to "आप क्या निर्णय लेना चाहते हैं?",
            "TE" to "మీరు ఎలాంటి నిర్ణయం తీసుకోబోతున్నారు?"
        ),
        "question_placeholder" to mapOf(
            "EN" to "e.g. Should I learn Kotlin or Flutter?",
            "HI" to "जैसे: क्या मुझे कोटलिन या फ्लटर सीखना चाहिए?",
            "TE" to "ఉదా: నేను కోట్లిన్ లేదా ఫ్లట్టర్ నేర్చుకోవాలా?"
        ),
        "context_label" to mapOf(
            "EN" to "Additional Context / Preferences (Optional)",
            "HI" to "अतिरिक्त संदर्भ / प्राथमिकताएं (वैकल्पिक)",
            "TE" to "అదనపు సందర్భం / ప్రాధాన్యతలు (ఐచ్ఛికం)"
        ),
        "context_placeholder" to mapOf(
            "EN" to "e.g. I already know Java/Android; looking to get a job quickly.",
            "HI" to "जैसे: मुझे पहले से ही जावा/एंड्रॉइड पता है; जल्दी नौकरी पाना चाहता हूँ।",
            "TE" to "ఉదా: నాకు ఇప్పటికే జావా/ఆండ్రాయిడ్ తెలుసు; త్వరగా ఉద్యోగం పొందాలని చూస్తున్నాను."
        ),
        "choose_framework" to mapOf(
            "EN" to "Choose Evaluation Framework",
            "HI" to "मूल्यांकन ढांचा चुनें",
            "TE" to "మూల్యాంకన విధానం ఎంచుకోండి"
        ),
        "pros_cons" to mapOf(
            "EN" to "Pros & Cons",
            "HI" to "पक्ष और विपक्ष",
            "TE" to "లాభాలు & నష్టాలు"
        ),
        "comparison" to mapOf(
            "EN" to "Comparison Table",
            "HI" to "तुलना तालिका",
            "TE" to "పోలిక పట్టిక"
        ),
        "swot" to mapOf(
            "EN" to "SWOT Analysis",
            "HI" to "स्वॉट (SWOT) विश्लेषण",
            "TE" to "SWOT విశ్లేషణ"
        ),
        "analyze_btn" to mapOf(
            "EN" to "Analyze & Break Tie",
            "HI" to "विश्लेषण करें और टाई तोड़ें",
            "TE" to "విశ్లేషించి టైబ్రేక్ చేయండి"
        ),
        "processing" to mapOf(
            "EN" to "Processing...",
            "HI" to "प्रसंस्करण हो रहा है...",
            "TE" to "ప్రాసెస్ అవుతోంది..."
        ),
        "ready_title" to mapOf(
            "EN" to "Ready to break a tie?",
            "HI" to "क्या आप टाई तोड़ने के लिए तैयार हैं?",
            "TE" to "టై బ్రేక్ చేయడానికి సిద్ధంగా ఉన్నారా?"
        ),
        "ready_desc" to mapOf(
            "EN" to "Enter your dilemma above, pick a framework, and watch AI analyze pros, cons, and alternatives securely.",
            "HI" to "ऊपर अपनी दुविधा दर्ज करें, एक ढांचा चुनें, और सुरक्षित रूप से एआई को पक्ष, विपक्ष और विकल्पों का विश्लेषण करते हुए देखें।",
            "TE" to "మీ సందిగ్ధతను పైన నమోదు చేయండి, ఒక విధానాన్ని ఎంచుకోండి మరియు AI మీ లాభాలు, నష్టాలు మరియు ప్రత్యామ్నాయాలను సురక్షితంగా విశ్లేషించడాన్ని చూడండి."
        ),
        "tie_broken" to mapOf(
            "EN" to "Tie Broken ✅",
            "HI" to "टाई टूट गई ✅",
            "TE" to "టై బ్రేక్ చేయబడింది ✅"
        ),
        "context_prefix" to mapOf(
            "EN" to "Context",
            "HI" to "संदर्भ",
            "TE" to "సందర్భం"
        ),
        "parsed_error" to mapOf(
            "EN" to "Oops! We stored the recommendation, but had difficulty formatting the details grid beautifully. Please check back soon or try re-running with a refined prompt.",
            "HI" to "ओह! हमने सिफारिश तो सेव कर ली है, लेकिन विवरण ग्रिड को सही ढंग से व्यवस्थित करने में असमर्थ रहे। कृपया फिर से प्रयास करें।",
            "TE" to "అయ్యో! మేము మీ విశ్లేషణను సేవ్ చేసాము, కాని వివరాల గ్రిడ్‌ను సరిగ్గా చూపించలేకపోయాము. దయచేసి మళ్ళీ ప్రయత్నించండి."
        ),
        "confidence_label" to mapOf(
            "EN" to "Confidence",
            "HI" to "विश्वास स्कोर",
            "TE" to "నమ్మకం"
        ),
        "success_prob_label" to mapOf(
            "EN" to "Prob. of Success",
            "HI" to "सफलता की संभावना",
            "TE" to "విజయ సంభావ్యత"
        ),
        "ai_certainty_label" to mapOf(
            "EN" to "AI Certainty Score",
            "HI" to "निश्चितता स्कोर",
            "TE" to "AI ఖచ్చితత్వ స్కోరు"
        ),
        "feasibility_label" to mapOf(
            "EN" to "Feasibility Index",
            "HI" to "व्यवहार्यता सूचकांक",
            "TE" to "సాధ్యత సూచిక"
        ),
        "verdict_title" to mapOf(
            "EN" to "🤖 AI Verdict Summary",
            "HI" to "🤖 एआई निर्णय सारांश",
            "TE" to "🤖 AI తీర్పు సారాంశం"
        ),
        "structured_point_title" to mapOf(
            "EN" to "Structured Point Analysis",
            "HI" to "संरचित बिंदु विश्लेषण",
            "TE" to "నిర్మాణాత్మక పాయింట్ విశ్లేషణ"
        ),
        "items_count" to mapOf(
            "EN" to "items",
            "HI" to "बिंदु",
            "TE" to "అంశాలు"
        ),
        "pros_group" to mapOf(
            "EN" to "💚 Pros",
            "HI" to "💚 पक्ष (Pros)",
            "TE" to "💚 లాభాలు (Pros)"
        ),
        "cons_group" to mapOf(
            "EN" to "❤️ Cons",
            "HI" to "❤️ विपक्ष (Cons)",
            "TE" to "❤️ నష్టాలు (Cons)"
        ),
        "no_pros" to mapOf(
            "EN" to "No pros identified.",
            "HI" to "कोई पक्ष नहीं मिला।",
            "TE" to "ఎలాంటి లాభాలు లేవు."
        ),
        "no_cons" to mapOf(
            "EN" to "No cons identified.",
            "HI" to "कोई विपक्ष नहीं मिला।",
            "TE" to "ఎలాంటి నష్టాలు లేవు."
        ),
        "dimension_grid_title" to mapOf(
            "EN" to "Dimension Grid Analysis",
            "HI" to "आयाम ग्रिड विश्लेषण",
            "TE" to "డైమెన్షన్ గ్రిడ్ విశ్లేషణ"
        ),
        "draw_badge" to mapOf(
            "EN" to "🤝 Draw",
            "HI" to "🤝 सम्मान/ड्रा",
            "TE" to "🤝 డ్రా"
        ),
        "strategic_quadrant_title" to mapOf(
            "EN" to "Strategic Quadrant Matrix",
            "HI" to "रणनीतिक चतुर्थांश मैट्रिक्स",
            "TE" to "వ్యూహాత్మక చతుర్భుజ మ్యాట్రిక్స్"
        ),
        "swot_strengths" to mapOf(
            "EN" to "💪 Strengths",
            "HI" to "💪 ताकत (Strengths)",
            "TE" to "💪 బలాలు (Strengths)"
        ),
        "swot_weaknesses" to mapOf(
            "EN" to "⚠️ Weaknesses",
            "HI" to "⚠️ कमजोरियां (Weaknesses)",
            "TE" to "⚠️ బలహీనతలు (Weaknesses)"
        ),
        "swot_opportunities" to mapOf(
            "EN" to "🚀 Opportunities",
            "HI" to "🚀 अवसर (Opportunities)",
            "TE" to "🚀 అవకాశాలు (Opportunities)"
        ),
        "swot_threats" to mapOf(
            "EN" to "⚡ Threats",
            "HI" to "⚡ खतरे (Threats)",
            "TE" to "⚡ ముప్పులు (Threats)"
        ),
        "none_found" to mapOf(
            "EN" to "None identified.",
            "HI" to "कोई पहचान नहीं की गई।",
            "TE" to "ఏమీ గుర్తించబడలేదు."
        ),
        "past_tiebreakers_title" to mapOf(
            "EN" to "Past Tiebreakers",
            "HI" to "पुराने टाईब्रेकर्स",
            "TE" to "గత టైబ్రేకర్లు"
        ),
        "past_tiebreakers_format" to mapOf(
            "EN" to "Past Tiebreakers (%d)",
            "HI" to "पुराने टाईब्रेकर्स (%d)",
            "TE" to "గత టైబ్రేకర్లు (%d)"
        ),
        "comparison_badge" to mapOf(
            "EN" to "Comparison",
            "HI" to "तुलना",
            "TE" to "పోలిక"
        ),
        "swot_badge" to mapOf(
            "EN" to "SWOT",
            "HI" to "स्वॉट",
            "TE" to "SWOT"
        ),
        "proscons_badge" to mapOf(
            "EN" to "Pros & Cons",
            "HI" to "पक्ष-विपक्ष",
            "TE" to "లాభాలు/నష్టాలు"
        ),
        "validation_empty_query" to mapOf(
            "EN" to "Please enter a decision you need to make.",
            "HI" to "कृपया वह निर्णय दर्ज करें जिसे आप लेना चाहते हैं।",
            "TE" to "దయచేసి మీరు తీసుకోవలసిన నిర్ణయాన్ని నమోదు చేయండి."
        ),
        "validation_api_required" to mapOf(
            "EN" to "Custom API key required. Please configure GEMINI_API_KEY in the AI Studio Secrets panel.",
            "HI" to "कस्टम एपीआई कुंजी की आवश्यकता है। कृपया एआई स्टूडियो सीक्रेट्स पैनल में GEMINI_API_KEY कॉन्फ़िगर करें।",
            "TE" to "అనుకూల API కీ అవసరం. దయచేసి AI స్టూడియో సీక్రెట్స్ ప్యానెల్‌లో GEMINI_API_KEYని కాన్ఫిగర్ చేయండి."
        ),
        "success_marker" to mapOf(
            "EN" to "Tie Broken ✅",
            "HI" to "टाई टूट गई ✅",
            "TE" to "టై బ్రేక్ చేయబడింది ✅"
        ),
        "voice_input_descr" to mapOf(
            "EN" to "Input using voice",
            "HI" to "आवाज से इनपुट करें",
            "TE" to "వాయిస్ ద్వారా ఇన్‌పుట్ చేయండి"
        ),
        "speak_btn" to mapOf(
            "EN" to "Listen to Verdict 🔊",
            "HI" to "निर्णय सुनें 🔊",
            "TE" to "తీర్పు వినండి 🔊"
        ),
        "stop_speak_btn" to mapOf(
            "EN" to "Stop Listening 🔇",
            "HI" to "सुनना बंद करें 🔇",
            "TE" to "వినడం ఆపివేయి 🔇"
        )
    )
}

data class LanguageOption(
    val code: String,
    val name: String,
    val flag: String
)
