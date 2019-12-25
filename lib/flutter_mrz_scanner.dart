import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/services.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';

class FlutterMrzScanner {
  static const String TESS_DATA_PATH = 'assets/tessdata';
  static const MethodChannel _channel = MethodChannel('flutter_mrz_scanner');

  Future<void> init() => _channel.invokeMethod<void>('init');

  Future<String> recognize(File image) async {
    final List<int> bytes = image.readAsBytesSync();
    final base64 = base64Encode(bytes);
    final String tessData = await _loadTessData();
    return await _channel.invokeMethod('recognize', {
      'image': base64,
      'tessData': tessData,
    });
  }

  static Future<String> _loadTessData() async {
    final Directory appDirectory = await getApplicationDocumentsDirectory();
    final String tessdataDirectory = join(appDirectory.path, 'tessdata');

    if (!await Directory(tessdataDirectory).exists()) {
      await Directory(tessdataDirectory).create();
    }
    await _copyTessDataToAppDocumentsDirectory(tessdataDirectory);
    return appDirectory.path;
  }

  static Future _copyTessDataToAppDocumentsDirectory(
      String tessdataDirectory) async {
    const file = 'ocrb.traineddata';
    // final String config = await rootBundle.loadString(TESS_DATA_CONFIG);
    // Map<String, dynamic> files = jsonDecode(config);
    // for (var file in files["files"]) {
    if (!await File('$tessdataDirectory/$file').exists()) {
      final ByteData data = await rootBundle.load('assets/tessdata/$file');
      final Uint8List bytes = data.buffer.asUint8List(
        data.offsetInBytes,
        data.lengthInBytes,
      );
      await File('$tessdataDirectory/$file').writeAsBytes(bytes);
    }
    // }
  }
}
