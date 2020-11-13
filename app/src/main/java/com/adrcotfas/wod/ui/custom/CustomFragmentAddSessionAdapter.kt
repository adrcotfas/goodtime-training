package com.adrcotfas.wod.ui.custom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adrcotfas.wod.R

class CustomFragmentAddSessionAdapter(
    private val listener: Listener
)
    : RecyclerView.Adapter<CustomFragmentAddSessionAdapter.FooterViewHolder>()
{
    interface Listener {
        fun onAddSessionClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FooterViewHolder {
        return FooterViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: FooterViewHolder, position: Int) {
        holder.itemView.setOnClickListener{ listener.onAddSessionClicked() }
    }

    override fun getItemCount() = 1

    class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) : FooterViewHolder {
                val layoutInflater =  LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.row_custom_workout_session_add_session, parent, false)
                return FooterViewHolder(view)
            }
        }
    }
}