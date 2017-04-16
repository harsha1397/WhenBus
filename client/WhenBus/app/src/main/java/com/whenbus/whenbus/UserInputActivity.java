package com.whenbus.whenbus;

/**
 * Created by harsha on 1/4/17.
 */
import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.LocationSource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.whenbus.whenbus.Constants.*;
import static com.whenbus.whenbus.R.anim.fade_in;

public class UserInputActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    Context context;
    private Button go, goBusNo;
    private EditText busNo;
    private AutoCompleteTextView busNoAuto, source, destination;
    private boolean hasLocation = false;
    Location currentLocation = null;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private Button showMap, showBuses, getNearestStop;
    String bus;
    HashMap<String, String> hm;
    public void enableButtons(){
        if(!showMap.isEnabled()){
            showMap.setEnabled(true);
            showBuses.setEnabled(true);
            getNearestStop.setEnabled(true);
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        hasLocation = true;
        currentLocation = location;
        enableButtons();
        Log.i("Location", "got location");
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
    LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleClient;
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    String busStart, busEnd;
    ProgressDialog progressDialog;
    String dest = null;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.show();
        //overridePendingTransition(fade_in, fade_in);
        context = this;
        if ( ContextCompat.checkSelfPermission( context, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( UserInputActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    1);
        }

        context = this;
        showMap = (Button) findViewById(R.id.goBusNo);
        showBuses = (Button) findViewById(R.id.btn_showBuses);
        getNearestStop = (Button) findViewById(R.id.get_nearest_stop);
        busNoAuto = (AutoCompleteTextView) findViewById(R.id.busNo);
        source = (AutoCompleteTextView) findViewById(R.id.source);
        destination = (AutoCompleteTextView) findViewById(R.id.destination);
//        go = (Button) findViewById(R.id.go);
        busNo = (EditText) findViewById(R.id.busNo) ;
        goBusNo = (Button) findViewById(R.id.goBusNo);


        if (mGoogleClient == null) {
            mGoogleClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleClient.connect();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 20 seconds, in milliseconds
                .setFastestInterval(5 * 1000) // 10 second, in milliseconds
        ;
        final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
             @Override
             public void onResult(LocationSettingsResult result) {
                 final Status status = result.getStatus();
                 final LocationSettingsStates states = result.getLocationSettingsStates();
                 switch (status.getStatusCode()) {
                     case LocationSettingsStatusCodes.SUCCESS:
                         // All location settings are satisfied. The client can
                         // initialize location requests here.
                         Handler handler = new Handler();
                         handler.postDelayed(new Runnable() {

                             @Override
                             public void run() {
                                 currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                                         mGoogleClient);
                                 if (currentLocation != null) {
                                     enableButtons();
                                 }
                             }
                         }, 1000);
                         Log.i("....", "Success");
                         break;
                     case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                         // Location settings are not satisfied, but this can be fixed
                         // by showing the user a dialog.
                         try {
                             // Show the dialog by calling startResolutionForResult(),
                             // and check the result in onActivityResult().
                             status.startResolutionForResult(
                                     UserInputActivity.this,
                                     REQUEST_CHECK_SETTINGS);
                             Log.i("....", "Request");

                         } catch (IntentSender.SendIntentException e) {
                             // Ignore the error.
                         }
                         break;
                     case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                         // Location settings are not satisfied. However, we have no way
                         // to fix the settings so we won't show the dialog.
                         Log.i("....", "unavailable");
                         break;
                     default:
                         Log.w("....", "shouldn't occur");
                 }
             }
         });






        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

//        try{
//            Location lastKnownLocation = currentLocation; //todo check
//            double currentLatitude = lastKnownLocation.getLatitude();
//        }
//        catch (Exception e){
////            e.printStackTrace();
//        }
        getNearestStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                progressDialog = new ProgressDialog(UserInputActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Please wait...");
                //progressDialog.show();

                JSONObject post = new JSONObject();
                JSONObject coord = new JSONObject();

                Location lastKnownLocation = currentLocation; //todo check
                double currentLatitude = lastKnownLocation.getLatitude();
                double currentLongitude = lastKnownLocation.getLongitude();
                try{
                    coord.put("lat", currentLatitude);
                    coord.put("lng", currentLongitude);
                    post.put("coord", coord);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                GetNearestStop nearestStop = new GetNearestStop();
                nearestStop.execute(post.toString());
            }
        });

        showBuses.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View arg0) {
                                           progressDialog = new ProgressDialog(UserInputActivity.this);
                                           progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                           progressDialog.setIndeterminate(true);
                                           progressDialog.setMessage("Please wait...");
                                           //progressDialog.show();

                                           JSONObject post = new JSONObject();
                                           JSONObject coord = new JSONObject();

                                           Location lastKnownLocation = currentLocation; //todo check
                                           double currentLatitude = lastKnownLocation.getLatitude();
                                           double currentLongitude = lastKnownLocation.getLongitude();
                                           try{
                                               coord.put("lat", currentLatitude);
                                               coord.put("lng", currentLongitude);
                                               post.put("coord", coord);
                                               if(source != null && source.length() != 0)post.put("src", source.getText().toString());
                                               dest = destination.getText().toString();
                                               post.put("dest", dest);
                                           }
                                           catch (Exception e){
                                               e.printStackTrace();
                                           }
                                           SuggestBuses suggestBuses = new SuggestBuses();
                                           suggestBuses.execute(post.toString());
                                       }
                                   });
        goBusNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
