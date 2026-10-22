package com.laarasoft.moepay.onboarding.presentation.review

import com.laarasoft.moepay.core.presentation.util.UiText

data class ReviewState(
    val isLoading: Boolean = true,
    val fullName: String = "",
    val initials: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val email: String? = null,
    val isCompleting: Boolean = false,
    val error: UiText? = null,
)
