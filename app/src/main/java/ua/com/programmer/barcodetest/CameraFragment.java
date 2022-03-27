package ua.com.programmer.barcodetest;

import android.Manifest;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

public class CameraFragment extends Fragment implements Executor{

    private PreviewView cameraView;

    private TextView textView;
    private ListenableFuture<ProcessCameraProvider> cameraProvider;
    private ExecutorService cameraExecutor;

    private LinearLayout buttons;
    private SharedPreferences sharedPreferences;
    private String barcodeValue;
    private String barcodeFormat;
    private int barcodeFormatInt;
    private boolean flagSaved=false;
    private FloatingActionButton floatingActionButton;

    private Context mContext;
    private DBHelper dbHelper;

    private final Utils utils = new Utils();

    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = getContext();
        cameraExecutor = Executors.newSingleThreadExecutor();
        super.onCreate(savedInstanceState);
    }

    private void buttonsVisibilityTrigger(boolean visible){
        if (visible) {
            stopCamera();
            floatingActionButton.setVisibility(View.GONE);
            buttons.setVisibility(View.VISIBLE);
        }else{
            floatingActionButton.setVisibility(View.VISIBLE);
            buttons.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera,container,false);

        dbHelper = new DBHelper(mContext);

        floatingActionButton = view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(view1 -> buttonsVisibilityTrigger(true));

        buttons = view.findViewById(R.id.buttons);
        buttonsVisibilityTrigger(false);

        TextView btShare = view.findViewById(R.id.button_share);
        btShare.setOnClickListener((View v) -> {
            if (!barcodeValue.equals("")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,barcodeValue);
                intent.setType("text/plain");
                startActivity(intent);
            }
        });

        TextView btSearch = view.findViewById(R.id.button_search);
        btSearch.setOnClickListener((View v) -> {
            if (!barcodeValue.equals("")){
                try{
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY,barcodeValue);
                    startActivity(intent);
                }catch (ActivityNotFoundException noActivity){
                    Toast.makeText(mContext, R.string.no_activity_error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView btReset = view.findViewById(R.id.button_reset);
        btReset.setOnClickListener((View v) -> resetScanner());

        sharedPreferences = mContext.getSharedPreferences("ua.com.programmer.qrscanner.preference", Context.MODE_PRIVATE);
        //int scanMode = sharedPreferences.getInt("SCAN_MODE", Barcode.ALL_FORMATS);
        barcodeValue = sharedPreferences.getString("BARCODE","");
        barcodeFormat = sharedPreferences.getString("FORMAT","");

        textView = view.findViewById(R.id.txtContent);

        cameraView = view.findViewById(R.id.camera_view);
        cameraProvider = ProcessCameraProvider.getInstance(mContext);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }else {
                setupCamera();
            }
        }else {
            setupCamera();
        }

        return view;
    }

    private void setupCamera () {
        if (cameraProvider == null){
            textView.setText(R.string.error_camera);
            return;
        }

        BarcodeImageAnalyzer barcodeImageAnalyzer = new BarcodeImageAnalyzer(new BarcodeFoundListener() {
            @Override
            public void onBarcodeFound(String barCode, int format) {
                barcodeValue = barCode;
                barcodeFormatInt = format;
                barcodeFormat = new Utils().nameOfBarcodeFormat(format);
                showBarcodeValue();
            }

            @Override
            public void onCodeNotFound(String error) {
                utils.debug("on code not found: "+error);
            }
        });

        cameraProvider.addListener(() -> {
            try {
                ProcessCameraProvider provider = cameraProvider.get();
                Preview preview = new Preview.Builder()
                        .build();
                preview.setSurfaceProvider(cameraView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, barcodeImageAnalyzer);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                try{
                    provider.unbindAll();
                    provider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
                }catch (Exception e){
                    utils.debug("bind provider error; "+e.getMessage());
                }

            } catch (Exception e) {
                utils.debug("Error starting camera " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(mContext));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @Nonnull String[] permissions,@Nonnull int[] grantResults) {
        if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            resetScanner();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void stopCamera(){
        try {
            cameraProvider.get().unbindAll();
        }catch (Exception e){
            utils.debug("Unbinding camera provider "+e.getMessage());
        }
    }

    private void showBarcodeValue(){

        stopCamera();

        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION,100);
        tg.startTone(ToneGenerator.TONE_PROP_BEEP);

        String barcodeText = barcodeFormat + "\n" + barcodeValue;
        textView.setText(barcodeText);

        saveState();
        buttonsVisibilityTrigger(true);
    }

    private void saveState(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("BARCODE",barcodeValue);
        editor.putString("FORMAT",barcodeFormat);
        editor.apply();

        if (!flagSaved){
            if (!barcodeValue.equals("")&&!barcodeFormat.equals("")){
                Date currentDate = new Date();
                long eventTime = Integer.parseInt(String.format("%ts",currentDate));
                String eventDate = String.format(Locale.getDefault(),"%td-%tm-%tY",currentDate,currentDate,currentDate);

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("time",eventTime);
                cv.put("date",eventDate);
                cv.put("codeType",barcodeFormatInt);
                cv.put("codeValue",barcodeValue);
                db.insert("history",null,cv);
                flagSaved = true;
            }
        }

    }

    private void resetScanner(){
        flagSaved = false;
        barcodeValue = "";
        barcodeFormat = "";
        saveState();
        try {
            assert getFragmentManager() != null;
            getFragmentManager().beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
        }catch (NullPointerException ex){
            Toast.makeText(mContext, R.string.hint_try_to_reset, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void execute(Runnable command) {
        utils.debug("executor...");
    }
}
