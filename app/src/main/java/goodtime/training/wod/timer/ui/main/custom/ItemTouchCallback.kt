package goodtime.training.wod.timer.ui.main.custom

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView

class ItemTouchCallback : ItemTouchHelper.SimpleCallback(
    UP or DOWN, 0
) {
    private var recyclerView: RecyclerView? = null

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        this.recyclerView = recyclerView
        val adapter = recyclerView.adapter as CustomWorkoutAdapter
        val from = viewHolder.bindingAdapterPosition
        val to = target.bindingAdapterPosition
        adapter.moveItem(from, to)
        return true
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        val topY = viewHolder.itemView.top + dY
        val itemHeight = viewHolder.itemView.height
        val bottomY = topY + itemHeight

        // Only redraw child if it is inbounds of view
        if (topY > 0 && bottomY < recyclerView.height) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?,
                                   actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder as CustomWorkoutAdapter.ViewHolder
            viewHolder.parent.elevation = 8F
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            if (recyclerView != null) {
                (recyclerView!!.adapter as CustomWorkoutAdapter).onDragReleased()
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView,
                           viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder as CustomWorkoutAdapter.ViewHolder
        viewHolder.parent.elevation = 0F
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}
