/*
 * Yan Zhang
 * 300052103
 * Constants holder for application
 * */
package com.csi5175.googleservice;

public class Constants {
    //for broadcast receiver distinguish between activity
    public static final String BROADCAST_DETECTED_ACTIVITY = "DETECTED_ACTIVITY";
    public static final String TRANSITIONS_RECEIVER_ACTION = "TRANSITIONS_RECEIVER_ACTION";

    //duration for pending intent to get update
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 1000;

    //set confidence level
    public static final int CONFIDENCE = 70;
}