package goodtime.training.wod.timer.ui.main.amrap_for_time

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import goodtime.training.wod.timer.common.CombinedLiveData

class TimeSpinnerData(private var minutes: Int, private var seconds: Int = 0) {

    private val _minutes = MutableLiveData(minutes)
    private val _seconds = MutableLiveData(seconds)

    private val _data = CombinedLiveData(_minutes, _seconds) { data: List<Any?> ->
        if ((data[0] == null) || (data[1] == null))
            0
        else
            data[0] as Int * 60 + data[1] as Int
    }

    fun setMinutes(value: Int) {
        _minutes.postValue(value)
        minutes = value
    }

    fun setSeconds(value: Int) {
        _seconds.postValue(value)
        seconds = value
    }

    fun getMinutes() = minutes
    fun getSeconds() = seconds

    /**
     * Returns the duration in seconds
     */
    fun get() : LiveData<Int> { return _data }
}
