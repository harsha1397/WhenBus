package com.whenbus.whenbus;

/**
 * Created by harsha on 1/4/17.
 */
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.LocationSource;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserInputActivity extends AppCompatActivity implements LocationListener{
    Context context;
    private Button go, goBusNo;
    private EditText busNo;
    private AutoCompleteTextView busNoAuto;
    private boolean hasLocation = false;
    Location currentLocation = null;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private Button showMap;
    String bus;
    @Override
    public void onLocationChanged(Location location) {
        hasLocation = true;
        currentLocation = location;
        if(!showMap.isEnabled()){
            showMap.setText("Go!");
            showMap.setEnabled(true);
        }
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
    String busStart, busEnd;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        showMap = (Button) findViewById(R.id.goBusNo);
        busNoAuto = (AutoCompleteTextView) findViewById(R.id.busNo);
//        go = (Button) findViewById(R.id.go);
        busNo = (EditText) findViewById(R.id.busNo) ;
        goBusNo = (Button) findViewById(R.id.goBusNo);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        try{
            Location lastKnownLocation = currentLocation; //todo check
            double currentLatitude = lastKnownLocation.getLatitude();
        }
        catch (Exception e){
//            e.printStackTrace();
        }
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
                progressDialog.show();

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
                selectDirection(directions, post);
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

        String [] buses = getAllBusNos();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, buses);
        busNoAuto.setAdapter(adapter);

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
                Toast.makeText(UserInputActivity.this,
                        choices[which] + " Selected", Toast.LENGTH_LONG)
                        .show();
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


    public String[] getAllBusNos()
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

    public void toastMessage(String message){
        Toast.makeText(this,message,
                Toast.LENGTH_LONG).show();
    }

    class PostDataTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//          progressDialog = ProgressDialog.show(Confirmation.this, "Please wait...", "Retrieving data ...", true);
        }

        @Override
        protected Boolean doInBackground(String... post) {
            Boolean result = true;
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON, post[0]);
            HttpUrl.Builder urlBuilder = HttpUrl.parse("http://192.168.100.6:8000/info/bus/").newBuilder();
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

                Intent intent = new Intent(context, ShowMapActivity.class).putExtra("currentLocation", currentLocation)
                        .putExtra("busLocation", busL)
                        .putExtra("srcLocation", src)
                        .putExtra("busStart", busStart)
                        .putExtra("busEnd", busEnd)
                        .putExtra("busNo", bus)
                        .putExtra("time", time)
                        .putExtra("busId", id)
                        ;
                startActivity(intent);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            return  result;
        }

        @Override
        protected void onPostExecute(Boolean result){
//            Toast.makeText(this,result?"Message successfully sent!":"There wassome error in sending message. Please try again after some time.",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onStop(){
        locationManager.removeUpdates(UserInputActivity.this);
        super.onStop();
    }
    @Override
    protected void onRestart(){
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        super.onRestart();
    }
}

