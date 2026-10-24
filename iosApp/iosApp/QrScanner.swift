import UIKit
import VisionKit
import Shared

/// VisionKit's live scanner (system camera UI with highlighting), used by "Scan & pay".
/// Kotlin can't call this Swift-only API directly, so it's registered through `QrScannerBridge`.
final class QrScanner: NSObject, QrScannerBridge, DataScannerViewControllerDelegate {
    private var onResult: ((String?) -> Void)?
    private weak var scanner: DataScannerViewController?

    func isAvailable() -> Bool {
        DataScannerViewController.isSupported && DataScannerViewController.isAvailable
    }

    func scan(onResult: @escaping (String?) -> Void) {
        guard isAvailable(), let presenter = Self.topViewController() else {
            onResult(nil)
            return
        }
        self.onResult = onResult

        let scanner = DataScannerViewController(
            recognizedDataTypes: [.barcode(symbologies: [.qr])],
            qualityLevel: .balanced,
            recognizesMultipleItems: false,
            isHighFrameRateTrackingEnabled: false,
            isHighlightingEnabled: true
        )
        scanner.delegate = self
        scanner.navigationItem.leftBarButtonItem = UIBarButtonItem(
            systemItem: .close,
            primaryAction: UIAction { [weak self] _ in self?.finish(with: nil) }
        )
        self.scanner = scanner

        let navigation = UINavigationController(rootViewController: scanner)
        navigation.modalPresentationStyle = .fullScreen
        presenter.present(navigation, animated: true) {
            try? scanner.startScanning()
        }
    }

    func dataScanner(_ dataScanner: DataScannerViewController, didAdd addedItems: [RecognizedItem], allItems: [RecognizedItem]) {
        for item in addedItems {
            if case .barcode(let barcode) = item, let payload = barcode.payloadStringValue {
                UINotificationFeedbackGenerator().notificationOccurred(.success)
                finish(with: payload)
                return
            }
        }
    }

    private func finish(with payload: String?) {
        let callback = onResult
        onResult = nil
        scanner?.stopScanning()
        scanner?.navigationController?.dismiss(animated: true) {
            callback?(payload)
        }
    }

    private static func topViewController() -> UIViewController? {
        let root = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow }?
            .rootViewController
        var top = root
        while let presented = top?.presentedViewController { top = presented }
        return top
    }
}
