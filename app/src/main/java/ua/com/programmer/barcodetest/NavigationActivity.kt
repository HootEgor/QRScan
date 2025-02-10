package ua.com.programmer.barcodetest

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class NavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var backPressedTime: Long = 0
    private var appSettings: AppSettings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cleanDatabase()

        setContentView(R.layout.activity_navigation)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        appSettings = AppSettings(this)
        if (appSettings!!.launchCounter() == 14) {
            showRateDialog()
        }

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.nav_camera)
        attachFragment(CameraFragment::class.java)

        val headerView = navigationView.getHeaderView(0)
        val versionText = headerView.findViewById<TextView>(R.id.version)
        if (versionText != null) {
            val version = "v" + appSettings!!.versionName() + " (" + appSettings!!.userID()
                .substring(0, 8) + ")"
            versionText.text = version
        }

        firebaseAuthentication()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed()
            } else {
                Toast.makeText(this, R.string.hint_press_back, Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //getMenuInflater().inflate(R.menu.navigation, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.nav_camera) {
            attachFragment(CameraFragment::class.java)
        } else if (id == R.id.nav_history) {
            attachFragment(HistoryFragment::class.java)
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun attachFragment(fragmentClass: Class<*>) {
        var fragment: Fragment? = null
        try {
            fragment = fragmentClass.newInstance() as Fragment
        } catch (ex: Exception) {
            Toast.makeText(this, R.string.no_activity_error, Toast.LENGTH_SHORT).show()
        }
        if (fragment != null) {
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
        }
    }

    override fun onDestroy() {
        val sharedPreferences =
            getSharedPreferences("ua.com.programmer.qrscanner.preference", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("BARCODE", "")
        editor.putString("FORMAT", "")
        editor.apply()
        super.onDestroy()
    }

    private fun showRateDialog() {
        val builder = AlertDialog.Builder(this)
            .setTitle(R.string.rate_app_header)
            .setMessage(R.string.rate_app_text)
            .setPositiveButton(R.string.rate_app_OK) { _: DialogInterface?, _: Int ->
                val sharedPreferences = getSharedPreferences(
                    "ua.com.programmer.qrscanner.preference",
                    MODE_PRIVATE
                )
                val editor = sharedPreferences.edit()
                editor.putInt(START_COUNTER, 15)
                editor.apply()

                var link = "market://details?id="
                try {
                    // play market available
                    packageManager.getPackageInfo("com.android.vending", 0)
                    // not available
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                    // should use browser
                    link = "https://play.google.com/store/apps/details?id="
                }
                // starts external action
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(link + packageName)
                    )
                )
            }
            .setNegativeButton(R.string.dialog_cancel) { dialogInterface: DialogInterface?, i: Int ->
                //will ask to rate next time
                val sharedPreferences = getSharedPreferences(
                    "ua.com.programmer.qrscanner.preference",
                    MODE_PRIVATE
                )
                val editor = sharedPreferences.edit()
                editor.putInt(START_COUNTER, 0)
                editor.apply()
            }
        builder.show()
    }

    private fun cleanDatabase() {
        val utils = Utils()
        val db = DBHelper(this).writableDatabase
        if (db.isOpen) {
            try {
                db.beginTransaction()
                val query = "DELETE FROM history WHERE time<?"
                val statement = db.compileStatement(query)
                statement.bindLong(1, utils.dateBeginShiftDate())
                statement.executeUpdateDelete()
                db.setTransactionSuccessful()
            } catch (ex: Exception) {
                Log.e("XBUG", "Purge history error. $ex")
            } finally {
                db.endTransaction()
            }
        }
        db.close()
    }

    private fun firebaseAuthentication() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser
        if (user == null) {
            val pass = appSettings!!.firestorePassword()
            if (pass != null) {
                firebaseAuth.signInWithEmailAndPassword("support@programmer.com.ua", pass)
                    .addOnCompleteListener(
                        this
                    ) { task: Task<AuthResult?> ->
                        if (task.isSuccessful) {
                            userInfo()
                        }
                    }
            }
        } else {
            userInfo()
        }
    }

    private fun userInfo() {
        val document: MutableMap<String, Any> = HashMap()
        document["loginTime"] = Date()
        document["userID"] = appSettings!!.userID()
        document["appVersion"] = appSettings!!.versionName()
        document["launchCounter"] = appSettings!!.launchCounter()

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users")
            .document(appSettings!!.userID())
            .set(document)
    }

    companion object {
        private const val START_COUNTER = "APP_START_COUNTER"
    }
}
