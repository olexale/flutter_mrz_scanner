# flutter_mrz_scanner

Scan MRZ (Machine Readable Zone) from identity documents (passport, id, visa) using iOS and Android. Heavily insipred by [QKMRZScanner](https://github.com/Mattijah/QKMRZScanner).

### Supported formats:
* TD1
* TD2
* TD3
* MRV-A
* MRV-B

## Usage

### Import the package
Add to `pubspec.yaml`
```yaml
dependencies:
  flutter_mrz_scanner: ^0.7.0
```
### Use the widget
```dart
...
MRZScanner(
  onParsed: (result) {
    print(result.documentType);
    print(result.countryCode);
    print(result.surnames);
    print(result.givenNames);
    print(result.documentNumber);
    print(result.nationalityCountryCode);
    print(result.birthDate);
    print(result.sex);
    print(result.expiryDate);
    print(result.personalNumber);
    print(result.personalNumber2);
  },
  onError: (error) => print(error),
),
...
```
Refer to `example` project for the complete app sample.

## Acknowledgements
* [Anna Domashych](https://github.com/foxanna) for helping with [mrz_parser](https://github.com/olexale/mrz_parser) implementation in Dart
* [Anton Derevyanko](https://github.com/antonderevyanko) for hours of Android-related discussions
* [Mattijah](https://github.com/Mattijah) for beautiful [QKMRZScanner](https://github.com/Mattijah/QKMRZScanner) library

## License
`flutter_mrz_scanner` is released under a [MIT License](https://opensource.org/licenses/MIT). See `LICENSE` for details.