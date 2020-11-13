package com.adrcotfas.wod.ui.amrap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.number_picker.NumberPicker.Companion.Color
import com.adrcotfas.wod.common.number_picker.NumberPicker.Companion.PickerSize

class NumberPickerAdapter(
    private val data : List<Int>,
    private val context : Context,
    private val listener: Listener,
    private val prefixWithZero: Boolean,
    private val textSize : PickerSize,
    private val textColor : Color
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
        return ViewHolder.from(context, parent, prefixWithZero, textSize, textColor)
    }

    fun getLastIndex() = data.lastIndex

    class ViewHolder private constructor(itemView: View, private val prefixWithZero: Boolean)
        : RecyclerView.ViewHolder(itemView) {

        private val text: TextView = itemView.findViewById(R.id.text)
        fun bind(item: Int) {
            text.text =
                if (prefixWithZero) {
                    if (item < 10)  {"0$item"} else item.toString()
                } else {
                    item.toString()
                }
        }

        companion object {
            fun from(context: Context, parent: ViewGroup, prefixWithZero: Boolean, textSize: PickerSize, textColor: Color) : ViewHolder {
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
                        Color.GREEN -> context.resources.getColor(R.color.green_goodtime)
                        Color.RED -> context.resources.getColor(R.color.red_goodtime)
                        Color.NEUTRAL -> context.resources.getColor(R.color.grey500)
                    })
                return ViewHolder(view, prefixWithZero)
            }
        }
    }
}