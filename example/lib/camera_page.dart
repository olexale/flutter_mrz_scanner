import 'package:flutter/material.dart';
import 'package:flutter_mrz_scanner/flutter_mrz_scanner.dart';

class CameraPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text('Camera'),
        ),
        body: MRZScanner(
          onParsed: (str) => print(str),
          onError: (error) => print(error),
        ));
  }
}
