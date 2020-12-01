package goodtime.training.wod.timer.ui.main.emom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import goodtime.training.wod.timer.common.CombinedLiveData

class EmomSpinnerData(private var minutes: Int, private var seconds: Int, private var rounds: Int) {

    private val _minutes = MutableLiveData(minutes)
    private val _seconds = MutableLiveData(seconds)
    private val _rounds = MutableLiveData(rounds)

    private val _data = CombinedLiveData(_minutes, _seconds, _rounds) { data: List<Any?> ->
        if ((data[0] == null) || (data[1] == null) || data[2] == null)
            Pair(0, 0)
        else
            Pair(data[0] as Int * 60 + data[1] as Int, data[2] as Int)
    }

    fun setMinutes(value: Int) {
        _minutes.value = value
        minutes = value
    }
    fun setSeconds(value: Int) {
        _seconds.value = value
        seconds = value
    }
    fun setRounds(value: Int) {
        _rounds.value = value
        seconds = value
    }

    /**
     * Returns a pair or duration in seconds and rounds
     */
    fun get() : LiveData<Pair<Int, Int>> { return _data }

    fun getMinutes() = minutes
    fun getSeconds() = seconds
    fun getRounds() = rounds
}
