package com.example.smartairmonitoring.ui.theme

import androidx.compose.ui.graphics.Color

// Background Colors
val BackgroundDeepNavy = Color(0xFF0B1220)
val BackgroundSecondary = Color(0xFF111827)
val BackgroundElevated = Color(0xFF1F2937)

// Text Colors
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFD1D5DB)
val TextHint = Color(0xFF9CA3AF)
val TextDisabled = Color(0xFF6B7280)

// AQI / Status Colors
val AQIGood = Color(0xFF22C55E)
val AQIModerate = Color(0xFFEAB308)
val AQIUnhealthySensitive = Color(0xFFF97316)
val AQIUnhealthy = Color(0xFFEF4444)
val AQIHazardous = Color(0xFFA855F7)

// Data Colors
val DataBlue = Color(0xFF38BDF8)
val DataPink = Color(0xFFF43F5E)
val DataAmber = Color(0xFFF59E0B)

// AI Assistant Colors
val AIAccent = Color(0xFF0EA5E9)
val AICyanGlow = Color(0xFF22D3EE)
val AIChatHighlight = Color(0xFF6366F1)

// Chat UI Colors
val ChatBackground = Color(0xFF1E293B)
val ChatUserBubble = Color(0xFF334155)
val ChatAIHighlight = Color(0xFF2563EB)

// Gradient definitions (useful for UI components)
val MapHeatmapGradient = listOf(AQIGood, AQIModerate, AQIUnhealthySensitive, AQIUnhealthy)
val AQICircleGradient = listOf(AQIGood, AQIModerate, AQIUnhealthySensitive, AQIUnhealthy)
