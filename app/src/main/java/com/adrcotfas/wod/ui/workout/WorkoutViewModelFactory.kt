package com.adrcotfas.wod.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.common.soundplayer.SoundPlayer
import com.adrcotfas.wod.data.repository.AppRepository

class WorkoutViewModelFactory(private val soundPlayer: SoundPlayer, private val appRepository: AppRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WorkoutViewModel(soundPlayer, appRepository) as T
    }
}
