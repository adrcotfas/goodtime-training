package com.adrcotfas.wod.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.*

class WorkoutViewModel(application: Application) : AndroidViewModel(application){

    private val workManager = WorkManager.getInstance(application)
    var outputWorkInfo: LiveData<List<WorkInfo>>

    init {
        outputWorkInfo = workManager.getWorkInfosForUniqueWorkLiveData(WORKOUT_WORK_NAME)
    }

    fun startWorkout(input: String) {
        val inputData = workDataOf(KEY_INPUT_SESSION_LIST to input)

        val workRequest = OneTimeWorkRequestBuilder<WorkoutWorker>()
            .setInputData(inputData)
            .build()

        workManager.beginUniqueWork(WORKOUT_WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest).enqueue()
    }

    internal fun cancelWork() {
        workManager.cancelUniqueWork(WORKOUT_WORK_NAME)
    }
}
