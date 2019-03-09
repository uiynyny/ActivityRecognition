/*
 * Yan Zhang
 * 300052103
 * entrance of the app, inflate the main view of the app.
 * */
package com.csi5175.googleservice.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.DetectedActivity;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
 * Main structure of the app comes from the following tutorial
 * https://www.androidhive.info/2017/12/android-user-activity-recognition-still-walking-running-driving-etc/
 * */
public class MainActivity extends AppCompatActivity {
    //DEBUG only
    private static final String TAG = MainActivity.class.getSimpleName();

    //element in ui
    private TextView textView;
    private ImageView imageView;
    //broadcast parameter
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter mIntentFilter;

    //inflate the layout and initiate receiver, buttons and map fragments
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        initBroadcastReceiver();
        openFragment(new MyMapsFragment());
    }

    //initiate receiver
    private void initBroadcastReceiver() {
        broadcastReceiver = new ActivityReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.TRANSITIONS_RECEIVER_ACTION);
        mIntentFilter.addAction(Constants.BROADCAST_DETECTED_ACTIVITY);
    }

    //handle map fragments
    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.map_holder, fragment)
                .addToBackStack(null)
                .commit();
    }

    //reregister receiver
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, mIntentFilter);
    }

    //unregister receiver
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    //super result handle
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "Main result back");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    //intent triggered events
    private void startBackgroundMusic() {
        Intent intent = new Intent(this, BackgroundMusicService.class);
        startService(intent);
        Toast.makeText(this, "Detected Activity, Playing music", Toast.LENGTH_LONG).show();
    }

    private void stopBackgroundMusic() {
        Intent intent = new Intent(this, BackgroundMusicService.class);
        stopService(intent);
    }

    //broadcast receiver class
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

        //handle the user transition when enter or exit certain states
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
}
