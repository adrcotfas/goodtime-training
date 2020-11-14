package goodtime.training.wod.timer.data.model

enum class SessionType(val value: Int) {
    AMRAP(0),
    EMOM(1),
    FOR_TIME(2),
    TABATA(3),
    REST(4)
}