package goodtime.training.wod.timer.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.number_picker.NumberPicker.Companion.Color
import goodtime.training.wod.timer.common.number_picker.NumberPicker.Companion.PickerSize

class NumberPickerAdapter(
    val data: List<Int>,
    private val listener: Listener,
    private val prefixWithZero: Boolean,
    private val textSize: PickerSize,
    private val textColor: Color
) : RecyclerView.Adapter<NumberPickerAdapter.ViewHolder>() {

    interface Listener {
        fun onClick(position: Int)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener { listener.onClick(position) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, prefixWithZero, textSize, textColor)
    }

    fun getLastIndex() = data.lastIndex

    class ViewHolder private constructor(itemView: View, private val prefixWithZero: Boolean)
        : RecyclerView.ViewHolder(itemView) {

        private val text: TextView = itemView.findViewById(R.id.row_number_picker_tv)
        fun bind(item: Int) {
            text.text =
                if (prefixWithZero) {
                    if (item < 10)  {"0$item"} else item.toString()
                } else {
                    item.toString()
                }
        }

        companion object {
            fun from(
                parent: ViewGroup,
                prefixWithZero: Boolean,
                textSize: PickerSize,
                textColor: Color
            ) : ViewHolder {
                val layoutInflater =  LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(
                        when (textSize) {
                            PickerSize.LARGE -> R.layout.row_number_picker_large
                            PickerSize.MEDIUM -> R.layout.row_number_picker_medium
                        }, parent, false)
                view as TextView
                view.setTextColor(
                    when(textColor) {
                        Color.GREEN -> ResourcesHelper.green
                        Color.RED -> ResourcesHelper.red
                        Color.NEUTRAL -> ResourcesHelper.grey500
                    })
                return ViewHolder(view, prefixWithZero)
            }
        }
    }
}