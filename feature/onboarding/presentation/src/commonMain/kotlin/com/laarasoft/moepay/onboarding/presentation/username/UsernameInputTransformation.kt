package com.laarasoft.moepay.onboarding.presentation.username

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import com.laarasoft.moepay.onboarding.domain.UsernameValidator

/** Lowercases input and drops anything the backend would reject outright. */
internal object UsernameInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val original = asCharSequence().toString()
        val cleaned = original
            .lowercase()
            .filter { it in 'a'..'z' || it in '0'..'9' || it == '_' || it == '.' }
            .take(UsernameValidator.MAX_LENGTH)
        if (cleaned != original) {
            replace(0, length, cleaned)
        }
    }
}
