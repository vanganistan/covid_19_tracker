package com.example.tracker.ui.countries

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.Constants.Constants.DAY
import com.example.tracker.Constants.Constants.MONTH
import com.example.tracker.R
import com.example.tracker.utils.ExpansionUtils.decimalFormatter
import com.example.tracker.utils.ExpansionUtils.fromMillis
import com.example.tracker.utils.ExpansionUtils.toMillis
import com.example.tracker.data.local.entity.TimeLine
import java.util.*

class CountriesDaysAdapter(val timeLine: TimeLine) : RecyclerView.Adapter<CountriesDaysAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.country_detail_bottom_sheet,parent, false))
        return v
    }
    inner class ViewHolder(val v: View): RecyclerView.ViewHolder(v)

    override fun getItemCount(): Int = timeLine.cases.size - 1

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CountriesDaysAdapter.ViewHolder, position: Int) {
        val mDay = holder.v.findViewById<TextView>(R.id.day)
        val mMonth = holder.v.findViewById<TextView>(R.id.month)
        val mCases = holder.v.findViewById<TextView>(R.id.cases)
        val mCasesOnDay = holder.v.findViewById<TextView>(R.id.cases_on_day)
        val mRecovered = holder.v.findViewById<TextView>(R.id.recovered)
        val mRecoveredOnDay = holder.v.findViewById<TextView>(R.id.recovered_on_today)
        val mDeaths = holder.v.findViewById<TextView>(R.id.deaths)
        val mDeathsOnDay = holder.v.findViewById<TextView>(R.id.deaths_on_today)

        val keys = timeLine.cases.keys.reversed().toTypedArray()

        val day = keys[position].toMillis().fromMillis(DAY , Locale(holder.v.context.resources.getString(R.string.app_locale)))
        val month = keys[position].toMillis().fromMillis(MONTH, Locale(holder.v.context.resources.getString(R.string.app_locale)))

        val cases = timeLine.cases.getValue(keys[position])
        val casesOnDay = cases.minus(timeLine.cases.getValue(keys[position + 1]))

        val recovered = timeLine.recovered.getValue(keys[position])
        val recoveredOnDay = recovered.minus(timeLine.recovered.getValue(keys[position + 1]))

        val death= timeLine.deaths.getValue(keys[position])
        val deathOnDay= death.minus(timeLine.deaths.getValue(keys[position + 1]))

        mDay.text = day
        mMonth.text = month

        mCases.text = cases.decimalFormatter()
        mCasesOnDay.text =  "+${casesOnDay.decimalFormatter()}"

        mRecovered.text = recovered.decimalFormatter()
        mRecoveredOnDay.text = "+${recoveredOnDay.decimalFormatter()}"

        mDeaths.text = death.decimalFormatter()
        mDeathsOnDay.text = "+${deathOnDay.decimalFormatter()}"
    }
}