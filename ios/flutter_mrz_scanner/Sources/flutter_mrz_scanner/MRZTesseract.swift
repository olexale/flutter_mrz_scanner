import UIKit

#if !SWIFT_PACKAGE
import SwiftyTesseract
#endif

/// Wraps Tesseract so the rest of the plugin does not have to care which
/// dependency manager it was built with.
///
/// Swift Package Manager builds compile the SwiftyTesseract 4.0.1 sources
/// vendored under `Vendor/SwiftyTesseract` and read the training data from
/// `Bundle.module`. CocoaPods builds link the SwiftyTesseract 3.1.3 pod — the
/// last version published to the trunk — and read it from the generated
/// `TraineedDataBundle.bundle`. The two versions renamed the entry point and
/// changed `performOCR` from a callback to a `Result`.
struct MRZTesseract {
    #if SWIFT_PACKAGE
    private let tesseract: Tesseract
    #else
    private let tesseract: SwiftyTesseract
    #endif

    init() {
        #if SWIFT_PACKAGE
        tesseract = Tesseract(
            language: .custom("ocrb"),
            dataSource: Bundle.module,
            engineMode: .tesseractLstmCombined
        )
        #else
        let pluginBundle = Bundle(for: MRZScannerView.self)
        let bundle = pluginBundle.url(forResource: "TraineedDataBundle", withExtension: "bundle")
            .flatMap(Bundle.init(url:)) ?? pluginBundle
        tesseract = SwiftyTesseract(
            language: .custom("ocrb"),
            bundle: bundle,
            engineMode: .tesseractLstmCombined
        )
        #endif
    }

    func performOCR(on image: UIImage) -> String? {
        #if SWIFT_PACKAGE
        return try? tesseract.performOCR(on: image).get()
        #else
        var recognizedString: String?
        tesseract.performOCR(on: image) { recognizedString = $0 }
        return recognizedString
        #endif
    }
}
