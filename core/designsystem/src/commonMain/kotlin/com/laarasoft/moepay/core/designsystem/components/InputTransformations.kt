package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer

/** Rejects any edit that would put a non-digit into the field. */
object DigitsOnlyTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        if (!asCharSequence().all { it.isDigit() }) {
            revertAllChanges()
        }
    }
}
