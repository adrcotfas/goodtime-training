package goodtime.training.wod.timer.ui.main.hiit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import goodtime.training.wod.timer.common.CombinedLiveData

class HiitSpinnerData(private val secondsWork: Int, private val secondsBreak: Int, private val rounds: Int) {

    private val _secondsWork = MutableLiveData(secondsWork)
    private val _secondsBreak = MutableLiveData(secondsBreak)
    private val _rounds = MutableLiveData(rounds)

    private val _data = CombinedLiveData(_secondsWork, _secondsBreak, _rounds) {
            data: List<Any?> ->

        var foundNull = false
        for (d in data) {
            if (d == null) {
                foundNull = true
                break
            }
        }
        if (foundNull) {
            Triple(0, 0, 0)
        } else {
            Triple(data[0] as Int, data[1] as Int, data[2] as Int)
        }
    }

    fun setSecondsWork(value: Int) { _secondsWork.value = value }
    fun setSecondsBreak(value: Int) { _secondsBreak.value = value }
    fun setRounds(value: Int) { _rounds.value = value }

    /**
     * Returns a triple of work duration, break duration and rounds
     */
    fun get() : LiveData<Triple<Int, Int, Int>> { return _data }

    fun getSecondsWork() = secondsWork
    fun getSecondsBreak() = secondsBreak
    fun getRounds() = rounds
}