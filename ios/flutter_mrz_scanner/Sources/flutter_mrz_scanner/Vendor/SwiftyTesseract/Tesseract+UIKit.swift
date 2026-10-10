//
//  Tesseract+UIKit.swift
//
//
//  Created by Steven Sherry on 7/14/20.
//
//  Trimmed for flutter_mrz_scanner: only the plain `performOCR(on:)` overload
//  is kept. The `recognizedBlocks` overloads and the Combine publishers were
//  dropped along with `Tesseract+RecognizedBlock.swift`.
//

#if canImport(UIKit)
import UIKit
import libtesseract

extension UIImage {
  var data: Result<Data, Tesseract.Error> {
    guard let data = pngData() else { return .failure(.imageConversionError) }
    return .success(data)
  }
}

extension Tesseract {
  /// Performs OCR on a `UIImage`
  /// - Parameter image: The image to perform recognition on
  /// - Returns: A result containing the recognized `String `or an `Error` if recognition failed
  func performOCR(on image: UIImage) -> Result<String, Error> {
    image.data
      .flatMap { performOCR(on: $0) }
  }
}
#endif
