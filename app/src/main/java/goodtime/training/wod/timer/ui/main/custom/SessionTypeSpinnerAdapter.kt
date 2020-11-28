package goodtime.training.wod.timer.ui.main.custom

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.DimensionsUtils
import goodtime.training.wod.timer.common.ViewUtils.Companion.toDrawable
import goodtime.training.wod.timer.data.model.TypeConverter
import kotlinx.android.synthetic.main.row_spinner_dropdown_item_with_image.view.*


class SessionTypeSpinnerAdapter(context: Context, objects: Array<String>) :
    ArrayAdapter<String>(context, R.layout.row_spinner_dropdown_item_with_image, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view =  super.getView(position, convertView, parent)

        val drawable = toDrawable(context.resources, TypeConverter().fromInt(position))
        val drawableSize = DimensionsUtils.dpToPx(context, 20F)
        drawable.setBounds(0, 0, drawableSize, drawableSize)

        view.name.setCompoundDrawables(drawable, null, null, null)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val drawable = toDrawable(context.resources, TypeConverter().fromInt(position))
        val drawableSize = DimensionsUtils.dpToPx(context, 20F)
        drawable.setBounds(0, 0, drawableSize, drawableSize)

        view.name.setCompoundDrawables(drawable, null, null, null)
        return view
    }
}
