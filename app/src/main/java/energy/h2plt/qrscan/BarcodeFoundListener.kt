package energy.h2plt.qrscan

interface BarcodeFoundListener {
    fun onBarcodeFound(barCode: String?, format: Int)
    fun onCodeNotFound(error: String?)
}
