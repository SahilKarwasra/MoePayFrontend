import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.startKoinForIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
