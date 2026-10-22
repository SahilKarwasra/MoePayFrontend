package com.laarasoft.moepay.onboarding.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep
import com.laarasoft.moepay.onboarding.presentation.profile.ProfileRoot
import com.laarasoft.moepay.onboarding.presentation.review.ReviewRoot
import com.laarasoft.moepay.onboarding.presentation.username.UsernameRoot

/**
 * Profile -> username -> review. The backend decides the next step after every save,
 * so each screen reports it and this graph routes on it.
 */
fun NavGraphBuilder.onboardingGraph(
    navController: NavController,
    startStep: OnboardingStep,
    onCompleted: () -> Unit,
) {
    fun goTo(step: OnboardingStep) {
        if (step == OnboardingStep.COMPLETED) {
            onCompleted()
        } else {
            navController.navigate(step.toRoute()) {
                launchSingleTop = true
            }
        }
    }

    navigation<OnboardingGraphRoutes.Graph>(
        startDestination = startStep.toRoute(),
    ) {
        composable<OnboardingGraphRoutes.Profile> {
            ProfileRoot(
                canGoBack = navController.canGoBackWithinOnboarding(),
                onBack = { navController.navigateUp() },
                onNextStep = ::goTo,
            )
        }
        composable<OnboardingGraphRoutes.Username> {
            UsernameRoot(
                canGoBack = navController.canGoBackWithinOnboarding(),
                onBack = { navController.navigateUp() },
                onNextStep = ::goTo,
            )
        }
        composable<OnboardingGraphRoutes.Review> {
            ReviewRoot(
                canGoBack = navController.canGoBackWithinOnboarding(),
                onBack = { navController.navigateUp() },
                onCompleted = onCompleted,
            )
        }
    }
}

/**
 * True when there is an earlier onboarding screen to return to. After sign-in the stack is
 * `[OnboardingGraph, <step>]`, and the graph entry itself is not a screen.
 */
private fun NavController.canGoBackWithinOnboarding(): Boolean {
    val previous = previousBackStackEntry?.destination ?: return false
    return previous !is NavGraph
}