//                if()
                SQLiteDatabase mydatabase = openOrCreateDatabase("WhenBus_db.db",MODE_PRIVATE,null);
                bus = busNo.getText().toString();
                Cursor cursor = mydatabase.query("busno_info", new String[] {"src, dest"}, "busno=?", new String[] {bus}, null, null, null);
                cursor.moveToFirst();
                String [] directions = new String[2];
                directions [0] = cursor.getString(cursor.getColumnIndex("src"));
                directions[1] = cursor.getString(cursor.getColumnIndex("dest"));
                final String [] choices = new String[2];
                progressDialog = new ProgressDialog(UserInputActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Please wait...");
//                progressDialog.show();

                JSONObject post = new JSONObject();
                JSONObject coord = new JSONObject();

                Location lastKnownLocation = currentLocation; //todo check
                double currentLatitude = lastKnownLocation.getLatitude();
                double currentLongitude = lastKnownLocation.getLongitude();
                try{
                    coord.put("lat", currentLatitude);
                    coord.put("lng", currentLongitude);
                    post.put("bus_no", busNo.getText().toString());
                    post.put("coord", coord);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                String tokens[] = hm.get("route").split(Pattern.quote(" - "));

                try {
                    post.put("start_point", tokens[0]);
                    post.put("end_point", tokens[1]);
                }
                catch (Exception e){
//            e.printStackTrace();
                }
                PostDataTask postDataTask = new PostDataTask();
                postDataTask.execute(post.toString());
//                selectDirection(directions, post);
            }
        });
        DataBaseHelper myDbHelper = new DataBaseHelper(this);

        try {
            myDbHelper.createDataBase();
        }
        catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        try {
            myDbHelper.openDataBase();
        }catch(SQLException sqle){

            throw sqle;

        }






//        String [] buses = getAllBusNos();
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, buses);
//        busNoAuto.setAdapter(adapter);

        autocomplete_setup();

        autocomplete_setup2();
