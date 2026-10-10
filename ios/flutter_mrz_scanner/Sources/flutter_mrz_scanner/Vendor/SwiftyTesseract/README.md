# Vendored SwiftyTesseract

These files are copied verbatim (apart from the trims noted in their headers)
from [SwiftyTesseract](https://github.com/SwiftyTesseract/SwiftyTesseract) at
tag **4.0.1**, MIT licensed — see `LICENSE.md`.

They are vendored rather than consumed as a package dependency because:

* SwiftyTesseract 4.x is the only version with Swift Package Manager support,
  but it was never published to the CocoaPods trunk (which stops at 3.1.3).
  Vendoring keeps a single source of truth for both build systems.
* The upstream repository was archived in April 2022, so there is nothing to
  track upstream any more.

The only external dependency left is the `libtesseract` binary target, mirrored
at [olexale/libtesseract](https://github.com/olexale/libtesseract).

Files dropped from upstream because the plugin does not use them:
`Tesseract+AppKit.swift`, `Tesseract+Combine.swift`,
`Tesseract+RecognizedBlock.swift`, `Tesseract+Variable.swift`.

All `public` modifiers were removed so the vendored types stay internal to the
plugin module and cannot clash with an app's own SwiftyTesseract dependency.

These sources are compiled **only** in Swift Package Manager builds. CocoaPods
builds pull the SwiftyTesseract 3.1.3 pod instead, and `MRZTesseract.swift`
hides the API difference between the two.
