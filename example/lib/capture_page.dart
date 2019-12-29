import 'dart:async';
import 'dart:ui' as ui;

import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_mrz_scanner/flutter_mrz_scanner.dart';

class CapturePage extends StatefulWidget {
  @override
  _CapturePageState createState() => _CapturePageState();
}

class _CapturePageState extends State<CapturePage> {
  CameraController controller;
  String _recognized;

  @override
  void initState() {
    super.initState();
    availableCameras().then((cameras) {
      if (cameras.isNotEmpty) {
        _initCameraController(cameras.first).then((void v) {});
      } else {
        print('No camera available');
      }
    }).catchError((dynamic err) {
      // 3
      print('Error: $err.code\nError Message: $err.message');
    });
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        body: Column(
          children: <Widget>[
            Text(_recognized ?? 'recognizing'),
            _cameraPreviewWidget(),
          ],
        ),
      ),
    );
  }

  Future _initCameraController(CameraDescription cameraDescription) async {
    if (controller != null) {
      await controller.dispose();
    }

    controller = CameraController(
      cameraDescription,
      ResolutionPreset.medium,
      enableAudio: false,
    );

    controller.addListener(() {
      if (mounted) {
        setState(() {});
      }

      if (controller.value.hasError) {
        print('Camera error ${controller.value.errorDescription}');
      }
    });

    try {
      await controller.initialize();
      controller.startImageStream(_processCameraFrame);
    } on CameraException catch (e) {
      print(e);
    }

    if (mounted) {
      setState(() {});
    }
  }

  Widget _cameraPreviewWidget() {
    if (controller == null || !controller.value.isInitialized) {
      return const Text(
        'Loading',
        style: TextStyle(
          color: Colors.white,
          fontSize: 20.0,
          fontWeight: FontWeight.w900,
        ),
      );
    }

    return RepaintBoundary(
      key: _globalKey,
      child: AspectRatio(
        aspectRatio: controller.value.aspectRatio,
        child: CameraPreview(controller),
      ),
    );
  }

  final _globalKey = GlobalKey();

  Future<void> _processCameraFrame(CameraImage image) async {
    final RenderRepaintBoundary boundary =
        _globalKey.currentContext.findRenderObject();
    final image = await boundary.toImage();
    final byteData = await image.toByteData(format: ui.ImageByteFormat.png);
    final pngBytes = byteData.buffer.asUint8List();

    final recognized = await FlutterMrzScanner.recognizeBytes(pngBytes);

//    image.format.raw
//    final imgBytes = image.planes.first.bytes;
//    final img = convertYUV420toImageColor(
//        image.width,
//        image.height,
//        image.planes[1].bytesPerRow,
//        image.planes[1].bytesPerPixel,
//        image.planes[0].bytes,
//        image.planes[1].bytes,
//        image.planes[2].bytes);
//    final recognized = await FlutterMrzScanner.recognizeBytes(
//      img,
//    );
//    final recognized = imgBytes.take(10).toString();
    setState(() => _recognized = recognized);
  }
}

//Future<Image> convertYUV420toImageColor(CameraImage image) async {
//  try {
//    final int width = image.width;
//    final int height = image.height;
//    final int uvRowStride = image.planes[1].bytesPerRow;
//    final int uvPixelStride = image.planes[1].bytesPerPixel;
//
//    print("uvRowStride: " + uvRowStride.toString());
//    print("uvPixelStride: " + uvPixelStride.toString());
//
//    // imgLib -> Image package from https://pub.dartlang.org/packages/image
//    var img = imglib.Image(width, height); // Create Image buffer
//
//    // Fill image buffer with plane[0] from YUV420_888
//    for (int x = 0; x < width; x++) {
//      for (int y = 0; y < height; y++) {
//        final int uvIndex =
//            uvPixelStride * (x / 2).floor() + uvRowStride * (y / 2).floor();
//        final int index = y * width + x;
//
//        final yp = image.planes[0].bytes[index];
//        final up = image.planes[1].bytes[uvIndex];
//        final vp = image.planes[2].bytes[uvIndex];
//        // Calculate pixel color
//        int r = (yp + vp * 1436 / 1024 - 179).round().clamp(0, 255);
//        int g = (yp - up * 46549 / 131072 + 44 - vp * 93604 / 131072 + 91)
//            .round()
//            .clamp(0, 255);
//        int b = (yp + up * 1814 / 1024 - 227).round().clamp(0, 255);
//        // color: 0x FF  FF  FF  FF
//        //           A   B   G   R
//        img.data[index] = (0xFF << 24) | (b << 16) | (g << 8) | r;
//      }
//    }
//
//    imglib.PngEncoder pngEncoder = new imglib.PngEncoder(level: 0, filter: 0);
//    List<int> png = pngEncoder.encodeImage(img);
//    muteYUVProcessing = false;
//    return Image.memory(png);
//  } catch (e) {
//    print(">>>>>>>>>>>> ERROR:" + e.toString());
//  }
//  return null;
//}