//        String [] busstops = getAllBusStops();
//        ArrayAdapter<String> stopsadapter = new ArrayAdapter<String>(this, R.layout.list_item, busstops);
//        source.setAdapter(stopsadapter);
//        destination.setAdapter(stopsadapter);
//        busNoAuto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                in.hideSoftInputFromWindow(arg1.getWindowToken(), 0);
//
//            }
//
//        });
//
//        destination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                in.hideSoftInputFromWindow(arg1.getWindowToken(), 0);
//
//            }
//
//        });
//        source.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                in.hideSoftInputFromWindow(arg1.getWindowToken(), 0);
//
//            }
//
//        });


    }
    public void autocomplete_setup2() {
        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();
        aList = getAllstops();
        String[] from = { "flag","txt"};
        int[] to = { R.id.flag,R.id.stop};
        SimpleAdapter adapter = new SimpleAdapter(this, aList, R.layout.autocomplete_layout1, from, to);
        CustomAutoCompleteTextView autoComplete1 = ( CustomAutoCompleteTextView) findViewById(R.id.source);
        CustomAutoCompleteTextView autoComplete2 = ( CustomAutoCompleteTextView) findViewById(R.id.destination);
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                HashMap<String, String>hm2 = (HashMap<String, String>) arg0.getAdapter().getItem(position);
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);
            }
        };
        autoComplete1.setOnItemClickListener(itemClickListener);
        autoComplete1.setAdapter(adapter);
        autoComplete2.setOnItemClickListener(itemClickListener);
        autoComplete2.setAdapter(adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch (requestCode)
        {
            case REQUEST_CHECK_SETTINGS:
            {
                final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                                        mGoogleClient);
                                if (currentLocation != null) {
                                    enableButtons();
                                }
                            }
                        }, 3000);

                        break;
                    case Activity.RESULT_CANCELED:
                    {// The user was asked to change settings, but chose not to
                        if (!states.isLocationUsable())
                        {
                            if (!states.isGpsUsable())
                            {
                                Log.d("", "Please enable GPS and try again.");
                            }
                        }

                        break;
                    }
                    default:
                        break;
                }
                break;
            }
        }
    }


    private void selectDirection(final String [] directions, final JSONObject post) {
        final String [] choices = new String[2];
        choices[0] = directions[0] + " - " + directions[1];
        choices[1] = directions[1] + " - " + directions[0];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Direction");
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(UserInputActivity.this,
//                        choices[which] + " Selected", Toast.LENGTH_LONG)
//                        .show();
                dialog.dismiss();
                busStart = directions[0^which];
                busEnd = directions[1^which];
                try {
                    post.put("start_point", busStart);
                    post.put("end_point", busEnd);
                }
                catch (Exception e){
//            e.printStackTrace();
                }
                PostDataTask postDataTask = new PostDataTask();
                postDataTask.execute(post.toString());
            }
        });
