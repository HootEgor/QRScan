package ua.com.programmer.barcodetest;

import android.annotation.SuppressLint;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

public class BarcodeImageAnalyzer implements ImageAnalysis.Analyzer {

    private final BarcodeFoundListener listener;
    private final BarcodeScanner scanner;

    public BarcodeImageAnalyzer(BarcodeFoundListener barcodeFoundListener){
        listener = barcodeFoundListener;
//        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
//                //.setBarcodeFormats(com.google.mlkit.vision.barcode.Barcode.FORMAT_EAN_13, com.google.mlkit.vision.barcode.Barcode.FORMAT_QR_CODE)
////                .setZoomSuggestionOptions(
////                        new ZoomSuggestionOptions.Builder(zoomCallback)
////                                .setMaxSupportedZoomRatio(maxSupportedZoomRatio)
////                                .build()) // Optional
//                .build();
        scanner = BarcodeScanning.getClient();
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        @SuppressLint({"UnsafeExperimentalUsageError", "UnsafeOptInUsageError"}) Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            scanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            listener.onBarcodeFound(barcode.getRawValue(),barcode.getFormat());
                        }
                    })
                    .addOnFailureListener(e -> listener.onCodeNotFound(e.getMessage()))
                    .addOnCompleteListener(task -> {
                        imageProxy.close();
                        mediaImage.close();
                    });
        }else {
            imageProxy.close();
        }
    }
}
