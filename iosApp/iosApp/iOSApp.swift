import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.startKoinForIos()
        MainViewControllerKt.registerQrScanner(bridge: QrScanner())
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    MainViewControllerKt.handleDeepLink(url: url.absoluteString)
                }
        }
    }
}
