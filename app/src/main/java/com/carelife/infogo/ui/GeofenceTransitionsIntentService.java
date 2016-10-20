package com.carelife.infogo.ui;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.carelife.infogo.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.internal.ParcelableGeofence;

import java.util.List;

/**
 * Created by chenzhuwei on 16/10/20.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "Geofence";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    public GeofenceTransitionsIntentService() {
        super("GeofenceService");
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, geofencingEvent.getErrorCode()+"");
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    geofenceTransition,
                    triggeringGeofences
            );

            if(geofenceTransitionDetails == null) {
                return;
            }
            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
        }
    }

    private String getGeofenceTransitionDetails(int geofenceTransition, List<Geofence> triggeringGeofences) {
        if(triggeringGeofences == null || triggeringGeofences.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            sb.append("You enter: ");
        } else {
            sb.append("You leave: ");
        }
        ParcelableGeofence geo = ((ParcelableGeofence)triggeringGeofences.get(0));
        sb.append("Lat:").append(geo.getLatitude()).append("   Lon:").append(geo.getLongitude());
        return sb.toString();
    }

    //弹出Notification
    private void sendNotification(String content) {
        NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent i = new Intent(this, InfoListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);

        Notification.Builder builder = new Notification.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        Notification mNotification = builder.build();

        mManager.notify(0, mNotification);
    }

}