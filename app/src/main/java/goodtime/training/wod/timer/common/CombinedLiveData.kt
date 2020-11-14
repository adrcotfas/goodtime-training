package goodtime.training.wod.timer.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class CombinedLiveData<R>(vararg liveData: LiveData<*>,
                          private val combine: (data: List<Any?>) -> R) : MediatorLiveData<R>() {

    private val data: MutableList<Any?> = MutableList(liveData.size) { null }

    init {
        for(i in liveData.indices){
            super.addSource(liveData[i]) {
                data[i] = it
                value = combine(data)
            }
        }
    }
}