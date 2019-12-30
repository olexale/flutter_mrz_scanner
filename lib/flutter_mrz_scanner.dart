import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:mrz_parser/mrz_parser.dart';

class MRZScanner extends StatefulWidget {
  const MRZScanner({Key key, this.onParsed, this.onError}) : super(key: key);

  final void Function(MRZResult mrz) onParsed;
  final void Function(String text) onError;

  @override
  _MRZScannerState createState() => _MRZScannerState();
}

class _MRZScannerState extends State<MRZScanner> {
  MethodChannel _channel;

  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform == TargetPlatform.iOS) {
      return Stack(children: [
        UiKitView(
          viewType: 'mrzscanner',
          onPlatformViewCreated: (int id) => onPlatformViewCreated(id),
          creationParamsCodec: const StandardMessageCodec(),
        ),
        ClipPath(
          clipper: _DocumentClipper(),
          child: Container(
            foregroundDecoration: const BoxDecoration(
              color: Color.fromRGBO(0, 0, 0, 0.45),
            ),
          ),
        ),
      ]);
    }

    return Text('$defaultTargetPlatform is not supported by this plugin');
  }

  void onPlatformViewCreated(int id) {
    _channel = MethodChannel('mrzscanner_$id');
    _channel.setMethodCallHandler(_platformCallHandler);
  }

  Future<void> _platformCallHandler(MethodCall call) {
    switch (call.method) {
      case 'onError':
        if (widget.onError != null) {
          widget.onError(call.arguments);
        }
        break;
      case 'onParsed':
        if (widget.onParsed != null) {
          final lines = _splitRecognized(call.arguments);
          if (lines.isNotEmpty) {
            final result = MRZParser.parse(lines);
            if (result != null) {
              widget.onParsed(result);
            }
          }
        }
        break;
    }
    return Future.value();
  }

  List<String> _splitRecognized(String recognizedText) {
    final mrzString = recognizedText.replaceAll(' ', '');
    return mrzString.split('\n').where((s) => s.isNotEmpty).toList();
  }
}

class _DocumentClipper extends CustomClipper<Path> {
  final documentFrameRatio =
      1.42; // Passport's size (ISO/IEC 7810 ID-3) is 125mm × 88mm

  @override
  Path getClip(Size size) {
    double width, height;
    if (size.height > size.width) {
      width = size.width * 0.9;
      height = width / documentFrameRatio;
    } else {
      height = size.height * 0.75;
      width = height * documentFrameRatio;
    }
    final topOffset = (size.height - height) / 2;
    final leftOffset = (size.width - width) / 2;

    final rect = RRect.fromLTRBR(leftOffset, topOffset, leftOffset + width,
        topOffset + height, const Radius.circular(8));

    return Path()
      ..addRRect(rect)
      ..addRect(Rect.fromLTWH(0.0, 0.0, size.width, size.height))
      ..fillType = PathFillType.evenOdd;
  }

  @override
  bool shouldReclip(_DocumentClipper oldClipper) => false;
}
