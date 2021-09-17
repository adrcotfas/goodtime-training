package goodtime.training.wod.timer.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import java.time.DayOfWeek

class DayOfWeekPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr) {

    private var selectedDays = BooleanArray(DayOfWeek.values().size)
    private lateinit var chipGroup: ChipGroup

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        if (!this::chipGroup.isInitialized) {
            chipGroup = holder.findViewById(R.id.chip_group) as ChipGroup
            chipGroup.removeAllViews()
            setupDayChips()
        }
    }

    private fun setupDayChips() {
        val daysOfWeekShortName = StringUtils.getDaysOfWeekShort()

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val firstDayOfWeek = StringUtils.firstDayOfWeek()
        for (i in DayOfWeek.values().indices) {
            val currentDay = DayOfWeek.of(i + 1)
            val chipLayout = inflater.inflate(R.layout.preference_days_of_week_item, chipGroup, false)
            val chip = chipLayout.findViewById<Chip>(R.id.chip)
            chip.text = daysOfWeekShortName[i]
            chip.isChecked = selectedDays[i]
            chip.setOnCheckedChangeListener { _, isChecked ->
                selectedDays[i] = isChecked
                setCheckedDay(currentDay, selectedDays[i])
            }
            when (firstDayOfWeek) {
                DayOfWeek.SUNDAY -> {
                    when (currentDay) {
                        DayOfWeek.SUNDAY -> chipGroup.addView(chip, 0)
                        else -> chipGroup.addView(chip)
                    }
                }
                DayOfWeek.SATURDAY -> {
                    when (currentDay) {
                        DayOfWeek.SATURDAY -> chipGroup.addView(chip, 0)
                        DayOfWeek.SUNDAY -> chipGroup.addView(chip, 1)
                        else -> chipGroup.addView(chip)
                    }
                }
                else -> { // MONDAY
                    chipGroup.addView(chip)
                }
            }
        }
    }

    private fun setCheckedDay(currentDay: DayOfWeek, value: Boolean) {
        (preferenceDataStore as PreferenceDataStore).putBoolean(
            PreferenceHelper.REMINDER_DAYS + "_" + currentDay.ordinal, value)
        notifyDependencyChange(shouldDisableDependents())
    }

    override fun isSelectable() = false
    override fun shouldDisableDependents() = !selectedDays.contains(true)

    private fun setCheckedDays(selectedDays: BooleanArray) {
        this.selectedDays = selectedDays
        notifyDependencyChange(shouldDisableDependents())
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        setCheckedDays(
            (preferenceDataStore as PreferenceDataStore).getBooleanArray(
                PreferenceHelper.REMINDER_DAYS, DayOfWeek.values().size))
    }
}