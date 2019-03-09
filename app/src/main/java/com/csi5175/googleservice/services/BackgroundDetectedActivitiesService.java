/*
 * Yan Zhang
 * 300052103
 * The pre layer background service for activity recognition which handles registration of services
 * for activity recognition and transition
 * */
package com.csi5175.googleservice.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.csi5175.googleservice.Constants;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

/*
 * The skeleton comes from the tutorial in the main activity
 *
 * */
public class BackgroundDetectedActivitiesService extends Service {

    //DEBUG only
    private static final String TAG = BackgroundDetectedActivitiesService.class.getSimpleName();
    //private fields for activity recognition
    private PendingIntent pendingIntent;
    private ActivityRecognitionClient activityRecognitionClient;
    private IBinder mBinder = new BackgroundDetectedActivitiesService.LocalBinder();

    //binder for services
    private class LocalBinder extends Binder {
        public BackgroundDetectedActivitiesService getServerInstance() {
            return BackgroundDetectedActivitiesService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public BackgroundDetectedActivitiesService() {
    }

    /*
     * Initiate the recognition client and pending intent services
     * */
    @Override
    public void onCreate() {
        super.onCreate();
        activityRecognitionClient = new ActivityRecognitionClient(this);
        pendingIntent = PendingIntent.getService(this, 1,
                new Intent(this, DetectedActivitiesIntentService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        requestActivityUpdatesButtonHandler();
        setupActivityTransition();
    }


    /*
     * remove the all services related
     * */
    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActivityUpdatesButtonHandler();
        unregisterActivityTransition();
        pendingIntent.cancel();
    }

    /*
     * Register the pending intent for update
     * */
    private void requestActivityUpdatesButtonHandler() {
        Task<Void> task = activityRecognitionClient.requestActivityUpdates(Constants.DETECTION_INTERVAL_IN_MILLISECONDS, pendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Successfully requested activity updates", Toast.LENGTH_LONG).show();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to start activity updates", Toast.LENGTH_LONG).show();
            }
        });
    }

    /*
     * Unregister the recognition from pending intent
     * */
    private void removeActivityUpdatesButtonHandler() {
        Task<Void> task = activityRecognitionClient.removeActivityUpdates(pendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Successfully removed activity updates", Toast.LENGTH_LONG).show();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to remove activity updates", Toast.LENGTH_LONG).show();
            }
        });
    }

    /*
     * Register the transition state in pending intent and get update
     * */
    private void setupActivityTransition() {
        List<ActivityTransition> transitions = new ArrayList<>();
        transitions.add(new ActivityTransition.Builder().setActivityType(DetectedActivity.WALKING).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build());
        transitions.add(new ActivityTransition.Builder().setActivityType(DetectedActivity.WALKING).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT).build());
        transitions.add(new ActivityTransition.Builder().setActivityType(DetectedActivity.RUNNING).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build());
        transitions.add(new ActivityTransition.Builder().setActivityType(DetectedActivity.RUNNING).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT).build());
        transitions.add(new ActivityTransition.Builder().setActivityType(DetectedActivity.STILL).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build());
        transitions.add(new ActivityTransition.Builder().setActivityType(DetectedActivity.STILL).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT).build());
        transitions.add(new ActivityTransition.Builder().setActivityType(DetectedActivity.IN_VEHICLE).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build());
        transitions.add(new ActivityTransition.Builder().setActivityType(DetectedActivity.IN_VEHICLE).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT).build());
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        Task<Void> task = ActivityRecognition.getClient(this).requestActivityTransitionUpdates(request, pendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "transition api successfully register");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "transition api could not register");
            }
        });
    }

    /*
     * Unregister the transition from pending intent
     * */
    private void unregisterActivityTransition() {
        ActivityRecognition.getClient(this).removeActivityTransitionUpdates(pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Transition unregistered successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Transition could not be unregistered");
                    }
                });
    }
}
