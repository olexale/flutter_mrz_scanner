//
//  Tesseract+ConfigurationBuilder.swift
//
//
//  Created by Steven Sherry on 7/15/20.
//
//  Trimmed for flutter_mrz_scanner: the `set(_:value:)` helper, the `String`
//  convenience extensions and `Tesseract.Variable` were dropped since the
//  plugin never configures Tesseract variables. `@_functionBuilder` was
//  updated to its modern `@resultBuilder` spelling.
//

import Foundation
import libtesseract

extension Tesseract {
  @resultBuilder
  struct ConfigurationBuilder {
    static func buildBlock(_ configurations: (TessBaseAPI) -> Void...) -> (TessBaseAPI) -> Void {
      return { tessPointer in
        configurations.forEach { $0(tessPointer) }
      }
    }
  }
}
