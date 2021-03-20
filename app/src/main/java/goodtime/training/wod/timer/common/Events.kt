package goodtime.training.wod.timer.common

class Events {
    companion object{
        class FilterButtonClickEvent
        class FilterSelectedEvent(val name: String)
        class FilterClearButtonClickEvent
        class AddToStatisticsClickEvent
        class SetStartButtonState(val enabled: Boolean)
        class SetStartButtonStateWithColor(val enabled: Boolean)
        class ShowUpgradeDialog
    }
}