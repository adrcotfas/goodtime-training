package com.adrcotfas.wod.common.number_picker

import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.smoothSnapToPosition
import com.adrcotfas.wod.ui.amrap.NumberPickerAdapter
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.bekawestberg.loopinglayout.library.LoopingSnapHelper

class NumberPicker(
    context: Context,
    private val recyclerView: RecyclerView,
    data: ArrayList<Int>,
    default: Int,
    private val rowHeight: Float,
    prefixWithZero: Boolean = true,
    largeText: Boolean = true,
    private val scrollListener : ScrollListener,
    private val clickListener : ClickListener
) : NumberPickerAdapter.Listener {

    interface ScrollListener {
        fun onScroll(value: Int)
    }

    /**
     * handles the click on the center of the picker
     */
    interface ClickListener {
        fun onClick()
    }

    private val viewManager : LoopingLayoutManager = LoopingLayoutManager(context)
    private val viewAdapter : NumberPickerAdapter = NumberPickerAdapter(this, prefixWithZero, largeText)
    private val snapHelper = LoopingSnapHelper()

    init {
        viewAdapter.data = data
        snapHelper.attachToRecyclerView(recyclerView)
        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
            layoutParams.height = 3 * rowHeight.toInt()
        }

        scrollToPosition(data.indexOf(default))

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        scrollListener.onScroll(getCurrentValue())
                    }
                }
            }
        })
    }

    fun setValue(value: Int) {
        scrollToPosition(viewAdapter.data.indexOf(value))
    }

    private fun scrollToPosition(position: Int) {
        val pos = if (position == 0) viewAdapter.data.lastIndex else (position - 1)
        recyclerView.smoothSnapToPosition(pos)
    }

    private fun adjustScrollToSnap(position: Int) {
        val view = viewManager.findViewByPosition(position)
        val snapDistance: IntArray? =
            view?.let { snapHelper.calculateDistanceToFinalSnap(viewManager, it) }
        snapDistance?.let {
            val y = snapDistance[1]
            if (y != 0) {
                recyclerView.scrollBy(0, y)
            }
        }
    }

    fun getCurrentValue() : Int {
        val center = snapHelper.findSnapView(recyclerView.layoutManager)
        return (center?.findViewById(R.id.text) as TextView).text.toString().toInt()
    }

    override fun onClick(position: Int) {
        val top = viewManager.topLeftIndex
        val bottom = viewManager.bottomRightIndex
        if (position == top || position == bottom) {
            adjustScrollToSnap(position)
            scrollListener.onScroll(getCurrentValue())
        } else {
            clickListener.onClick()
        }
    }
}