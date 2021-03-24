package goodtime.training.wod.timer.common.number_picker

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.bekawestberg.loopinglayout.library.LoopingSnapHelper
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.NumberPickerAdapter
import goodtime.training.wod.timer.common.smoothSnapToPosition

class NumberPicker(
        context: Context,
        private val recyclerView: RecyclerView,
        data: ArrayList<Int>,
        defaultValue: Int,
        private val rowHeight: Float,
        prefixWithZero: Boolean = true,
        textSize: PickerSize = PickerSize.LARGE,
        textColor: Color = Color.GREEN,
        private val scrollListener: ScrollListener
) : NumberPickerAdapter.Listener {

    companion object {
        enum class PickerSize {
            MEDIUM,
            LARGE
        }

        enum class Color {
            GREEN,
            RED,
            NEUTRAL
        }
    }

    interface ScrollListener {
        fun onScrollFinished(value: Int)
        fun onScroll()
    }

    private val loopingLayoutManager = LoopingLayoutManager(context)
    private val viewAdapter: NumberPickerAdapter =
            NumberPickerAdapter(data, this, prefixWithZero, textSize, textColor)
    private val snapHelper = LoopingSnapHelper()

    init {
        snapHelper.attachToRecyclerView(recyclerView)
        recyclerView.apply {
            layoutManager = loopingLayoutManager
            adapter = viewAdapter
            layoutParams.height = 3 * rowHeight.toInt()
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            setHasFixedSize(true)
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        val value = getCurrentValue()
                        scrollListener.onScrollFinished(value)
                    }
                    else -> scrollListener.onScroll()
                }
            }
        })
        smoothScrollToValue(defaultValue)
    }

    fun smoothScrollToValue(value: Int) {
        val indexOfValue = viewAdapter.data.indexOf(value)
        val pos = if (indexOfValue == 0) viewAdapter.getLastIndex() else indexOfValue - 1
        recyclerView.smoothSnapToPosition(pos)
    }

    private fun adjustScrollToSnap(position: Int) {
        val view = loopingLayoutManager.findViewByPosition(position)
        val snapDistance: IntArray? =
                view?.let { snapHelper.calculateDistanceToFinalSnap(loopingLayoutManager, it) }
        snapDistance?.let {
            val y = snapDistance[1]
            if (y != 0) {
                recyclerView.scrollBy(0, y)
            }
        }
    }

    fun getCurrentValue(): Int {
        val center = snapHelper.findSnapView(recyclerView.layoutManager)
        return (center?.findViewById(R.id.text) as TextView).text.toString().toInt()
    }

    override fun onClick(position: Int) {
        val top = loopingLayoutManager.topLeftIndex
        val bottom = loopingLayoutManager.bottomRightIndex
        if (position == top || position == bottom) {
            adjustScrollToSnap(position)
            scrollListener.onScrollFinished(getCurrentValue())
        } else {
            // center of the picker was clicked
        }
    }
}