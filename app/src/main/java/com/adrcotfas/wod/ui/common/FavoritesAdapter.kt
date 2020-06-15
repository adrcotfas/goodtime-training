package com.adrcotfas.wod.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adrcotfas.wod.R
import com.adrcotfas.wod.data.model.SessionMinimal

class FavoritesAdapter (private val listener : Listener)
    : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>(){

    interface Listener {
        fun onClick(session: SessionMinimal)
        fun onLongClick(id: Int) : Boolean
    }

    var data = listOf<SessionMinimal>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = data[position]
        holder.bind(session)
        holder.itemView.setOnClickListener { listener.onClick(session) }
        holder.itemView.setOnLongClickListener { listener.onLongClick(session.id) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { return ViewHolder.from(parent) }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title : TextView = itemView.findViewById(R.id.name)
        private val value : TextView = itemView.findViewById(R.id.value)

        fun bind(session: SessionMinimal) {
            title.text = session.name
            value.text = session.duration.toString() // TODO: format according to session.type
        }

        companion object {
            fun from(parent: ViewGroup) : ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.row_favorites, parent, false)
                return ViewHolder(
                    view
                )
            }
        }
    }
}