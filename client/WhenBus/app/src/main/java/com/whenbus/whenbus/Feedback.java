package com.whenbus.whenbus;

/**
 * Created by harsha on 5/4/17.
 */

import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import android.location.LocationListener;

import org.json.JSONObject;

import java.util.Calendar;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Feedback extends Service{
    String key;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private double currentLat = 0;
    private double currentLon = 0;

    protected LocationManager locationManager;
//    protected android.location.LocationListener locationListener;
    /** indicates how to behave if the service is killed */
    int mStartMode = START_NOT_STICKY;

    /** interface for clients that bind */
    IBinder mBinder;

    /** indicates whether onRebind should be used */
    boolean mAllowRebind;

    /** Called when the service is being created. */
    @Override
    public void onCreate() {

    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        key = intent.getExtras().get("key").toString();
        addListenerLocation();
        return mStartMode;
    }

    private void addListenerLocation() {
        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                JSONObject post = new JSONObject();
                JSONObject coord = new JSONObject();
                double currentLatitude = location.getLatitude();
                double currentLongitude = location.getLongitude();
                int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60 + Calendar.getInstance().get(Calendar.MINUTE);
                 try{
                    coord.put("lat", currentLatitude);
                    coord.put("lng", currentLongitude);
                    post.put("key", key);
                    post.put("coord", coord);
                    post.put("timestamp", time);
                }
                catch (Exception e){

                }
                SendFeedback sendFeedback = new SendFeedback();
                sendFeedback.execute(post.toString());
                Log.i("Updating ", "Location");
                Toast.makeText(getBaseContext(),currentLat+"-"+currentLon, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(lastKnownLocation!=null){
                    currentLat = lastKnownLocation.getLatitude();
                    currentLon = lastKnownLocation.getLongitude();
                }

            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, mLocationListener);
    }
    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** Called when all clients have unbound with unbindService() */
    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    /** Called when a client is binding to the service with bindService()*/
    @Override
    public void onRebind(Intent intent) {

    }

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {
        mLocationManager.removeUpdates(mLocationListener);
        super.onDestroy();
    }

    class SendFeedback extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... post) {
            Boolean result = true;
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON, post[0]);
            HttpUrl.Builder urlBuilder = HttpUrl.parse("http://192.168.100.6:8000/feedback/send/").newBuilder();
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            //Send the request
            try {
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject JSONresponse = new JSONObject(responseData);
                String status = JSONresponse.get("status").toString();
                if(status.equals("DROP")) {
                    locationManager.removeUpdates(mLocationListener);
                    stopSelf();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

            return  result;
        }
        @Override
        protected void onPostExecute(Boolean result){
//            Toast.makeText(context,"Started tracking", Toast.LENGTH_LONG).show();
//            ShowMapActivity.this.finish();
        }
    }
}