// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "flutter_mrz_scanner",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(name: "flutter-mrz-scanner", targets: ["flutter_mrz_scanner"])
    ],
    dependencies: [
        .package(name: "FlutterFramework", path: "../FlutterFramework"),
        // Mirror of the archived SwiftyTesseract/libtesseract, which vends
        // Tesseract 4.1.3 and Leptonica 1.82.0 as a binary xcframework.
        .package(url: "https://github.com/olexale/libtesseract.git", .upToNextMinor(from: "0.2.0"))
    ],
    targets: [
        .target(
            name: "flutter_mrz_scanner",
            dependencies: [
                .product(name: "FlutterFramework", package: "FlutterFramework"),
                .product(name: "libtesseract", package: "libtesseract")
            ],
            exclude: [
                "Vendor/SwiftyTesseract/LICENSE.md",
                "Vendor/SwiftyTesseract/README.md"
            ],
            resources: [
                .copy("Resources/tessdata")
            ],
            linkerSettings: [
                .linkedLibrary("z"),
                .linkedLibrary("c++")
            ]
        )
    ]
)
