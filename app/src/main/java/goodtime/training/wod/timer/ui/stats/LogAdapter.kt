package goodtime.training.wod.timer.ui.stats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import goodtime.training.wod.timer.R
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

        private val exercise: TextView = itemView.findViewById(R.id.exercise)
        private val date: TextView = itemView.findViewById(R.id.date)

        fun bind(item: Session) {
            exercise.text = "${item.type} ${item.actualDuration}"
            date.text = formatDateAndTime(item.timestamp)
        }

        companion object {
            fun from(parent: ViewGroup) : ViewHolder {
                val layoutInflater =  LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.row_log, parent, false)
                return ViewHolder(view)
            }
        }
    }

}
