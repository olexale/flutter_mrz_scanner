import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';

class FlutterMrzScanner {
  static const MethodChannel _channel = MethodChannel('flutter_mrz_scanner');

  Future<String> recognize(File image) {
    final List<int> bytes = image.readAsBytesSync();
    final base64 = base64Encode(bytes);
    return _channel.invokeMethod('recognize', {'image': base64});
  }
}
