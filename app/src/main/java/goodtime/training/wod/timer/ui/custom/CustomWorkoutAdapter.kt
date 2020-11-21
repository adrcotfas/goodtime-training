package goodtime.training.wod.timer.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.ViewUtils
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import java.util.Collections

class CustomWorkoutAdapter(
    var data: ArrayList<SessionSkeleton>,
    private val context: Context,
    private val listener: Listener
)
    : RecyclerView.Adapter<CustomWorkoutAdapter.ViewHolder>()
{
    /**
     * Use this to compare the data before and after releasing the drag handle for rearranging
     */
    private lateinit var tmpData: ArrayList<SessionSkeleton>

    interface Listener {
        fun onDeleteButtonClicked(position: Int)
        fun onScrollHandleTouch(holder: ViewHolder)
        fun onDataReordered()
        fun onScrollHandleRelease()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context.resources, data[position])

        holder.scrollHandle.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                tmpData = data.clone() as ArrayList<SessionSkeleton>
                listener.onScrollHandleTouch(holder)
            }
            return@setOnTouchListener true
        }

        holder.closeButton.setOnClickListener{
            val pos = holder.bindingAdapterPosition
            data.removeAt(pos)
            notifyItemRemoved(pos)
            listener.onDeleteButtonClicked(pos)
        }
    }

    override fun getItemCount() = data.size

    fun moveItem(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) {
                Collections.swap(data, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(data, i, i - 1)
            }
        }
        notifyItemMoved(from, to)
    }

    fun onDragReleased() {
        if (data != tmpData) {
            listener.onDataReordered()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val scrollHandle: ImageView = view.findViewById(R.id.scroll_handle)
        val closeButton: ImageView = view.findViewById(R.id.close_button)
        val parent: ConstraintLayout = view.findViewById(R.id.parent)
        private val sessionTypeImage: ImageView = view.findViewById(R.id.session_type_image)
        private val sessionTypeText: TextView = view.findViewById(R.id.session_type_text)
        private val sessionChip: Chip = view.findViewById(R.id.session_chip)

        fun bind(resources: Resources, session: SessionSkeleton) {
            sessionTypeImage.setImageDrawable(ViewUtils.toDrawable(resources, session.type))
            sessionTypeText.text = StringUtils.toString(session.type)

            if (session.type == SessionType.REST) {
                sessionChip.setTextColor(ResourcesHelper.red)
                sessionChip.chipBackgroundColor = ColorStateList.valueOf(ResourcesHelper.darkRed)
            } else {
                sessionChip.setTextColor(ResourcesHelper.green)
                sessionChip.chipBackgroundColor = ColorStateList.valueOf(ResourcesHelper.darkGreen)
            }

            sessionChip.text = StringUtils.toFavoriteFormat(session)
            sessionTypeImage.setImageDrawable(ViewUtils.toDrawable(resources, session.type))
        }

        companion object {
            fun from(parent: ViewGroup) : ViewHolder {
                val layoutInflater =  LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.row_custom_workout_session, parent, false)
                return ViewHolder(view)
            }
        }
    }
}