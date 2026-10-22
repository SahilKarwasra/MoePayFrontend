package com.laarasoft.moepay.auth.presentation.phone

sealed interface PhoneNumberAction {
    data object OnContinueClick : PhoneNumberAction
    data object OnBackClick : PhoneNumberAction
}
