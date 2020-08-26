package com.example.tracker.ui.statistic

import android.animation.Animator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.Constants.Status
import com.example.tracker.R
import com.example.tracker.base.BaseFragment
import com.example.tracker.utils.ExpansionUtils.decimalFormatter
import com.example.tracker.utils.ExpansionUtils.setColorBefore
import com.example.tracker.utils.ExpansionUtils.timestampToDate
import com.example.tracker.utils.Utils
import com.example.tracker.data.local.entity.Statistic
import com.example.tracker.data.local.entity.TimeLine
import com.example.tracker.ui.countries.CountriesDaysAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.loading_and_error_layout.*
import kotlinx.android.synthetic.main.statistic_layout.*


class StatisticFragment : BaseFragment() {
    private val mViewModel: StatisticViewModel by injectViewModel()

    private var mNestedScroll: NestedScrollView? = null
    private var mRecyclerView: RecyclerView? = null
    private var mLineChart: LineChart? = null
    private var mPieChart: PieChart? = null
    private var mBehaviorCard: MaterialCardView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.statistic_layout, container, false)
        init(v)
        return v
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(v: View) {
        setHasOptionsMenu(true)

        mRecyclerView = v.findViewById(R.id.statistic_days)
        mNestedScroll = v.findViewById(R.id.nested_scroll)
        mBehaviorCard = v.findViewById(R.id.behavior_card)
        mLineChart = v.findViewById(R.id.line_chart)
        mPieChart = v.findViewById(R.id.pie_chart)


        initBottomSheet(v)

        v.findViewById<MaterialButton>(R.id.try_again).setOnClickListener {
            mViewModel.getOverallStatistic()
            mViewModel.getOverallHistoric()
        }



        v.findViewById<MaterialButton>(R.id.change_chart).setOnClickListener {

            if (mLineChart!!.isVisible) {
                mLineChart!!.animate().apply {
                    alpha(0f)
                    translationY(-mLineChart!!.height.toFloat())
                    duration = resources.getInteger(R.integer.primary_duration).toLong()
                    setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}

                        override fun onAnimationEnd(animation: Animator?) {
                            mLineChart!!.visibility = View.GONE
                        }

                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {}
                    })
                }

                mPieChart!!.animate().apply {
                    alpha(1f)
                    duration = resources.getInteger(R.integer.primary_duration).toLong()
                    translationY(0f)
                    setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}

                        override fun onAnimationEnd(animation: Animator?) {
                        }

                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {
                            mPieChart!!.visibility = View.VISIBLE
                        }
                    })
                }


                (it as MaterialButton).icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_line_chart)
            } else {
                mPieChart!!.animate().apply {
                    alpha(0f)
                    translationY(mPieChart!!.height.toFloat())
                    duration = resources.getInteger(R.integer.primary_duration).toLong()
                    setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}

                        override fun onAnimationEnd(animation: Animator?) {}

                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {
                            mLineChart!!.visibility = View.VISIBLE
                        }
                    })
                }
                mLineChart!!.animate().apply {
                    alpha(1f)
                    translationY(0f)
                    duration = resources.getInteger(R.integer.primary_duration).toLong()
                    setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}

                        override fun onAnimationEnd(animation: Animator?) {
                            mPieChart!!.visibility = View.GONE
                        }

                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {

                        }
                    })
                }

                (it as MaterialButton).icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_pie_chart)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    private fun initObservers() {
        mViewModel.mStatus.observe(viewLifecycleOwner, Observer {
            when (it) {
                Status.LOADING -> {
                    fab_show_hide.visibility = View.GONE
                    overall_container.visibility = View.GONE
                    failure_container.visibility = View.GONE
                    progress.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    fab_show_hide.visibility = View.VISIBLE
                    failure_container.visibility = View.GONE
                    progress.visibility = View.GONE
                    overall_container.visibility = View.VISIBLE
                }
                Status.ERROR -> {
                    progress.visibility = View.GONE
                    failure_container.visibility = View.VISIBLE
                    overall_container.visibility = View.GONE
                }

            }
        })

        mViewModel.mHistoric.observe(viewLifecycleOwner, Observer {
            it?.let { timeLine ->
                Utils.initializeLineChart(mLineChart!!, mNestedScroll!!, requireContext(), timeLine)
                initRecyclerView(timeLine)
            }

        })

        mViewModel.mStatistic.observe(viewLifecycleOwner, Observer { statistic ->
            statistic?.let {
                fab_show_hide.visibility = View.VISIBLE
                failure_container.visibility = View.GONE
                progress.visibility = View.GONE
                overall_container.visibility = View.VISIBLE

                Utils.initializePieChart(
                    mPieChart!!,
                    statistic.cases!!.toFloat(),
                    statistic.deaths!!.toFloat(),
                    statistic.recovered!!.toFloat(),
                    requireContext()
                )

                initStatistic(statistic)
            }
        })
    }


    private fun initBottomSheet(v: View) {
        val behavior = BottomSheetBehavior.from(mBehaviorCard!!)

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    STATE_EXPANDED -> {
                        fab_show_hide.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_close
                            )
                        )
                    }
                    STATE_HIDDEN -> {
                        fab_show_hide.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_list
                            )
                        )
                    }
                    else -> {
                        fab_show_hide.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_list
                            )
                        )
                    }
                }
            }

        })

        v.findViewById<FloatingActionButton>(R.id.fab_show_hide).setOnClickListener {
            if (behavior.state == STATE_EXPANDED) {
                behavior.state = STATE_HIDDEN
            } else {
                behavior.state = STATE_EXPANDED
            }
        }
    }

    private fun initRecyclerView(it: TimeLine) {
        mRecyclerView?.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        mRecyclerView?.adapter = CountriesDaysAdapter(it)
        mRecyclerView?.adapter!!.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun initStatistic(it: Statistic) {
        updated.text = it.updated.timestampToDate(requireContext())

        cases.text = it.cases?.decimalFormatter()
        deaths.text = it.deaths?.decimalFormatter()
        recovered.text = it.recovered?.decimalFormatter()

        cases_today.text =
            "+${it.todayCases?.decimalFormatter()} ${requireContext().getString(R.string.today)}"
        cases_today.setColorBefore(requireContext().getString(R.string.today))

        deaths_today.text =
            "+${it.todayDeaths?.decimalFormatter()} ${requireContext().getString(R.string.today)}"
        deaths_today.setColorBefore(requireContext().getString(R.string.today))

        recovered_today.text =
            "+${it.todayRecovered?.decimalFormatter()} ${requireContext().getString(R.string.today)}"
        recovered_today.setColorBefore(requireContext().getString(R.string.today))

        active.text = it.active?.decimalFormatter()
        tests.text = it.tests?.decimalFormatter()
        critical.text = it.critical?.decimalFormatter()
        cases_per_million.text = it.casesPerOneMillion?.toString()
        deaths_per_million.text = it.deathsPerOneMillion?.toString()
        tests_per_million.text = it.testsPerOneMillion?.toString()
        affected_country.text = it.affectedCountries?.decimalFormatter()
    }

}
