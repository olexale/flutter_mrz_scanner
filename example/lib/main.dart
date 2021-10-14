import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_mrz_scanner_example/camera_page.dart';
import 'package:permission_handler/permission_handler.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: FutureBuilder<PermissionStatus>(
        future: Permission.camera.request(),
        builder: (context, snapshot) {
          if (snapshot.hasData && snapshot.data == PermissionStatus.granted) {
            return CameraPage();
          }
          if (snapshot.data == PermissionStatus.permanentlyDenied) {
            // The user opted to never again see the permission request dialog for this
            // app. The only way to change the permission's status now is to let the
            // user manually enable it in the system settings.
            openAppSettings();
          }
          return Scaffold(
            body: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const CircularProgressIndicator(),
                  const Padding(
                    padding: EdgeInsets.all(8.0),
                    child: Text('Awaiting for permissions'),
                  ),
                  Text('Current status: ${snapshot.data?.toString()}'),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
