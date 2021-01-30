package goodtime.training.wod.timer.ui.stats

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Spinner
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.data.model.Session
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.round

class HistoryChartWrapper(private val chart: BarChart, private val spinner: Spinner) {

    private var xValues: MutableList<LocalDate> = mutableListOf()
    private lateinit var sessions: List<Session>
    
    init {
        setupHistoryChart()
        setupSpinner()
    }

    private fun setupSpinner() {
        spinner.setSelection(0, false)
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                (chart.xAxis.valueFormatter as DayWeekMonthXAxisFormatter)
                        .setRangeType(HistorySpinnerRangeType.values()[position])
                if (this@HistoryChartWrapper::sessions.isInitialized) {
                    refreshHistoryChart(sessions)
                }
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun setupHistoryChart() {
        chart.setXAxisRenderer(
                CustomXAxisRenderer(
                        chart.viewPortHandler,
                        chart.xAxis,
                        chart.getTransformer(YAxis.AxisDependency.LEFT)
                )
        )

        val yAxis: YAxis = chart.axisLeft
        yAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return TimeUnit.SECONDS.toMinutes(round(value).toLong()).toString()
            }
        }

        yAxis.textColor = ResourcesHelper.grey200
        yAxis.textSize = ResourcesHelper.getResources().getDimension(R.dimen.tinyTextSize) / ResourcesHelper.getResources().displayMetrics.density
        yAxis.setDrawAxisLine(false)
        val xAxis: XAxis = chart.xAxis
        xAxis.textColor = ResourcesHelper.grey500
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val rangeType: HistorySpinnerRangeType = HistorySpinnerRangeType.values()[spinner.selectedItemPosition]

        xAxis.valueFormatter = DayWeekMonthXAxisFormatter(xValues, rangeType)
        xAxis.setAvoidFirstLastClipping(false)

        xAxis.textSize = ResourcesHelper.getResources().getDimension(R.dimen.tinyTextSize) / ResourcesHelper.getResources().displayMetrics.density

        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.yOffset = 10f
        chart.axisLeft.gridColor = ResourcesHelper.grey1000
        chart.axisLeft.gridLineWidth = 1f
        chart.extraBottomOffset = 20f
        chart.extraLeftOffset = 10f
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.setNoDataText("")
        chart.setHardwareAccelerationEnabled(true)
        chart.animateY(500, Easing.EaseOutCubic)
        chart.legend.isEnabled = false
        chart.isDoubleTapToZoomEnabled = false
        chart.setScaleEnabled(false)
        chart.invalidate()
        chart.notifyDataSetChanged()
    }

    private fun generateHistoryChartData(sessions: List<Session>): BarData {
        val rangeType: HistorySpinnerRangeType = HistorySpinnerRangeType.values()[spinner.selectedItemPosition]
        val dummyIntervalRange = 42L
        val yVals: MutableList<BarEntry> = ArrayList()
        val tree = TreeMap<LocalDate, Int>()

        // generate dummy data
        val dummyEnd = LocalDate.now().plusDays(1)
        when (rangeType) {
            HistorySpinnerRangeType.DAYS -> {
                val dummyBegin = dummyEnd.minusDays(dummyIntervalRange)
                var i = dummyBegin
                while (i.isBefore(dummyEnd)) {
                    tree[i] = 0
                    i = i.plusDays(1)
                }
            }
            HistorySpinnerRangeType.WEEKS -> {
                val dummyBegin = dummyEnd.minusWeeks(dummyIntervalRange).with(
                        TemporalAdjusters.firstInMonth(StringUtils.firstDayOfWeek()))

                var i: LocalDate = dummyBegin
                while (i.isBefore(dummyEnd)) {
                    tree[i] = 0
                    i = i.plusWeeks(1)
                }
            }
            HistorySpinnerRangeType.MONTHS -> {
                val dummyBegin = dummyEnd.minusMonths(dummyIntervalRange)
                var i: LocalDate = dummyBegin
                while (i.isBefore(dummyEnd)) {
                    tree[i] = 0
                    i = i.plusMonths(1).withDayOfMonth(1)
                }
            }
        }

        // this is to sum up entries from the same day for visualization
        for (i in sessions.indices) {
            val millis = sessions[i].timestamp
            val localDate =
                Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            val localTime = when (rangeType) {
                HistorySpinnerRangeType.DAYS ->
                    localDate
                HistorySpinnerRangeType.WEEKS ->
                    localDate.with(
                            TemporalAdjusters.previousOrSame(StringUtils.firstDayOfWeek()))

                HistorySpinnerRangeType.MONTHS ->
                    localDate.with(TemporalAdjusters.firstDayOfMonth())
            }
            if (!tree.containsKey(localTime)) {
                tree[localTime] = sessions[i].actualDuration
            } else {
                tree[localTime] = (tree[localTime]!! + sessions[i].actualDuration)
            }
        }
        if (tree.size > 0) {
            xValues.clear()
            var i = 0
            var previousTime = tree.firstKey()
            for (crt in tree.keys) {
                // visualize intermediate days/weeks/months in case of days without completed sessions
                val beforeWhat = when (rangeType) {
                    HistorySpinnerRangeType.DAYS -> crt.minusDays(1)
                    HistorySpinnerRangeType.WEEKS -> crt.minusWeeks(1)
                    HistorySpinnerRangeType.MONTHS -> crt.minusMonths(1)
                }
                while (previousTime.isBefore(beforeWhat)) {
                    yVals.add(BarEntry(i.toFloat(), 0f))
                    previousTime = when (rangeType) {
                        HistorySpinnerRangeType.DAYS -> previousTime.plusDays(1)
                        HistorySpinnerRangeType.WEEKS -> previousTime.plusWeeks(1)
                        HistorySpinnerRangeType.MONTHS -> previousTime.plusMonths(1)
                    }
                    xValues.add(previousTime)
                    ++i
                }
                yVals.add(BarEntry(i.toFloat(), tree[crt]!!.toFloat()))
                xValues.add(crt)
                ++i
                previousTime = crt
            }
        }
        return BarData(generateLineDataSet(yVals, ResourcesHelper.green))
    }

    fun refreshHistoryChart(sessions: List<Session>) {
        this.sessions = sessions
        val data = generateHistoryChartData(sessions)
        data.barWidth = 0.4f
        chart.moveViewToX(data.xMax)
        chart.data = data
        chart.data.isHighlightEnabled = false

        val sixMinutes = TimeUnit.MINUTES.toSeconds(5).toFloat()
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = sixMinutes

        val visibleXCount = 10

        chart.setVisibleXRangeMaximum(visibleXCount.toFloat())
        chart.setVisibleXRangeMinimum(visibleXCount.toFloat())
        chart.xAxis.labelCount = visibleXCount
        chart.axisLeft.setLabelCount(6, true)
        val yMax = data.yMax
        if (sessions.isNotEmpty() && yMax > sixMinutes) {
            chart.axisLeft.axisMaximum = (ceil((yMax / 20).toDouble()) * 20).toFloat()
        } else {
            chart.axisLeft.axisMaximum =  sixMinutes
        }
    }

    private fun generateLineDataSet(entries: List<BarEntry>, color: Int): BarDataSet {
        val set = BarDataSet(entries, null)
        set.color = color
        return set
    }
}