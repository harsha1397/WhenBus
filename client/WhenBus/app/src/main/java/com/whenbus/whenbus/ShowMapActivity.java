package com.whenbus.whenbus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by harsha on 11/2/17.
 */

public class ShowMapActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, RoutingListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private Location currentLocation;
    private Location busLocation;
    private Location srcLocation;
    private GoogleMap map;
    private List<Polyline> polylines;
    private Context context;
    private LatLng start, end, userLatLng;
    private Marker m1, m2;
    private String bus, busStart, busEnd;
    private TextView timeTV, busNoTV;
    private int busId;
    @Override
    public void onLocationChanged(Location location) {
//        hasLocation = true;
        currentLocation = location;
        Log.i("Location", "got location");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_map);
        currentLocation = (Location) getIntent().getExtras().get("currentLocation");
        busLocation = (Location) getIntent().getExtras().get("busLocation");
        srcLocation = (Location) getIntent().getExtras().get("srcLocation");
        busStart = getIntent().getExtras().get("busStart").toString();
        busEnd = getIntent().getExtras().get("busEnd").toString();
        bus  = getIntent().getExtras().get("busNo").toString();
        busId  = Integer.parseInt(getIntent().getExtras().get("busId").toString());
        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        polylines = new ArrayList<>();
        mapFragment.getMapAsync(this);
        timeTV = (TextView) findViewById(R.id.arrival);
        busNoTV = (TextView) findViewById(R.id.showBusNo);
        busNoTV.setText(bus);
        String time;
        int timetmp;
        timetmp = Integer.parseInt(getIntent().getExtras().get("time").toString());
        time = Integer.toString(timetmp/60) + ":" + Integer.toString(timetmp%60);
        timeTV.setText(time);

    }
    Handler handler;
    @Override
    public void onPostResume(){
        super.onPostResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        userLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions m = new MarkerOptions()
                .position(userLatLng)
                .title("User Location")
//                .snippet("")
                ;
        map.addMarker(m).showInfoWindow();
        ArrayList<Location> list = new ArrayList<>();
        list.add(busLocation);
        list.add(srcLocation);

        start = new LatLng(busLocation.getLatitude(), busLocation.getLongitude());
        end = new LatLng(srcLocation.getLatitude(), srcLocation.getLongitude());

        route(start, end);

        Log.i(srcLocation.toString(), busLocation.toString());
        handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                JSONObject post = new JSONObject();
                JSONObject coord = new JSONObject();
                double currentLatitude = currentLocation.getLatitude();
                double currentLongitude = currentLocation.getLongitude();
                try{
                    coord.put("lat", currentLatitude);
                    coord.put("lng", currentLongitude);
                    post.put("bus_no", bus);
                    post.put("coord", coord);
                    post.put("start_point", busStart);
                    post.put("end_point", busEnd);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                PostDataTask postDataTask = new PostDataTask();
                postDataTask.execute(post.toString());
                handler.postDelayed(this, 30000);
            }
        }, 4000);
        startTracking();
    }

    public void route(LatLng start, LatLng end){//todo
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.TRANSIT)
                .withListener(this)
                .waypoints(start, end)
