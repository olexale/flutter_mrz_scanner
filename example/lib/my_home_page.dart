import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_mrz_scanner/flutter_mrz_scanner.dart';
import 'package:image_picker/image_picker.dart';

class MyHomePage extends StatefulWidget {
  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  File _image;
  String _recognized;

  Future getImage() async {
    var image = await ImagePicker.pickImage(source: ImageSource.gallery);
    var recognized = await FlutterMrzScanner.recognizeFile(image);

    setState(() {
      _image = image;
      _recognized = recognized;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Image Picker Example'),
      ),
      body: Center(
          child: _image == null
              ? const Text('No image selected.')
              : Column(
                  children: <Widget>[
                    Text(_recognized),
                    Image.file(_image),
                  ],
                )),
      floatingActionButton: FloatingActionButton(
        onPressed: getImage,
        tooltip: 'Pick Image',
        child: Icon(Icons.add_a_photo),
      ),
    );
  }
}
