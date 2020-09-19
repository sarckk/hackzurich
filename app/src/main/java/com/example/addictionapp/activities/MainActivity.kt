package com.example.addictionapp.activities

import android.Manifest
import android.app.PendingIntent
import android.app.usage.UsageEvents
import android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED
import android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.addictionapp.R
import com.example.addictionapp.utils.LocationBroadcastReceiver
import com.example.addictionapp.utils.MessageEvent
import com.huawei.hms.location.*
import org.greenrobot.eventbus.EventBus


class MainActivity : AppCompatActivity() {

    private lateinit var usm: UsageStatsManager;
    private var lastCheck = 0L;
    private var currentlyOnFacebook = false;
    private var activatedAction = false;
    private var onFacebookSince = 0L;

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)

        val activityIdentificationService = ActivityIdentification.getService(this)
        val pendingIntent = getPendingIntent()

        activityIdentificationService.createActivityIdentificationUpdates(5000, pendingIntent)
            .addOnSuccessListener {
                Log.i(
                    "test",
                    "createActivityIdentificationUpdates onSuccess"
                )
            }
            .addOnFailureListener { e ->
                Log.e(
                    "test",
                    "createActivityIdentificationUpdates onFailure:" + e.message
                )
            }

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val mLocationCallback: LocationCallback
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Process the location callback result.
            }
        }

        fusedLocationProviderClient
            .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
            .addOnSuccessListener {
                // Processing when the API call is successful.
            }
            .addOnFailureListener {
                // Processing when the API call fails.
            }


        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this,
                REQUIRED_PERMISSIONS, 1)
        }

        usm = getSystemService(UsageStatsManager::class.java)

        lastCheck = System.currentTimeMillis(); //5 seconds ago

        for(i in 1..100) {
            Handler().postDelayed(this::pollingLoop, (1000L*i))
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        // The LocationBroadcastReceiver class is a customized static broadcast class. For details about the implementation, please refer to the sample code.
        val intent = Intent(this, LocationBroadcastReceiver::class.java)
        intent.action = LocationBroadcastReceiver.ACTION_PROCESS_LOCATION
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun pollingLoop() {
        if(currentlyOnFacebook) {
            var duration = (System.currentTimeMillis() - onFacebookSince) / 1000;
            Log.i("test", "On facebook currently for ${duration}s")
            if(duration > 2 && !activatedAction) {
                activatedAction = true
                EventBus.getDefault().post(
                    MessageEvent(
                        false
                    )
                );
            }
        }

        queryNewEvents()
    }

    private fun queryNewEvents() {
        Log.i("test", "Querying for events...")
        var currentCheck = System.currentTimeMillis();
        val evts = usm.queryEvents(lastCheck, currentCheck) //One day
        lastCheck = currentCheck

        val evt = UsageEvents.Event();

        var lastResumeTime = System.currentTimeMillis();
        while (evts.hasNextEvent()) {
            evts.getNextEvent(evt);
            if(evt.packageName == "com.facebook.katana" && (evt.eventType == ACTIVITY_PAUSED || evt.eventType == ACTIVITY_RESUMED)) {
                if (evt.eventType == ACTIVITY_RESUMED) {
                    currentlyOnFacebook = true;
                    onFacebookSince = evt.timeStamp
                }
                if (evt.eventType == ACTIVITY_PAUSED) {
                    currentlyOnFacebook = false;
                    var duration = (evt.timeStamp - onFacebookSince) / 1000
                    if (duration > 1) {
                        Log.i("test", "Closed facebook after ${duration}s")
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        @RequiresApi(Build.VERSION_CODES.Q)
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
}
