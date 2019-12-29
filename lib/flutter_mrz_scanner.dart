import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:mrz_parser/mrz_parser.dart';

class FlutterMrzScanner {
  static const MethodChannel _channel = MethodChannel('flutter_mrz_scanner');

  Future<String> recognize(File image) {
    final List<int> bytes = image.readAsBytesSync();
    final base64 = base64Encode(bytes);
    return _channel.invokeMethod('recognize', {'image': base64});
  }
}

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
      return UiKitView(
        viewType: 'mrzscanner',
        onPlatformViewCreated: (int id) => onPlatformViewCreated(id),
        creationParamsCodec: const StandardMessageCodec(),
      );
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
