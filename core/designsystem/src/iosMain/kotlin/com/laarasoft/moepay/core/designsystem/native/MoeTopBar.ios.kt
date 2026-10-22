package com.laarasoft.moepay.core.designsystem.native

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIAction
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIImage
import platform.UIKit.UINavigationBar
import platform.UIKit.UINavigationBarAppearance
import platform.UIKit.UINavigationItem
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel

/**
 * iOS 26 bar buttons are 48pt glass capsules with a soft shadow. UINavigationBar draws them
 * flush with the top of its frame and clips them to it, so the bar is hosted in a container
 * that leaves room above and below.
 */
private const val BAR_TOP_GAP = 4.0
private const val BAR_HEIGHT = 52.0
private const val BAR_BOTTOM_GAP = 6.0
private val NAVIGATION_BAR_CONTAINER_HEIGHT = (BAR_TOP_GAP + BAR_HEIGHT + BAR_BOTTOM_GAP).dp

private data class TopBarConfig(
    val title: String,
    val background: Color,
    val foreground: Color,
    val navigationAction: MoeBarAction?,
    val actions: List<MoeBarAction>,
)

/** Lays the bar out with a gap above and below, using whatever width Compose gives the view. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@ExportObjCClass
private class NavigationBarContainer : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    val bar = UINavigationBar()

    init {
        clipsToBounds = false
        addSubview(bar)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        val width = bounds.useContents { size.width }
        bar.setFrame(CGRectMake(0.0, BAR_TOP_GAP, width, BAR_HEIGHT))
    }
}

/** A real UINavigationBar hosted in Compose. Compose paints the status bar area behind it. */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun MoeTopBar(
    title: String,
    modifier: Modifier,
    navigationAction: MoeBarAction?,
    actions: List<MoeBarAction>,
) {
    val background = MaterialTheme.colorScheme.background
    val config by rememberUpdatedState(
        TopBarConfig(
            title = title,
            background = background,
            foreground = MaterialTheme.colorScheme.onBackground,
            navigationAction = navigationAction,
            actions = actions,
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        UIKitView(
            factory = { NavigationBarContainer() },
            update = { container -> container.bar.apply(config) },
            modifier = Modifier
                .fillMaxWidth()
                .height(NAVIGATION_BAR_CONTAINER_HEIGHT),
            properties = UIKitInteropProperties(
                isNativeAccessibilityEnabled = true,
            ),
        )
    }
}

private fun UINavigationBar.apply(config: TopBarConfig) {
    val foreground = config.foreground.toUIColor()
    val appearance = UINavigationBarAppearance().apply {
        configureWithOpaqueBackground()
        backgroundColor = config.background.toUIColor()
        shadowColor = null
        titleTextAttributes = mapOf<Any?, Any?>(NSForegroundColorAttributeName to foreground)
    }
    standardAppearance = appearance
    compactAppearance = appearance
    scrollEdgeAppearance = appearance
    tintColor = foreground

    val item = UINavigationItem(title = config.title)
    item.leftBarButtonItem = config.navigationAction?.toBarButtonItem()
    item.rightBarButtonItems = config.actions.map { it.toBarButtonItem() }
    setItems(listOf(item), animated = false)
}

private fun MoeBarAction.toBarButtonItem(): UIBarButtonItem {
    val action = UIAction.actionWithTitle(
        title = "",
        image = UIImage.systemImageNamed(icon.sfSymbol),
        identifier = null,
        handler = { _ -> onClick() },
    )
    return UIBarButtonItem(primaryAction = action).apply {
        accessibilityLabel = contentDescription
    }
}
