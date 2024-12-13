package com.capstone.injureal.api

data class ApiResponse(
    val imageUrl: String,
    val wounds_name: String,
    val description: String,
    val treatment: List<String>,
    val warnings: List<String>,
    val do_not: List<String>,
    val recommended_medicines: List<String>
)