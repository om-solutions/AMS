package com.attendance.om.myapplication;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("ResourceType")
public class SingleLocationProvider {

    private int mTimeout = 5000;
    private LocationManager mLocationManager;
    private SingleLocationListener mListener;
    private Location mLastKnownLocation;

    private Timer mTimer;
    private RequestTimerTask mTimerTask;

    private OnLocationProviderListener mCallback;

    public SingleLocationProvider(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mListener = new SingleLocationListener();

        mTimer = new Timer();
        mTimerTask = new RequestTimerTask();
    }

    public void requestLocation() {
        startSeeking();
    }

    public boolean checkGPSProviderEnabled() {
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (mCallback != null) {
                mCallback.onGPSProviderDisabled();
            }

            return false;
        }

        return true;
    }

    public void cancel() {
        stopSeeking();
    }

    private void startSeeking() {
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (mCallback != null) {
                mCallback.onGPSProviderDisabled();
                return;
            }
        }

        if (mCallback != null){
            mCallback.onLocationStartedSeeking();
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Integer.MAX_VALUE, Integer.MAX_VALUE, mListener);

        mTimer.schedule(mTimerTask, mTimeout);
    }

    private void stopSeeking() {
        mLocationManager.removeUpdates(mListener);

        mTimer.purge();
        mTimer.cancel();

        if (mCallback != null){
            mCallback.onLocationStoppedSeeking();
        }
    }

    public void setTimeout(int timeout) {
        mTimeout = timeout;
    }

    public void setOnLocationProviderListener(OnLocationProviderListener callback) {
        mCallback = callback;
    }

    public Location getLastKnownLocation() {
        return mLastKnownLocation;
    }

    private class SingleLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {

                mLastKnownLocation = location;

                if (mCallback != null) {
                    mCallback.onLocationFound(location);
                }

                stopSeeking();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Empty Block
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Empty Block
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Empty Block
        }
    }

    private class RequestTimerTask extends TimerTask {

        @Override
        public void run() {
            mLocationManager.removeUpdates(mListener);

            if (mCallback != null) {
                mCallback.onLocationNotFound();
            }

            stopSeeking();
        }
    }

    public interface OnLocationProviderListener {
        void onLocationStartedSeeking();

        void onLocationStoppedSeeking();

        void onLocationFound(Location location);

        void onLocationNotFound();

        void onGPSProviderDisabled();
    }

}