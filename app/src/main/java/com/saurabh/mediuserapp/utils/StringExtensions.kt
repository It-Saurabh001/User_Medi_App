package com.saurabh.mediuserapp.utils

fun String.capitalizeEachWord(): String =
    lowercase().split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
