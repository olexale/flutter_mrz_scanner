import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const CircularProgressIndicator(),
              const Padding(
                padding: EdgeInsets.all(8.0),
                child: Text('Awaiting for permissions'),
              ),
              Text('Current status: toString()}'),
            ],
          ),
        ),
      ),
    );
  }
}
