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
          onParsed: (result) {
            print(result.documentType); // 'P'
            print(result.countryCode); // 'UTO'
            print(result.surnames); // 'ERIKSSON'
            print(result.givenNames); // 'ANNA MARIA'
            print(result.documentNumber); // 'L898902C3'
            print(result.nationalityCountryCode); // 'UTO'
            print(result.birthDate); // DateTime(1974, 08, 12)
            print(result.sex); // Sex.female
            print(result.expiryDate); // DateTime(2012, 04, 15)
            print(result.personalNumber); // 'ZE184226B'
            print(result.personalNumber2); // null
          },
          onError: (error) => print(error),
        ));
  }
}
