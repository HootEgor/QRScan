package ua.com.programmer.barcodetest

interface BarcodeFoundListener {
    fun onBarcodeFound(barCode: String?, format: Int)
    fun onCodeNotFound(error: String?)
}
