package goodtime.training.wod.timer.common.number_picker

import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.smoothSnapToPosition
import goodtime.training.wod.timer.common.NumberPickerAdapter
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.bekawestberg.loopinglayout.library.LoopingSnapHelper

class NumberPicker(
    context: Context,
    private val recyclerView: RecyclerView,
    data: ArrayList<Int>,
    default: Int,
    private val rowHeight: Float,
    prefixWithZero: Boolean = true,
    textSize: PickerSize = PickerSize.LARGE,
    textColor: Color = Color.GREEN,
    private val scrollListener : ScrollListener
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
        fun onScroll(value: Int)
    }

    private val viewManager : LoopingLayoutManager = LoopingLayoutManager(context)
    private val viewAdapter : NumberPickerAdapter =
        NumberPickerAdapter(data, this, prefixWithZero, textSize, textColor)
    private val snapHelper = LoopingSnapHelper()

    init {
        snapHelper.attachToRecyclerView(recyclerView)
        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
            layoutParams.height = 3 * rowHeight.toInt()
            layoutParams.width = rowHeight.toInt()
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

    /**
     * Used for smooth scrolling when selecting a favorite
     */
    fun smoothScrollToPosition(position: Int) {
        val pos = if (position == 0) viewAdapter.getLastIndex() else (position - 1)
        recyclerView.smoothSnapToPosition(pos)
    }

    /**
     * Used for the initial scroll when opening the app
     */
    private fun scrollToPosition(position: Int) {
        val pos = if (position == 0) viewAdapter.getLastIndex() else (position + 1)
        recyclerView.scrollToPosition(pos)
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
            // TODO: do I need this?
            // center of the picker was clicked
        }
    }
}