import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_mrz_scanner_example/camera_page.dart';
import 'package:permission_handler/permission_handler.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: FutureBuilder<Map<PermissionGroup, PermissionStatus>>(
        future:
            PermissionHandler().requestPermissions([PermissionGroup.camera]),
        builder: (context, snapshot) {
          if (snapshot.hasData &&
              snapshot.data[PermissionGroup.camera] ==
                  PermissionStatus.granted) {
            return CameraPage();
          }
          return Scaffold(
            body: Center(
              child: Column(
                children: const [
                  CircularProgressIndicator(),
                  Text('Awaiting for permissions')
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
