package com.example.tracker.model


data class Statistic(
    val updated: Long = 0,
    val cases: Int? = 0,
    val todayCases: Int? = 0,
    val deaths: Int? = 0,
    val todayDeaths: Int? = 0,
    val recovered: Int? = 0,
    val active: Int? = 0,
    val critical: Int? = 0,
    val undefined: Int? = 0,
    val population: Long? = 0,
    val casesPerOneMillion: Double? = 0.0,
    val activePerOneMillion: Double? = 0.0,
    val recoveredPerOneMillion: Double? = 0.0,
    val deathsPerOneMillion: Double? = 0.0,
    val tests: Double? = 0.0,
    val testsPerOneMillion: Double? = 0.0,
    val affectedCountries: Int? = 0
)