//        builder.setNegativeButton("cancel",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void autocomplete_setup() {
        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();
        aList = getAllBusNos();
        String[] from = { "flag","txt","route"};
        int[] to = { R.id.flag,R.id.busNo,R.id.route};
        SimpleAdapter adapter2 = new SimpleAdapter(this,aList, R.layout.autocomplete_layout, from, to);
        CustomAutoCompleteTextView autoComplete = ( CustomAutoCompleteTextView) findViewById(R.id.busNo);
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);
                hm = (HashMap<String, String>) arg0.getAdapter().getItem(position);
            }
        };
        autoComplete.setOnItemClickListener(itemClickListener);
        autoComplete.setAdapter(adapter2);
    }


    public ArrayList getAllBusNos()
    {

        SQLiteDatabase mydatabase = openOrCreateDatabase("WhenBus_db.db", SQLiteDatabase.CREATE_IF_NECESSARY,null);
        Cursor cursor = mydatabase.query("busno_info", new String[] {"busno, src, dest"}, null, null, null, null, null);
        if(cursor.getCount() >0)
        {
            ArrayList list = new ArrayList();
            int i = 0;
            while (cursor.moveToNext())
            {
                String busno = cursor.getString(cursor.getColumnIndex("busno"));
                String src = cursor.getString(cursor.getColumnIndex("src"));
                String dest = cursor.getString(cursor.getColumnIndex("dest"));
                HashMap mMap = new HashMap();
                mMap.put("txt",busno);
                mMap.put("src",src);
                mMap.put("dest",dest);
                mMap.put("route",src+" - "+dest);
                mMap.put("flag",Integer.toString(R.drawable.bus));
                list.add(mMap);
                i++;
            }
            cursor.close();
            return list;
        }
        else
        {
            cursor.close();
            return new ArrayList();
        }

    }

    public String[] getAllBusNos1()
    {
        SQLiteDatabase mydatabase = openOrCreateDatabase("WhenBus_db.db",MODE_PRIVATE,null);
        Cursor cursor = mydatabase.query("busno_info", new String[] {"busno"}, null, null, null, null, null);

        if(cursor.getCount() >0)
        {
            String[] str = new String[cursor.getCount()];
            int i = 0;

            while (cursor.moveToNext())
            {
                str[i] = cursor.getString(cursor.getColumnIndex("busno"));
                i++;
            }
            cursor.close();
            return str;
        }
        else
        {
            cursor.close();
            return new String[] {};
        }
    }


    public ArrayList getAllstops()
    {

        SQLiteDatabase mydatabase = openOrCreateDatabase("WhenBus_db.db", SQLiteDatabase.CREATE_IF_NECESSARY,null);
        Cursor cursor = mydatabase.query("busstop_coord", new String[] {"busstop, lat, lng"}, null, null, null, null, null);
        if(cursor.getCount() >0)
        {
            ArrayList list = new ArrayList();
            int i = 0;
            while (cursor.moveToNext())
            {
                String bus_stop = cursor.getString(cursor.getColumnIndex("busstop"));
                String lat = cursor.getString(cursor.getColumnIndex("lat"));
                String lng = cursor.getString(cursor.getColumnIndex("lng"));
                HashMap mMap = new HashMap();
                mMap.put("txt",bus_stop);
                mMap.put("lat",lat);
                mMap.put("lng",lng);
                mMap.put("flag",Integer.toString(R.drawable.stop_board_red));
                list.add(mMap);
                i++;
            }
            cursor.close();
            return list;
        }
        else
        {
            cursor.close();
            return new ArrayList();
        }

    }

    public String[] getAllBusStops1()
    {
        SQLiteDatabase mydatabase = openOrCreateDatabase("WhenBus_db.db",MODE_PRIVATE,null);
        Cursor cursor = mydatabase.query("busstop_coord", new String[] {"busstop"}, null, null, null, null, null);

        if(cursor.getCount() >0)
        {
            String[] str = new String[cursor.getCount()];
            int i = 0;

            while (cursor.moveToNext())
            {
                str[i] = cursor.getString(cursor.getColumnIndex("busstop"));
                i++;
            }
            cursor.close();
            return str;
        }
        else
        {
            cursor.close();
            return new String[] {};
        }
    }

    public void toastMessage(String message){
        Toast.makeText(this,message,
                Toast.LENGTH_LONG).show();
    }





    class GetNearestStop extends AsyncTask<String, Integer, Boolean> {
        int code = 0;
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
            Log.i("post", post[0]);
            String postUrl = HOST + "/suggest/nearestStop/";
            HttpUrl.Builder urlBuilder = HttpUrl.parse(postUrl).newBuilder();
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            //Send the request
            //Intent intent2 = new Intent(context, ShowBuses.class)
            ;
            //startActivity(intent2);
            String responseData = "";
            try {
                Response response = client.newCall(request).execute();
                try {
                    responseData = response.body().string();
                }
                catch(Exception e){
                    code = 1;
                    result = false;
                    return result;
                }
                if(responseData.length() == 0){
                    code = 1;
                    result = false;
                    return result;
                }
                JSONObject JSONresponse = new JSONObject(responseData);
                String stopName = JSONresponse.get("stopName").toString();
                float distance = Float.parseFloat(JSONresponse.get("distance").toString());
                Intent intent = new Intent(context, NearestStop.class)
                        .putExtra("dest", stopName)
                        .putExtra("distance", distance)
                        ;
                startActivity(intent);
            }
            catch (Exception e){
                e.printStackTrace();
                progressDialog.dismiss();
                result = false;
            }

            return  result;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                if (code == 1) {
                    toastMessage("Server error");
                    progressDialog.dismiss();
                }
                else
                    toastMessage("No internet connection. Please try again");
            }
        }
    }




    class SuggestBuses extends AsyncTask<String, Integer, Boolean> {
        int code = 0;
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
            Log.i("post", post[0]);
            String postUrl = HOST + "/suggest/bus/";
            HttpUrl.Builder urlBuilder = HttpUrl.parse(postUrl).newBuilder();
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            //Send the request
            //Intent intent2 = new Intent(context, ShowBuses.class)
                    ;
            //startActivity(intent2);
            String responseData = "";
            try {
                Response response = client.newCall(request).execute();
                try {
                    responseData = response.body().string();
                }
                catch(Exception e){
                    code = 1;
                    result = false;
                    return result;
                }
                if(responseData.length() == 0){
                    code = 1;
                    result = false;
                    return result;
                }
                Log.i("reponse", responseData);
                JSONArray buses = new JSONArray(responseData);
                //JSONObject JSONresponse = new JSONObject(responseData);
//                startService(new Intent(getBaseContext(),Feedback.class)
//                        .putExtra("key", key)
//                        .putExtra("")
//                );
//                String [] suggestBuses = new String[buses.length()];
//                for (int i = 0;i < buses.length();i++){
//                    suggestBuses[i] = (buses.getString(i));
//                }
                double currentLatitude = currentLocation.getLatitude();
                double currentLongitude = currentLocation.getLongitude();
                progressDialog.dismiss();
                Intent intent = new Intent(context, ShowBuses.class)
                        .putExtra("buses", buses.toString())
                        .putExtra("lat", currentLatitude)
                        .putExtra("lng", currentLongitude)
                        .putExtra("currentLocation", currentLocation)
                        .putExtra("dest", dest);
//                        .putExtra("dest", dest)
                        ;
                startActivity(intent);
            }
            catch (Exception e){
                e.printStackTrace();
                progressDialog.dismiss();
                result = false;
            }

            return  result;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                if (code == 1) {
                    toastMessage("Server error");
                    progressDialog.dismiss();
                }
                else
                    toastMessage("No internet connection. Please try again");
            }
        }
    }
    class PostDataTask extends AsyncTask<String, Integer, Boolean> {
        int code = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//          progressDialog = ProgressDialog.show(Confirmation.this, "Please wait...", "Retrieving data ...", true);
        }

        @Override
        protected Boolean doInBackground(String... post) {
            Boolean result = false;
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON, post[0]);
            String postUrl = HOST + "/info/bus/";
            Log.i("url",postUrl);
            HttpUrl.Builder urlBuilder = HttpUrl.parse(postUrl).newBuilder();
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            //Send the request
            String responseData = "";
            try {
                Response response = client.newCall(request).execute();
                try {
                    responseData = response.body().string();
                }
                catch(Exception e){
                    code = 1;
                    result = false;
                    return result;
                }
                if(responseData.length() == 0){
                    code = 1;
                    result = false;
                    return result;
                }
                JSONObject JSONresponse = new JSONObject(responseData);
                int time = Integer.parseInt(JSONresponse.get("time").toString());
                JSONObject busLocation = (JSONObject) JSONresponse.get("busLoc");
                String nearestStop = JSONresponse.get("stop").toString();
                int id = JSONresponse.getInt("id");
                String [] stop = new String[1];
                        stop[0] = nearestStop;
                String [] columns = {
                    "lat",
                    "lng"
                };
                SQLiteDatabase mydatabase = openOrCreateDatabase("WhenBus_db.db",MODE_PRIVATE,null);
                Cursor sourceCursor = mydatabase.query("busstop_coord", columns, "busstop=?", stop, null, null, null);
                sourceCursor.moveToFirst();

                Location src = new Location(LocationManager.GPS_PROVIDER);

                src.setLatitude(Double.parseDouble(sourceCursor.getString(sourceCursor.getColumnIndex("lat"))));
                src.setLongitude(Double.parseDouble(sourceCursor.getString(sourceCursor.getColumnIndex("lng"))));
                sourceCursor.close();

                Location busL = new Location(LocationManager.GPS_PROVIDER);
                busL.setLatitude(Double.parseDouble(busLocation.get("lat").toString()));
                busL.setLongitude(Double.parseDouble(busLocation.get("lng").toString()));

                progressDialog.dismiss();
                result = true;
                Intent intent = new Intent(context, ShowMapActivity.class).putExtra("currentLocation", currentLocation)
                        .putExtra("busLocation", busL)
                        .putExtra("srcLocation", src)
                        .putExtra("busStart", busStart)
                        .putExtra("busEnd", busEnd)
                        .putExtra("busNo", bus)
                        .putExtra("time", time)
                        .putExtra("busId", id)
                        .putExtra("dest", dest)
                        ;
                startActivity(intent);
            }
            catch (Exception e){
                progressDialog.dismiss();
                result = false;

                e.printStackTrace();
            }

            return  result;
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(!result){
                if(code == 1) {
                    toastMessage("Server error");
                    progressDialog.dismiss();
                }
                else
                    toastMessage("No internet connection. Please try again");
            }
//            Toast.makeText(this,result?"Message successfully sent!":"There wassome error in sending message. Please try again after some time.",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onStop(){
        locationManager.removeUpdates(UserInputActivity.this);
        mGoogleClient.disconnect();
        super.onStop();
    }
    @Override
    protected void onStart(){
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        mGoogleClient.connect();

        currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleClient);
        if (currentLocation != null) {
            enableButtons();
        }
        super.onStart();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleClient);
        if (currentLocation != null) {
            enableButtons();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleClient.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(context, UserInputActivity.class);
                        startActivity(intent);
                    } else {
                        this.finish();

                    }
                }
            }
        }
    }
}

