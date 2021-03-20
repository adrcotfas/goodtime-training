package goodtime.training.wod.timer.ui.upgrade

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.ResourcesHelper

class ExtraFeaturesAdapter(
        private val context: Context,
        private val data : List<Pair<String, Int>>)
    : RecyclerView.Adapter<ExtraFeaturesAdapter.ViewHolder>() {

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        private val text: TextView = itemView.findViewById(R.id.text)
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        fun bind(context : Context, item: Pair<String, Int>) {
            text.text = item.first
            icon.setImageDrawable(ContextCompat.getDrawable(context, item.second))
            icon.setColorFilter(ResourcesHelper.grey200)
        }

        companion object {
            fun from(parent: ViewGroup) : ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = inflater.inflate(R.layout.dialog_upgrade_row, parent, false)
                return ViewHolder(view)
            }
        }
    }
}