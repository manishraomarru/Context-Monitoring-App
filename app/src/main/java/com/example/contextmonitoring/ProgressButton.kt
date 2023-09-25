package com.example.contextmonitoring

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable

class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialButtonStyle,
) : MaterialButton(context, attrs, defStyleAttr) {

    fun showProgress(params: ProgressParams.() -> Unit = {}) {
        val progressParams = ProgressParams(
            isEnabled = false,
            showProgress = true
        )
        progressParams.params()
        updateState(progressParams)
    }

    fun hideProgress(params: ProgressParams.() -> Unit = {}) {
        val progressParams = ProgressParams(
            isEnabled = true,
            showProgress = false
        )
        progressParams.params()
        updateState(progressParams)
    }

    private fun updateState(params: ProgressParams) {
        isEnabled = params.isEnabled
        text = params.textResourceId?.let { context.getText(it) }
        icon = if (params.showProgress) createProgressDrawable() else null
        iconGravity = params.iconGravity
    }

    private fun createProgressDrawable(): IndeterminateDrawable<CircularProgressIndicatorSpec> {
        val spec = CircularProgressIndicatorSpec(
            context, null, 0,
            R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
        )
        return IndeterminateDrawable.createCircularDrawable(context, spec)
    }
}