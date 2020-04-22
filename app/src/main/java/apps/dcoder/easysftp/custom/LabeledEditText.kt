package apps.dcoder.easysftp.custom

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.getStringOrThrow
import androidx.core.view.ViewCompat
import apps.dcoder.easysftp.R
import kotlinx.android.synthetic.main.view_labeled_edit_text.view.*

class LabeledEditText(context: Context, private val attrs: AttributeSet): LinearLayout(context, attrs) {

    private val compoundLayout = inflateAndAttachCompoundLayout()
    private val label: TextView? = compoundLayout.label
    private val editText: EditText? = compoundLayout.editText

    init {
        this.orientation = HORIZONTAL
        applyCustomAttributes()
    }

    private fun inflateAndAttachCompoundLayout(): View {
        return (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.view_labeled_edit_text, this, true)
    }

    private fun applyCustomAttributes() {
        val customAttrs = context.obtainStyledAttributes(attrs, R.styleable.LabeledEditText, 0, 0)
        try {
            applyLabelAttributes(customAttrs)
            applyEditTextAttributes(customAttrs)
        } finally {
            customAttrs.recycle()
        }
    }

    private fun applyLabelAttributes(customAttrs: TypedArray) {
        label?.text = customAttrs.getStringOrThrow(R.styleable.LabeledEditText_labelText)
        val labelWidth = customAttrs.getDimensionPixelSize(R.styleable.LabeledEditText_labelWidth, -1)
        if (labelWidth != -1) {
            label?.width = labelWidth
        }
    }

    private fun applyEditTextAttributes(customAttrs: TypedArray) {
        editText?.hint = customAttrs.getString(R.styleable.LabeledEditText_hint)
        editText?.let {
            val layoutParams = editText.layoutParams as LayoutParams
            val defaultSpacing = context.resources.getDimensionPixelSize(R.dimen.default_labeled_Edit_text_spacing)
            val spacing = customAttrs.getDimensionPixelSize(R.styleable.LabeledEditText_spacing, defaultSpacing)

            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
                layoutParams.leftMargin = spacing
            } else {
                layoutParams.rightMargin = spacing
            }
        }
    }

}