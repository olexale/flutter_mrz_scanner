import 'package:flutter/material.dart';
import 'package:flutter_mrz_scanner_example/capture_page.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: CapturePage(),
    );
  }
}
