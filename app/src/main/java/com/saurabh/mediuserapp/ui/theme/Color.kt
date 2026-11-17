package com.saurabh.mediuserapp.ui.theme

import androidx.compose.ui.graphics.Color

// Blue: brand / action
val Blue50 = Color(0xFFEEF5FF); val Blue100 = Color(0xFFDCEBFF)
val Blue200 = Color(0xFFB8D6FD); val Blue300 = Color(0xFF86BAFB)
val Blue400 = Color(0xFF4D9AF5); val Blue500 = Color(0xFF1F82EC)
val Blue600 = Color(0xFF0A6EE0); val Blue700 = Color(0xFF0858B8)
val Blue800 = Color(0xFF064490); val Blue900 = Color(0xFF042F66)

// Green: success only
val Green50 = Color(0xFFEFF9F4); val Green100 = Color(0xFFD9F2E4)
val Green200 = Color(0xFFB3E5C9); val Green600 = Color(0xFF12804A)
val Green700 = Color(0xFF0E6439); val Green800 = Color(0xFF0A4C2B)

// Amber: warning
val Amber50 = Color(0xFFFFF8EB); val Amber100 = Color(0xFFFFEBC8)
val Amber200 = Color(0xFFFFD791); val Amber400 = Color(0xFFE8A23A)
val Amber600 = Color(0xFFB8730A); val Amber700 = Color(0xFF965C08)
val Amber800 = Color(0xFF754806)

// Red: danger
val Red50 = Color(0xFFFFF1F2); val Red100 = Color(0xFFFFDDE0)
val Red200 = Color(0xFFFFBFC5); val Red600 = Color(0xFFD13440)
val Red700 = Color(0xFFB52A35); val Red800 = Color(0xFF8F1F29)

// Sky: info
val Sky50 = Color(0xFFEAF8FE); val Sky100 = Color(0xFFD0F0FC)
val Sky200 = Color(0xFFA4E2F9); val Sky600 = Color(0xFF0B7FAE)
val Sky700 = Color(0xFF096890); val Sky800 = Color(0xFF07516F)

// Neutral
val White = Color(0xFFFFFFFF)
val IconFill = Color(0xFFF3F7FD)
val FieldFill = Color(0xFFE8EFF8)
val Ink = Color(0xFF0E2240)
val Muted = Color(0xFF5B6F8C)
val Outline = Color(0xFFDBE5F2)
val Transparent = Color(0x00000000)

// Logo only (splash / logo, never in UI components)
val LogoBlue = Color(0xFF0888F8)
val LogoCyan = Color(0xFF18B8F8)
val LogoLavender = Color(0xFFE8E8F8)

/** Normal / pressed / held shades of one colour family. */
data class Shade(val normal: Color, val pressed: Color, val held: Color)

object Shades {
    val BlueSolid = Shade(Blue600, Blue700, Blue800)
    val BlueRow = Shade(White, Blue50, Blue100)
    val BlueChipSelected = Shade(Blue100, Blue200, Blue300)
    val BlueSecondaryButton = Shade(White, Blue100, Blue200)
    val IconButton = Shade(IconFill, Blue100, Blue200)
    val BlueText = Shade(Blue600, Blue700, Blue800)
    val SuccessPill = Shade(Green100, Green200, Green200)
    val SuccessCard = Shade(Green50, Green100, Green200)
    val GreenText = Shade(Green600, Green700, Green800)
    val AmberSolid = Shade(Amber600, Amber700, Amber800)
    val AmberPill = Shade(Amber100, Amber200, Amber400)
    val AmberCard = Shade(Amber50, Amber100, Amber200)
    val AmberRow = Shade(White, Amber50, Amber100)
    val AmberText = Shade(Amber600, Amber700, Amber800)
    val RedSolid = Shade(Red600, Red700, Red800)
    val RedPill = Shade(Red100, Red200, Red200)
    val RedCard = Shade(Red50, Red100, Red200)
    val RedText = Shade(Red600, Red700, Red800)
    val SkyTile = Shade(Sky100, Sky200, Sky200)
    val SkyCard = Shade(Sky50, Sky100, Sky200)
    val SkyText = Shade(Sky600, Sky700, Sky800)
}
