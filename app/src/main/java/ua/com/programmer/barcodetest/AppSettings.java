package ua.com.programmer.barcodetest;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;
import java.util.UUID;

class AppSettings {

    private static SharedPreferences sharedPreferences;
    private final Context context;

    AppSettings(Context context){
        this.context = context;
        final String PREF_NAME = "ua.com.programmer.qrscanner.preference";
        sharedPreferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
    }

    int launchCounter(){
        final String START_COUNTER = "APP_START_COUNTER";
        int value = sharedPreferences.getInt(START_COUNTER,0);
        value++;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(START_COUNTER,value);
        editor.apply();
        return value;
    }

    String userID(){
        final String USER_ID = "USER_ID";
        String userID = sharedPreferences.getString(USER_ID,null);

        if (userID == null){
            userID = UUID.randomUUID().toString();
            long time = Calendar.getInstance().getTimeInMillis();
            userID = userID+"-"+time;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(USER_ID,userID);
            editor.apply();
        }

        return userID;
    }

    String versionName(){
        return BuildConfig.VERSION_NAME;
    }

    String firestorePassword(){
        try {
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            return bundle.getString("ua.com.programmer.qrscanner.default_user_pass");
        }catch (Exception e){
            Log.e("XBUG","meta-data: "+e.toString());
        }
        return null;
    }
}
