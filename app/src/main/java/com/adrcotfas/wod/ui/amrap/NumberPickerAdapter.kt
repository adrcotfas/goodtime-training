package com.adrcotfas.wod.ui.amrap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adrcotfas.wod.R

class NumberPickerAdapter(
    private val listener: Listener,
    private val prefixWithZero: Boolean,
    private val largeText : Boolean) : RecyclerView.Adapter<NumberPickerAdapter.ViewHolder>() {

    interface Listener {
        fun onClick(position: Int)
    }

    var data = listOf<Int>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener { listener.onClick(position) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, prefixWithZero, largeText)
    }

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
            fun from(parent: ViewGroup, prefixWithZero: Boolean, largeText: Boolean) : ViewHolder {
                val layoutInflater =  LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(
                        if (largeText) R.layout.row_number_picker_large else R.layout.row_number_picker,
                        parent, false)
                return ViewHolder(view, prefixWithZero)
            }
        }
    }
}