package goodtime.training.wod.timer.ui.main.custom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.DimensionsUtils
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.databinding.RowSpinnerDropdownItemWithImageBinding


class SessionTypeSpinnerAdapter(context: Context, objects: Array<String>) :
    ArrayAdapter<String>(context, R.layout.row_spinner_dropdown_item_with_image, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: RowSpinnerDropdownItemWithImageBinding =
            if (convertView != null) RowSpinnerDropdownItemWithImageBinding.bind(convertView)
            else RowSpinnerDropdownItemWithImageBinding.inflate(LayoutInflater.from(context), parent, false)

        val drawable = ResourcesHelper.getDrawableFor(TypeConverter().fromInt(position))
        val drawableSize = DimensionsUtils.dpToPx(context, 20F)
        drawable.setBounds(0, 0, drawableSize, drawableSize)

        binding.name.setCompoundDrawables(drawable, null, null, null)
        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: RowSpinnerDropdownItemWithImageBinding =
            if (convertView != null) RowSpinnerDropdownItemWithImageBinding.bind(convertView)
            else RowSpinnerDropdownItemWithImageBinding.inflate(LayoutInflater.from(context), parent, false)

        val drawable = ResourcesHelper.getDrawableFor(TypeConverter().fromInt(position))
        val drawableSize = DimensionsUtils.dpToPx(context, 20F)
        drawable.setBounds(0, 0, drawableSize, drawableSize)

        binding.name.setCompoundDrawables(drawable, null, null, null)
        return binding.root
    }
}
