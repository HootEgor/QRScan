package ua.com.programmer.barcodetest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private long backPressedTime;
    private static final String START_COUNTER = "APP_START_COUNTER";

    private AppSettings appSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cleanDatabase();

        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        appSettings = new AppSettings(this);
        if (appSettings.launchCounter() == 14){
            showRateDialog();
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_camera);
        attachFragment(CameraFragment.class);



        View headerView = navigationView.getHeaderView(0);
        TextView versionText = headerView.findViewById(R.id.version);
        if (versionText != null){
            String version = "v"+appSettings.versionName()+" ("+appSettings.userID().substring(0,8)+")";
            versionText.setText(version);
        }

        firebaseAuthentication();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (backPressedTime+2000>System.currentTimeMillis()) {
                super.onBackPressed();
            }else {
                Toast.makeText(this, R.string.hint_press_back, Toast.LENGTH_SHORT).show();
                backPressedTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            attachFragment(CameraFragment.class);
        } else if (id == R.id.nav_history) {
            attachFragment(HistoryFragment.class);
//        } else if (id == R.id.nav_settings) {
//            attachFragment(SettingsFragment.class);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void attachFragment(Class fragmentClass){
        Fragment fragment=null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        }catch (Exception ex){
            Toast.makeText(this, R.string.no_activity_error, Toast.LENGTH_SHORT).show();
        }
        if (fragment!=null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container,fragment).commit();
        }
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = getSharedPreferences("ua.com.programmer.qrscanner.preference",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("BARCODE","");
        editor.putString("FORMAT","");
        editor.apply();
        super.onDestroy();
    }

    public void showRateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.rate_app_header)
                .setMessage(R.string.rate_app_text)
                .setPositiveButton(R.string.rate_app_OK, (DialogInterface dialog, int which) -> {

                        SharedPreferences sharedPreferences = getSharedPreferences("ua.com.programmer.qrscanner.preference",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(START_COUNTER,15);
                        editor.apply();

                        String link = "market://details?id=";
                        try {
                            // play market available
                            getPackageManager().getPackageInfo("com.android.vending", 0);
                            // not available
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                            // should use browser
                            link = "https://play.google.com/store/apps/details?id=";
                        }
                        // starts external action
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(link + getPackageName())));
                })
                .setNegativeButton(R.string.dialog_cancel, (DialogInterface dialogInterface, int i) -> {
                        //will ask to rate next time
                        SharedPreferences sharedPreferences = getSharedPreferences("ua.com.programmer.qrscanner.preference",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(START_COUNTER,0);
                        editor.apply();
                });
        builder.show();
    }

    private void cleanDatabase(){
        Utils utils = new Utils();
        SQLiteDatabase db = new DBHelper(this).getWritableDatabase();
        if (db.isOpen()){
            try {
                db.beginTransaction();
                String query = "DELETE FROM history WHERE time<?";
                SQLiteStatement statement = db.compileStatement(query);
                statement.bindLong(1, utils.dateBeginShiftDate());
                statement.executeUpdateDelete();
                db.setTransactionSuccessful();
            }catch (Exception ex) {
                Log.e("XBUG","Purge history error. "+ex);
            }finally {
                db.endTransaction();
            }
        }
        db.close();
    }

    private void firebaseAuthentication(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            String pass = appSettings.firestorePassword();
            if (pass != null) {
                firebaseAuth.signInWithEmailAndPassword("support@programmer.com.ua", pass)
                        .addOnCompleteListener(this, (@NonNull Task<AuthResult> task) -> {
                            if (task.isSuccessful()) {
                                userInfo();
                            }
                        });
            }
        }else {
            userInfo();
        }
    }

    private void userInfo(){
        Map<String,Object> document = new HashMap<>();
        document.put("loginTime",new Date());
        document.put("userID",appSettings.userID());
        document.put("appVersion",appSettings.versionName());
        document.put("launchCounter",appSettings.launchCounter());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .document(appSettings.userID())
                .set(document);
    }
}
