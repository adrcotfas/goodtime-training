package goodtime.training.wod.timer.ui.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.slider.Slider
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.preferences.PreferenceHelper

class ProperSeekbarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr) {

    private lateinit var slider: Slider
    private var value: Int = 10

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        slider = holder.findViewById(R.id.slider) as Slider
        slider.value = value.toFloat()

        slider.setLabelFormatter { value: Float ->
            "${value.toInt()} sec"
        }

        slider.addOnChangeListener { _, value, _ ->
            (preferenceDataStore as PreferenceDataStore).putInt(
                PreferenceHelper.PRE_WORKOUT_COUNTDOWN_SECONDS, value.toInt())
        }
    }


    override fun onSetInitialValue(defaultValue: Any?) {
        value = (preferenceDataStore as PreferenceDataStore).getInt(
            PreferenceHelper.PRE_WORKOUT_COUNTDOWN_SECONDS, 10)
    }
}