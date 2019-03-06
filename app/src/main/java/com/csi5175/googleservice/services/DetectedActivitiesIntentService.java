/*
* This class is for intent service and retreive results from intent of google services
* */
package com.csi5175.googleservice.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.csi5175.googleservice.Constants;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DetectedActivitiesIntentService extends IntentService {

    protected static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public DetectedActivitiesIntentService(String name) {
        super(name);
    }

    public DetectedActivitiesIntentService() {
        super(TAG);
    }

    //receive and extract result from intent, then call broadcast to handle each detected activity
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //if returned is activity transition
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            List<ActivityTransitionEvent> events = result.getTransitionEvents();
            for (ActivityTransitionEvent a : events) {
                Log.d(TAG, "Detected Transition: " + a.getActivityType() + ", " + a.getTransitionType() + ", " + a.getTransitionType());
                broadcastActivity(a, Constants.TRANSITIONS_RECEIVER_ACTION);
            }
            //if returned is activity recognition
        } else if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            List<DetectedActivity> activities = result.getProbableActivities();
            //for debugging use
            for (DetectedActivity a : activities) {
                Log.d(TAG, "Detected Activity: " + a.getType() + ", " + a.getConfidence());
            }
            //find max probable activity
            DetectedActivity da = Collections.max(activities, new Comparator<DetectedActivity>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public int compare(DetectedActivity o1, DetectedActivity o2) {
                    int o1c=o1.getConfidence();
                    int o2c=o2.getConfidence();
                    if(o1.getType()==2)
                        o1c-=100;
                    if(o2.getType()==2)
                        o2c-=100;
                    return o1c-o2c;
                }
            });
            broadcastActivity(da, Constants.BROADCAST_DETECTED_ACTIVITY);
        }
    }

    //create intent for activity and broadcast
    private void broadcastActivity(Object activity, String serviceType) {
        Intent intent = new Intent(serviceType);
        if (activity instanceof DetectedActivity) {
            DetectedActivity a = DetectedActivity.class.cast(activity);
            intent.putExtra("type", a.getType());
            intent.putExtra("confidence", a.getConfidence());
        } else if (activity instanceof ActivityTransitionEvent) {
            ActivityTransitionEvent a = ActivityTransitionEvent.class.cast(activity);
            intent.putExtra("type", a.getActivityType());
            intent.putExtra("transition", a.getTransitionType());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
