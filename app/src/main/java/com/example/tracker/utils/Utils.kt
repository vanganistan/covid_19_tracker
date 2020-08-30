package com.example.tracker.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.example.tracker.Constants.Constants
import com.example.tracker.R
import com.example.tracker.utils.ExpansionUtils.fromMillis
import com.example.tracker.utils.ExpansionUtils.toMillis
import com.example.tracker.data.local.entity.TimeLine
import com.example.tracker.utils.ExpansionUtils.isDarkThemeOn
import com.example.tracker.view.LineChartMarker
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*


object Utils {

    fun shareNews(it: String, context: Context) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, it)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.choose)))
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initializeLineChart(
        mLineChart: LineChart,
        mScrollView: NestedScrollView?,
        context: Context,
        timeline: TimeLine
    ) {
        val xAxisLabel = ArrayList<String>(timeline.cases.size)
        val cases = ArrayList<Entry>()
        val deaths = ArrayList<Entry>()
        val recovered = ArrayList<Entry>()

        val keys = timeline.cases.keys.toTypedArray()

        for ((index, key) in keys.withIndex()) {
            val previousKey = keys.getOrNull(index - 1)


            if (previousKey != null) {
                val increasedValue = timeline.cases.getOrDefault(key, 0)
                    .minus(timeline.cases.getOrDefault(previousKey, 0))

                val data = Triple<Long, Int, String>(
                    key.toMillis(),
                    increasedValue,
                    Constants.CASES
                )

                val x_points = key.toMillis().toFloat()
                val y_points: Float = increasedValue.toFloat()
                cases.add(
                    Entry(
                        x_points,
                        y_points,
                        null,
                        data
                    )
                )
                xAxisLabel.add(
                    key.toMillis().fromMillis(
                        Constants.DAY_MONTH,
                        Locale(context.resources.getString(R.string.app_locale))
                    )
                )
            }
        }

        for ((index, key) in keys.withIndex()) {
            val previousKey = keys.getOrNull(index - 1)
            if (previousKey != null) {
                val increasedValue = timeline.recovered.getOrDefault(key, 0)
                    .minus(timeline.recovered.getOrDefault(previousKey, 0))


                val data = Triple<Long, Int, String>(
                    key.toMillis(),
                    increasedValue,
                    Constants.RECOVERED
                )

                val x_points = key.toMillis().toFloat()
                val y_points: Float = increasedValue.toFloat()
                recovered.add(
                    Entry(
                        x_points,
                        y_points,
                        null,
                        data
                    )
                )
                xAxisLabel.add(
                    key.toMillis().fromMillis(
                        Constants.DAY_MONTH,
                        Locale(context.resources.getString(R.string.app_locale))
                    )
                )
            }
        }

        for ((index, key) in keys.withIndex()) {
            val previousKey = keys.getOrNull(index - 1)
            if (previousKey != null) {
                val increasedValue = timeline.deaths.getOrDefault(key, 0)
                    .minus(timeline.deaths.getOrDefault(previousKey, 0))

                val data = Triple<Long, Int, String>(
                    key.toMillis(),
                    increasedValue,
                    Constants.DEATHS
                )

                val x_points = key.toMillis().toFloat()
                val y_points: Float = increasedValue.toFloat()
                deaths.add(
                    Entry(
                        x_points,
                        y_points,
                        null,
                        data
                    )
                )
                xAxisLabel.add(
                    key.toMillis().fromMillis(
                        Constants.DAY_MONTH,
                        Locale(context.resources.getString(R.string.app_locale))
                    )
                )
            }
        }


        val casesDataSet = LineDataSet(cases, null).apply {
            lineWidth = 3.0f
            circleRadius = 1f
            color = ContextCompat.getColor(context, R.color.blue)
            setCircleColor(ContextCompat.getColor(context, R.color.blue))
            highLightColor = context.getColor(R.color.blue)
            highlightLineWidth = 2f
            setDrawValues(false)
        }

        val deathsDataSet = LineDataSet(deaths, null).apply {
            lineWidth = 3.0f
            circleRadius = 1f
            color = ContextCompat.getColor(context, R.color.red)
            setCircleColor(ContextCompat.getColor(context, R.color.red))
            highLightColor = context.getColor(R.color.red)
            highlightLineWidth = 2f
            setDrawValues(false)
        }

        val recoveredDataSet = LineDataSet(recovered, null).apply {
            lineWidth = 3f
            circleRadius = 1f
            color = ContextCompat.getColor(context, R.color.green)
            setCircleColor(ContextCompat.getColor(context, R.color.green))
            highLightColor = context.getColor(R.color.green)
            highlightLineWidth = 2f
            setDrawValues(false)
        }
        val lineData = LineData(
            listOf(
                casesDataSet,
                deathsDataSet, recoveredDataSet
            )
        )

        mLineChart.apply {
            axisLeft.apply {
                textColor = context.getColor(R.color.primary_text)
                valueFormatter = LargeValueFormatter()
            }
            xAxis.apply {
                textColor = context.getColor(R.color.primary_text)
                setAvoidFirstLastClipping(true)
                spaceMax = 0.01f
                spaceMin = 0.01f
                setLabelCount(4, false)
                isGranularityEnabled = true
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toLong().fromMillis(
                            Constants.DAY_MONTH,
                            Locale(context.resources.getString(R.string.app_locale))
                        )
                    }
                }
            }
            setTouchEnabled(true)
            setDrawMarkers(true)
            markerView = LineChartMarker(
                context,
                R.layout.marker_layout
            )
            axisRight.setDrawLabels(false)
            setGridBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorPrimaryDark
                )
            )
            description.isEnabled = false
            legend.isEnabled = false
            getPaint(LineChart.PAINT_DESCRIPTION).color =
                ContextCompat.getColor(context, android.R.color.transparent)
            setDrawGridBackground(false)
            invalidate()
            animateX(750)
            data = lineData
            invalidate()
            visibility = View.VISIBLE
        }

        mScrollView?.let {
            mLineChart.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mScrollView.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                        mScrollView.requestDisallowInterceptTouchEvent(false)
                    }
                }
                false
            }
        }

    }


    fun initializePieChart(
        mChart: PieChart,
        cases: Float,
        deaths: Float,
        recovered: Float,
        context: Context
    ) {
        val data: ArrayList<PieEntry> = ArrayList()
        data.apply {
            this.add(PieEntry(cases, context.getString(R.string.cases)))
            this.add(PieEntry(deaths, context.getString(R.string.deaths)))
            this.add(PieEntry(recovered, context.getString(R.string.recovered)))
        }

        val pieDataSet = PieDataSet(data, "").apply {
            this.setColors(
                arrayListOf(
                    ContextCompat.getColor(context, R.color.blue),
                    ContextCompat.getColor(context, R.color.red),
                    ContextCompat.getColor(context, R.color.green)
                )
            )
        }

        val pieData = PieData(pieDataSet).apply {
            this.setValueTextSize(17F)
            this.setValueTextColor(ContextCompat.getColor(context, R.color.white))
        }

        mChart.apply {
            this.isHighlightPerTapEnabled = false
            this.data = pieData
            this.description.isEnabled = false
            this.legend?.isEnabled = false
            this.holeRadius = 0f
            this.transparentCircleRadius = 0f
            this.setEntryLabelTextSize(20f)
            this.animateXY(0, 0)
        }
    }

    suspend fun translateCountryNames(
        appLang: String,
        it: List<String>?,
        onResult: (ArrayList<String>) -> Unit
    ) {
        val names = ArrayList<String>()

        val translation = GlobalScope.async {
            withContext(Dispatchers.Default) {
                it?.let {
                    if (appLang != "en") {
                        it.forEachIndexed { _, country ->
                            val inLocale: Locale = Locale.forLanguageTag("en-EN")
                            val outLocale: Locale = Locale.forLanguageTag(appLang)
                            for (l in Locale.getAvailableLocales()) {
                                if (l.getDisplayCountry(inLocale).equals(country)) {
                                    names.add(l.getDisplayCountry(outLocale))
                                    break
                                }
                            }
                        }
                    } else {
                        it.forEachIndexed { _, country ->
                            names.add(country)
                        }
                    }

                    return@withContext names
                }
            }

        }
        translation.await()
        onResult(names)
    }

    fun getTrueLink(s: String): String {
        var link = ""
        try {
            val document = Jsoup.connect(s).get()
            link = document.select("div.m2L3rb > a")
                .attr("href")

        } catch (e: IOException) {

        }

        return link
    }

     fun showWebPage(context: Context, link: String) {
        val builder = CustomTabsIntent.Builder()
        if (context.isDarkThemeOn()) {
            builder.setColorScheme(CustomTabsIntent.COLOR_SCHEME_DARK)
        } else {
            builder.setColorScheme(CustomTabsIntent.COLOR_SCHEME_LIGHT)
        }
        builder.addDefaultShareMenuItem()
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(link))
    }
}




