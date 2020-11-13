package com.adrcotfas.wod.ui.custom

import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.data.model.SessionMinimal

class CustomViewModel : ViewModel() {
    lateinit var sessions : ArrayList<SessionMinimal>
}