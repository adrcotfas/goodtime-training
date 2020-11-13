package com.adrcotfas.wod.ui.custom

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.StringUtils
import com.adrcotfas.wod.common.ViewUtils
import com.adrcotfas.wod.data.model.SessionMinimal
import com.google.android.material.chip.Chip

class CustomFragmentSessionAdapter(
    private val data : ArrayList<SessionMinimal>,
    private val context: Context,
    private val listener : Listener)
    : RecyclerView.Adapter<CustomFragmentSessionAdapter.ViewHolder>()
{

    interface Listener {
        fun onCloseButtonClicked()
        fun onScrollHandleTouch()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context.resources, data[position])
        holder.scrollHandle.setOnClickListener{ listener.onScrollHandleTouch() }
        holder.closeButton.setOnClickListener{ listener.onCloseButtonClicked() }
    }

    override fun getItemCount() = data.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val scrollHandle: ImageView = view.findViewById(R.id.scroll_handle)
        val closeButton: ImageView = view.findViewById(R.id.close_button)
        private val sessionTypeImage: ImageView = view.findViewById(R.id.session_type_image)
        private val sessionTypeText: TextView = view.findViewById(R.id.session_type_text)
        private val sessionChip: Chip = view.findViewById(R.id.session_chip)

        fun bind(resources: Resources, session: SessionMinimal) {
            sessionTypeImage.setImageDrawable(ViewUtils.toDrawable(resources, session.type))
            sessionTypeText.text = StringUtils.toString(session.type)
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