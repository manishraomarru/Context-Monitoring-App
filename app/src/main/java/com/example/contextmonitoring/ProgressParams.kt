package com.example.contextmonitoring

import androidx.annotation.StringRes
import com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START
import com.google.android.material.button.MaterialButton.IconGravity

class ProgressParams(
    var isEnabled: Boolean,
    var showProgress: Boolean,
    @StringRes var textResourceId: Int? = null,
    @IconGravity var iconGravity: Int = ICON_GRAVITY_TEXT_START
)