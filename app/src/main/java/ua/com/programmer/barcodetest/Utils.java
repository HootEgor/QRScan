package ua.com.programmer.barcodetest;

import android.util.Log;

import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.Calendar;
import java.util.GregorianCalendar;

class Utils {

    long dateBeginOfToday(){
        Calendar calendar = new GregorianCalendar();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DATE);
        calendar.set(currentYear,currentMonth,currentDay,0,0);
        return calendar.getTimeInMillis()/1000;
    }

    long dateBeginShiftDate(){
        Calendar calendar = new GregorianCalendar();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DATE);
        calendar.set(currentYear,currentMonth,currentDay,0,0);
        return calendar.getTimeInMillis()/1000 - 86400*30;
    }

    String nameOfBarcodeFormat(int format){
        String name;
        switch (format) {
            case Barcode.FORMAT_QR_CODE:
                name = "QR code";
                break;
            case Barcode.FORMAT_DATA_MATRIX:
                name = "Data Matrix";
                break;
            case Barcode.FORMAT_EAN_13:
                name = "EAN13";
                break;
            case Barcode.FORMAT_CODE_128:
                name = "Code128";
                break;
            case Barcode.FORMAT_UPC_A:
                name = "UPC A";
                break;
            case Barcode.TYPE_CALENDAR_EVENT:
                name = "Calendar event";
                break;
            case Barcode.TYPE_URL:
                name = "URL";
                break;
            default:
                name = "?";
        }
        return name;
    }

    public void debug(String text){
        Log.d("XBUG", text);
    }
}
