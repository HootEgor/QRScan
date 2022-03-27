package ua.com.programmer.barcodetest;

public interface BarcodeFoundListener {
    void onBarcodeFound(String barCode, int format);
    void onCodeNotFound(String error);
}
