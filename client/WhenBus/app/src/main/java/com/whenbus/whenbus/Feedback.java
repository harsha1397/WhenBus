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


import com.google.android.gms.location.LocationListener;

import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.json.JSONObject;

import java.util.Calendar;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.whenbus.whenbus.Constants.*;

public class Feedback extends Service implements LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    String key;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private double currentLat = 0;
    private double currentLon = 0;
    private Context context = this;
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
        createLocationRequest();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.v("Connection failed",connectionResult.toString());
    }
    boolean mRequestingLocationUpdates = false;

    @Override
    public void onConnected(Bundle bundle) {
        if (!mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        mRequestingLocationUpdates = true;
    }
    protected void stopLocationUpdates() {
        if(mRequestingLocationUpdates == true)
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }
    @Override
    public void onLocationChanged(Location location) {
//        hasLocation = true;
        //currentLocation = location;
        Log.i("Location", "got location");
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


        };
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
        stopLocationUpdates();
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
            String postUrl  = HOST + "/feedback/send/";
            HttpUrl.Builder urlBuilder = HttpUrl.parse(postUrl).newBuilder();
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
                    stopLocationUpdates();
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
            Toast.makeText(context,"Tracking", Toast.LENGTH_LONG).show();
//            ShowMapActivity.this.finish();
        }
    }
}