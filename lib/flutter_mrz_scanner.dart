import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/services.dart';
import 'package:image/image.dart' as imglib;

class FlutterMrzScanner {
  static const MethodChannel _channel = MethodChannel('flutter_mrz_scanner');

  static Future<String> recognizeFile(File image) {
    final bytes = image.readAsBytesSync();
    return recognizeBytes(bytes);
  }

  static Future<String> recognizeBytes(List<int> image) {
    final base64 = base64Encode(image);
    return recognizeBase64(base64);
  }

  static Future<String> recognizeBase64(String base64) {
    return _channel.invokeMethod('recognize', {'image': base64});
  }

  /// On Android, expects `android.graphics.ImageFormat.NV21` format. Note:
  /// Concatenating the planes of `android.graphics.ImageFormat.YUV_420_888`
  /// into a single plane, converts it to `android.graphics.ImageFormat.NV21`.
  ///
  /// On iOS, expects `kCVPixelFormatType_32BGRA` format. However, this should
  /// work with most formats from `kCVPixelFormatType_*`.
  static Future<String> recognizeImage(List<int> bytes) {
    return _channel.invokeMethod('recognizeBytes', {'bytes': bytes});
  }
}

final imglib.PngEncoder pngEncoder = imglib.PngEncoder(level: 0, filter: 0);
List<int> convertYUV420toImageColor(
  int width,
  int height,
  int uvRowStride,
  int uvPixelStride,
  Uint8List planes0,
  Uint8List planes1,
  Uint8List planes2,
) {
  try {
    final croppedHeight = (height * 0.75).floor();
    final croppedWidth = (croppedHeight * 1.42).floor();

    var img = imglib.Image(croppedWidth, croppedHeight,
        channels: imglib.Channels.rgb); // Create Image buffer

    final maxY = 2 * croppedHeight;
    // Fill image buffer with plane[0] from YUV420_888
    for (int x = 0; x < width; x++) {
      for (int y = croppedHeight; y < maxY; y++) {
        final int uvIndex =
            uvPixelStride * (x / 2).floor() + uvRowStride * (y / 2).floor();
        final int index = y * width + x;

        final yp = planes0[index];
        final up = planes1[uvIndex];
        final vp = planes2[uvIndex];
        // Calculate pixel color
        final int r = (yp + vp * 1436 / 1024 - 179).round().clamp(0, 255);
        final int g = (yp - up * 46549 / 131072 + 44 - vp * 93604 / 131072 + 91)
            .round()
            .clamp(0, 255);
        final int b = (yp + up * 1814 / 1024 - 227).round().clamp(0, 255);
        // color: 0x FF  FF  FF  FF
        //           A   B   G   R
        final yImg = (y - croppedHeight) * width + x;
        img.data[yImg] = (b << 16) | (g << 8) | r;
      }
    }
//    img = imglib.grayscale(img);
//    img = imglib.copyCrop(
//        img, 0, (height / 3).floor(), width, (height / 3).floor());
//    return img.data;
//    final imglib.PngEncoder pngEncoder = imglib.PngEncoder(level: 0, filter: 0);
    final png = pngEncoder.encodeImage(img);
//    muteYUVProcessing = false;
    return png;
//    return Image.memory(png);
  } catch (e) {
    print('>>>>>>>>>>>> ERROR:' + e.toString());
  }
  return null;
}
