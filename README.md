# Barcode Scanner App

This is a Barcode Scanner application for Android, developed using Kotlin. The app uses the CameraX and Google ML Kit libraries to scan barcodes and provides various functionalities such as sharing, searching, and saving scanned barcodes.

## Features

- Scan barcodes using the device camera
- Share scanned barcodes via other applications
- Search scanned barcodes on the web
- Save scanned barcodes to local storage
- Broadcast scanned barcode data to other applications

## Requirements

- Android SDK 23 or higher

## Permissions

The app requires the following permissions:
- Camera: To scan barcodes

Make sure to grant the necessary permissions when prompted.

## Usage

### Scanning Barcodes

1. Open the app.
2. Point the camera at a barcode.
3. The app will automatically detect and scan the barcode.

### Sharing Barcodes

1. After scanning a barcode, tap the "Share" button.
2. Choose an app to share the barcode data.

### Searching Barcodes

1. After scanning a barcode, tap the "Search" button.
2. The app will perform a web search for the barcode data.

### Resetting the Scanner

Tap the "Reset" button to reset the scanner and scan a new barcode.

## Broadcast Receiver

The app broadcasts scanned barcode data using the action `ua.com.programmer.qrscanner.BARCODE_SCANNED`. Other applications can register a broadcast receiver to listen for this intent and handle the barcode data.

### Example Broadcast Receiver

To listen for the broadcast barcode data, you can create a `BroadcastReceiver` in your application. Here is an example:

   ```kotlin
   class BarcodeReceiver : BroadcastReceiver() {
       override fun onReceive(context: Context, intent: Intent) {
           if (intent.action == "ua.com.programmer.qrscanner.BARCODE_SCANNED") {
               val barcodeValue = intent.getStringExtra("BARCODE_VALUE")
               val barcodeFormat = intent.getStringExtra("BARCODE_FORMAT")
               // Handle the received barcode data
               Toast.makeText(context, "Scanned: $barcodeValue ($barcodeFormat)", Toast.LENGTH_LONG).show()
           }
       }
   }
   ```
To register the BroadcastReceiver in your AndroidManifest.xml, add the following:

   ```xml
   <receiver android:name=".BarcodeReceiver">
       <intent-filter>
           <action android:name="ua.com.programmer.qrscanner.BARCODE_SCANNED" />
       </intent-filter>
   </receiver>
   ```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

If you have any questions or feedback, feel free to reach out to the project maintainer at support@programmer.com.ua.
