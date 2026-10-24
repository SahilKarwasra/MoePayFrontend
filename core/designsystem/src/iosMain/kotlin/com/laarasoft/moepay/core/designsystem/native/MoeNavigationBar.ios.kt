package com.laarasoft.moepay.core.designsystem.native

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.laarasoft.moepay.core.designsystem.theme.extended
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIImage
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.UIKit.UIView
import platform.darwin.NSObject

private data class TabBarConfig(
    val items: List<MoeNavigationItem>,
    val selectedIndex: Int,
    val selected: Color,
    val unselected: Color,
)

/** UITabBar keeps a weak reference to its delegate, so Compose holds it. */
private class TabBarDelegate : NSObject(), UITabBarDelegateProtocol {
    var onSelect: (Int) -> Unit = {}

    override fun tabBar(tabBar: UITabBar, didSelectItem: UITabBarItem) {
        onSelect(didSelectItem.tag.toInt())
    }
}

/**
 * The iOS 26 tab bar is a floating glass capsule roughly 62pt tall, not the classic 49pt strip.
 * Forcing 49pt slices the icons and labels in half, so the bar is hosted in a container that
 * asks UIKit for its natural height and leaves a little room around the capsule.
 */
private const val TAB_BAR_VERTICAL_GAP = 4.0
private val TAB_BAR_CONTAINER_HEIGHT = 72.dp

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@ExportObjCClass
private class TabBarContainer : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    val bar = UITabBar()

    init {
        clipsToBounds = false
        addSubview(bar)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        val width = bounds.useContents { size.width }
        val height = bar.sizeThatFits(CGSizeMake(width, 0.0)).useContents { height }
        bar.setFrame(CGRectMake(0.0, TAB_BAR_VERTICAL_GAP, width, height))
    }
}

/** A real UITabBar hosted in Compose. Compose paints the home-indicator area below it. */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun MoeNavigationBar(
    items: List<MoeNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val currentOnItemSelected by rememberUpdatedState(onItemSelected)
    val delegate = remember { TabBarDelegate() }
    delegate.onSelect = { currentOnItemSelected(it) }

    val config by rememberUpdatedState(
        TabBarConfig(
            items = items,
            selectedIndex = selectedIndex,
            selected = colorScheme.onBackground,
            unselected = colorScheme.extended.textTertiary,
        )
    )

    // No background: the capsule floats over the tab content, which shows around it.
    Column(modifier = modifier.fillMaxWidth()) {
        UIKitView(
            factory = { TabBarContainer().also { it.bar.delegate = delegate } },
            update = { container -> container.bar.apply(config) },
            modifier = Modifier
                .fillMaxWidth()
                .height(TAB_BAR_CONTAINER_HEIGHT),
            properties = UIKitInteropProperties(
                isNativeAccessibilityEnabled = true,
                // Above the Compose canvas so the transparent area around the capsule shows the
                // content beneath, and the glass blurs it.
                placedAsOverlay = true,
            ),
        )
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

private fun UITabBar.apply(config: TabBarConfig) {
    // The system's default (Liquid Glass) material; only the capsule is drawn.
    val appearance = UITabBarAppearance().apply { configureWithDefaultBackground() }
    standardAppearance = appearance
    scrollEdgeAppearance = appearance
    tintColor = config.selected.toUIColor()
    unselectedItemTintColor = config.unselected.toUIColor()

    val currentTitles = items?.map { (it as UITabBarItem).title }
    val newTitles = config.items.map { it.label }
    if (currentTitles != newTitles) {
        val tabItems = config.items.mapIndexed { index, item ->
            UITabBarItem(
                title = item.label,
                image = UIImage.systemImageNamed(item.icon.sfSymbol),
                selectedImage = UIImage.systemImageNamed(item.selectedIcon.sfSymbol),
            ).apply { tag = index.toLong() }
        }
        setItems(tabItems, animated = false)
    }
    val tabItems = items?.map { it as UITabBarItem }.orEmpty()
    selectedItem = tabItems.getOrNull(config.selectedIndex)
}
