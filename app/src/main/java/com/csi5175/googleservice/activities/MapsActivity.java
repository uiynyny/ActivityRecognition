package com.csi5175.googleservice.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.csi5175.googleservice.Constants;
import com.csi5175.googleservice.R;
import com.csi5175.googleservice.database.AppDatabase;
import com.csi5175.googleservice.database.UserActivityState;
import com.csi5175.googleservice.services.BackgroundDetectedActivitiesService;
import com.csi5175.googleservice.services.BackgroundMusicService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    //debug use
    private String TAG = MapsActivity.class.getSimpleName();
    //element in ui
    private TextView textView;
    private ImageView imageView;
    //broadcast parameter
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter mIntentFilter;
    //embed map parameter
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private final int DEFAULT_ZOOM = 15;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private FollowMeLocationSource followMeLocationSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        imageView = findViewById(R.id.main_image);
        textView = findViewById(R.id.main_text);
        Button btnStartTrack = findViewById(R.id.btn_start_tracking);
        Button btnStopTrack = findViewById(R.id.btn_stop_tracking);
        btnStartTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrack();
            }
        });
        btnStopTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTrack();
                stopBackgroundMusic();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        followMeLocationSource = new FollowMeLocationSource();

        broadcastReceiver = new ActivityReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.TRANSITIONS_RECEIVER_ACTION);
        mIntentFilter.addAction(Constants.BROADCAST_DETECTED_ACTIVITY);
        checkGooglePlayServicesAvailable(this);
    }

    public boolean checkGooglePlayServicesAvailable(Activity activity) {
        int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(activity);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result))
                googleAPI.getErrorDialog(activity, result, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        followMeLocationSource.getBestProvider();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //button click events
    private void stopTrack() {
        Intent intent = new Intent(this, BackgroundDetectedActivitiesService.class);
        stopService(intent);

    }

    private void startTrack() {
        Intent intent = new Intent(this, BackgroundDetectedActivitiesService.class);
        startService(intent);
    }

    //button or intent events
    private void startBackgroundMusic() {
        Intent intent = new Intent(this, BackgroundMusicService.class);
        startService(intent);
        Toast.makeText(this, "Detected Activity, Playing music", Toast.LENGTH_LONG).show();
    }

    private void stopBackgroundMusic() {
        Intent intent = new Intent(this, BackgroundMusicService.class);
        stopService(intent);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        requestLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        mMap.setLocationSource(followMeLocationSource);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()),
                                DEFAULT_ZOOM));
                return true;
            }
        });
    }

    //helpers for onMapReady
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (mMap == null)
            return;
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                requestLocationPermission();
            }
        } catch (SecurityException se) {
            Log.e("EXCEPTION", se.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
        }
        updateLocationUI();
    }

    private class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Receiver:  " + intent.getAction());
            if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                int type = intent.getIntExtra("type", -1);
                int confidence = intent.getIntExtra("confidence", 0);
                handleUserActivity(type, confidence);
            }
            if (intent.getAction().equals(Constants.TRANSITIONS_RECEIVER_ACTION)) {
                int type = intent.getIntExtra("type", -1);
                int transition = intent.getIntExtra("transition", -1);
                handleUserTransition(type, transition);
            }
        }

        //Activity recognition translate
        private void handleUserActivity(int type, int confidence) {
            String label = getString(R.string.activity_unknown);
            int icon = R.drawable.ic_unknown;

            switch (type) {
                case DetectedActivity.IN_VEHICLE: {
                    label = getString(R.string.activity_in_vehicle);
                    icon = R.drawable.ic_driving;
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    label = getString(R.string.activity_on_bicycle);
                    icon = R.drawable.ic_on_bicycle;
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    label = getString(R.string.activity_on_foot);
                    icon = R.drawable.ic_walking;
                    break;
                }
                case DetectedActivity.STILL: {
                    label = getString(R.string.activity_still);
                    icon = R.drawable.ic_still;
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    label = getString(R.string.activity_unknown);
                    icon = R.drawable.ic_unknown;
                    break;
                }
                case DetectedActivity.TILTING: {
                    label = getString(R.string.activity_tilting);
                    icon = R.drawable.ic_tilting;
                    break;
                }
                case DetectedActivity.WALKING: {
                    label = getString(R.string.activity_walking);
                    icon = R.drawable.ic_walking;
                    break;
                }
                case DetectedActivity.RUNNING: {
                    label = getString(R.string.activity_running);
                    icon = R.drawable.ic_running;
                    break;
                }
            }

            Log.i(TAG, "User activity: " + type + "-" + label + ", Confidence: " + confidence);

            if (confidence > Constants.CONFIDENCE) {
                textView.setText(MessageFormat.format("{0} {1}", label, confidence));
                imageView.setImageResource(icon);
            } else {
                textView.setText(MessageFormat.format("{0} {1}", label, confidence));
                imageView.setImageResource(icon);
            }
        }

        //Activity transition translate
        private String toTransitionType(int transitionType) {
            switch (transitionType) {
                case DetectedActivity.IN_VEHICLE:
                    return "IN_VEHICLE";
                case DetectedActivity.ON_BICYCLE:
                    return "ON_BICYCLE";
                case DetectedActivity.ON_FOOT:
                    return "ON_FOOT";
                case DetectedActivity.RUNNING:
                    return "RUNNING";
                case DetectedActivity.STILL:
                    return "STILL";
                case DetectedActivity.TILTING:
                    return "TILTING";
                case DetectedActivity.WALKING:
                    return "WALKING";
                case DetectedActivity.UNKNOWN:
                    return "UNKNOWN";
            }
            return "NULL";
        }

        private String toActivityString(int activityType) {
            switch (activityType) {
                case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                    return "ENTER";
                case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                    return "EXIT";
            }
            return "NULL";
        }

        private void handleUserTransition(int type, int transition) {
            String label = toTransitionType(type);
            String trans = toActivityString(transition);
            String time = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
            String mess = "Transition: "
                    + label + " (" + trans + ")" + "   "
                    + time;
            Log.i(TAG, mess);
            //Toast.makeText(getApplicationContext(), mess, Toast.LENGTH_LONG).show();
            if (transition == 0) {
                insertActivity(label, time);
                if (type == 7 || type == 8) {
                    startBackgroundMusic();
                }
            } else if (transition == 1) {
                if (type == 7 || type == 8) {
                    stopBackgroundMusic();
                    notifyUserChangeOfActivity(time);
                }
            }
        }
    }

    //notify user the duration of activity
    private void notifyUserChangeOfActivity(String time) {
        UserActivityState lastActivity = getLastActivity();
        long td = 0;
        try {
            Date now = new SimpleDateFormat("HH:mm:ss", Locale.US).parse(time);
            Date last = new SimpleDateFormat("HH:mm:ss", Locale.US).parse(lastActivity.getTime());
            td = (now.getTime() - last.getTime()) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long minute = td / 60;
        long second = td % 60;
        String message = MessageFormat.format("You have just {0} for {1} min {2} s", lastActivity.getType(), minute, second);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    //db insertion operation
    private void insertActivity(String label, String time) {
        UserActivityState uas = new UserActivityState();
        uas.setType(label);
        uas.setTime(time);
        AppDatabase.getInstance(getApplicationContext()).activityDao().insert(uas);
    }

    //db select operation
    private UserActivityState getLastActivity() {
        return AppDatabase.getInstance(getApplicationContext()).activityDao().findLastId();
    }

    private class FollowMeLocationSource implements LocationListener, LocationSource {

        private final Criteria criteria;
        private OnLocationChangedListener onLocationChangedListener;
        private LocationManager locationManager;
        private String bestProvider;

        public FollowMeLocationSource() {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            criteria.setAltitudeRequired(true);
            criteria.setBearingRequired(true);
            criteria.setSpeedRequired(true);
            criteria.setCostAllowed(true);

        }

        public void getBestProvider() {
            bestProvider = locationManager.getBestProvider(criteria,true);
        }

        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            this.onLocationChangedListener = onLocationChangedListener;
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(bestProvider, 1000 * 10, 10, this);
        }

        @Override
        public void deactivate() {
            locationManager.removeUpdates(this);
        }

        @Override
        public void onLocationChanged(Location location) {
            if (onLocationChangedListener != null) {
                onLocationChangedListener.onLocationChanged(location);
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}

