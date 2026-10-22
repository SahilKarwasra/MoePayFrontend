package com.laarasoft.moepay.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.laarasoft.moepay.auth.presentation.otp.OtpRoot
import com.laarasoft.moepay.auth.presentation.phone.PhoneNumberRoot
import com.laarasoft.moepay.auth.presentation.welcome.WelcomeRoot
import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep

/**
 * Welcome -> phone number -> OTP. [onAuthenticated] receives the backend's next
 * onboarding step so the app root can route to onboarding or home.
 */
fun NavGraphBuilder.authGraph(
    navController: NavController,
    onAuthenticated: (OnboardingStep) -> Unit,
) {
    navigation<AuthGraphRoutes.Graph>(
        startDestination = AuthGraphRoutes.Welcome,
    ) {
        composable<AuthGraphRoutes.Welcome> {
            WelcomeRoot(
                onGetStartedClick = {
                    navController.navigate(AuthGraphRoutes.PhoneNumber) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<AuthGraphRoutes.PhoneNumber> {
            PhoneNumberRoot(
                onBack = { navController.navigateUp() },
                onOtpSent = { phoneNumber, resendAfterSeconds ->
                    navController.navigate(
                        AuthGraphRoutes.OtpVerification(
                            phoneNumber = phoneNumber,
                            resendAfterSeconds = resendAfterSeconds,
                        )
                    ) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<AuthGraphRoutes.OtpVerification> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthGraphRoutes.OtpVerification>()
            OtpRoot(
                phoneNumber = route.phoneNumber,
                resendAfterSeconds = route.resendAfterSeconds,
                onBack = { navController.navigateUp() },
                onVerified = onAuthenticated,
            )
        }
    }
}
