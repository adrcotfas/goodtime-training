package com.adrcotfas.wod.ui.amrap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adrcotfas.wod.R

class NumberPickerAdapter(private val listener: Listener) :
    RecyclerView.Adapter<NumberPickerAdapter.ViewHolder>() {

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
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text: TextView = itemView.findViewById(R.id.text)
        fun bind(item: Int) {
            text.text = if (item < 10)  {"0$item"} else item.toString()
        }

        companion object {
            fun from(parent: ViewGroup) : ViewHolder {
                val layoutInflater =  LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.row_number_picker, parent, false)
                return ViewHolder(view)
            }
        }
    }
}