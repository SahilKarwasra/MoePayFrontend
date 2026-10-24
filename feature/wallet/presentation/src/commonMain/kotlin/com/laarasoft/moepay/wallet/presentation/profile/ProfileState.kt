package com.laarasoft.moepay.wallet.presentation.profile

import com.laarasoft.moepay.core.domain.auth.User
import com.laarasoft.moepay.core.domain.wallet.BankAccount
import com.laarasoft.moepay.core.domain.wallet.WalletOverview
import com.laarasoft.moepay.core.presentation.util.UiText

data class ProfileState(
    val user: User? = null,
    val overview: WalletOverview? = null,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
    /** Bank account whose actions sheet is open. */
    val selectedBank: BankAccount? = null,
    val bankPendingRemoval: BankAccount? = null,
    val isBankActionInProgress: Boolean = false,
    val bankActionError: UiText? = null,
    val isLogoutDialogVisible: Boolean = false,
    val isLoggingOut: Boolean = false,
) {
    val displayName: String
        get() = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { user?.username.orEmpty() }
}

sealed interface ProfileAction {
    data object OnRefresh : ProfileAction
    data object OnBackClick : ProfileAction
    data object OnPinClick : ProfileAction
    data object OnResetPinClick : ProfileAction
    data class OnBankClick(val bank: BankAccount) : ProfileAction
    data object OnDismissBankSheet : ProfileAction
    data object OnMakePrimaryClick : ProfileAction
    data object OnRemoveBankClick : ProfileAction
    data object OnConfirmRemoveBank : ProfileAction
    data object OnDismissRemoveBank : ProfileAction
    data object OnLinkBankClick : ProfileAction
    data object OnLogoutClick : ProfileAction
    data object OnConfirmLogout : ProfileAction
    data object OnDismissLogoutDialog : ProfileAction
}

sealed interface ProfileEvent {
    data object Back : ProfileEvent
    data object SetPin : ProfileEvent
    data object ChangePin : ProfileEvent
    data object ResetPin : ProfileEvent
    data object LinkBank : ProfileEvent
}
