package goodtime.training.wod.timer.ui.stats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.StringUtils.Companion.formatDateAndTime
import goodtime.training.wod.timer.data.model.Session

class LogAdapter(private val listener: Listener) : RecyclerView.Adapter<LogAdapter.ViewHolder>() {

    interface Listener {
        fun onClick(position: Int)
    }

    var data = listOf<Session>()
        set(data) {
            field = data
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener { listener.onClick(position) }
    }

    class ViewHolder private constructor(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val exercise: TextView = itemView.findViewById(R.id.title)
        private val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        private val rounds: TextView = itemView.findViewById(R.id.rounds)
        private val duration: TextView = itemView.findViewById(R.id.duration)
        private val notes: TextView = itemView.findViewById(R.id.notes) //TODO: toggle visibility according to content; add notes to Entity

        fun bind(session: Session) {
            icon.setImageDrawable(ResourcesHelper.getDrawableFor(session.skeleton.type))
            exercise.text = StringUtils.toFavoriteFormatExtended(session.skeleton)
            timestamp.text = formatDateAndTime(session.timestamp)
            if (session.actualRounds.size == 0) {
                rounds.visibility = View.INVISIBLE
            } else {
                rounds.visibility = View.VISIBLE
                rounds.text = "${session.actualRounds.size} rounds"
            }
            duration.text = StringUtils.secondsToNiceFormat(session.actualDuration)
        }

        companion object {
            fun from(parent: ViewGroup) : ViewHolder {
                val layoutInflater =  LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.row_statistics_row, parent, false)
                return ViewHolder(view)
            }
        }
    }

}
