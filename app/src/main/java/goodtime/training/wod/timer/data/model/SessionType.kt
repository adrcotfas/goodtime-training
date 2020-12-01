package goodtime.training.wod.timer.data.model

enum class SessionType(val value: Int) {
    AMRAP(0),
    FOR_TIME(1),
    EMOM(2),
    HIIT(3),
    REST(4)
}