//                .key("@string/google_maps_key")
                .build();
        routing.execute();
    }
    @Override
    public void onRoutingCancelled() {
        Log.i(":", "Routing was cancelled.");
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        // The Routing request failed
        Log.i(":", "Routing failed");
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onRoutingStart() {
        Log.i(":", "Routing started");
        // The Routing Request starts
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex)
    {
//        progressDialog.dismiss();
        Log.i(":", "Routing was success");
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(userLatLng);
        boundsBuilder.include(start);
        boundsBuilder.include(end);
        LatLngBounds bounds = boundsBuilder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 30);

        map.animateCamera(cu);


        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
//            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(Color.BLACK);
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = map.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

        // Start marker
        if(m1 != null)m1.remove();
        MarkerOptions options = new MarkerOptions();
        options.position(start).title("Bus location");
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
        m1 = map.addMarker(options);
        m1.showInfoWindow();

        // End marker
        if(m2 != null)m2.remove();
        options = new MarkerOptions();
        options.position(end).title("Bus stop");
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop));
        m2 =  map.addMarker(options);
        m2.showInfoWindow();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.v("Connection failed",connectionResult.toString());
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    String nearestStop;
    JSONArray stopList;
    class PostDataTask extends AsyncTask<String, Integer, Boolean> {
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
                final int time = Integer.parseInt(JSONresponse.get("time").toString());
                JSONObject busLocation = (JSONObject) JSONresponse.get("busLoc");
                nearestStop = JSONresponse.get("stop").toString();
                int id = JSONresponse.getInt("id");
                stopList = (JSONArray) JSONresponse.get("stopList");
                busId  = id;
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

                Location bus = new Location(LocationManager.GPS_PROVIDER);
                bus.setLatitude(Double.parseDouble(busLocation.get("lat").toString()));
                bus.setLongitude(Double.parseDouble(busLocation.get("lng").toString()));

                end = new LatLng(Double.parseDouble(sourceCursor.getString(sourceCursor.getColumnIndex("lat")))
                , Double.parseDouble(sourceCursor.getString(sourceCursor.getColumnIndex("lng")))
                );

                start = new LatLng(Double.parseDouble(busLocation.get("lat").toString())
                        , Double.parseDouble(busLocation.get("lng").toString())
                );
                sourceCursor.close();


                ShowMapActivity.this.runOnUiThread(new Runnable() {
                                                       public void run() {
                                                           String timetmp;
                                                           timetmp = Integer.toString(time / 60) + ":" + Integer.toString(time % 60);
                                                           timeTV.setText(timetmp);
                                                       }
                                                   });
                    Log.i(start.toString(), end.toString());
            }
            catch (Exception e){
                e.printStackTrace();
            }

            return  result;
        }
        @Override
        protected void onPostExecute(Boolean result){
            route(start, end);

            Toast.makeText(context,result?"Successful":"Error",Toast.LENGTH_LONG).show();
        }
    }
    public void getKey(){
        GetTrackingKey getTrackingKey = new GetTrackingKey();
        JSONObject post = new JSONObject();
//        JSONObject coord = new JSONObject();
        double currentLatitude = currentLocation.getLatitude();
        double currentLongitude = currentLocation.getLongitude();
        try{
//            coord.put("lat", currentLatitude);
//            coord.put("lng", currentLongitude);
            post.put("bus_no", bus);
//            post.put("coord", coord);
            post.put("start_point", busStart);
            post.put("end_point", busEnd);
            post.put("id", busId);
            post.put("src", nearestStop);
            post.put("end", busEnd);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        getTrackingKey.execute(post.toString());

    }
    public void startTracking(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Have you boarded "+bus+"?");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getDestination();
                    }
                });
        builder.setNegativeButton("No",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    ShowMapActivity.this.finish();
                }
            });
        AlertDialog alert = builder.create();
        alert.show();
    }
    String userDestination;
    String[] stop;
    public void getDestination(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter destination");
        final ArrayList<String> stops = new ArrayList<String>();

        stop = new String[stops.size()];
        stop = stops.toArray(stop);
        try {
            if (stopList != null) {
                int i = 0;
                while(!stopList.getString(i).equals(nearestStop))
                    i++;
                i++;
                for (;i < stopList.length();i++){
                    stops.add(stopList.getString(i));
                }
            }
//            Log.i("stoplist: ", Integer.toString(stopList.length()));
//            Log.i("stops: ", Integer.toString(stops.size()));
//            Log.i("stop: ", Integer.toString(stop.length));
            stop = new String[stops.size()];
            stop = stops.toArray(stop);
        }
        catch (Exception e){}
        builder.setItems(stop, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ShowMapActivity.this,
                        stop[which] + " Selected", Toast.LENGTH_LONG)
                        .show();
                userDestination = stop[which];
                dialog.dismiss();
                getKey();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    class GetTrackingKey extends AsyncTask<String, Integer, Boolean> {
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
            HttpUrl.Builder urlBuilder = HttpUrl.parse("http://192.168.100.6:8000/feedback/access/").newBuilder();
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
                String key = JSONresponse.get("key").toString();
//                startService(new Intent(getBaseContext(),Feedback.class)
//                        .putExtra("key", key)
//                        .putExtra("")
//                );
                Intent intent = new Intent(context, TrackingActivity.class).putExtra("key", key)
                        .putExtra("dest", userDestination)
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
            Toast.makeText(context,"Started tracking", Toast.LENGTH_LONG).show();
            ShowMapActivity.this.finish();
        }
    }
    @Override
    protected void onStop(){
        handler.removeCallbacksAndMessages(null);   //todo
        super.onStop();
    }
}
